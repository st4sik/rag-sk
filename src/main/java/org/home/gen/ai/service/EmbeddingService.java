package org.home.gen.ai.service;

import java.io.File;
import java.util.List;
import org.springframework.ai.document.Document;

public interface EmbeddingService {
  boolean addDocument(File uploadedFile);

  List<Document> similaritySearch(String searchText);
}
