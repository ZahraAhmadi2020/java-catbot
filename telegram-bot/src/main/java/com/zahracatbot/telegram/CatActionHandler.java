package com.zahracatbot.telegram;

import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import com.zahracatbot.common.model.Cat;
import com.zahracatbot.common.model.CatAction;

@Component
public class CatActionHandler {

  private final RabbitTemplate rabbitTemplate;
  private final ZahraCatBot bot;

  public CatActionHandler(RabbitTemplate rabbitTemplate, ZahraCatBot bot) {
    this.rabbitTemplate = rabbitTemplate;
    this.bot = bot;
  }

  public void handleUpdate(Update update) {
    Long chatId = update.hasMessage() ? update.getMessage().getChatId()
        : update.hasCallbackQuery() ? update.getCallbackQuery().getMessage().getChatId() : null;
    if (chatId == null)
      return;

    if (update.hasMessage() && update.getMessage().hasText()) {
      String text = update.getMessage().getText().trim();
      if (text.equals("My Cats")) {
        Object response = rabbitTemplate.convertSendAndReceive("cat-actions",
            new CatAction("findCatsByChatId", chatId, null, null));
        if (!(response instanceof List)) {
          sendMessage(chatId, "Error retrieving your cats!");
          return;
        }
        @SuppressWarnings("unchecked")
        List<Cat> cats = (List<Cat>) response;
        if (cats.isEmpty()) {
          sendMessage(chatId, "You haven't uploaded any cats yet!");
        } else {
          StringBuilder sb = new StringBuilder("Your cats:\n");
          for (Cat cat : cats) {
            sb.append("ID: ").append(cat.getId()).append(", Name: ").append(cat.getName())
                .append("\n");
          }
          sendMessage(chatId, sb.toString());
        }
      } else if (text.startsWith("Delete Cat ")) {
        String[] parts = text.split(" ");
        if (parts.length == 3) {
          try {
            Long catId = Long.parseLong(parts[2]);
            rabbitTemplate.convertAndSend("cat-actions",
                new CatAction("deleteCat", chatId, catId, null));
            sendMessage(chatId, "Cat deleted successfully!");
          } catch (NumberFormatException e) {
            sendMessage(chatId, "Invalid cat ID!");
          }
        } else {
          sendMessage(chatId, "Please use format: Delete Cat <ID>");
        }
      }
    } else if (update.hasMessage() && update.getMessage().hasPhoto()) {
      String fileId = update.getMessage().getPhoto().get(update.getMessage().getPhoto().size() - 1).getFileId();
      rabbitTemplate.convertAndSend("cat-actions",
          new CatAction("saveCat", chatId, null, fileId));
      sendMessage(chatId, "Cat photo uploaded successfully!");
    }
  }

  private void sendMessage(Long chatId, String text) {
    SendMessage message = new SendMessage();
    message.setChatId(chatId.toString());
    message.setText(text);
    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    keyboardMarkup.setResizeKeyboard(true);
    List<KeyboardRow> keyboard = new ArrayList<>();
    KeyboardRow row1 = new KeyboardRow();
    row1.add("Cat Photos");
    row1.add("Upload Cat Photo");
    KeyboardRow row2 = new KeyboardRow();
    row2.add("My Cats");
    row2.add("Back");
    KeyboardRow row3 = new KeyboardRow();
    row3.add("Change Language");
    keyboard.add(row1);
    keyboard.add(row2);
    keyboard.add(row3);
    keyboardMarkup.setKeyboard(keyboard);
    message.setReplyMarkup(keyboardMarkup);
    try {
      bot.execute(message);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
