package br.techservice.audiooggtranscriberapp.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class TranscriptFileService {

    public void save(Path outputFile, String text) throws IOException {
        Files.writeString(outputFile, text, StandardCharsets.UTF_8);
    }
}