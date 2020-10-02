package ru.ok.technopolis.training.smo.source;

import androidx.annotation.NonNull;

import java.util.concurrent.atomic.AtomicInteger;

import ru.ok.technopolis.training.smo.controller.Controller;
import ru.ok.technopolis.training.smo.request.Request;

public class Source implements Runnable {

    @NonNull
    private final Controller controller;
    private final float lambda;
    private final int sourceNumber;
    private final AtomicInteger requestNumber = new AtomicInteger(0);
    private final AtomicInteger countRefusedRequests = new AtomicInteger(0);

    private boolean isSourceWorking;

    public Source(float lambda, int sourceNumber, @NonNull Controller controller) {
        this.lambda = lambda;
        this.sourceNumber = sourceNumber;
        this.controller = controller;
    }

    public void onSystemEndWorking() {
        isSourceWorking = false;
    }

    @Override
    public void run() {
        isSourceWorking = true;
        while (isSourceWorking) {
            try {
                final long timeGeneration = generateDelayTime();
                Thread.sleep(timeGeneration);
                System.out.printf("generated %d.%d with time generation %d%n", sourceNumber, requestNumber.incrementAndGet(), timeGeneration);
                controller.submitRequest(this, new Request(sourceNumber, requestNumber.get()), timeGeneration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void incrementRefusalRequests() {
        countRefusedRequests.incrementAndGet();
    }

    public int getSourceNumber() {
        return sourceNumber;
    }

    public int getRequestNumber() {
        return requestNumber.get();
    }

    public int getCountRefusedRequests() {
        return countRefusedRequests.get();
    }

    private long generateDelayTime() {
        return (long) (- 1 / lambda * Math.log(Math.random()) * 1000);
    }
}
