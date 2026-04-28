package br.com.concorrencia.stats;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatisticsTest {
    @Test
    void shouldCalculateCoreMetrics() {
        List<Double> values = List.of(1.0, 2.0, 3.0, 4.0);

        assertEquals(2.5, Statistics.average(values), 0.000001);
        assertEquals(1.0, Statistics.min(values), 0.000001);
        assertEquals(4.0, Statistics.max(values), 0.000001);
        assertEquals(1.1180339887, Statistics.standardDeviation(values), 0.000001);
    }

    @Test
    void shouldCalculateSpeedupAndEfficiency() {
        double speedup = Statistics.speedup(10.0, 5.0);
        double efficiency = Statistics.efficiency(speedup, 4);

        assertEquals(2.0, speedup, 0.000001);
        assertEquals(0.5, efficiency, 0.000001);
    }

    @Test
    void shouldHandleEmptyAndZeroDivisionCases() {
        assertEquals(0.0, Statistics.average(List.of()), 0.000001);
        assertEquals(0.0, Statistics.min(List.of()), 0.000001);
        assertEquals(0.0, Statistics.max(List.of()), 0.000001);
        assertEquals(0.0, Statistics.standardDeviation(List.of(5.0)), 0.000001);
        assertEquals(0.0, Statistics.speedup(10.0, 0.0), 0.000001);
        assertEquals(0.0, Statistics.efficiency(2.0, 0), 0.000001);
    }
}
