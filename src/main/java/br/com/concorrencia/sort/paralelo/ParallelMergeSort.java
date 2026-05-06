package br.com.concorrencia.sort.paralelo;

import br.com.concorrencia.sort.SortAlgorithm;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

public class ParallelMergeSort implements SortAlgorithm, AutoCloseable {
    private static final int DEFAULT_THRESHOLD = 2_048;

    private final ForkJoinPool pool;
    private final int threshold;

    public ParallelMergeSort(int parallelism) {
        this(parallelism, DEFAULT_THRESHOLD);
    }

    public ParallelMergeSort(int parallelism, int threshold) {
        int effectiveParallelism = parallelism > 0 ? parallelism : Runtime.getRuntime().availableProcessors();
        this.pool = new ForkJoinPool(effectiveParallelism);
        this.threshold = Math.max(64, threshold);
    }

    @Override
    public void sort(int[] array) {
        if (array.length < 2) {
            return;
        }
        int[] temp = new int[array.length];
        pool.invoke(new MergeSortTask(array, temp, 0, array.length - 1, threshold));
    }

    @Override
    public String getName() {
        return "ParallelMergeSort";
    }

    @Override
    public boolean isParallel() {
        return true;
    }

    @Override
    public void close() {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(1, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static class MergeSortTask extends RecursiveAction {
        private final int[] array;
        private final int[] temp;
        private final int left;
        private final int right;
        private final int threshold;

        MergeSortTask(int[] array, int[] temp, int left, int right, int threshold) {
            this.array = array;
            this.temp = temp;
            this.left = left;
            this.right = right;
            this.threshold = threshold;
        }

        @Override
        protected void compute() {
            if (right - left <= threshold) {
                // Arrays.sort is used only for small chunks to reduce ForkJoin overhead.
                Arrays.sort(array, left, right + 1);
                return;
            }

            int mid = left + (right - left) / 2;
            MergeSortTask leftTask = new MergeSortTask(array, temp, left, mid, threshold);
            MergeSortTask rightTask = new MergeSortTask(array, temp, mid + 1, right, threshold);

            invokeAll(leftTask, rightTask);
            merge(array, temp, left, mid, right);
        }

        private void merge(int[] array, int[] temp, int left, int mid, int right) {
            int i = left;
            int j = mid + 1;
            int k = left;

            while (i <= mid && j <= right) {
                if (array[i] <= array[j]) {
                    temp[k++] = array[i++];
                } else {
                    temp[k++] = array[j++];
                }
            }

            while (i <= mid) {
                temp[k++] = array[i++];
            }

            while (j <= right) {
                temp[k++] = array[j++];
            }

            for (int idx = left; idx <= right; idx++) {
                array[idx] = temp[idx];
            }
        }
    }
}
