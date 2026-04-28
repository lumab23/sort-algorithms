package br.com.concorrencia.data;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataGeneratorTest {
    private final DataGenerator generator = new DataGenerator(123L);

    @Test
    void shouldGenerateSortedData() {
        int[] data = generator.generate(100, InputType.SORTED);
        assertEquals(100, data.length);
        for (int i = 1; i < data.length; i++) {
            assertTrue(data[i - 1] <= data[i]);
        }
    }

    @Test
    void shouldGenerateReversedData() {
        int[] data = generator.generate(100, InputType.REVERSED);
        assertEquals(100, data.length);
        for (int i = 1; i < data.length; i++) {
            assertTrue(data[i - 1] >= data[i]);
        }
    }

    @Test
    void shouldGenerateNearlySortedData() {
        int[] data = generator.generate(200, InputType.NEARLY_SORTED);
        int[] sortedCopy = data.clone();
        Arrays.sort(sortedCopy);

        for (int i = 0; i < sortedCopy.length; i++) {
            assertEquals(i, sortedCopy[i]);
        }

        int outOfOrderPairs = 0;
        for (int i = 1; i < data.length; i++) {
            if (data[i - 1] > data[i]) {
                outOfOrderPairs++;
            }
        }
        assertTrue(outOfOrderPairs > 0);
        assertTrue(outOfOrderPairs < 40);
    }

    @Test
    void shouldGenerateManyRepeatedData() {
        int[] data = generator.generate(1_000, InputType.MANY_REPEATED);
        long uniqueCount = Arrays.stream(data).distinct().count();
        assertTrue(uniqueCount < 200);
    }

    @Test
    void shouldGenerateRandomDataWithExpectedSize() {
        int[] data = generator.generate(250, InputType.RANDOM);
        assertEquals(250, data.length);
        assertTrue(Arrays.stream(data).distinct().count() > 10);
    }

    @Test
    void sameSeedShouldGenerateSameRandomSequence() {
        DataGenerator first = new DataGenerator(999L);
        DataGenerator second = new DataGenerator(999L);

        int[] firstArray = first.generate(200, InputType.RANDOM);
        int[] secondArray = second.generate(200, InputType.RANDOM);

        assertArrayEquals(firstArray, secondArray);
    }

    @Test
    void shouldHandleZeroAndNegativeSizes() {
        assertEquals(0, generator.generate(0, InputType.RANDOM).length);
        assertThrows(IllegalArgumentException.class, () -> generator.generate(-1, InputType.RANDOM));
    }
}
