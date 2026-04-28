package br.com.concorrencia.benchmark;

import br.com.concorrencia.data.InputType;
import br.com.concorrencia.stats.BenchmarkSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvWriterTest {
    @TempDir
    Path tempDir;

    @Test
    void shouldWriteRawAndSummaryCsvWithSingleHeader() throws IOException {
        Path rawPath = tempDir.resolve("results").resolve("raw_results.csv");
        Path summaryPath = tempDir.resolve("results").resolve("summary_results.csv");

        List<BenchmarkResult> rawResults = List.of(
                new BenchmarkResult("MergeSort", "sequential", InputType.RANDOM, 1000, 1, 1, 12.345678),
                new BenchmarkResult("ParallelMergeSort", "parallel", InputType.RANDOM, 1000, 4, 1, 6.172839)
        );

        List<BenchmarkSummary> summaries = List.of(
                new BenchmarkSummary("MergeSort", "sequential", InputType.RANDOM, 1000, 1,
                        12.345678, 12.345678, 12.345678, 0.0, 1.0, 1.0)
        );

        CsvWriter.writeRawResults(rawPath, rawResults);
        CsvWriter.writeSummaryResults(summaryPath, summaries);

        List<String> rawLines = Files.readAllLines(rawPath);
        List<String> summaryLines = Files.readAllLines(summaryPath);

        assertEquals("algorithm,mode,input_type,array_size,threads,sample,time_ms", rawLines.get(0));
        assertEquals(3, rawLines.size());
        assertTrue(rawLines.get(1).contains("12.345678"));

        assertEquals("algorithm,mode,input_type,array_size,threads,avg_time_ms,min_time_ms,max_time_ms,std_dev_ms,speedup,efficiency",
                summaryLines.get(0));
        assertEquals(2, summaryLines.size());
        assertTrue(summaryLines.get(1).contains("12.345678"));
    }
}
