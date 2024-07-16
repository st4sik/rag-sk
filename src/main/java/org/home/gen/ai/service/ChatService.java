package org.home.gen.ai.service;

import org.home.gen.ai.model.Message;

public interface ChatService {
  String sendMessage(Message message);
}
