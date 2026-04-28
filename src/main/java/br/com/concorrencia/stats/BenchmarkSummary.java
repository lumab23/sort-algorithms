package br.com.concorrencia.stats;

import br.com.concorrencia.data.InputType;

public record BenchmarkSummary(
        String algorithm,
        String mode,
        InputType inputType,
        int arraySize,
        int threads,
        double avgTimeMs,
        double minTimeMs,
        double maxTimeMs,
        double stdDevMs,
        double speedup,
        double efficiency
) {
}
