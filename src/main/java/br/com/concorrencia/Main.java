package br.com.concorrencia;

import br.com.concorrencia.benchmark.BenchmarkRunner;
import br.com.concorrencia.benchmark.CsvWriter;
import br.com.concorrencia.data.DataGenerator;
import br.com.concorrencia.data.InputType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        List<Integer> arraySizes = Arrays.asList(1_000, 5_000, 10_000, 50_000, 100_000);
        List<InputType> inputTypes = Arrays.asList(
                InputType.RANDOM,
                InputType.SORTED,
                InputType.REVERSED,
                InputType.NEARLY_SORTED,
                InputType.MANY_REPEATED
        );

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        List<Integer> threadOptions = Arrays.asList(1, 2, 4, 8, availableProcessors);

        int samples = 5;
        int warmupRuns = 2;
        int maxQuadraticSize = 10_000;

        DataGenerator dataGenerator = new DataGenerator(42L);
        BenchmarkRunner runner = new BenchmarkRunner(dataGenerator, samples, warmupRuns, maxQuadraticSize);

        BenchmarkRunner.BenchmarkOutput output = runner.runBenchmarks(arraySizes, inputTypes, threadOptions);

        Path rawOutput = Path.of("results", "raw_results.csv");
        Path summaryOutput = Path.of("results", "summary_results.csv");

        CsvWriter.writeRawResults(rawOutput, output.rawResults());
        CsvWriter.writeSummaryResults(summaryOutput, output.summaryResults());

        System.out.println();
        System.out.println("============================================================");
        System.out.println("SAIDA GERADA COM SUCESSO");
        System.out.println("============================================================");
        System.out.println("Arquivo bruto   : " + rawOutput.toAbsolutePath());
        System.out.println("Arquivo resumo  : " + summaryOutput.toAbsolutePath());
        System.out.println("Amostras (raw)  : " + output.rawResults().size());
        System.out.println("Linhas (summary): " + output.summaryResults().size());
        System.out.println("============================================================");
    }
}
