package br.com.concorrencia.benchmark;

import br.com.concorrencia.data.DataGenerator;
import br.com.concorrencia.data.InputType;
import br.com.concorrencia.sort.BubbleSort;
import br.com.concorrencia.sort.InsertionSort;
import br.com.concorrencia.sort.MergeSort;
import br.com.concorrencia.sort.ParallelMergeSort;
import br.com.concorrencia.sort.ParallelQuickSort;
import br.com.concorrencia.sort.QuickSort;
import br.com.concorrencia.sort.SortAlgorithm;
import br.com.concorrencia.stats.BenchmarkSummary;
import br.com.concorrencia.stats.Statistics;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BenchmarkRunner {
    private static final String LINE = "======================================================================";
    private static final String SUBLINE = "----------------------------------------------------------------------";

    private final DataGenerator dataGenerator;
    private final int samples;
    private final int warmupRuns;
    private final int maxQuadraticSize;

    public BenchmarkRunner(DataGenerator dataGenerator, int samples, int warmupRuns, int maxQuadraticSize) {
        this.dataGenerator = dataGenerator;
        this.samples = Math.max(1, samples);
        this.warmupRuns = Math.max(0, warmupRuns);
        this.maxQuadraticSize = Math.max(1, maxQuadraticSize);
    }

    public BenchmarkOutput runBenchmarks(List<Integer> arraySizes, List<InputType> inputTypes, List<Integer> threadOptions) {
        List<BenchmarkResult> rawResults = new ArrayList<>();
        Set<Integer> normalizedThreads = normalizeThreads(threadOptions);
        int totalCombinations = inputTypes.size() * arraySizes.size();
        int combinationIndex = 0;
        long benchmarkStart = System.nanoTime();

        printBenchmarkHeader(totalCombinations, normalizedThreads);

        for (InputType inputType : inputTypes) {
            for (Integer size : arraySizes) {
                combinationIndex++;
                long combinationStart = System.nanoTime();
                printCombinationHeader(combinationIndex, totalCombinations, inputType, size);
                int[] baseArray = dataGenerator.generate(size, inputType);

                runSequentialBenchmarks(rawResults, baseArray, inputType, size);
                runParallelBenchmarks(rawResults, baseArray, inputType, size, normalizedThreads);

                double elapsedSeconds = (System.nanoTime() - combinationStart) / 1_000_000_000.0;
                System.out.printf("[END ] Entrada %d/%d concluida em %.2fs%n", combinationIndex, totalCombinations, elapsedSeconds);
            }
        }

        List<BenchmarkSummary> summaries = buildSummary(rawResults);
        double totalElapsedSeconds = (System.nanoTime() - benchmarkStart) / 1_000_000_000.0;
        printBenchmarkFooter(totalElapsedSeconds, rawResults.size(), summaries.size());
        return new BenchmarkOutput(rawResults, summaries);
    }

    private void runSequentialBenchmarks(List<BenchmarkResult> rawResults, int[] baseArray, InputType inputType, int size) {
        List<SortAlgorithm> algorithms = List.of(
                new BubbleSort(),
                new InsertionSort(),
                new MergeSort(),
                new QuickSort()
        );

        for (SortAlgorithm algorithm : algorithms) {
            if (isQuadraticAlgorithm(algorithm.getName()) && size > maxQuadraticSize) {
                System.out.printf("[SKIP] %-18s | modo=%-10s | tipo=%-13s | tamanho=%7d | limite=%d%n",
                        algorithm.getName(), modeOf(algorithm), inputType, size, maxQuadraticSize);
                continue;
            }

            warmup(algorithm, baseArray);
            double totalMs = 0.0;
            for (int sample = 1; sample <= samples; sample++) {
                int[] workingCopy = baseArray.clone();
                long start = System.nanoTime();
                algorithm.sort(workingCopy);
                long end = System.nanoTime();

                validateSorted(workingCopy, algorithm.getName(), inputType, size);
                double elapsedMs = (end - start) / 1_000_000.0;
                totalMs += elapsedMs;

                rawResults.add(new BenchmarkResult(
                        algorithm.getName(),
                        modeOf(algorithm),
                        inputType,
                        size,
                        1,
                        sample,
                        elapsedMs
                ));
            }

            printAlgorithmDone(algorithm.getName(), modeOf(algorithm), inputType, size, 1, totalMs / samples);
        }
    }

    private void runParallelBenchmarks(
            List<BenchmarkResult> rawResults,
            int[] baseArray,
            InputType inputType,
            int size,
            Set<Integer> threadOptions
    ) {
        for (Integer threads : threadOptions) {
            try (ParallelMergeSort parallelMergeSort = new ParallelMergeSort(threads);
                 ParallelQuickSort parallelQuickSort = new ParallelQuickSort(threads)) {
                List<SortAlgorithm> algorithms = List.of(parallelMergeSort, parallelQuickSort);

                for (SortAlgorithm algorithm : algorithms) {
                    warmup(algorithm, baseArray);
                    double totalMs = 0.0;
                    for (int sample = 1; sample <= samples; sample++) {
                        int[] workingCopy = baseArray.clone();
                        long start = System.nanoTime();
                        algorithm.sort(workingCopy);
                        long end = System.nanoTime();

                        validateSorted(workingCopy, algorithm.getName(), inputType, size);
                        double elapsedMs = (end - start) / 1_000_000.0;
                        totalMs += elapsedMs;

                        rawResults.add(new BenchmarkResult(
                                algorithm.getName(),
                                modeOf(algorithm),
                                inputType,
                                size,
                                threads,
                                sample,
                                elapsedMs
                        ));
                    }

                    printAlgorithmDone(algorithm.getName(), modeOf(algorithm), inputType, size, threads, totalMs / samples);
                }
            }
        }
    }

    private void printBenchmarkHeader(int totalCombinations, Set<Integer> threads) {
        System.out.println();
        System.out.println(LINE);
        System.out.println("BENCHMARK DE ORDENACAO - EXECUCAO INICIADA");
        System.out.println(LINE);
        System.out.printf("Combinacoes de entrada: %d | Amostras por combinacao: %d | Warm-up: %d%n",
                totalCombinations, samples, warmupRuns);
        System.out.printf("Threads paralelas: %s | Limite quadratico: %d%n", threads, maxQuadraticSize);
        System.out.println(SUBLINE);
    }

    private void printCombinationHeader(int current, int total, InputType inputType, int size) {
        double progress = (current * 100.0) / total;
        System.out.println();
        System.out.println(SUBLINE);
        System.out.printf("[START] Entrada %d/%d (%.1f%%) | tipo=%-13s | tamanho=%d%n",
                current, total, progress, inputType, size);
        System.out.println(SUBLINE);
    }

    private void printAlgorithmDone(String algorithm, String mode, InputType inputType, int size, int threads, double avgMs) {
        System.out.printf("[OK  ] %-18s | modo=%-10s | tipo=%-13s | tamanho=%7d | threads=%2d | media=%9.3f ms%n",
                algorithm, mode, inputType, size, threads, avgMs);
    }

    private void printBenchmarkFooter(double elapsedSeconds, int rawCount, int summaryCount) {
        System.out.println();
        System.out.println(LINE);
        System.out.println("BENCHMARK FINALIZADO");
        System.out.println(LINE);
        System.out.printf("Tempo total: %.2fs | Amostras coletadas: %d | Linhas de resumo: %d%n",
                elapsedSeconds, rawCount, summaryCount);
        System.out.println(LINE);
    }

    private List<BenchmarkSummary> buildSummary(List<BenchmarkResult> rawResults) {
        Map<SummaryKey, List<Double>> grouped = new LinkedHashMap<>();
        for (BenchmarkResult result : rawResults) {
            SummaryKey key = new SummaryKey(
                    result.algorithm(),
                    result.mode(),
                    result.inputType(),
                    result.arraySize(),
                    result.threads()
            );
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(result.timeMs());
        }

        Map<BaselineKey, Double> sequentialBaseline = new LinkedHashMap<>();
        for (Map.Entry<SummaryKey, List<Double>> entry : grouped.entrySet()) {
            SummaryKey key = entry.getKey();
            if ("sequential".equals(key.mode)) {
                sequentialBaseline.put(
                        new BaselineKey(key.algorithm, key.inputType, key.arraySize),
                        Statistics.average(entry.getValue())
                );
            }
        }

        List<BenchmarkSummary> summaries = new ArrayList<>();
        for (Map.Entry<SummaryKey, List<Double>> entry : grouped.entrySet()) {
            SummaryKey key = entry.getKey();
            List<Double> times = entry.getValue();

            double avg = Statistics.average(times);
            double min = Statistics.min(times);
            double max = Statistics.max(times);
            double stdDev = Statistics.standardDeviation(times);

            double speedup = 1.0;
            double efficiency = 1.0;

            if ("parallel".equals(key.mode)) {
                String baseAlgorithmName = sequentialCounterpartName(key.algorithm);
                Double baselineAvg = sequentialBaseline.get(new BaselineKey(baseAlgorithmName, key.inputType, key.arraySize));
                if (baselineAvg != null) {
                    speedup = Statistics.speedup(baselineAvg, avg);
                    efficiency = Statistics.efficiency(speedup, key.threads);
                } else {
                    speedup = 0.0;
                    efficiency = 0.0;
                }
            }

            summaries.add(new BenchmarkSummary(
                    key.algorithm,
                    key.mode,
                    key.inputType,
                    key.arraySize,
                    key.threads,
                    avg,
                    min,
                    max,
                    stdDev,
                    speedup,
                    efficiency
            ));
        }

        return summaries;
    }

    private void warmup(SortAlgorithm algorithm, int[] baseArray) {
        for (int i = 0; i < warmupRuns; i++) {
            int[] copy = baseArray.clone();
            algorithm.sort(copy);
        }
    }

    private static String modeOf(SortAlgorithm algorithm) {
        return algorithm.isParallel() ? "parallel" : "sequential";
    }

    private static boolean isQuadraticAlgorithm(String algorithmName) {
        return "BubbleSort".equals(algorithmName) || "InsertionSort".equals(algorithmName);
    }

    private static String sequentialCounterpartName(String algorithmName) {
        if ("ParallelMergeSort".equals(algorithmName)) {
            return "MergeSort";
        }
        if ("ParallelQuickSort".equals(algorithmName)) {
            return "QuickSort";
        }
        return algorithmName;
    }

    private static void validateSorted(int[] array, String algorithmName, InputType inputType, int size) {
        for (int i = 1; i < array.length; i++) {
            if (array[i - 1] > array[i]) {
                throw new IllegalStateException(
                        "Array not sorted by " + algorithmName
                                + " for input=" + inputType
                                + " size=" + size
                );
            }
        }
    }

    private static Set<Integer> normalizeThreads(List<Integer> threadOptions) {
        Set<Integer> normalized = new LinkedHashSet<>();
        if (threadOptions == null || threadOptions.isEmpty()) {
            normalized.add(1);
            return normalized;
        }

        for (Integer thread : threadOptions) {
            if (thread != null && thread > 0) {
                normalized.add(thread);
            }
        }

        if (normalized.isEmpty()) {
            normalized.add(1);
        }
        return normalized;
    }

    private record SummaryKey(String algorithm, String mode, InputType inputType, int arraySize, int threads) {
    }

    private record BaselineKey(String algorithm, InputType inputType, int arraySize) {
    }

    public record BenchmarkOutput(List<BenchmarkResult> rawResults, List<BenchmarkSummary> summaryResults) {
    }
}
