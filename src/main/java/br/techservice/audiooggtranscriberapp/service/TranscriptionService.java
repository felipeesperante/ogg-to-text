package br.techservice.audiooggtranscriberapp.service;

import br.techservice.audiooggtranscriberapp.model.HistoryEntry;
import br.techservice.audiooggtranscriberapp.model.TranscriptionRequest;
import br.techservice.audiooggtranscriberapp.model.TranscriptionResult;
import br.techservice.audiooggtranscriberapp.repository.HistoryRepository;
import br.techservice.audiooggtranscriberapp.util.DurationUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;

public class TranscriptionService {

    private final EnvironmentService environmentService;
    private final AudioConverterService audioConverterService;
    private final VoskRecognitionService voskRecognitionService;
    private final TranscriptFileService transcriptFileService;
    private final HistoryRepository historyRepository;

    public TranscriptionService(EnvironmentService environmentService,
                                AudioConverterService audioConverterService,
                                VoskRecognitionService voskRecognitionService,
                                TranscriptFileService transcriptFileService,
                                HistoryRepository historyRepository) {
        this.environmentService = environmentService;
        this.audioConverterService = audioConverterService;
        this.voskRecognitionService = voskRecognitionService;
        this.transcriptFileService = transcriptFileService;
        this.historyRepository = historyRepository;
    }

    public void validateEnvironment() throws Exception {
        environmentService.validateFfmpegAvailable();
    }

    public TranscriptionResult transcribe(TranscriptionRequest request,
                                          IntConsumer progressCallback,
                                          BooleanSupplier cancelled) throws Exception {
        validateRequest(request);

        long sizeBytes = Files.size(request.inputOgg());
        LocalDateTime startedAt = LocalDateTime.now();
        long startNanos = System.nanoTime();

        Path tempWav = null;
        try {
            publish(progressCallback, 5);

            tempWav = Files.createTempFile("audio_transcribe_", ".wav");

            checkCancelled(cancelled);
            publish(progressCallback, 15);

            audioConverterService.convertOggToWav(request.inputOgg(), tempWav);

            checkCancelled(cancelled);
            publish(progressCallback, 40);

            String text = voskRecognitionService.transcribe(
                    tempWav,
                    request.modelPath(),
                    progressCallback,
                    cancelled
            );

            checkCancelled(cancelled);
            publish(progressCallback, 97);

            transcriptFileService.save(request.outputTxt(), text);

            publish(progressCallback, 100);

            Duration duration = Duration.ofNanos(System.nanoTime() - startNanos);

            TranscriptionResult result = new TranscriptionResult(
                    text,
                    request.inputOgg(),
                    request.outputTxt(),
                    request.modelPath(),
                    sizeBytes,
                    duration,
                    LocalDateTime.now()
            );

            historyRepository.save(new HistoryEntry(
                    startedAt,
                    request.inputOgg().getFileName().toString(),
                    request.inputOgg().toString(),
                    request.outputTxt().toString(),
                    DurationUtils.format(duration),
                    sizeBytes
            ));

            return result;
        } finally {
            if (tempWav != null) {
                Files.deleteIfExists(tempWav);
            }
        }
    }

    private void publish(IntConsumer progressCallback, int value) {
        if (progressCallback != null) {
            progressCallback.accept(value);
        }
    }

    private void checkCancelled(BooleanSupplier cancelled) throws InterruptedException {
        if (cancelled != null && cancelled.getAsBoolean()) {
            throw new InterruptedException("Processo cancelado pelo usuário.");
        }
    }

    private void validateRequest(TranscriptionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Requisição inválida.");
        }
        if (request.inputOgg() == null || !Files.exists(request.inputOgg())) {
            throw new IllegalArgumentException("Arquivo OGG de entrada não encontrado.");
        }
        if (request.outputTxt() == null) {
            throw new IllegalArgumentException("Arquivo TXT de saída não informado.");
        }
        if (request.modelPath() == null || !Files.isDirectory(request.modelPath())) {
            throw new IllegalArgumentException("Pasta do modelo Vosk inválida.");
        }
    }
}