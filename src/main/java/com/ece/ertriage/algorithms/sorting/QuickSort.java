package com.ece.ertriage.algorithms.sorting;

import java.util.Comparator;

public final class QuickSort {
    private QuickSort() {}

    public static <T> void sort(T[] a, int n, Comparator<T> c) {
        if (n <= 1) return;
        quick(a, 0, n - 1, c);
    }

    private static <T> void quick(T[] a, int lo, int hi, Comparator<T> c) {
        while (lo < hi) {
            int p = partition(a, lo, hi, c);
            if (p - lo < hi - p) {
                quick(a, lo, p - 1, c);
                lo = p + 1;
            } else {
                quick(a, p + 1, hi, c);
                hi = p - 1;
            }
        }
    }

    private static <T> int partition(T[] a, int lo, int hi, Comparator<T> c) {
        T pivot = a[hi];
        int i = lo - 1;
        for (int j = lo; j < hi; j++) {
            if (c.compare(a[j], pivot) <= 0) {
                i++;
                swap(a, i, j);
            }
        }
        swap(a, i + 1, hi);
        return i + 1;
    }

    private static <T> void swap(T[] a, int i, int j) {
        T t = a[i]; a[i] = a[j]; a[j] = t;
    }
}