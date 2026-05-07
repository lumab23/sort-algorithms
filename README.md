# Análise de Desempenho de Algoritmos de Ordenação em Ambientes Sequenciais e Paralelos

## Objetivo

Este projeto acadêmico compara o desempenho de algoritmos de ordenação em Java nas versões sequencial e paralela.

O foco é executar benchmarks com diferentes tamanhos de entrada, diferentes tipos de dados e diferentes quantidades de threads, gerando resultados em arquivos CSV para análise estatística e construção de gráficos.

## Algoritmos implementados

### Sequenciais

- Bubble Sort
- Insertion Sort
- Merge Sort
- Quick Sort

### Paralelos

- Parallel Bubble Sort
- Parallel Insertion Sort
- Parallel Merge Sort
- Parallel Quick Sort

As versões `ParallelMergeSort` e `ParallelQuickSort` utilizam `ForkJoinPool` e `RecursiveAction`, pois são baseadas em divisão e conquista.

As versões `ParallelBubbleSort` e `ParallelInsertionSort` utilizam `ExecutorService` com `FixedThreadPool`.

Nas versões `ParallelMergeSort` e `ParallelQuickSort`, `Arrays.sort` é usado apenas em partições pequenas abaixo de um `threshold`, com o objetivo de reduzir o overhead de criação de tarefas paralelas.

## Tecnologias utilizadas

- Java 17
- Maven
- JUnit 5
- Java Swing
- JFreeChart
- Fork/Join Framework
- ExecutorService

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
├── gui/
│   └── BenchmarkMainGui.java
├── sort/
│   ├── SortAlgorithm.java
│   ├── serial/
│   │   ├── BubbleSort.java
│   │   ├── InsertionSort.java
│   │   ├── MergeSort.java
│   │   └── QuickSort.java
│   └── paralelo/
│       ├── ParallelBubbleSort.java
│       ├── ParallelInsertionSort.java
│       ├── ParallelMergeSort.java
│       └── ParallelQuickSort.java
└── stats/
    ├── Statistics.java
    └── BenchmarkSummary.java
```

A pasta de testes segue a estrutura:

```text
src/test/java/br/com/concorrencia/
├── benchmark/
│   └── CsvWriterTest.java
├── data/
│   └── DataGeneratorTest.java
├── sort/
│   ├── SortAlgorithmsTest.java
│   └── ParallelQuadraticSortsTest.java
└── stats/
    └── StatisticsTest.java
```

## Requisitos

* Java 17 ou superior
* Maven 3.9 ou superior

Para verificar a versão do Java:

```bash
java -version
```

Para verificar a versão do Maven:

```bash
mvn -version
```

## Como executar

### Rodar os testes unitários

```bash
mvn clean test
```

Esse comando compila o projeto e executa os testes com JUnit 5.

Os testes validam:

* funcionamento dos algoritmos de ordenação;
* ordenação de arrays vazios;
* ordenação de arrays com um elemento;
* ordenação de arrays já ordenados;
* ordenação de arrays em ordem reversa;
* ordenação com valores repetidos;
* ordenação com valores negativos;
* geração dos diferentes tipos de entrada;
* cálculos estatísticos;
* escrita dos arquivos CSV.

### Abrir a interface gráfica

```bash
mvn exec:java
```

Sem argumentos, a aplicação abre a interface gráfica (Java Swing).

A interface permite configurar:

* tamanhos dos arrays;
* tipo de entrada;
* número de threads;
* quantidade de amostras;
* quantidade de warm-ups;
* limite para algoritmos quadráticos;
* algoritmos que serão executados.

Também permite visualizar:

* log da execução;
* tabela de resultados consolidados;
* gráfico de tempo médio em função do tamanho do array.

### Rodar o benchmark completo via terminal

```bash
mvn exec:java -Dexec.args="cli"
```

Com qualquer argumento que não seja modo GUI (por exemplo `cli`), o programa executa o benchmark completo no terminal, sem abrir a janela.

Durante a execução via terminal, o benchmark imprime o progresso no console por:

* tipo de entrada;
* tamanho do array;
* algoritmo;
* modo de execução;
* número de threads;
* tempo médio.

Ao final, os arquivos CSV são gerados automaticamente na pasta `results/`.

## Configuração do benchmark completo

A execução completa configurada no `Main` utiliza:

* tamanhos: `1000, 5000, 10000, 50000, 100000`
* tipos de entrada: `RANDOM, SORTED, REVERSED, NEARLY_SORTED, MANY_REPEATED`
* amostras por combinação: `5`
* warm-up por combinação: `2`
* threads paralelas: `1, 2, 4, 8, availableProcessors()`
* limite padrão para algoritmos O(n²): `10000` elementos

Os algoritmos Bubble Sort, Insertion Sort, Parallel Bubble Sort e Parallel Insertion Sort são limitados por padrão a 10.000 elementos, pois possuem custo elevado em entradas grandes.

## Tipos de entrada

O projeto gera automaticamente diferentes tipos de arrays por meio da classe `DataGenerator`.

### RANDOM

Array com valores aleatórios.

### SORTED

Array já ordenado em ordem crescente.

### REVERSED

Array em ordem decrescente.

### NEARLY_SORTED

Array quase ordenado, com algumas posições trocadas.

### MANY_REPEATED

Array com muitos valores repetidos.

## Arquivos CSV gerados

Os arquivos de saída são gerados na pasta:

```text
results/
```

Arquivos principais:

```text
results/raw_results.csv
results/summary_results.csv
```

## Formato de `raw_results.csv`

Cabeçalho:

```csv
algorithm,mode,input_type,array_size,threads,sample,time_ms
```

Cada linha representa uma amostra individual.

Campos:

* `algorithm`: nome do algoritmo executado;
* `mode`: modo de execução, podendo ser `sequential` ou `parallel`;
* `input_type`: tipo de entrada utilizada;
* `array_size`: tamanho do array;
* `threads`: número de threads utilizadas;
* `sample`: índice da amostra;
* `time_ms`: tempo de execução em milissegundos.

Exemplo:

```csv
QuickSort,sequential,RANDOM,10000,1,1,0.886000
ParallelQuickSort,parallel,RANDOM,10000,8,1,0.425000
```

## Formato de `summary_results.csv`

Cabeçalho:

```csv
algorithm,mode,input_type,array_size,threads,avg_time_ms,min_time_ms,max_time_ms,std_dev_ms,speedup,efficiency
```

Cada linha resume um grupo de amostras com a mesma combinação de algoritmo, modo, tipo de entrada, tamanho do array e número de threads.

Campos:

* `algorithm`: nome do algoritmo;
* `mode`: modo de execução;
* `input_type`: tipo de entrada;
* `array_size`: tamanho do array;
* `threads`: número de threads utilizadas;
* `avg_time_ms`: tempo médio;
* `min_time_ms`: menor tempo observado;
* `max_time_ms`: maior tempo observado;
* `std_dev_ms`: desvio padrão;
* `speedup`: razão entre o tempo médio sequencial e o tempo médio paralelo;
* `efficiency`: eficiência paralela, calculada como `speedup / threads`.

## Regra adotada para speedup e eficiência

Para execuções sequenciais:

```text
speedup = 1.0
efficiency = 1.0
```

Para execuções paralelas:

```text
speedup = tempo_médio_sequencial / tempo_médio_paralelo
efficiency = speedup / número_de_threads
```

Quando não existe baseline sequencial correspondente:

```text
speedup = 0.0
efficiency = 0.0
```

## Interface gráfica

A interface gráfica foi criada para facilitar a execução e a análise dos benchmarks.

Ela apresenta:

* painel de configuração dos parâmetros;
* seleção dos algoritmos;
* log de execução;
* tabela de resultados consolidados;
* gráfico `Tempo Médio (ms) vs Tamanho do Array`;
* botão para iniciar o benchmark;
* barra de status da execução.

A interface mostra, de forma visual, informações semelhantes às exibidas no terminal, acrescentando tabela e gráfico para facilitar a interpretação dos resultados.

## Decisões de implementação

Algumas decisões foram adotadas para tornar os resultados mais consistentes:

* uso de warm-up antes das medições para reduzir efeitos da compilação Just-In-Time da JVM;
* cópia do array base antes de cada execução, evitando que um algoritmo receba dados já ordenados por outro;
* validação da ordenação ao final de cada execução;
* criação automática da pasta `results/`;
* escrita dos CSVs com `Locale.US`, garantindo ponto decimal;
* fechamento explícito de `ForkJoinPool` e `ExecutorService`;
* limite de tamanho para algoritmos quadráticos;
* uso de threshold em algoritmos paralelos baseados em Fork/Join para reduzir overhead.

## Resultados da execução completa

Na execução completa utilizada no artigo, foram gerados:

* 2.000 registros brutos em `raw_results.csv`;
* 400 registros consolidados em `summary_results.csv`;
* testes com 5 tipos de entrada;
* testes com arrays de até 100.000 elementos;
* execuções paralelas com 1, 2, 4 e 8 threads.

## Limitações conhecidas

* Algoritmos O(n²), como Bubble Sort e Insertion Sort, crescem rapidamente com entradas grandes.
* Em alguns cenários, a versão paralela pode ser mais lenta que a sequencial por causa do overhead de criação e sincronização de tarefas.
* Resultados de benchmark podem variar entre execuções devido à carga da máquina, frequência da CPU, garbage collection e outros processos do sistema operacional.
* A interface gráfica é voltada para visualização e execução manual; a execução completa mais abrangente é feita via terminal.

## Possíveis melhorias futuras

* Adicionar novos algoritmos, como Counting Sort e Selection Sort.
* Permitir comparação direta entre arquivos CSV antigos e novos.
* Melhorar os gráficos da interface.
* Adicionar filtros por algoritmo, tipo de entrada e número de threads.
* Exportar gráficos automaticamente como imagem.
* Adicionar suporte a datasets externos.

## Autoras

* Amanda Fonseca
* Luma Brandão
