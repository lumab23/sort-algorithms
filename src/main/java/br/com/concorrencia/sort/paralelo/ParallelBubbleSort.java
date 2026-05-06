package br.com.concorrencia.sort.paralelo;

import br.com.concorrencia.sort.SortAlgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ParallelBubbleSort implements SortAlgorithm, AutoCloseable {
    private final ExecutorService executor;
    private final int quantidadeThreads;

    public ParallelBubbleSort(int parallelism) {
        this.quantidadeThreads = Math.max(1, parallelism);
        this.executor = Executors.newFixedThreadPool(this.quantidadeThreads);
    }

    @Override
    public void sort(int[] array) {
        int tamanho = array.length;
        if (tamanho < 2) {
            return;
        }

        for (int fase = 0; fase < tamanho; fase++) {
            int inicio = fase % 2;
            List<Future<Boolean>> tarefas = new ArrayList<>();
            int pares = (tamanho - inicio) / 2;
            int bloco = Math.max(1, (pares + quantidadeThreads - 1) / quantidadeThreads);

            for (int indicePar = 0; indicePar < pares; indicePar += bloco) {
                int inicioBloco = indicePar;
                int fimBloco = Math.min(pares, indicePar + bloco);
                tarefas.add(executor.submit(() -> compararETrocarBloco(array, inicio, inicioBloco, fimBloco)));
            }

            boolean houveTroca = false;
            for (Future<Boolean> tarefa : tarefas) {
                try {
                    houveTroca |= tarefa.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Erro ao executar ParallelBubbleSort", e);
                } catch (ExecutionException e) {
                    throw new IllegalStateException("Erro ao executar ParallelBubbleSort", e);
                }
            }

            if (!houveTroca && fase % 2 == 1) {
                break;
            }
        }
    }

    private boolean compararETrocarBloco(int[] array, int inicioFase, int inicioBloco, int fimBloco) {
        boolean houveTroca = false;
        for (int indicePar = inicioBloco; indicePar < fimBloco; indicePar++) {
            int esquerda = inicioFase + indicePar * 2;
            int direita = esquerda + 1;
            if (direita >= array.length) {
                break;
            }
            if (array[esquerda] > array[direita]) {
                int temp = array[esquerda];
                array[esquerda] = array[direita];
                array[direita] = temp;
                houveTroca = true;
            }
        }
        return houveTroca;
    }

    @Override
    public String getName() {
        return "ParallelBubbleSort";
    }

    @Override
    public boolean isParallel() {
        return true;
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
