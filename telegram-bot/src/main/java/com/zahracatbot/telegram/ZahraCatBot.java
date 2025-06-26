package com.zahracatbot.telegram;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

public class ZahraCatBot extends TelegramLongPollingBot {
    private final Map<Long, String> userStates = new ConcurrentHashMap<>();
    private final Map<Long, Integer> userPhotoIndex = new ConcurrentHashMap<>();
    private final Map<Long, String> userCommentingPhoto = new ConcurrentHashMap<>();
    private final List<String> catPhotoIds = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, String> photoIdToPath = new ConcurrentHashMap<>();
    private static final Map<String, String> uploadedPhotoIds = new ConcurrentHashMap<>();
    private final Map<String, String> photoNames = new ConcurrentHashMap<>();
    private final Map<String, Integer> viewCounts = new ConcurrentHashMap<>();
    private final Map<String, Integer> likeCounts = new ConcurrentHashMap<>();
    private final Map<String, Integer> dislikeCounts = new ConcurrentHashMap<>();
    private final Map<String, Integer> heartCounts = new ConcurrentHashMap<>();
    private final Map<String, Integer> fireCounts = new ConcurrentHashMap<>();
    private final Map<String, List<String>> photoComments = new ConcurrentHashMap<>();
    private final Map<Long, String> userLanguage = new ConcurrentHashMap<>(); // Track user language preference
    private int nextUploadedId = 1;

    public ZahraCatBot() {
        String basePath = "src/main/java/com/zahracatbot/telegram/img/";
        String[][] catData = {
                { "cat_001", "Lucy", basePath + "cat1.jpeg" },
                { "cat_002", "Milo", basePath + "cat2.jpeg" },
                { "cat_003", "Bella", basePath + "cat3.jpeg" },
                { "cat_004", "Oliver", basePath + "cat4.jpeg" },
                { "cat_005", "Luna", basePath + "cat5.jpeg" },
                { "cat_006", "Leo", basePath + "cat6.jpeg" },
                { "cat_007", "Sasha", basePath + "cat7.jpeg" },
                { "cat_008", "Kitty", basePath + "cat8.jpeg" },
                { "cat_009", "Max", basePath + "cat9.jpeg" },
                { "cat_010", "Zara", basePath + "cat10.jpeg" }
        };
        for (String[] data : catData) {
            String photoId = data[0];
            String name = data[1];
            String filePath = data[2];
            catPhotoIds.add(photoId);
            photoIdToPath.put(photoId, filePath);
            photoNames.put(photoId, name);
            viewCounts.put(photoId, 0);
            likeCounts.put(photoId, 0);
            dislikeCounts.put(photoId, 0);
            heartCounts.put(photoId, 0);
            fireCounts.put(photoId, 0);
            photoComments.put(photoId, new ArrayList<>());
        }
        for (Map.Entry<String, String> entry : uploadedPhotoIds.entrySet()) {
            String shortId = entry.getKey();
            viewCounts.putIfAbsent(shortId, 0);
            likeCounts.putIfAbsent(shortId, 0);
            dislikeCounts.putIfAbsent(shortId, 0);
            heartCounts.putIfAbsent(shortId, 0);
            fireCounts.putIfAbsent(shortId, 0);
            photoComments.putIfAbsent(shortId, new ArrayList<>());
        }
    }

    @Override
    public String getBotUsername() {
        return "@ZahraCatBot";
    }

    @Override
    public String getBotToken() {
        return System.getenv("BOT_TOKEN") != null ? System.getenv("BOT_TOKEN")
                : "7858092728:AAEmGwyzkFW_bg-RXan9tZ8sd4bjsPzLU_s";
    }

    @Override
    public void onUpdateReceived(Update update) {
        Long chatId = update.hasMessage() ? update.getMessage().getChatId()
                : update.hasCallbackQuery() ? update.getCallbackQuery().getMessage().getChatId() : null;
        if (chatId == null)
            return;

        String state = userStates.getOrDefault(chatId, "");
        String lang = userLanguage.getOrDefault(chatId, "en");

        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            if (data.equals("lang_en")) {
                userLanguage.put(chatId, "en");
                sendMessage(chatId, "Language set to English!");
                showNextCatPhoto(chatId);
                return;
            } else if (data.equals("lang_ru")) {
                userLanguage.put(chatId, "ru");
                sendMessage(chatId, "Язык установлен на русский!");
                showNextCatPhoto(chatId);
                return;
            }
            handleCallbackQuery(chatId, data);
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText().trim();
            if (text.equals("/start") || text.equals("Back")) {
                userStates.put(chatId, "");
                userCommentingPhoto.remove(chatId);
                sendWelcomeMessage(chatId);
                sendMainMenu(chatId);
            } else if (text.equals("Cat Photos")) {
                userStates.put(chatId, "viewing_cats");
                userPhotoIndex.put(chatId, 0);
                showNextCatPhoto(chatId);
            } else if (text.equals("Upload Cat Photo")) {
                userStates.put(chatId, "uploading");
                sendMessage(chatId,
                        lang.equals("ru") ? "Пожалуйста, отправьте фото кота!" : "Please send a cat photo!");
            } else if (text.equals("Change Language")) {
                sendLanguageMenu(chatId);
            } else if (state.equals("commenting")) {
                String photoId = userCommentingPhoto.get(chatId);
                if (photoId != null) {
                    List<String> comments = photoComments.getOrDefault(photoId, new ArrayList<>());
                    comments.add(text);
                    photoComments.put(photoId, comments);
                    sendMessage(chatId, lang.equals("ru") ? "Ваш комментарий сохранён: " + text
                            : "Your comment has been saved: " + text);
                    userStates.put(chatId, "viewing_cats");
                    userCommentingPhoto.remove(chatId);
                    showNextCatPhoto(chatId);
                }
            }
        } else if (update.hasMessage() && update.getMessage().hasPhoto()) {
            if (state.equals("uploading")) {
                String fileId = update.getMessage().getPhoto().get(update.getMessage().getPhoto().size() - 1)
                        .getFileId();
                String shortId = "u" + nextUploadedId++;
                synchronized (uploadedPhotoIds) {
                    uploadedPhotoIds.put(shortId, fileId);
                    viewCounts.put(shortId, 0);
                    likeCounts.put(shortId, 0);
                    dislikeCounts.put(shortId, 0);
                    heartCounts.put(shortId, 0);
                    fireCounts.put(shortId, 0);
                    photoComments.put(shortId, new ArrayList<>());
                    photoNames.put(shortId, "UserCat_" + shortId);
                }
                sendMessage(chatId, lang.equals("ru") ? "Какая милая кошка! 😺" : "Wow, what a cute cat! 😺");
                sendCatPhoto(chatId, shortId, true);
                userStates.put(chatId, "");
            } else {
                sendMessage(chatId, lang.equals("ru") ? "Пожалуйста, сначала используйте кнопку 'Загрузить фото кота'!"
                        : "Please use the 'Upload Cat Photo' button first!");
            }
        }
    }

    private void handleCallbackQuery(Long chatId, String data) {
        String[] parts = data.split(":");
        String action = parts[0];
        String photoId = parts.length > 1 ? parts[1] : "";

        if (action.equals("like")) {
            likeCounts.compute(photoId, (k, v) -> v == null ? 1 : v + 1);
            String lang = userLanguage.getOrDefault(chatId, "en");
            sendMessage(chatId, lang.equals("ru") ? "Фото лайкнуто! 👍" : "Photo liked! 👍");
            updatePhotoCaption(chatId, photoId);
        } else if (action.equals("dislike")) {
            dislikeCounts.compute(photoId, (k, v) -> v == null ? 1 : v + 1);
            String lang = userLanguage.getOrDefault(chatId, "en");
            sendMessage(chatId, lang.equals("ru") ? "Фото не лайкнуто! 👎" : "Photo disliked! 👎");
            updatePhotoCaption(chatId, photoId);
        } else if (action.equals("heart")) {
            heartCounts.compute(photoId, (k, v) -> v == null ? 1 : v + 1);
            String lang = userLanguage.getOrDefault(chatId, "en");
            sendMessage(chatId, lang.equals("ru") ? "Фото получило сердце! ❤️" : "Photo got a heart! ❤️");
            updatePhotoCaption(chatId, photoId);
        } else if (action.equals("fire")) {
            fireCounts.compute(photoId, (k, v) -> v == null ? 1 : v + 1);
            String lang = userLanguage.getOrDefault(chatId, "en");
            sendMessage(chatId, lang.equals("ru") ? "Фото в огне! 🔥" : "Photo is on fire! 🔥");
            updatePhotoCaption(chatId, photoId);
        } else if (action.equals("comment")) {
            userStates.put(chatId, "commenting");
            userCommentingPhoto.put(chatId, photoId);
            String lang = userLanguage.getOrDefault(chatId, "en");
            sendMessage(chatId,
                    lang.equals("ru") ? "Пожалуйста, напишите свой комментарий:" : "Please write your comment:");
        } else if (action.equals("next")) {
            showNextCatPhoto(chatId);
        }
    }

    private void sendWelcomeMessage(Long chatId) {
        String lang = userLanguage.getOrDefault(chatId, "en");
        sendMessage(chatId, lang.equals("ru") ? "Добро пожаловать в ZahraCatBot! 😺 Готовы посмотреть котов?"
                : "Welcome to ZahraCatBot! 😺 Ready to see some cats?");
    }

    private void sendMainMenu(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        String lang = userLanguage.getOrDefault(chatId, "en");
        message.setText(lang.equals("ru") ? "Выберите опцию:" : "Choose an option:");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(lang.equals("ru") ? "Фото котов" : "Cat Photos");
        row1.add(lang.equals("ru") ? "Загрузить фото кота" : "Upload Cat Photo");
        KeyboardRow row2 = new KeyboardRow();
        row2.add(lang.equals("ru") ? "Назад" : "Back");
        row2.add(lang.equals("ru") ? "Изменить язык" : "Change Language");
        keyboard.add(row1);
        keyboard.add(row2);
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendLanguageMenu(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Select language / Выберите язык:");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton enButton = new InlineKeyboardButton();
        enButton.setText("English");
        enButton.setCallbackData("lang_en");
        InlineKeyboardButton ruButton = new InlineKeyboardButton();
        ruButton.setText("Русский");
        ruButton.setCallbackData("lang_ru");
        row.add(enButton);
        row.add(ruButton);
        buttons.add(row);
        markup.setKeyboard(buttons);
        message.setReplyMarkup(markup);
        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendCatPhoto(Long chatId, String photoId, boolean isFileId) {
        viewCounts.compute(photoId, (k, v) -> v == null ? 1 : v + 1);
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId.toString());
        if (isFileId) {
            String fileId = uploadedPhotoIds.get(photoId);
            if (fileId == null || fileId.isEmpty()) {
                sendMessage(chatId, "Error: Invalid uploaded file ID!");
                return;
            }
            photo.setPhoto(new InputFile(fileId));
        } else {
            String filePath = photoIdToPath.get(photoId);
            if (filePath == null) {
                sendMessage(chatId, "Error: Cat photo not found!");
                return;
            }
            File file = new File(filePath);
            if (!file.exists()) {
                sendMessage(chatId, "Error: File at " + filePath + " does not exist! Please check the path.");
                return;
            }
            photo.setPhoto(new InputFile(file));
        }

        String lang = userLanguage.getOrDefault(chatId, "en");
        String name = photoNames.getOrDefault(photoId, "Unknown Cat");
        String caption;
        if (lang.equals("ru")) {
            switch (name) {
                case "Lucy":
                    caption = "Привет! Меня зовут Люси, я игривый кот. Поставь мне лайк!";
                    break;
                case "Milo":
                    caption = "Привет! Меня зовут Мило, я игривый кот. Поставь мне лайк!";
                    break;
                case "Bella":
                    caption = "Привет! Меня зовут Белла, я игривый кот. Поставь мне лайк!";
                    break;
                case "Oliver":
                    caption = "Привет! Меня зовут Оливер, я игривый кот. Поставь мне лайк!";
                    break;
                case "Luna":
                    caption = "Привет! Меня зовут Луна, я игривый кот. Поставь мне лайк!";
                    break;
                case "Leo":
                    caption = "Привет! Меня зовут Лео, я игривый кот. Поставь мне лайк!";
                    break;
                case "Sasha":
                    caption = "Привет! Меня зовут Саша, я игривый кот. Поставь мне лайк!";
                    break;
                case "Kitty":
                    caption = "Привет! Меня зовут Китти, я игривый кот. Поставь мне лайк!";
                    break;
                case "Max":
                    caption = "Привет! Меня зовут Макс, я игривый кот. Поставь мне лайк!";
                    break;
                case "Zara":
                    caption = "Привет! Меня зовут Зара, я игривый кот. Поставь мне лайк!";
                    break;
                default:
                    caption = "Привет! Меня зовут " + name + ", я игривый кот. Поставь мне лайк!";
            }
        } else {
            switch (name) {
                case "Lucy":
                    caption = "Hello! My name is Lucy, a playful cat. Like me!";
                    break;
                case "Milo":
                    caption = "Hello! My name is Milo, a playful cat. Like me!";
                    break;
                case "Bella":
                    caption = "Hello! My name is Bella, a playful cat. Like me!";
                    break;
                case "Oliver":
                    caption = "Hello! My name is Oliver, a playful cat. Like me!";
                    break;
                case "Luna":
                    caption = "Hello! My name is Luna, a playful cat. Like me!";
                    break;
                case "Leo":
                    caption = "Hello! My name is Leo, a playful cat. Like me!";
                    break;
                case "Sasha":
                    caption = "Hello! My name is Sasha, a playful cat. Like me!";
                    break;
                case "Kitty":
                    caption = "Hello! My name is Kitty, a playful cat. Like me!";
                    break;
                case "Max":
                    caption = "Hello! My name is Max, a playful cat. Like me!";
                    break;
                case "Zara":
                    caption = "Hello! My name is Zara, a playful cat. Like me!";
                    break;
                default:
                    caption = "Hello! My name is " + name + ", a playful cat. Like me!";
            }
        }
        caption += String.format("\n👀 Views: %d\n👍 Likes: %d\n👎 Dislikes: %d\n❤️ Hearts: %d\n🔥 Fires: %d",
                viewCounts.get(photoId), likeCounts.get(photoId), dislikeCounts.get(photoId),
                heartCounts.get(photoId), fireCounts.get(photoId));
        List<String> comments = photoComments.getOrDefault(photoId, new ArrayList<>());
        if (!comments.isEmpty()) {
            caption += "\n\nComments:\n";
            for (String comment : comments) {
                caption += "- " + comment + "\n";
            }
        }
        photo.setCaption(caption);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton likeButton = new InlineKeyboardButton();
        likeButton.setText("👍");
        likeButton.setCallbackData("like:" + photoId);
        InlineKeyboardButton dislikeButton = new InlineKeyboardButton();
        dislikeButton.setText("👎");
        dislikeButton.setCallbackData("dislike:" + photoId);
        InlineKeyboardButton heartButton = new InlineKeyboardButton();
        heartButton.setText("❤️");
        heartButton.setCallbackData("heart:" + photoId);
        InlineKeyboardButton fireButton = new InlineKeyboardButton();
        fireButton.setText("🔥");
        fireButton.setCallbackData("fire:" + photoId);
        row1.add(likeButton);
        row1.add(dislikeButton);
        row1.add(heartButton);
        row1.add(fireButton);
        buttons.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton commentButton = new InlineKeyboardButton();
        commentButton.setText("Comment");
        commentButton.setCallbackData("comment:" + photoId);
        row2.add(commentButton);
        buttons.add(row2);

        markup.setKeyboard(buttons);
        photo.setReplyMarkup(markup);
        try {
            execute(photo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showNextCatPhoto(Long chatId) {
        synchronized (catPhotoIds) {
            List<String> allPhotos = new ArrayList<>(catPhotoIds);
            synchronized (uploadedPhotoIds) {
                allPhotos.addAll(uploadedPhotoIds.keySet());
            }
            if (allPhotos.isEmpty()) {
                sendMessage(chatId, "No photos available! Upload a cat photo!");
                userStates.put(chatId, "");
                sendMainMenu(chatId);
                return;
            }
            int index = userPhotoIndex.getOrDefault(chatId, 0);
            if (index >= allPhotos.size()) {
                index = 0;
                userPhotoIndex.put(chatId, 0);
            }
            String photoId = allPhotos.get(index);
            boolean isFileId = uploadedPhotoIds.containsKey(photoId);
            sendCatPhoto(chatId, photoId, isFileId);
            userPhotoIndex.put(chatId, index + 1);

            SendMessage nextMessage = new SendMessage();
            nextMessage.setChatId(chatId.toString());
            String lang = userLanguage.getOrDefault(chatId, "en");
            nextMessage.setText(lang.equals("ru") ? "Следующее фото с плавной анимацией..."
                    : "Next photo with smooth animation...");
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton nextButton = new InlineKeyboardButton();
            nextButton.setText(lang.equals("ru") ? "Дальше" : "Next");
            nextButton.setCallbackData("next");
            row.add(nextButton);
            buttons.add(row);
            markup.setKeyboard(buttons);
            nextMessage.setReplyMarkup(markup);
            try {
                execute(nextMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updatePhotoCaption(Long chatId, String photoId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        String lang = userLanguage.getOrDefault(chatId, "en");
        String name = photoNames.getOrDefault(photoId, "Unknown Cat");
        String caption;
        if (lang.equals("ru")) {
            caption = "Обновление:\nИмя: " + name + " (ID: " + photoId + ")\n👀 Просмотры: " + viewCounts.get(photoId) +
                    "\n👍 Лайки: " + likeCounts.get(photoId) + "\n👎 Не лайки: " + dislikeCounts.get(photoId) +
                    "\n❤️ Сердца: " + heartCounts.get(photoId) + "\n🔥 Огни: " + fireCounts.get(photoId);
        } else {
            caption = "Update:\nName: " + name + " (ID: " + photoId + ")\n👀 Views: " + viewCounts.get(photoId) +
                    "\n👍 Likes: " + likeCounts.get(photoId) + "\n👎 Dislikes: " + dislikeCounts.get(photoId) +
                    "\n❤️ Hearts: " + heartCounts.get(photoId) + "\n🔥 Fires: " + fireCounts.get(photoId);
        }
        List<String> comments = photoComments.getOrDefault(photoId, new ArrayList<>());
        if (!comments.isEmpty()) {
            caption += "\n\n" + (lang.equals("ru") ? "Комментарии:\n" : "Comments:\n");
            for (String comment : comments) {
                caption += "- " + comment + "\n";
            }
        }
        message.setText(caption);
        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}