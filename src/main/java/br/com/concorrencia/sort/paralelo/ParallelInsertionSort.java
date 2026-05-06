package br.com.concorrencia.sort.paralelo;

import br.com.concorrencia.sort.SortAlgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ParallelInsertionSort implements SortAlgorithm, AutoCloseable {
    private final ExecutorService executor;
    private final int quantidadeThreads;

    public ParallelInsertionSort(int parallelism) {
        this.quantidadeThreads = Math.max(1, parallelism);
        this.executor = Executors.newFixedThreadPool(this.quantidadeThreads);
    }

    @Override
    public void sort(int[] array) {
        if (array.length < 2) {
            return;
        }

        int tamanhoBloco = Math.max(1, (array.length + quantidadeThreads - 1) / quantidadeThreads);
        List<Intervalo> blocos = new ArrayList<>();
        List<Future<?>> tarefas = new ArrayList<>();

        for (int inicio = 0; inicio < array.length; inicio += tamanhoBloco) {
            int fim = Math.min(array.length, inicio + tamanhoBloco);
            blocos.add(new Intervalo(inicio, fim));
            int inicioBloco = inicio;
            int fimBloco = fim;
            tarefas.add(executor.submit(() -> insertionSortFaixa(array, inicioBloco, fimBloco)));
        }

        aguardarTarefas(tarefas);

        while (blocos.size() > 1) {
            List<Future<?>> merges = new ArrayList<>();
            List<Intervalo> proximosBlocos = new ArrayList<>();

            for (int i = 0; i < blocos.size(); i += 2) {
                if (i + 1 >= blocos.size()) {
                    proximosBlocos.add(blocos.get(i));
                    continue;
                }

                Intervalo esquerda = blocos.get(i);
                Intervalo direita = blocos.get(i + 1);
                Intervalo combinado = new Intervalo(esquerda.inicio, direita.fim);
                proximosBlocos.add(combinado);
                merges.add(executor.submit(() -> merge(array, esquerda, direita)));
            }

            aguardarTarefas(merges);
            blocos = proximosBlocos;
        }
    }

    private void insertionSortFaixa(int[] array, int inicio, int fim) {
        for (int i = inicio + 1; i < fim; i++) {
            int chave = array[i];
            int j = i - 1;
            while (j >= inicio && array[j] > chave) {
                array[j + 1] = array[j];
                j--;
            }
            array[j + 1] = chave;
        }
    }

    private void merge(int[] array, Intervalo esquerda, Intervalo direita) {
        int[] temp = new int[direita.fim - esquerda.inicio];
        int i = esquerda.inicio;
        int j = direita.inicio;
        int k = 0;

        while (i < esquerda.fim && j < direita.fim) {
            if (array[i] <= array[j]) {
                temp[k++] = array[i++];
            } else {
                temp[k++] = array[j++];
            }
        }
        while (i < esquerda.fim) {
            temp[k++] = array[i++];
        }
        while (j < direita.fim) {
            temp[k++] = array[j++];
        }

        System.arraycopy(temp, 0, array, esquerda.inicio, temp.length);
    }

    private void aguardarTarefas(List<Future<?>> tarefas) {
        for (Future<?> tarefa : tarefas) {
            try {
                tarefa.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Erro ao executar ParallelInsertionSort", e);
            } catch (ExecutionException e) {
                throw new IllegalStateException("Erro ao executar ParallelInsertionSort", e);
            }
        }
    }

    @Override
    public String getName() {
        return "ParallelInsertionSort";
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

    private record Intervalo(int inicio, int fim) {
    }
}
