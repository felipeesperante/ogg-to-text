package br.techservice.audiooggtranscriberapp.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class EnvironmentService {

    public void validateFfmpegAvailable() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("D:\\ffmpeg\\ffmpeg-master-latest-win64-gpl-shared\\bin\\ffmpeg.exe", "-version");
        pb.redirectErrorStream(true);

        Process process = pb.start();

        String output;
        try (InputStream is = process.getInputStream()) {
            output = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        int exit = process.waitFor();
        if (exit != 0 || !output.toLowerCase().contains("ffmpeg")) {
            throw new IOException("ffmpeg não está disponível no PATH do sistema.");
        }
    }
}