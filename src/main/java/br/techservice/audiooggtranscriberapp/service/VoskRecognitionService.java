package br.techservice.audiooggtranscriberapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;

public class VoskRecognitionService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String transcribe(Path wavFile,
                             Path modelPath,
                             IntConsumer progressCallback,
                             BooleanSupplier cancelled) throws Exception {
        LibVosk.setLogLevel(LogLevel.INFO);

        try (Model voskModel = new Model(modelPath.toString());
             AudioInputStream audio = AudioSystem.getAudioInputStream(wavFile.toFile())) {

            AudioFormat target = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    16000,
                    16,
                    1,
                    2,
                    16000,
                    false
            );

            try (AudioInputStream pcmStream = AudioSystem.getAudioInputStream(target, audio);
                 Recognizer recognizer = new Recognizer(voskModel, 16000)) {

                long totalBytes = Math.max(1, pcmStream.getFrameLength() * target.getFrameSize());
                long readBytes = 0;

                byte[] buffer = new byte[4096];
                StringBuilder sb = new StringBuilder();

                int n;
                while ((n = pcmStream.read(buffer)) >= 0) {
                    if (cancelled != null && cancelled.getAsBoolean()) {
                        throw new InterruptedException("Processo cancelado pelo usuário.");
                    }

                    if (n == 0) {
                        continue;
                    }

                    readBytes += n;

                    if (recognizer.acceptWaveForm(buffer, n)) {
                        String partial = extractText(recognizer.getResult());
                        if (!partial.isBlank()) {
                            sb.append(partial).append(' ');
                        }
                    }

                    if (progressCallback != null) {
                        int pct = 40 + (int) (55.0 * readBytes / totalBytes);
                        progressCallback.accept(Math.min(95, pct));
                    }
                }

                String finalText = extractText(recognizer.getFinalResult());
                if (!finalText.isBlank()) {
                    sb.append(finalText);
                }

                return sb.toString().trim().replaceAll("\\s+", " ");
            }
        }
    }

    private String extractText(String json) throws IOException {
        if (json == null || json.isBlank()) {
            return "";
        }

        JsonNode node = objectMapper.readTree(json);
        return node.path("text").asText("");
    }
}