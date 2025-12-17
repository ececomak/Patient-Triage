package com.ece.ertriage.algorithms.sorting;

import java.util.Comparator;

public final class MergeSort {
    private MergeSort() {}

    public static <T> void sort(T[] a, int n, Comparator<T> c) {
        if (n <= 1) return;
        @SuppressWarnings("unchecked")
        T[] aux = (T[]) new Object[n];
        mergeSort(a, aux, 0, n - 1, c);
    }

    private static <T> void mergeSort(T[] a, T[] aux, int lo, int hi, Comparator<T> c) {
        if (lo >= hi) return;
        int mid = (lo + hi) >>> 1;
        mergeSort(a, aux, lo, mid, c);
        mergeSort(a, aux, mid + 1, hi, c);
        merge(a, aux, lo, mid, hi, c);
    }

    private static <T> void merge(T[] a, T[] aux, int lo, int mid, int hi, Comparator<T> c) {
        for (int i = lo; i <= hi; i++) aux[i] = a[i];

        int i = lo, j = mid + 1, k = lo;
        while (i <= mid && j <= hi) {
            if (c.compare(aux[i], aux[j]) <= 0) a[k++] = aux[i++];
            else a[k++] = aux[j++];
        }
        while (i <= mid) a[k++] = aux[i++];
        while (j <= hi) a[k++] = aux[j++];
    }
}