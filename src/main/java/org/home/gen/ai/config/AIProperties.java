package org.home.gen.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("client.azureopenai")
@Data
public class AIProperties {
  private String endpoint;
  private String key;
  private String deploymentName;
}
