package com.ece.ertriage.algorithms.sorting;

import java.util.Comparator;

public final class HeapSort {
    private HeapSort() {}

    public static <T> void sort(T[] a, int n, Comparator<T> c) {
        if (n <= 1) return;

        for (int i = (n / 2) - 1; i >= 0; i--) {
            siftDown(a, i, n, c);
        }

        for (int end = n - 1; end > 0; end--) {
            swap(a, 0, end);
            siftDown(a, 0, end, c);
        }
    }

    private static <T> void siftDown(T[] a, int i, int n, Comparator<T> c) {
        while (true) {
            int left = 2 * i + 1;
            int right = left + 1;
            int largest = i;

            if (left < n && c.compare(a[left], a[largest]) > 0) largest = left;
            if (right < n && c.compare(a[right], a[largest]) > 0) largest = right;

            if (largest == i) return;
            swap(a, i, largest);
            i = largest;
        }
    }

    private static <T> void swap(T[] a, int i, int j) {
        T t = a[i]; a[i] = a[j]; a[j] = t;
    }
}