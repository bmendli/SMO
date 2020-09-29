package ru.ok.technopolis.training.smo.buffer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

import ru.ok.technopolis.training.smo.request.Request;

public class RingBuffer extends AbstractBuffer {

    private final AtomicInteger pointer;

    public RingBuffer(int capacity) {
        super(capacity);
        pointer = new AtomicInteger(0);
    }

    @Override
    @Nullable
    public Request putRequest(@NonNull Request request) {
        final int oldPointerPosition = pointer.get();
        do {
            final Request requestFromBuffer = requests[pointer.get()];
            if (requestFromBuffer == null) {
                System.out.printf("request %d.%d in buffer on %d position%n", request.getSourceNumber(), request.getRequestNumber(), pointer.get());
                requests[pointer.get()] = request;
                request.onPutRequestInBuffer();
                if (pointer.incrementAndGet() == capacity) {
                    pointer.set(0);
                }
                return null;
            } else {
                if (pointer.incrementAndGet() == capacity) {
                    pointer.set(0);
                }
            }
        } while (oldPointerPosition != pointer.get());
        System.out.println("buffer is busy, searching place");
        return getRequestForRefusal(request);
    }

    @NonNull
    private Request getRequestForRefusal(@NonNull Request newRequest) {
        Request requestWithLowPriority = newRequest;
        int index = -1;
        for (int i = 0; i < capacity; i++) {
            final Request request = requests[i];
            if (request != null
                    && (request.getSourceNumber() > requestWithLowPriority.getSourceNumber()
                    || request.getSourceNumber() == requestWithLowPriority.getSourceNumber()
                    && request.getRequestNumber() < requestWithLowPriority.getRequestNumber())) {
                requestWithLowPriority = request;
                index = i;
            }
        }
        if (index != -1) {
            requests[index] = newRequest;
            System.out.printf("request %d.%d in buffer on %d position%n", newRequest.getSourceNumber(), newRequest.getRequestNumber(), index);
            newRequest.onPutRequestInBuffer();
        }
        requestWithLowPriority.onGetRequestFromBuffer();
        return requestWithLowPriority;
    }

    @Override
    @Nullable
    public Request getRequestForDevice() {
        int index = -1;
        Request requestWithHighPriority = null;
        for (int i = 0; i < capacity; i++) {
            final Request request = requests[i];
            if (request != null
                    && (requestWithHighPriority == null
                    || request.getSourceNumber() < requestWithHighPriority.getSourceNumber()
                    || request.getSourceNumber() == requestWithHighPriority.getSourceNumber()
                    && request.getRequestNumber() < requestWithHighPriority.getRequestNumber())) {
                requestWithHighPriority = request;
                index = i;

            }
        }
        if (requestWithHighPriority != null) {
            requests[index] = null;
            System.out.printf("request %d.%d from buffer on %d position%n", requestWithHighPriority.getSourceNumber(), requestWithHighPriority.getRequestNumber(), index);
            requestWithHighPriority.onGetRequestFromBuffer();
        }
        return requestWithHighPriority;
    }

    @Override
    public void clear() {
        super.clear();
        pointer.set(0);
    }
}
