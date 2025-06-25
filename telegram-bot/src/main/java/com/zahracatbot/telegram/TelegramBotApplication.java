package com.zahracatbot.telegram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.zahracatbot")
public class TelegramBotApplication {
  public static void main(String[] args) {
    SpringApplication.run(TelegramBotApplication.class, args);
  }
}
