package br.com.concorrencia.stats;

import java.util.List;

public final class Statistics {
    private Statistics() {
    }

    public static double average(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (Double value : values) {
            sum += value;
        }
        return sum / values.size();
    }

    public static double min(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        double min = Double.MAX_VALUE;
        for (Double value : values) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    public static double max(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        double max = -Double.MAX_VALUE;
        for (Double value : values) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    public static double standardDeviation(List<Double> values) {
        if (values == null || values.size() < 2) {
            return 0.0;
        }

        double mean = average(values);
        double sumSquaredDiff = 0.0;
        for (Double value : values) {
            double diff = value - mean;
            sumSquaredDiff += diff * diff;
        }

        return Math.sqrt(sumSquaredDiff / values.size());
    }

    public static double speedup(double sequentialTimeMs, double parallelTimeMs) {
        if (parallelTimeMs <= 0.0) {
            return 0.0;
        }
        return sequentialTimeMs / parallelTimeMs;
    }

    public static double efficiency(double speedup, int threads) {
        if (threads <= 0) {
            return 0.0;
        }
        return speedup / threads;
    }
}
