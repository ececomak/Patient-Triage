package com.ece.ertriage.ui.benchmark;

import com.ece.ertriage.algorithms.sorting.HeapSort;
import com.ece.ertriage.algorithms.sorting.MergeSort;
import com.ece.ertriage.algorithms.sorting.QuickSort;
import com.ece.ertriage.core.TriagePolicy;
import com.ece.ertriage.model.Patient;
import com.ece.ertriage.model.Severity;

import java.util.*;

public final class BenchmarkRunner {

    public static final class Result {
        public final long[] heapUs;
        public final long[] mergeUs;
        public final long[] quickUs;
        public final long[] heapSortUs;

        public Result(long[] heapUs, long[] mergeUs, long[] quickUs, long[] heapSortUs) {
            this.heapUs = heapUs;
            this.mergeUs = mergeUs;
            this.quickUs = quickUs;
            this.heapSortUs = heapSortUs;
        }
    }

    private static final long FIXED_NOW  = 1_700_000_000_000L;
    private static final long FIXED_BASE = 1_700_000_000_000L;
    private static final long SEED = 42L;
    private static boolean isWarmedUp = false;

    private enum Alg { MERGE, QUICK, HEAP }

    @FunctionalInterface
    private interface MeasureFn { long measureUs(); }

    public static Result run(int[] sizes) {
        if (!isWarmedUp) {
            performAggressiveWarmup();
            isWarmedUp = true;
        }

        long[] heap = new long[sizes.length];
        long[] merge = new long[sizes.length];
        long[] quick = new long[sizes.length];
        long[] heapSort = new long[sizes.length];

        final int WARMUP = 5;
        final int REPEAT = 25;

        for (int i = 0; i < sizes.length; i++) {
            int n = sizes[i];
            List<Patient> baseData = generatePatientsDeterministic(n);
            Comparator<Patient> cmp = fixedComparator();

            System.gc();

            for (int w = 0; w < WARMUP; w++) {
                measureHeapUs(baseData, cmp);
                measureSortUs(baseData, cmp, Alg.MERGE);
                measureSortUs(baseData, cmp, Alg.QUICK);
                measureSortUs(baseData, cmp, Alg.HEAP);
            }

            heap[i]     = medianUs(REPEAT, () -> measureHeapUs(baseData, cmp));
            merge[i]    = medianUs(REPEAT, () -> measureSortUs(baseData, cmp, Alg.MERGE));
            quick[i]    = medianUs(REPEAT, () -> measureSortUs(baseData, cmp, Alg.QUICK));
            heapSort[i] = medianUs(REPEAT, () -> measureSortUs(baseData, cmp, Alg.HEAP));
        }

        return new Result(heap, merge, quick, heapSort);
    }

    private static void performAggressiveWarmup() {
        List<Patient> warmupData = generatePatientsDeterministic(500);
        Comparator<Patient> cmp = fixedComparator();
        for (int i = 0; i < 5000; i++) {
            measureSortUs(warmupData, cmp, Alg.QUICK);
            measureSortUs(warmupData, cmp, Alg.MERGE);
            measureSortUs(warmupData, cmp, Alg.HEAP);
        }
    }

    private static long medianUs(int repeat, MeasureFn fn) {
        long[] arr = new long[repeat];
        for (int i = 0; i < repeat; i++) arr[i] = fn.measureUs();
        Arrays.sort(arr);
        return arr[repeat / 2];
    }

    private static Comparator<Patient> fixedComparator() {
        final long now = FIXED_NOW;
        return (a, b) -> {
            int sa = TriagePolicy.score(a, now);
            int sb = TriagePolicy.score(b, now);
            int byScore = Integer.compare(sb, sa);
            if (byScore != 0) return byScore;
            return Long.compare(a.arrivalOrder(), b.arrivalOrder());
        };
    }

    private static long measureHeapUs(List<Patient> data, Comparator<Patient> cmp) {
        long start = System.nanoTime();
        PriorityQueue<Patient> pq = new PriorityQueue<>(cmp);
        pq.addAll(data);
        while (!pq.isEmpty()) pq.poll();
        long end = System.nanoTime();
        return Math.max(1, (end - start) / 1_000L);
    }

    private static long measureSortUs(List<Patient> data, Comparator<Patient> cmp, Alg alg) {
        Patient[] arr = data.toArray(new Patient[0]);
        long start = System.nanoTime();
        switch (alg) {
            case MERGE -> MergeSort.sort(arr, arr.length, cmp);
            case QUICK -> QuickSort.sort(arr, arr.length, cmp);
            case HEAP  -> HeapSort.sort(arr, arr.length, cmp);
        }
        long end = System.nanoTime();
        if (arr.length > 0 && arr[0] == null) System.out.print("");
        return Math.max(1, (end - start) / 1_000L);
    }

    private static List<Patient> generatePatientsDeterministic(int n) {
        Random rnd = new Random(SEED + n);
        List<Patient> list = new ArrayList<>(n);
        for (int i = 1; i <= n; i++) {
            long arrivalMillis = FIXED_BASE - (long) rnd.nextInt(120) * 60_000L;
            list.add(new Patient("B" + i, "Hasta" + i, 1 + rnd.nextInt(94),
                    Severity.values()[rnd.nextInt(Severity.values().length)],
                    "—", rnd.nextBoolean(), rnd.nextBoolean(), arrivalMillis, i));
        }
        return list;
    }

    private BenchmarkRunner() {}
}