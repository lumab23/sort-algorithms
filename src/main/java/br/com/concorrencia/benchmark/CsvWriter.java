package br.com.concorrencia.benchmark;

import br.com.concorrencia.stats.BenchmarkSummary;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class CsvWriter {
    private CsvWriter() {
    }

    public static void writeRawResults(Path outputPath, List<BenchmarkResult> results) throws IOException {
        ensureParent(outputPath);

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            writer.write("algorithm,mode,input_type,array_size,threads,sample,time_ms");
            writer.newLine();

            for (BenchmarkResult result : results) {
                writer.write(String.format(
                        Locale.US,
                        "%s,%s,%s,%d,%d,%d,%.6f",
                        result.algorithm(),
                        result.mode(),
                        result.inputType(),
                        result.arraySize(),
                        result.threads(),
                        result.sample(),
                        result.timeMs()
                ));
                writer.newLine();
            }
        }
    }

    public static void writeSummaryResults(Path outputPath, List<BenchmarkSummary> summaries) throws IOException {
        ensureParent(outputPath);

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            writer.write("algorithm,mode,input_type,array_size,threads,avg_time_ms,min_time_ms,max_time_ms,std_dev_ms,speedup,efficiency");
            writer.newLine();

            for (BenchmarkSummary summary : summaries) {
                writer.write(String.format(
                        Locale.US,
                        "%s,%s,%s,%d,%d,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f",
                        summary.algorithm(),
                        summary.mode(),
                        summary.inputType(),
                        summary.arraySize(),
                        summary.threads(),
                        summary.avgTimeMs(),
                        summary.minTimeMs(),
                        summary.maxTimeMs(),
                        summary.stdDevMs(),
                        summary.speedup(),
                        summary.efficiency()
                ));
                writer.newLine();
            }
        }
    }

    private static void ensureParent(Path outputPath) throws IOException {
        Path parent = outputPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }
}
