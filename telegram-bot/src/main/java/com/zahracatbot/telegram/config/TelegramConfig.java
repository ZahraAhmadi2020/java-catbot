package com.zahracatbot.telegram.config;

import com.zahracatbot.telegram.ZahraCatBot;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramConfig {

  @Value("${telegram.bot.token}")
  private String botToken;

  private final RabbitTemplate rabbitTemplate;

  public TelegramConfig(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  @Bean
  public ZahraCatBot zahraCatBot() {
    return new ZahraCatBot();
  }
}
