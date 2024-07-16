package org.home.gen.ai.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.home.gen.ai.service.EmbeddingService;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingServiceImpl implements EmbeddingService {

  private static final int MAX_LENGTH = 7500;

  private final VectorStore vectorStore;

  @Override
  public boolean addDocument(File uploadedFile) {
    return addPDFDocument(uploadedFile);
  }

  @Override
  public List<Document> similaritySearch(String searchText) {
    var searchRequest = SearchRequest.query(searchText);

    searchRequest.withTopK(2);
    searchRequest.withSimilarityThreshold(0.8);
    return vectorStore.similaritySearch(searchRequest);
  }

  private boolean addPDFDocument(File pdfFile) {
    try (PDDocument pdDocument = Loader.loadPDF(pdfFile)) {
      PDFTextStripper textStripper = new PDFTextStripper();
      int numberOfPages = pdDocument.getNumberOfPages();
      IntStream.rangeClosed(1, numberOfPages)
          .forEach(
              pageNumber -> {
                try {
                  textStripper.setStartPage(pageNumber);
                  textStripper.setEndPage(pageNumber);
                  String pageText = textStripper.getText(pdDocument);
                  // Replace newline characters with whitespace
                  pageText = pageText.replace("\n", " ");
                  pageText = pageText.replaceAll("\\s{2,}", " ");

                  // If the text on one page exceeds 7500 characters, split it
                  if (pageText.length() > MAX_LENGTH) {
                    List<String> splitText = splitText(pageText);
                    splitText.forEach(text -> vectorStore.add(List.of(new Document(text))));
                  } else {
                    if (StringUtils.hasText(pageText)) {
                      vectorStore.add(List.of(new Document(pageText)));
                    }
                  }

                } catch (IOException e) {
                  throw new RuntimeException(
                      "Error occurred while parsing the file: {}".formatted(e.getMessage()), e);
                }
              });
    } catch (IOException e) {
      throw new RuntimeException(
          "Error occurred while loading the file: {}".formatted(e.getMessage()), e);
    }

    return true;
  }

  private List<String> splitText(String text) {
    List<String> chunks = new ArrayList<>();
    int textLength = text.length();

    while (textLength > MAX_LENGTH) {
      int splitIndex = findSplitIndex(text);
      chunks.add(text.substring(0, splitIndex));
      text = text.substring(splitIndex);
      textLength = text.length();
    }
    chunks.add(text);
    return chunks;
  }

  private int findSplitIndex(String text) {
    // Search for punctuation marks within the range of 7200-7500 characters
    int start = MAX_LENGTH - 300;
    int splitIndex = MAX_LENGTH;
    while (splitIndex > start) {
      char c = text.charAt(splitIndex);
      if (isPunctuation(c)) {
        break;
      }
      splitIndex--;
    }
    if (splitIndex == 0) {
      splitIndex = MAX_LENGTH;
    }
    return splitIndex;
  }

  private boolean isPunctuation(char c) {
    return c == '.' || c == ':' || c == ';' || c == '?' || c == '!';
  }
}
