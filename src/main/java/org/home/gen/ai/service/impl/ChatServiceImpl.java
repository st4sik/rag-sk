package org.home.gen.ai.service.impl;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionArguments;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import jakarta.annotation.PostConstruct;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.home.gen.ai.config.AIProperties;
import org.home.gen.ai.model.Message;
import org.home.gen.ai.service.ChatService;
import org.home.gen.ai.service.EmbeddingService;
import org.home.gen.ai.service.OpenAIAsyncClientService;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
  private final OpenAIAsyncClientService aiClientService;
  private final EmbeddingService embeddingService;
  private final AIProperties aiProperties;
  private Kernel kernel;

  @PostConstruct
  public void init() {
    this.kernel = buildSemanticKernel();
  }

  @Override
  public String sendMessage(Message message) {

    var documents = embeddingService.similaritySearch(message.input());
    var context =
        documents.stream()
            .map(Document::getContent)
            .collect(Collectors.joining(System.lineSeparator()));

    log.debug("Context {}", context);

    var answerVariables =
        KernelFunctionArguments.builder()
            .withVariable("context", context)
            .withVariable("question", message.input())
            .build();

    var answerExecutionContext =
        kernel
            .invokeAsync("RAG", "AnswerQuestion")
            .withArguments(answerVariables)
            .withResultType(String.class)
            .block();

    return (answerExecutionContext == null || answerExecutionContext.getResult() == null)
        ? null
        : answerExecutionContext.getResult();
  }

  private Kernel buildSemanticKernel() {
    return Kernel.builder()
        .withAIService(
            ChatCompletionService.class,
            ChatCompletionService.builder()
                .withModelId(aiProperties.getDeploymentName())
                .withOpenAIAsyncClient(aiClientService.get())
                .build())
        .withPlugin(
            KernelPluginFactory.importPluginFromResourcesDirectory(
                "plugins", "RAG", "AnswerQuestion", null, String.class))
        .build();
  }
}
