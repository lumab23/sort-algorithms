package br.com.concorrencia.sort;

public class MergeSort implements SortAlgorithm {
    @Override
    public void sort(int[] array) {
        if (array.length < 2) {
            return;
        }
        int[] temp = new int[array.length];
        mergeSort(array, temp, 0, array.length - 1);
    }

    private void mergeSort(int[] array, int[] temp, int left, int right) {
        if (left >= right) {
            return;
        }

        int mid = left + (right - left) / 2;
        mergeSort(array, temp, left, mid);
        mergeSort(array, temp, mid + 1, right);
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

    @Override
    public String getName() {
        return "MergeSort";
    }

    @Override
    public boolean isParallel() {
        return false;
    }
}
