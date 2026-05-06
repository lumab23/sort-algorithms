package br.com.concorrencia.sort.paralelo;

import br.com.concorrencia.sort.SortAlgorithm;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

public class ParallelQuickSort implements SortAlgorithm, AutoCloseable {
    private static final int DEFAULT_THRESHOLD = 2_048;

    private final ForkJoinPool pool;
    private final int threshold;

    public ParallelQuickSort(int parallelism) {
        this(parallelism, DEFAULT_THRESHOLD);
    }

    public ParallelQuickSort(int parallelism, int threshold) {
        int effectiveParallelism = parallelism > 0 ? parallelism : Runtime.getRuntime().availableProcessors();
        this.pool = new ForkJoinPool(effectiveParallelism);
        this.threshold = Math.max(64, threshold);
    }

    @Override
    public void sort(int[] array) {
        if (array.length < 2) {
            return;
        }
        pool.invoke(new QuickSortTask(array, 0, array.length - 1, threshold));
    }

    @Override
    public String getName() {
        return "ParallelQuickSort";
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

    private static class QuickSortTask extends RecursiveAction {
        private final int[] array;
        private final int low;
        private final int high;
        private final int threshold;

        QuickSortTask(int[] array, int low, int high, int threshold) {
            this.array = array;
            this.low = low;
            this.high = high;
            this.threshold = threshold;
        }

        @Override
        protected void compute() {
            if (low >= high) {
                return;
            }

            if (high - low <= threshold) {
                // Arrays.sort is used only for small chunks to reduce ForkJoin overhead.
                Arrays.sort(array, low, high + 1);
                return;
            }

            int[] partitionBounds = partition(array, low, high);
            QuickSortTask leftTask = new QuickSortTask(array, low, partitionBounds[0] - 1, threshold);
            QuickSortTask rightTask = new QuickSortTask(array, partitionBounds[1] + 1, high, threshold);

            invokeAll(leftTask, rightTask);
        }

        private int[] partition(int[] array, int low, int high) {
            int pivot = array[low + (high - low) / 2];
            int lt = low;
            int i = low;
            int gt = high;

            while (i <= gt) {
                if (array[i] < pivot) {
                    int temp = array[lt];
                    array[lt] = array[i];
                    array[i] = temp;
                    lt++;
                    i++;
                } else if (array[i] > pivot) {
                    int temp = array[i];
                    array[i] = array[gt];
                    array[gt] = temp;
                    gt--;
                } else {
                    i++;
                }
            }

            return new int[]{lt, gt};
        }
    }
}
