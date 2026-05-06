package br.com.concorrencia.gui;

import br.com.concorrencia.benchmark.BenchmarkRunner;
import br.com.concorrencia.benchmark.CsvWriter;
import br.com.concorrencia.data.DataGenerator;
import br.com.concorrencia.data.InputType;
import br.com.concorrencia.stats.BenchmarkSummary;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BenchmarkMainGui extends JFrame {
    private static final String TITULO = "Análise de Desempenho: Ordenação Serial vs Paralela";
    private static final List<String> NOMES_ALGORITMOS = List.of(
            "BubbleSort",
            "InsertionSort",
            "MergeSort",
            "QuickSort",
            "ParallelMergeSort",
            "ParallelQuickSort",
            "ParallelBubbleSort",
            "ParallelInsertionSort"
    );

    private final JTextField campoTamanhos = new JTextField("1000,5000,10000");
    private final JComboBox<InputType> comboTipoEntrada = new JComboBox<>(InputType.values());
    private final JTextField campoThreads = new JTextField("1,2,4");
    private final JSpinner spinnerAmostras = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
    private final JSpinner spinnerWarmup = new JSpinner(new SpinnerNumberModel(2, 0, 20, 1));
    private final JSpinner spinnerLimiteQuadratico = new JSpinner(new SpinnerNumberModel(10000, 1, 1_000_000, 500));
    private final JButton botaoIniciar = new JButton("Iniciar Benchmark");
    private final JProgressBar barraProgresso = new JProgressBar();
    private final JTextArea areaLog = new JTextArea();
    private final Map<String, JCheckBox> checkboxesAlgoritmos = new LinkedHashMap<>();

    private final DefaultTableModel modeloTabela = new DefaultTableModel(
            new Object[]{"Algoritmo", "Modo", "Entrada", "Tamanho", "Threads", "Tempo Médio (ms)", "Speedup", "Eficiência"},
            0
    );
    private final JTable tabelaResultados = new JTable(modeloTabela);
    private final XYSeriesCollection datasetGrafico = new XYSeriesCollection();
    private final JFreeChart graficoLinhas = ChartFactory.createXYLineChart(
            "Tempo (ms) vs Tamanho do Array",
            "Tamanho do Array",
            "Tempo Médio (ms)",
            datasetGrafico,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
    );

    public BenchmarkMainGui() {
        super("Benchmark de Ordenação");
        configurarJanela();
        construirTela();
        registrarEventos();
    }

    public static void abrir() {
        SwingUtilities.invokeLater(() -> {
            BenchmarkMainGui janela = new BenchmarkMainGui();
            janela.setVisible(true);
        });
    }

    private void configurarJanela() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1300, 850));
    }

    private void construirTela() {
        JPanel raiz = new JPanel(new BorderLayout(12, 12));
        raiz.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel titulo = new JLabel(TITULO, JLabel.CENTER);
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        raiz.add(titulo, BorderLayout.NORTH);

        JPanel painelEsquerdo = new JPanel(new BorderLayout(10, 10));
        painelEsquerdo.add(criarPainelConfiguracao(), BorderLayout.NORTH);
        painelEsquerdo.add(criarPainelLog(), BorderLayout.CENTER);

        JSplitPane centro = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, painelEsquerdo, criarPainelResultados());
        centro.setResizeWeight(0.40);
        raiz.add(centro, BorderLayout.CENTER);

        JPanel rodape = new JPanel(new BorderLayout(8, 8));
        rodape.add(botaoIniciar, BorderLayout.WEST);
        barraProgresso.setStringPainted(true);
        barraProgresso.setString("Aguardando execução");
        rodape.add(barraProgresso, BorderLayout.CENTER);
        raiz.add(rodape, BorderLayout.SOUTH);

        setContentPane(raiz);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel criarPainelConfiguracao() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(BorderFactory.createTitledBorder("Configurações de Entrada"));

        JPanel campos = new JPanel();
        campos.setLayout(new BoxLayout(campos, BoxLayout.Y_AXIS));
        campos.add(new JLabel("Tamanhos de array (csv):"));
        campos.add(campoTamanhos);
        campos.add(new JLabel("Tipo de entrada:"));
        campos.add(comboTipoEntrada);
        campos.add(new JLabel("Número de threads (csv):"));
        campos.add(campoThreads);
        campos.add(new JLabel("Quantidade de amostras:"));
        campos.add(spinnerAmostras);
        campos.add(new JLabel("Warm-up:"));
        campos.add(spinnerWarmup);
        campos.add(new JLabel("Limite para algoritmos quadráticos:"));
        campos.add(spinnerLimiteQuadratico);
        painel.add(campos, BorderLayout.CENTER);

        JPanel algoritmos = new JPanel();
        algoritmos.setLayout(new BoxLayout(algoritmos, BoxLayout.Y_AXIS));
        algoritmos.setBorder(BorderFactory.createTitledBorder("Algoritmos"));
        for (String nome : NOMES_ALGORITMOS) {
            JCheckBox checkbox = new JCheckBox(nome, true);
            checkboxesAlgoritmos.put(nome, checkbox);
            algoritmos.add(checkbox);
        }
        painel.add(algoritmos, BorderLayout.EAST);
        return painel;
    }

    private JPanel criarPainelLog() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Log de execução"));
        areaLog.setEditable(false);
        areaLog.setLineWrap(true);
        areaLog.setWrapStyleWord(true);
        painel.add(new JScrollPane(areaLog), BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarPainelResultados() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(BorderFactory.createTitledBorder("Visualização de Resultados"));

        tabelaResultados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollTabela = new JScrollPane(tabelaResultados);
        scrollTabela.setPreferredSize(new Dimension(700, 250));
        painel.add(scrollTabela, BorderLayout.NORTH);

        ChartPanel painelGrafico = new ChartPanel(graficoLinhas);
        painelGrafico.setMouseWheelEnabled(true);
        painel.add(painelGrafico, BorderLayout.CENTER);
        return painel;
    }

    private void registrarEventos() {
        botaoIniciar.addActionListener(e -> iniciarBenchmark());
    }

    private void iniciarBenchmark() {
        List<Integer> tamanhos;
        List<Integer> threads;
        Set<String> algoritmosSelecionados = getAlgoritmosSelecionados();
        InputType tipoEntrada = (InputType) comboTipoEntrada.getSelectedItem();
        int amostras = (Integer) spinnerAmostras.getValue();
        int warmup = (Integer) spinnerWarmup.getValue();
        int limiteQuadratico = (Integer) spinnerLimiteQuadratico.getValue();

        try {
            tamanhos = parseCsvInteiros(campoTamanhos.getText(), "tamanhos");
            threads = parseCsvInteiros(campoThreads.getText(), "threads");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro de validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (algoritmosSelecionados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione ao menos um algoritmo.", "Erro de validação", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (tipoEntrada == null) {
            JOptionPane.showMessageDialog(this, "Selecione um tipo de entrada.", "Erro de validação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        botaoIniciar.setEnabled(false);
        barraProgresso.setIndeterminate(true);
        barraProgresso.setString("Benchmark em execução...");
        areaLog.setText("");

        SwingWorker<BenchmarkRunner.BenchmarkOutput, String> worker = new SwingWorker<>() {
            @Override
            protected BenchmarkRunner.BenchmarkOutput doInBackground() throws Exception {
                PrintStream originalOut = System.out;
                PrintStream logStream = criarLogStream(originalOut, linha -> publish(linha));
                try {
                    System.setOut(logStream);
                    DataGenerator gerador = new DataGenerator(42L);
                    BenchmarkRunner runner = new BenchmarkRunner(gerador, amostras, warmup, limiteQuadratico);
                    BenchmarkRunner.BenchmarkOutput output = runner.runBenchmarks(
                            tamanhos,
                            List.of(tipoEntrada),
                            threads,
                            algoritmosSelecionados
                    );

                    Path rawOutput = Path.of("results", "raw_results.csv");
                    Path summaryOutput = Path.of("results", "summary_results.csv");
                    CsvWriter.writeRawResults(rawOutput, output.rawResults());
                    CsvWriter.writeSummaryResults(summaryOutput, output.summaryResults());
                    publish("CSV gerado em: " + rawOutput.toAbsolutePath());
                    publish("CSV resumo em: " + summaryOutput.toAbsolutePath());
                    return output;
                } finally {
                    System.setOut(originalOut);
                    logStream.flush();
                    logStream.close();
                }
            }

            @Override
            protected void process(List<String> chunks) {
                for (String linha : chunks) {
                    areaLog.append(linha);
                    areaLog.append(System.lineSeparator());
                }
                areaLog.setCaretPosition(areaLog.getDocument().getLength());
            }

            @Override
            protected void done() {
                try {
                    BenchmarkRunner.BenchmarkOutput output = get();
                    atualizarTabela(output.summaryResults());
                    atualizarGrafico(output.summaryResults());
                    barraProgresso.setString("Concluído (" + output.summaryResults().size() + " linhas no resumo)");
                } catch (Exception e) {
                    barraProgresso.setString("Falha na execução");
                    JOptionPane.showMessageDialog(
                            BenchmarkMainGui.this,
                            "Erro ao executar benchmark: " + e.getMessage(),
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    barraProgresso.setIndeterminate(false);
                    botaoIniciar.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private PrintStream criarLogStream(PrintStream originalOut, java.util.function.Consumer<String> consumidor) {
        return new PrintStream(new OutputStream() {
            private final StringBuilder buffer = new StringBuilder();

            @Override
            public void write(int b) throws IOException {
                originalOut.write(b);
                if (b == '\n') {
                    String linha = buffer.toString().stripTrailing();
                    if (!linha.isBlank()) {
                        consumidor.accept(linha);
                    }
                    buffer.setLength(0);
                } else {
                    buffer.append((char) b);
                }
            }
        }, true);
    }

    private Set<String> getAlgoritmosSelecionados() {
        return checkboxesAlgoritmos.entrySet().stream()
                .filter(entrada -> entrada.getValue().isSelected())
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<Integer> parseCsvInteiros(String texto, String nomeCampo) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("O campo '" + nomeCampo + "' não pode ficar vazio.");
        }
        List<Integer> valores = new ArrayList<>();
        for (String trecho : texto.split(",")) {
            String valorTexto = trecho.trim();
            if (valorTexto.isEmpty()) {
                continue;
            }
            try {
                int valor = Integer.parseInt(valorTexto);
                if (valor <= 0) {
                    throw new IllegalArgumentException("Todos os valores de '" + nomeCampo + "' devem ser positivos.");
                }
                valores.add(valor);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Valor inválido em '" + nomeCampo + "': " + valorTexto);
            }
        }
        if (valores.isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos um valor em '" + nomeCampo + "'.");
        }
        return valores.stream().distinct().sorted().toList();
    }

    private void atualizarTabela(List<BenchmarkSummary> summary) {
        modeloTabela.setRowCount(0);
        summary.stream()
                .sorted(Comparator.comparing(BenchmarkSummary::algorithm)
                        .thenComparing(BenchmarkSummary::arraySize)
                        .thenComparing(BenchmarkSummary::threads))
                .forEach(item -> modeloTabela.addRow(new Object[]{
                        item.algorithm(),
                        item.mode(),
                        item.inputType(),
                        item.arraySize(),
                        item.threads(),
                        String.format(Locale.US, "%.4f", item.avgTimeMs()),
                        String.format(Locale.US, "%.4f", item.speedup()),
                        String.format(Locale.US, "%.4f", item.efficiency())
                }));
    }

    private void atualizarGrafico(List<BenchmarkSummary> summary) {
        datasetGrafico.removeAllSeries();

        Map<String, List<BenchmarkSummary>> agrupado = summary.stream()
                .collect(Collectors.groupingBy(
                        item -> item.algorithm() + " (threads=" + item.threads() + ")",
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        for (Map.Entry<String, List<BenchmarkSummary>> entrada : agrupado.entrySet()) {
            XYSeries serie = new XYSeries(entrada.getKey());
            entrada.getValue().stream()
                    .sorted(Comparator.comparing(BenchmarkSummary::arraySize))
                    .forEach(item -> serie.add(item.arraySize(), item.avgTimeMs()));
            datasetGrafico.addSeries(serie);
        }
    }
}
