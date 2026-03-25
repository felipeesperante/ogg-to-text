package br.techservice.audiooggtranscriberapp.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.nio.file.Path;

public record TranscriptionResult(
        String text,
        Path inputFile,
        Path outputFile,
        Path modelPath,
        long audioSizeBytes,
        Duration processingTime,
        LocalDateTime finishedAt
) {
}