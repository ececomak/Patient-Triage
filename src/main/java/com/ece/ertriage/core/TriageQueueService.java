package com.ece.ertriage.core;

import com.ece.ertriage.model.Gender;
import com.ece.ertriage.model.Patient;
import com.ece.ertriage.model.Severity;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public final class TriageQueueService {
    private final PatientIdGenerator idGen = new PatientIdGenerator();
    private final AtomicLong orderGen = new AtomicLong(0);

    private PriorityQueue<Patient> heap = new PriorityQueue<>(comparator(System.currentTimeMillis()));
    private final List<Patient> allWaitingShadow = new ArrayList<>();

    private Comparator<Patient> comparator(long nowMillis) {
        return (a, b) -> {
            int sa = TriagePolicy.score(a, nowMillis);
            int sb = TriagePolicy.score(b, nowMillis);

            int byScoreDesc = Integer.compare(sb, sa);
            if (byScoreDesc != 0) return byScoreDesc;

            return Long.compare(a.arrivalOrder(), b.arrivalOrder());
        };
    }

    public Patient addPatient(String name, int age, Gender gender, int painScore, Severity severity, String complaint,
                              boolean chronic, boolean pregnant) {
        if (painScore < 0) painScore = 0;
        if (painScore > 10) painScore = 10;
        if (gender != Gender.FEMALE) pregnant = false;

        long now = System.currentTimeMillis();
        Patient p = new Patient(
                idGen.nextId(),
                name,
                age,
                gender,
                painScore,
                severity,
                complaint,
                chronic,
                pregnant,
                now,
                orderGen.incrementAndGet()
        );
        heap.add(p);
        allWaitingShadow.add(p);
        return p;
    }

    public Optional<Patient> peek() {
        return Optional.ofNullable(heap.peek());
    }

    public Optional<Patient> poll() {
        Patient p = heap.poll();
        if (p == null) return Optional.empty();
        allWaitingShadow.remove(p);
        return Optional.of(p);
    }

    public List<Patient> snapshotOrdered() {
        long now = System.currentTimeMillis();
        ArrayList<Patient> list = new ArrayList<>(allWaitingShadow);
        list.sort(comparator(now));
        return list;
    }

    public void rebuildHeapForNow() {
        long now = System.currentTimeMillis();
        PriorityQueue<Patient> newHeap = new PriorityQueue<>(comparator(now));
        newHeap.addAll(allWaitingShadow);
        this.heap = newHeap;
    }

    public int waitingCount() {
        return allWaitingShadow.size();
    }
}