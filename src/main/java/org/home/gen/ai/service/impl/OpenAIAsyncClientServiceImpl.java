package org.home.gen.ai.service.impl;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.home.gen.ai.config.AIProperties;
import org.home.gen.ai.service.OpenAIAsyncClientService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenAIAsyncClientServiceImpl implements OpenAIAsyncClientService {

  private final AIProperties aiProperties;

  @Override
  public OpenAIAsyncClient get() {
    return new OpenAIClientBuilder()
        .credential(new AzureKeyCredential(aiProperties.getKey()))
        .endpoint(aiProperties.getEndpoint())
        .buildAsyncClient();
  }
}
