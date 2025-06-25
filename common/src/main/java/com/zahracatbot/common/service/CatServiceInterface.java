package com.zahracatbot.common.service;

import com.zahracatbot.common.model.Cat;
import java.util.List;
import java.util.Optional;

public interface CatServiceInterface {
    Cat saveCat(Cat cat, String fileId, Long chatId);
    Optional<Cat> findCatByIdAndChatId(Long id, Long chatId);
    List<Cat> findCatsByChatId(Long chatId);
    void deleteCat(Long id);
    void likeCat(Long id);
    void dislikeCat(Long id);
    void heartCat(Long id);
    void fireCat(Long id);
}
