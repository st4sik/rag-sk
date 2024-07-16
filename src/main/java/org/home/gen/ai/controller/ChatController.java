package org.home.gen.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.home.gen.ai.model.Message;
import org.home.gen.ai.service.ChatService;
import org.home.gen.ai.service.EmbeddingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatController {

  private final EmbeddingService embeddingService;
  private final ChatService chatService;

  @PostMapping("/chat")
  public ResponseEntity<String> sendMessage(@RequestBody Message message) {
    try {
      var response = chatService.sendMessage(message);
      if (response != null) {
        var jsonResponse = new HashMap<>();
        jsonResponse.put("response", response);

        var mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(jsonResponse);

        return ResponseEntity.ok().body(json);
      } else {
        return ResponseEntity.noContent().build();
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PostMapping("/admin/documents/upload")
  public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
    try {
      File uploadedFile = getUploadedFile(file);
      boolean result = embeddingService.addDocument(uploadedFile);
      if (result) {
        return new ResponseEntity<>(HttpStatus.CREATED);
      }
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
  }

  private File getUploadedFile(MultipartFile file) throws IOException {
    String fileName = file.getOriginalFilename();
    Path path = Paths.get("target/uploads/" + fileName);

    if (path.toFile().exists()) {
      Files.delete(path);
    }
    Files.copy(file.getInputStream(), path);

    return path.toFile();
  }
}
