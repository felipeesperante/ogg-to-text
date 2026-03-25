package br.techservice.audiooggtranscriberapp.ui;

import br.techservice.audiooggtranscriberapp.model.HistoryEntry;
import br.techservice.audiooggtranscriberapp.model.TranscriptionRequest;
import br.techservice.audiooggtranscriberapp.model.TranscriptionResult;
import br.techservice.audiooggtranscriberapp.repository.HistoryRepository;
import br.techservice.audiooggtranscriberapp.service.TranscriptionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AudioOggTranscriberFrame extends JFrame {

    private final JTextField inputField = new JTextField();
    private final JTextField outputField = new JTextField();
    private final JTextField modelField = new JTextField();
    private final JTextArea transcriptArea = new JTextArea();
    private final DefaultListModel<String> historyModel = new DefaultListModel<>();
    private final JList<String> historyList = new JList<>(historyModel);
    private final JProgressBar progressBar = new JProgressBar(0, 100);
    private final JButton chooseInputButton = new JButton("Selecionar .ogg");
    private final JButton chooseOutputButton = new JButton("Salvar .txt em...");
    private final JButton chooseModelButton = new JButton("Selecionar modelo");
    private final JButton transcribeButton = new JButton("Transcrever");
    private final JButton cancelButton = new JButton("Cancelar");
    private final JLabel statusLabel = new JLabel("Pronto");

    private final TranscriptionService transcriptionService;
    private final HistoryRepository historyRepository;
    private SwingWorker<TranscriptionResult, Integer> currentWorker;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public AudioOggTranscriberFrame(TranscriptionService transcriptionService,
            HistoryRepository historyRepository) {
        super("Transcrição de Áudio OGG para TXT (Vosk)");
        this.transcriptionService = transcriptionService;
        this.historyRepository = historyRepository;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1080, 700);
        setLocationRelativeTo(null);

        buildUi();
        wireActions();
        loadHistory();
        validateStartupEnvironment();
    }

    private void buildUi() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(main);

        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        inputField.setEditable(false);
        outputField.setEditable(false);

        String defaultModel = System.getenv("VOSK_MODEL_PATH") != null
                ? System.getenv("VOSK_MODEL_PATH")
                : "./vosk-model-pt";
        modelField.setText(defaultModel);

        int y = 0;

        gc.gridx = 0;
        gc.gridy = y;
        gc.weightx = 0;
        top.add(new JLabel("Arquivo OGG:"), gc);

        gc.gridx = 1;
        gc.weightx = 1;
        top.add(inputField, gc);

        gc.gridx = 2;
        gc.weightx = 0;
        top.add(chooseInputButton, gc);

        y++;
        gc.gridx = 0;
        gc.gridy = y;
        top.add(new JLabel("Arquivo TXT:"), gc);

        gc.gridx = 1;
        gc.weightx = 1;
        top.add(outputField, gc);

        gc.gridx = 2;
        gc.weightx = 0;
        top.add(chooseOutputButton, gc);

        y++;
        gc.gridx = 0;
        gc.gridy = y;
        top.add(new JLabel("Modelo Vosk:"), gc);

        gc.gridx = 1;
        gc.weightx = 1;
        top.add(modelField, gc);

        gc.gridx = 2;
        gc.weightx = 0;
        top.add(chooseModelButton, gc);

        y++;
        gc.gridx = 0;
        gc.gridy = y;
        gc.gridwidth = 3;
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        top.add(progressBar, gc);

        y++;
        gc.gridx = 0;
        gc.gridy = y;
        gc.gridwidth = 3;
        top.add(statusLabel, gc);

        main.add(top, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.75);

        transcriptArea.setLineWrap(true);
        transcriptArea.setWrapStyleWord(true);
        transcriptArea.setEditable(false);

        JPanel transcriptPanel = new JPanel(new BorderLayout(6, 6));
        transcriptPanel.add(new JLabel("Transcrição"), BorderLayout.NORTH);
        transcriptPanel.add(new JScrollPane(transcriptArea), BorderLayout.CENTER);

        JPanel historyPanel = new JPanel(new BorderLayout(6, 6));
        historyPanel.add(new JLabel("Histórico"), BorderLayout.NORTH);
        historyPanel.add(new JScrollPane(historyList), BorderLayout.CENTER);

        split.setLeftComponent(transcriptPanel);
        split.setRightComponent(historyPanel);

        main.add(split, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cancelButton.setEnabled(false);
        bottom.add(cancelButton);
        bottom.add(transcribeButton);

        main.add(bottom, BorderLayout.SOUTH);
    }

    private void wireActions() {
        chooseInputButton.addActionListener(e -> chooseInputFile());
        chooseOutputButton.addActionListener(e -> chooseOutputFile());
        chooseModelButton.addActionListener(e -> chooseModelDirectory());
        transcribeButton.addActionListener(e -> startTranscription());
        cancelButton.addActionListener(e -> cancelCurrentTranscription());

        historyList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                reopenHistoryOutput();
            }
        });
    }

    private void chooseInputFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecione um arquivo de áudio .ogg");
        chooser.setFileFilter(new FileNameExtensionFilter("Áudio OGG", "ogg"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            inputField.setText(selected.getAbsolutePath());

            if (outputField.getText().isBlank()) {
                String out = selected.getAbsolutePath().replaceFirst("(?i)\\.ogg$", "") + ".txt";
                outputField.setText(out);
            }
        }
    }

    private void chooseOutputFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Salvar transcrição como .txt");
        chooser.setSelectedFile(new File("transcricao.txt"));
        chooser.setFileFilter(new FileNameExtensionFilter("Arquivo de texto", "txt"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            if (!path.toLowerCase().endsWith(".txt")) {
                path += ".txt";
            }
            outputField.setText(path);
        }
    }

    private void chooseModelDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecione a pasta do modelo Vosk");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            modelField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void startTranscription() {
        String input = inputField.getText().trim();
        String output = outputField.getText().trim();
        String model = modelField.getText().trim();

        if (input.isBlank() || output.isBlank() || model.isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecione arquivo .ogg, destino .txt e pasta do modelo.",
                    "Dados incompletos",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        setBusy(true);
        transcriptArea.setText("");
        progressBar.setValue(0);
        progressBar.setString("Preparando...");
        statusLabel.setText("Processando...");

        TranscriptionRequest request = new TranscriptionRequest(
                Path.of(input),
                Path.of(output),
                Path.of(model)
        );

        currentWorker = new SwingWorker<>() {
            private String error;

            @Override
            protected TranscriptionResult doInBackground() {
                try {
                    return transcriptionService.transcribe(
                            request,
                            this::publish,
                            this::isCancelled
                    );
                } catch (Exception ex) {
                    error = ex.getMessage();
                    return null;
                }
            }

            @Override
            protected void process(List<Integer> chunks) {
                int value = chunks.get(chunks.size() - 1);
                progressBar.setValue(value);
                progressBar.setString(value + "%");
            }

            @Override
            protected void done() {
                setBusy(false);

                if (isCancelled()) {
                    progressBar.setString("Cancelado");
                    statusLabel.setText("Processo cancelado.");
                    return;
                }

                if (error != null) {
                    progressBar.setString("Falha");
                    statusLabel.setText("Falha na transcrição.");
                    JOptionPane.showMessageDialog(
                            AudioOggTranscriberFrame.this,
                            "Erro ao transcrever: " + error,
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                try {
                    TranscriptionResult result = get();
                    if (result != null) {
                        transcriptArea.setText(result.text());
                        progressBar.setValue(100);
                        progressBar.setString("100%");
                        statusLabel.setText(
                                "Concluído em " + result.processingTime().toMillis() + " ms | "
                                + "Tamanho do áudio: " + result.audioSizeBytes() + " bytes"
                        );

                        loadHistory();

                        JOptionPane.showMessageDialog(
                                AudioOggTranscriberFrame.this,
                                "Transcrição concluída e salva em:\n" + result.outputFile(),
                                "Sucesso",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            AudioOggTranscriberFrame.this,
                            "Erro ao finalizar processamento: " + ex.getMessage(),
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        currentWorker.execute();
    }

    private void cancelCurrentTranscription() {
        if (currentWorker != null && !currentWorker.isDone()) {
            currentWorker.cancel(true);
            statusLabel.setText("Cancelando...");
        }
    }

    private void setBusy(boolean busy) {
        chooseInputButton.setEnabled(!busy);
        chooseOutputButton.setEnabled(!busy);
        chooseModelButton.setEnabled(!busy);
        transcribeButton.setEnabled(!busy);
        cancelButton.setEnabled(busy);
    }

    private void validateStartupEnvironment() {
        try {
            transcriptionService.validateEnvironment();
            statusLabel.setText("Ambiente OK: ffmpeg encontrado.");
        } catch (Exception ex) {
            statusLabel.setText("Atenção: ffmpeg não encontrado.");
            JOptionPane.showMessageDialog(
                    this,
                    "O ffmpeg não foi encontrado no PATH.\n"
                    + "A aplicação abrirá, mas a transcrição falhará até essa dependência ser corrigida.\n\n"
                    + "Detalhe: " + ex.getMessage(),
                    "Dependência ausente",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void loadHistory() {
        historyModel.clear();
        try {
            List<HistoryEntry> items = historyRepository.findAll();
            for (HistoryEntry item : items) {
                historyModel.addElement(
                        item.getDateTime().format(DATE_FORMAT) + " - "
                        + item.getInputFileName() + " - "
                        + item.getProcessingTime()
                );
            }
        } catch (Exception ex) {
            statusLabel.setText("Falha ao carregar histórico.");
        }
    }

    private void reopenHistoryOutput() {
        int index = historyList.getSelectedIndex();
        if (index < 0) {
            return;
        }

        try {
            List<HistoryEntry> items = historyRepository.findAll();
            if (index >= items.size()) {
                return;
            }

            HistoryEntry entry = items.get(index);
            File outputFile = new File(entry.getOutputFilePath());

            if (outputFile.exists()) {
                Desktop.getDesktop().open(outputFile);
            }
        } catch (Exception ignored) {
        }
    }
}
