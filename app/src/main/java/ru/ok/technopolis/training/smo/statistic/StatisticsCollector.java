package ru.ok.technopolis.training.smo.statistic;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import ru.ok.technopolis.training.smo.activity.SmoActivity;
import ru.ok.technopolis.training.smo.controller.Controller;
import ru.ok.technopolis.training.smo.device.Device;
import ru.ok.technopolis.training.smo.request.Request;

public class StatisticsCollector {

    private final AtomicLong processingTimeAllDevice;
    private final AtomicLong timeInBufferAllRequests;
    private final AtomicInteger countRefusalRequests;
    private final AtomicInteger countProcessedRequest;
    private final SmoActivity smoActivity;

    private long startTime;
    private long endTime;
    private int countSources;
    private int countDevices;
    private int countRequests;
    private int bufferCapacity;
    private float lambda;
    private float alpha;
    private float beta;

    public StatisticsCollector(@NonNull final SmoActivity smoActivity) {
        this.processingTimeAllDevice = new AtomicLong(0);
        this.timeInBufferAllRequests = new AtomicLong(0);
        this.countRefusalRequests = new AtomicInteger(0);
        this.countProcessedRequest = new AtomicInteger(0);
        this.smoActivity = smoActivity;
    }

    public void onStartSystem(int countSources,
                              int countDevices,
                              int countRequests,
                              int bufferCapacity,
                              float lambda,
                              float alpha,
                              float beta) {
        this.countSources = countSources;
        this.countDevices = countDevices;
        this.countRequests = countRequests;
        this.bufferCapacity = bufferCapacity;
        this.lambda = lambda;
        this.alpha = alpha;
        this.beta = beta;
        System.out.println("start system");
        startTime = System.currentTimeMillis();
    }

    public void onEndSystem(List<Device> devices) {
        endTime = System.currentTimeMillis();
        Controller.runOnMainThread(() -> smoActivity.showResult(countSources, countDevices, bufferCapacity, lambda, alpha, beta,
                (double) countRefusalRequests.get() / (countRefusalRequests.get() + countProcessedRequest.get()),
                ((double) processingTimeAllDevice.get() / countDevices) / (endTime - startTime),
                (((double) processingTimeAllDevice.get() / countRequests) + ((double) timeInBufferAllRequests.get() / (countRequests + countRefusalRequests.get()))) / 1000,
                devices, endTime - startTime));
    }

    public void onDeviceEndProcessing(@NonNull Request request, long processingTime) {
        timeInBufferAllRequests.set(timeInBufferAllRequests.get() + request.timeInBuffer());
        processingTimeAllDevice.set(processingTimeAllDevice.get() + processingTime);
    }

    public void onRequestRefusal(@NonNull Request request) {
        timeInBufferAllRequests.set(timeInBufferAllRequests.get() + request.timeInBuffer());
        countRefusalRequests.incrementAndGet();
    }

    public int incrementAndGetCountProcessedRequest() {
        return countProcessedRequest.incrementAndGet();
    }

    public int getCountProcessedRequest() {
        return countProcessedRequest.get();
    }
}
