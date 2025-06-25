package com.zahracatbot.telegram.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
  @Bean
  public Queue catActionsQueue() {
    return new Queue("cat-actions", true);
  }
}
