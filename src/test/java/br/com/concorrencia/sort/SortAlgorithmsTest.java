package br.com.concorrencia.sort;

import org.junit.jupiter.api.Test;
import br.com.concorrencia.sort.paralelo.ParallelMergeSort;
import br.com.concorrencia.sort.paralelo.ParallelQuickSort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import br.com.concorrencia.sort.serial.BubbleSort;
import br.com.concorrencia.sort.serial.InsertionSort;
import br.com.concorrencia.sort.serial.MergeSort;
import br.com.concorrencia.sort.serial.QuickSort;

class SortAlgorithmsTest {
    @Test
    void allAlgorithmsShouldSortAllRequiredScenarios() throws Exception {
        List<Supplier<SortAlgorithm>> factories = List.of(
                BubbleSort::new,
                InsertionSort::new,
                MergeSort::new,
                QuickSort::new,
                () -> new ParallelMergeSort(4),
                () -> new ParallelQuickSort(4)
        );

        List<int[]> scenarios = new ArrayList<>();
        scenarios.add(new int[]{});
        scenarios.add(new int[]{42});
        scenarios.add(new int[]{1, 2, 3, 4, 5, 6});
        scenarios.add(new int[]{6, 5, 4, 3, 2, 1});
        scenarios.add(new int[]{5, 3, 9, 1, 4, 7, 2, 8, 6, 0, 3, 3, 9});
        scenarios.add(new int[]{-5, -1, -3, 0, 2, -2, 2, 0, -5});

        for (Supplier<SortAlgorithm> factory : factories) {
            SortAlgorithm algorithm = factory.get();
            try {
                for (int[] input : scenarios) {
                    int[] copy = input.clone();
                    int[] expected = input.clone();
                    Arrays.sort(expected);
                    algorithm.sort(copy);
                    assertArrayEquals(expected, copy, "Falha no algoritmo: " + algorithm.getName());
                }
            } finally {
                if (algorithm instanceof AutoCloseable closeable) {
                    closeable.close();
                }
            }
        }
    }
}
