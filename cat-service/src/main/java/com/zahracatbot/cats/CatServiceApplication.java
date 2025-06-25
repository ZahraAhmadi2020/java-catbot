package com.zahracatbot.cats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = { "com.zahracatbot.common.model" })
public class CatServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(CatServiceApplication.class, args);
  }
}
