package com.zahracatbot.cats.config;

import com.zahracatbot.cats.CatService;
import com.zahracatbot.common.model.Cat;
import com.zahracatbot.common.model.CatAction;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  private final CatService catService;

  public RabbitMQConfig(CatService catService) {
    this.catService = catService;
  }

  @Bean
  public Queue catActionsQueue() {
    return new Queue("cat-actions", true);
  }

  @RabbitListener(queues = "cat-actions")
  public Object handleCatAction(CatAction action) {
    switch (action.getAction()) {
      case "saveCat":
        Cat cat = new Cat();
        cat.setName("UserCat_" + action.getChatId());
        return catService.saveCat(cat, action.getFileId(), action.getChatId());
      case "findCatsByChatId":
        return catService.findCatsByChatId(action.getChatId());
      case "deleteCat":
        catService.deleteCat(action.getCatId());
        return null;
      case "likeCat":
        catService.likeCat(action.getCatId());
        return null;
      case "dislikeCat":
        catService.dislikeCat(action.getCatId());
        return null;
      case "heartCat":
        catService.heartCat(action.getCatId());
        return null;
      case "fireCat":
        catService.fireCat(action.getCatId());
        return null;
      default:
        return null;
    }
  }
}
