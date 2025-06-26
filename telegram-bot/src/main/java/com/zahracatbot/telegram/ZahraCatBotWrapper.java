package com.zahracatbot.telegram;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class ZahraCatBotWrapper {
  public ZahraCatBotWrapper(ZahraCatBot bot) {
    try {
      TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
      botsApi.registerBot(bot);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }
}
