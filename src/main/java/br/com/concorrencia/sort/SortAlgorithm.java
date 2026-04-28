package br.com.concorrencia.sort;

public interface SortAlgorithm {
    void sort(int[] array);

    String getName();

    boolean isParallel();
}
