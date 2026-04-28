package br.com.concorrencia.data;

import java.util.Random;

public class DataGenerator {
    private final Random random;

    public DataGenerator() {
        this(System.currentTimeMillis());
    }

    public DataGenerator(long seed) {
        this.random = new Random(seed);
    }

    public int[] generate(int size, InputType inputType) {
        if (size < 0) {
            throw new IllegalArgumentException("Size must be >= 0");
        }

        return switch (inputType) {
            case RANDOM -> generateRandom(size);
            case SORTED -> generateSorted(size);
            case REVERSED -> generateReversed(size);
            case NEARLY_SORTED -> generateNearlySorted(size);
            case MANY_REPEATED -> generateManyRepeated(size);
        };
    }

    private int[] generateRandom(int size) {
        int[] array = new int[size];
        int bound = Math.max(10, size * 10);
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(bound);
        }
        return array;
    }

    private int[] generateSorted(int size) {
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = i;
        }
        return array;
    }

    private int[] generateReversed(int size) {
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = size - i;
        }
        return array;
    }

    private int[] generateNearlySorted(int size) {
        int[] array = generateSorted(size);
        if (size < 2) {
            return array;
        }

        int swaps = Math.max(1, size / 20);
        for (int i = 0; i < swaps; i++) {
            int a = random.nextInt(size);
            int b = random.nextInt(size);
            int tmp = array[a];
            array[a] = array[b];
            array[b] = tmp;
        }
        return array;
    }

    private int[] generateManyRepeated(int size) {
        int[] array = new int[size];
        int valueRange = Math.max(10, size / 100);
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(valueRange);
        }
        return array;
    }
}
