# Analise de Desempenho de Algoritmos de Ordenacao em Ambientes Sequenciais e Paralelos

## Objetivo
Este projeto academico compara o desempenho de algoritmos de ordenacao em Java nas versoes sequencial e paralela.
O foco e gerar resultados reprodutiveis em CSV para analise estatistica e construcao de graficos.

## Observacao importante
Este trabalho trata de **ordenacao**, nao de busca.

## Algoritmos implementados
### Sequenciais
- Bubble Sort
- Insertion Sort
- Merge Sort
- Quick Sort

### Paralelos (ForkJoinPool + RecursiveAction)
- Parallel Merge Sort
- Parallel Quick Sort

Nas versoes paralelas, `Arrays.sort` e usado apenas em particoes pequenas abaixo de um `threshold` para reduzir overhead de paralelizacao.

## Estrutura do projeto
```text
src/main/java/br/com/concorrencia/
├── Main.java
├── benchmark/
│   ├── BenchmarkRunner.java
│   ├── BenchmarkResult.java
│   └── CsvWriter.java
├── data/
│   ├── DataGenerator.java
│   └── InputType.java
├── sort/
│   ├── SortAlgorithm.java
│   ├── BubbleSort.java
│   ├── InsertionSort.java
│   ├── MergeSort.java
│   ├── QuickSort.java
│   ├── ParallelMergeSort.java
│   └── ParallelQuickSort.java
└── stats/
	├── Statistics.java
	└── BenchmarkSummary.java
```

## Requisitos
- Java 17+
- Maven 3.9+

## Como executar
### Rodar testes
```bash
mvn clean test
```

### Rodar benchmark
```bash
mvn exec:java
```

Durante a execucao, o benchmark imprime progresso no console por tipo de entrada, tamanho, algoritmo e numero de threads.

## Configuracao inicial (Main)
- tamanhos: `1000, 5000, 10000, 50000, 100000`
- tipos de entrada: `RANDOM, SORTED, REVERSED, NEARLY_SORTED, MANY_REPEATED`
- amostras por combinacao: `5`
- warm-up por combinacao: `2`
- threads paralelas: `1, 2, 4, 8, availableProcessors()`
- limite padrao para algoritmos O(n^2): `10000` (Bubble e Insertion), com limite maximo recomendado de `50000`

## Onde os CSVs sao gerados
Arquivos de saida em `results/`:
- `results/raw_results.csv`
- `results/summary_results.csv`

## Formato de raw_results.csv
Cabecalho exato:

`algorithm,mode,input_type,array_size,threads,sample,time_ms`

Cada linha representa uma amostra individual.

Campos:
- `algorithm`: nome do algoritmo
- `mode`: `sequential` ou `parallel`
- `input_type`: tipo de entrada
- `array_size`: tamanho do array
- `threads`: threads utilizadas
- `sample`: indice da amostra
- `time_ms`: tempo em milissegundos

## Formato de summary_results.csv
Cabecalho exato:

`algorithm,mode,input_type,array_size,threads,avg_time_ms,min_time_ms,max_time_ms,std_dev_ms,speedup,efficiency`

Cada linha resume um grupo de amostras com mesma combinacao de algoritmo/modo/entrada/tamanho/threads.

Campos:
- `avg_time_ms`: media
- `min_time_ms`: minimo
- `max_time_ms`: maximo
- `std_dev_ms`: desvio padrao
- `speedup`: razao entre tempo medio sequencial e tempo medio paralelo
- `efficiency`: `speedup / threads`

## Regra adotada para speedup e eficiencia
- Modo sequencial: `speedup = 1.0` e `efficiency = 1.0`.
- Modo paralelo: `speedup = avg_sequencial / avg_paralelo` e `efficiency = speedup / threads`.
- Em casos sem baseline sequencial correspondente: `speedup = 0.0` e `efficiency = 0.0`.

## Decisoes de implementacao
- Warm-up antes das medicoes para reduzir efeitos de JIT.
- Copia do array base antes de cada execucao para evitar contaminacao entre amostras.
- Validacao de ordenacao ao final de cada execucao.
- Criacao automatica da pasta `results/`.
- Escrita CSV com `Locale.US` para garantir ponto decimal.
- Fechamento explicito dos `ForkJoinPool` para evitar vazamento de threads.

## Limitacoes conhecidas
- Algoritmos O(n^2) (Bubble/Insertion) crescem rapidamente com entradas grandes.
- Em alguns cenarios, paralelismo pode ser mais lento por overhead de criacao/sincronizacao de tarefas.
- Resultados podem variar entre execucoes por carga da maquina, frequencia da CPU e garbage collection.

## Proximos passos sugeridos
- Gerar graficos de linhas (tempo x tamanho) por tipo de entrada.
- Gerar graficos de speedup e eficiencia por numero de threads.
- Repetir benchmark em maquina dedicada para reduzir ruido experimental.
# sort-algorithms
