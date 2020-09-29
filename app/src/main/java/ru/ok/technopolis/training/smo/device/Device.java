package ru.ok.technopolis.training.smo.device;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import ru.ok.technopolis.training.smo.controller.Controller;
import ru.ok.technopolis.training.smo.request.Request;

public class Device {

    private final float alpha;
    private final float beta;
    private final int deviceNumber;
    private final Runnable runnable;
    private final Controller controller;
    private final ExecutorService executorService;
    private final AtomicInteger countProcessedRequests = new AtomicInteger(0);
    private final AtomicLong allProcessingTime = new AtomicLong(0);

    private Request request;

    public Device(int deviceNumber, float alpha, float beta, @NonNull Controller controller) {
        this.alpha = alpha;
        this.beta = beta;
        this.deviceNumber = deviceNumber;
        this.controller = controller;
        this.executorService = Executors.newSingleThreadExecutor();
        this.runnable = () -> {
            final long processingTime = generateProcessingTime();
            try {
                Thread.sleep(processingTime);
                Request readyRequest = request;
                System.out.printf("request %d.%d was processed in device %d with processing time %d%n",
                        readyRequest.getSourceNumber(), readyRequest.getRequestNumber(), deviceNumber, processingTime);
                countProcessedRequests.incrementAndGet();
                allProcessingTime.set(allProcessingTime.get() + processingTime);
                request = null;
                controller.processingFinished(this, readyRequest, processingTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
    }

    public boolean isBusy() {
        return request != null;
    }

    public boolean setNewRequest(@NonNull Request newRequest) {
        if (isBusy()) {
            return false;
        } else {
            request = newRequest;
            System.out.printf("request %d.%d processing in device %d%n", newRequest.getSourceNumber(), newRequest.getRequestNumber(), deviceNumber);
            executorService.submit(runnable);
            return true;
        }
    }

    public void onSystemEndWorking() {
        executorService.shutdown();
    }

    private long generateProcessingTime() {
        return (long) ((Math.random() * (beta - alpha) + alpha) * 1000);
    }

    public int getDeviceNumber() {
        return deviceNumber;
    }

    public int getCountProcessedRequests() {
        return countProcessedRequests.get();
    }

    public long getAllProcessingTime() {
        return allProcessingTime.get();
    }
}
