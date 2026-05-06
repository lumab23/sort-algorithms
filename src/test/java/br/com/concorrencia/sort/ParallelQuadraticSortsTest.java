package br.com.concorrencia.sort;

import br.com.concorrencia.sort.paralelo.ParallelBubbleSort;
import br.com.concorrencia.sort.paralelo.ParallelInsertionSort;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ParallelQuadraticSortsTest {

    @Test
    void parallelBubbleSortShouldSortWithDifferentThreadCounts() throws Exception {
        int[] entrada = {9, 1, 3, 7, 5, 2, 8, 6, 4, 0, 1, 7};
        int[] esperado = entrada.clone();
        Arrays.sort(esperado);

        for (int threads : new int[]{1, 2, 4}) {
            try (ParallelBubbleSort algoritmo = new ParallelBubbleSort(threads)) {
                int[] copia = entrada.clone();
                algoritmo.sort(copia);
                assertArrayEquals(esperado, copia);
            }
        }
    }

    @Test
    void parallelInsertionSortShouldSortWithDifferentThreadCounts() throws Exception {
        int[] entrada = {5, -2, 4, -2, 8, 0, 3, 3, 7, -9, 1};
        int[] esperado = entrada.clone();
        Arrays.sort(esperado);

        for (int threads : new int[]{1, 2, 4}) {
            try (ParallelInsertionSort algoritmo = new ParallelInsertionSort(threads)) {
                int[] copia = entrada.clone();
                algoritmo.sort(copia);
                assertArrayEquals(esperado, copia);
            }
        }
    }
}
