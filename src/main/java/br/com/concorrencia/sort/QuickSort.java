package br.com.concorrencia.sort;

public class QuickSort implements SortAlgorithm {
    @Override
    public void sort(int[] array) {
        if (array.length < 2) {
            return;
        }
        quickSort(array, 0, array.length - 1);
    }

    private void quickSort(int[] array, int low, int high) {
        if (low >= high) {
            return;
        }

        int[] partitionBounds = partition(array, low, high);
        quickSort(array, low, partitionBounds[0] - 1);
        quickSort(array, partitionBounds[1] + 1, high);
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

    @Override
    public String getName() {
        return "QuickSort";
    }

    @Override
    public boolean isParallel() {
        return false;
    }
}
