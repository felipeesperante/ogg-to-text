package br.techservice.audiooggtranscriberapp.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class AudioConverterService {

    public void convertOggToWav(Path inputOgg, Path outputWav) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "D:\\ffmpeg\\ffmpeg-master-latest-win64-gpl-shared\\bin\\ffmpeg.exe",
                "-y",
                "-i", inputOgg.toAbsolutePath().toString(),
                "-ac", "1",
                "-ar", "16000",
                "-f", "wav",
                outputWav.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);

        Process process = pb.start();

        String ffmpegLog;
        try (InputStream is = process.getInputStream()) {
            ffmpegLog = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        int exit = process.waitFor();
        if (exit != 0) {
            throw new IOException("Falha ao converter OGG para WAV com ffmpeg. Saída: " + ffmpegLog);
        }
    }
}