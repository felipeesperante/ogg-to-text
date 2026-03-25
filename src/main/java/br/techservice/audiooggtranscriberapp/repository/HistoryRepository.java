package br.techservice.audiooggtranscriberapp.repository;

import br.techservice.audiooggtranscriberapp.model.HistoryEntry;

import java.io.IOException;
import java.util.List;

public interface HistoryRepository {
    List<HistoryEntry> findAll() throws IOException;
    void save(HistoryEntry entry) throws IOException;
}