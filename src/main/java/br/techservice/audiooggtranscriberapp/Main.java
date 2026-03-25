package br.techservice.audiooggtranscriberapp;

import br.techservice.audiooggtranscriberapp.repository.HistoryRepository;
import br.techservice.audiooggtranscriberapp.repository.JsonHistoryRepository;
import br.techservice.audiooggtranscriberapp.service.AudioConverterService;
import br.techservice.audiooggtranscriberapp.service.EnvironmentService;
import br.techservice.audiooggtranscriberapp.service.TranscriptFileService;
import br.techservice.audiooggtranscriberapp.service.TranscriptionService;
import br.techservice.audiooggtranscriberapp.service.VoskRecognitionService;
import br.techservice.audiooggtranscriberapp.ui.AudioOggTranscriberFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            Path historyFile = Path.of(System.getProperty("user.home"), ".audiooggtranscriber", "history.json");

            HistoryRepository historyRepository = new JsonHistoryRepository(historyFile);
            EnvironmentService environmentService = new EnvironmentService();
            AudioConverterService audioConverterService = new AudioConverterService();
            VoskRecognitionService voskRecognitionService = new VoskRecognitionService();
            TranscriptFileService transcriptFileService = new TranscriptFileService();

            TranscriptionService transcriptionService = new TranscriptionService(
                    environmentService,
                    audioConverterService,
                    voskRecognitionService,
                    transcriptFileService,
                    historyRepository
            );

            AudioOggTranscriberFrame frame = new AudioOggTranscriberFrame(transcriptionService, historyRepository);
            frame.setVisible(true);
        });
    }
}