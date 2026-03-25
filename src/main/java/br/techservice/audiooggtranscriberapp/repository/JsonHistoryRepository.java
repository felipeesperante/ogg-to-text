package br.techservice.audiooggtranscriberapp.repository;

import br.techservice.audiooggtranscriberapp.model.HistoryEntry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JsonHistoryRepository implements HistoryRepository {

    private final Path historyFile;
    private final ObjectMapper objectMapper;

    public JsonHistoryRepository(Path historyFile) {
        this.historyFile = historyFile;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public synchronized List<HistoryEntry> findAll() throws IOException {
        ensureParentDirectory();
        if (!Files.exists(historyFile)) {
            return new ArrayList<>();
        }

        byte[] bytes = Files.readAllBytes(historyFile);
        if (bytes.length == 0) {
            return new ArrayList<>();
        }

        return objectMapper.readValue(bytes, new TypeReference<List<HistoryEntry>>() {});
    }

    @Override
    public synchronized void save(HistoryEntry entry) throws IOException {
        List<HistoryEntry> items = findAll();
        items.add(0, entry);
        ensureParentDirectory();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(historyFile.toFile(), items);
    }

    private void ensureParentDirectory() throws IOException {
        Path parent = historyFile.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }
}