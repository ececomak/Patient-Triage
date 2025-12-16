package com.ece.ertriage.ui.benchmark;

import com.ece.ertriage.core.TriagePolicy;
import com.ece.ertriage.model.Patient;
import com.ece.ertriage.model.Severity;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class BenchmarkRunner {

    public static final class Result {
        public final long[] heapMs;
        public final long[] listSortMs;
        public Result(long[] heapMs, long[] listSortMs) {
            this.heapMs = heapMs;
            this.listSortMs = listSortMs;
        }
    }

    public static Result run(int[] sizes) {
        long[] heap = new long[sizes.length];
        long[] listSort = new long[sizes.length];

        for (int i = 0; i < sizes.length; i++) {
            int n = sizes[i];
            List<Patient> data = generatePatients(n);

            heap[i] = measureHeap(data);
            listSort[i] = measureListSort(data);
        }

        return new Result(heap, listSort);
    }

    private static long measureHeap(List<Patient> data) {
        long start = System.nanoTime();

        long now = System.currentTimeMillis();
        PriorityQueue<Patient> pq = new PriorityQueue<>((a, b) -> {
            int sa = TriagePolicy.score(a, now);
            int sb = TriagePolicy.score(b, now);
            int byScore = Integer.compare(sb, sa);
            if (byScore != 0) return byScore;
            return Long.compare(a.arrivalOrder(), b.arrivalOrder());
        });

        pq.addAll(data);
        while (!pq.isEmpty()) pq.poll();

        long end = System.nanoTime();
        return (end - start) / 1_000_000;
    }

    private static long measureListSort(List<Patient> data) {
        long start = System.nanoTime();

        ArrayList<Patient> list = new ArrayList<>(data);

        while (!list.isEmpty()) {
            long now = System.currentTimeMillis();
            list.sort((a, b) -> {
                int sa = TriagePolicy.score(a, now);
                int sb = TriagePolicy.score(b, now);
                int byScore = Integer.compare(sb, sa);
                if (byScore != 0) return byScore;
                return Long.compare(a.arrivalOrder(), b.arrivalOrder());
            });
            list.remove(0);
        }

        long end = System.nanoTime();
        return (end - start) / 1_000_000;
    }

    private static List<Patient> generatePatients(int n) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        long base = System.currentTimeMillis();

        List<Patient> list = new ArrayList<>(n);
        for (int i = 1; i <= n; i++) {
            String id = "B" + i;
            String name = "Hasta" + i;
            int age = rnd.nextInt(1, 95);
            Severity severity = Severity.values()[rnd.nextInt(Severity.values().length)];
            String complaint = "—";

            boolean chronic = rnd.nextBoolean() && rnd.nextBoolean();
            boolean pregnant = rnd.nextBoolean() && (age >= 18 && age <= 45);

            long arrivalMillis = base - rnd.nextLong(0, 120) * 60_000L;
            long order = i;

            list.add(new Patient(id, name, age, severity, complaint, chronic, pregnant, arrivalMillis, order));
        }
        return list;
    }

    private BenchmarkRunner() {}
}