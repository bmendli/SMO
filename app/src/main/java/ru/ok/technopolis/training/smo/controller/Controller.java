package ru.ok.technopolis.training.smo.controller;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.ok.technopolis.training.smo.activity.SmoActivity;
import ru.ok.technopolis.training.smo.adapter.DeviceAdapter;
import ru.ok.technopolis.training.smo.adapter.SourceAdapter;
import ru.ok.technopolis.training.smo.buffer.AbstractBuffer;
import ru.ok.technopolis.training.smo.buffer.RingBuffer;
import ru.ok.technopolis.training.smo.device.Device;
import ru.ok.technopolis.training.smo.request.Request;
import ru.ok.technopolis.training.smo.source.Source;
import ru.ok.technopolis.training.smo.statistic.StatisticsCollector;

public class Controller {

    private static Handler handler;

    private final List<Device> devices;
    private final List<Source> sources;
    private final SourceAdapter sourceAdapter;
    private final DeviceAdapter deviceAdapter;
    private final StatisticsCollector statisticsCollector;

    private int countSources;
    private int countDevices;
    private int countRequests;
    private int bufferCapacity;
    private int stepSize;
    private int step = 1;
    private float lambda;
    private float alpha;
    private float beta;
    private boolean isSystemWorking = false;
    private AbstractBuffer buffer;
    private ExecutorService sourceExecutor;

    public Controller(@NonNull final SourceAdapter sourceAdapter,
                      @NonNull final DeviceAdapter deviceAdapter,
                      @NonNull final SmoActivity smoActivity) {
        this.sourceAdapter = sourceAdapter;
        this.deviceAdapter = deviceAdapter;
        this.statisticsCollector = new StatisticsCollector(smoActivity);
        this.devices = new ArrayList<>();
        this.sources = new ArrayList<>();
    }

    public void submitRequest(@NonNull Source source, @NonNull Request request, long timeGeneration) {
        Request refusedRequest = buffer.putRequest(request);
        if (refusedRequest != null) {
            System.out.printf("request %d.%d was refused%n", refusedRequest.getSourceNumber(), refusedRequest.getRequestNumber());
            sources.get(refusedRequest.getSourceNumber() - 1).incrementRefusalRequests();
            statisticsCollector.onRequestRefusal(refusedRequest);
        }
        if (hasFreeDevice()) {
            final Request requestForDevice = buffer.getRequestForDevice();
            if (!sendRequestToDevice(requestForDevice) && requestForDevice != null) {
                refusedRequest = buffer.putRequest(requestForDevice);
                if (refusedRequest != null) {
                    System.out.printf("request %d.%d was refused%n", refusedRequest.getSourceNumber(), refusedRequest.getRequestNumber());
                    sources.get(refusedRequest.getSourceNumber() - 1).incrementRefusalRequests();
                    statisticsCollector.onRequestRefusal(refusedRequest);
                }
            }
        }
    }

    public void processingFinished(@NonNull final Device device, @NonNull Request request, long processingTime) {
        statisticsCollector.onDeviceEndProcessing(request, processingTime);
        if (statisticsCollector.incrementAndGetCountProcessedRequest() >= countRequests) {
            endSystem();
        } else {
            if (!buffer.isEmpty()) {
                final Request requestForDevice = buffer.getRequestForDevice();
                if (!sendRequestToDevice(requestForDevice) && requestForDevice != null) {
                    Request refusedRequest = buffer.putRequest(requestForDevice);
                    if (refusedRequest != null) {
                        System.out.printf("request %d.%d was refused%n", refusedRequest.getSourceNumber(), refusedRequest.getRequestNumber());
                        sources.get(refusedRequest.getSourceNumber() - 1).incrementRefusalRequests();
                        statisticsCollector.onRequestRefusal(refusedRequest);
                    }
                }
            }
        }
        if (statisticsCollector.getCountProcessedRequest() >= step * stepSize || !isSystemWorking) {
            runOnMainThread(() -> {
                step++;
                sourceAdapter.notifyDataSetChanged();
                deviceAdapter.notifyDataSetChanged();
            });
        }
    }

    public void startSystem(int countSources,
                            int countDevices,
                            int countRequests,
                            int bufferCapacity,
                            int stepSize,
                            float lambda,
                            float alpha,
                            float beta) {
        this.countSources = countSources;
        this.countDevices = countDevices;
        this.countRequests = countRequests;
        this.bufferCapacity = bufferCapacity;
        this.stepSize = stepSize;
        this.lambda = lambda;
        this.alpha = alpha;
        this.beta = beta;
        this.buffer = new RingBuffer(bufferCapacity);
        this.sourceExecutor = Executors.newFixedThreadPool(countSources);
        for (int i = 1; i <= countDevices; i++) {
            this.devices.add(new Device(i, alpha, beta, this));
        }
        deviceAdapter.setNewData(devices);
        statisticsCollector.onStartSystem(countSources, countDevices, countRequests, bufferCapacity, lambda, alpha, beta);
        isSystemWorking = true;
        for (int i = 1; i <= countSources; i++) {
            final Source source = new Source(lambda, i, this);
            sources.add(source);
            this.sourceExecutor.submit(source);
        }
        sourceAdapter.setNewData(sources);
    }

    public void endSystem() {
        for (Request request : buffer.getRequests()) {
            if (request != null) {
                sources.get(request.getSourceNumber() - 1).incrementRefusalRequests();
            }
        }
        runOnMainThread(() -> {
            step++;
            sourceAdapter.notifyDataSetChanged();
            deviceAdapter.notifyDataSetChanged();
        });
        if (isSystemWorking) {
            isSystemWorking = false;
            sourceExecutor.shutdown();
            for (Device device : devices) {
                device.onSystemEndWorking();
            }
            for (Source source : sources) {
                source.onSystemEndWorking();
            }
            buffer.clear();
            statisticsCollector.onEndSystem(devices);
        }
    }

    private boolean sendRequestToDevice(@Nullable Request request) {
        if (request == null) {
            return false;
        }
        for (Device device : devices) {
            if (device.setNewRequest(request)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasFreeDevice() {
        for (Device device : devices) {
            if (!device.isBusy()) {
                return true;
            }
        }
        return false;
    }

    public static void runOnMainThread(Runnable runnable) {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        handler.post(runnable);
    }
}
