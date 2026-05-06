package br.com.concorrencia.sort.serial;

import br.com.concorrencia.sort.SortAlgorithm;

public class InsertionSort implements SortAlgorithm {
    @Override
    public void sort(int[] array) {
        for (int i = 1; i < array.length; i++) {
            int key = array[i];
            int j = i - 1;

            while (j >= 0 && array[j] > key) {
                array[j + 1] = array[j];
                j--;
            }
            array[j + 1] = key;
        }
    }

    @Override
    public String getName() {
        return "InsertionSort";
    }

    @Override
    public boolean isParallel() {
        return false;
    }
}
