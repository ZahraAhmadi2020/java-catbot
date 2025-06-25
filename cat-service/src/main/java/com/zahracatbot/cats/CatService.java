package com.zahracatbot.cats;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.zahracatbot.common.model.Cat;
import com.zahracatbot.common.service.CatServiceInterface;

@Service
public class CatService implements CatServiceInterface {

  private final CatRepository catRepository;

  public CatService(CatRepository catRepository) {
    this.catRepository = catRepository;
  }

  @Override
  public Cat saveCat(Cat cat, String fileId, Long chatId) {
    cat.setFileId(fileId);
    cat.setChatId(chatId);
    return catRepository.save(cat);
  }

  @Override
  public Optional<Cat> findCatByIdAndChatId(Long id, Long chatId) {
    return catRepository.findById(id)
        .filter(cat -> cat.getChatId().equals(chatId));
  }

  @Override
  public List<Cat> findCatsByChatId(Long chatId) {
    return catRepository.findByChatId(chatId);
  }

  @Override
  public void deleteCat(Long id) {
    catRepository.deleteById(id);
  }

  @Override
  public void likeCat(Long id) {
    catRepository.findById(id).ifPresent(cat -> {
      cat.setLikes(cat.getLikes() + 1);
      catRepository.save(cat);
    });
  }

  @Override
  public void dislikeCat(Long id) {
    catRepository.findById(id).ifPresent(cat -> {
      cat.setDislikes(cat.getDislikes() + 1);
      catRepository.save(cat);
    });
  }

  @Override
  public void heartCat(Long id) {
    catRepository.findById(id).ifPresent(cat -> {
      cat.setHearts(cat.getHearts() + 1);
      catRepository.save(cat);
    });
  }

  @Override
  public void fireCat(Long id) {
    catRepository.findById(id).ifPresent(cat -> {
      cat.setFires(cat.getFires() + 1);
      catRepository.save(cat);
    });
  }
}
