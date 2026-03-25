package br.techservice.audiooggtranscriberapp.model;

import java.time.LocalDateTime;

public class HistoryEntry {
    private LocalDateTime dateTime;
    private String inputFileName;
    private String inputFilePath;
    private String outputFilePath;
    private String processingTime;
    private long audioSizeBytes;

    public HistoryEntry() {
    }

    public HistoryEntry(LocalDateTime dateTime, String inputFileName, String inputFilePath,
                        String outputFilePath, String processingTime, long audioSizeBytes) {
        this.dateTime = dateTime;
        this.inputFileName = inputFileName;
        this.inputFilePath = inputFilePath;
        this.outputFilePath = outputFilePath;
        this.processingTime = processingTime;
        this.audioSizeBytes = audioSizeBytes;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getInputFileName() {
        return inputFileName;
    }

    public void setInputFileName(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    public String getInputFilePath() {
        return inputFilePath;
    }

    public void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public void setOutputFilePath(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    public String getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(String processingTime) {
        this.processingTime = processingTime;
    }

    public long getAudioSizeBytes() {
        return audioSizeBytes;
    }

    public void setAudioSizeBytes(long audioSizeBytes) {
        this.audioSizeBytes = audioSizeBytes;
    }
}