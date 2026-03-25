package br.techservice.audiooggtranscriberapp.model;

import java.nio.file.Path;

public record TranscriptionRequest(
        Path inputOgg,
        Path outputTxt,
        Path modelPath
) {
}