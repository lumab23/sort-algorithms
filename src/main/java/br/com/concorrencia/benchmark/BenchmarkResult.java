package br.com.concorrencia.benchmark;

import br.com.concorrencia.data.InputType;

public record BenchmarkResult(
        String algorithm,
        String mode,
        InputType inputType,
        int arraySize,
        int threads,
        int sample,
        double timeMs
) {
}
