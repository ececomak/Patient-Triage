package com.ece.ertriage.core;

import java.util.concurrent.atomic.AtomicLong;

public final class PatientIdGenerator {
    private final AtomicLong seq = new AtomicLong(0);

    public String nextId() {
        long n = seq.incrementAndGet();
        return "P" + String.format("%04d", n);
    }
}