package com.zahracatbot.cats;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zahracatbot.common.model.Cat;
import com.zahracatbot.common.service.CatServiceInterface;

@RestController
@RequestMapping("/api/cats")
public class CatController {

  private final CatServiceInterface catService;

  public CatController(CatServiceInterface catService) {
    this.catService = catService;
  }

  @PostMapping
  public Cat saveCat(@RequestBody Cat cat, @RequestParam String fileId, @RequestParam Long chatId) {
    return catService.saveCat(cat, fileId, chatId);
  }

  @GetMapping("/{id}")
  public Optional<Cat> findCatByIdAndChatId(@PathVariable Long id, @RequestParam Long chatId) {
    return catService.findCatByIdAndChatId(id, chatId);
  }

  @GetMapping("/chat/{chatId}")
  public List<Cat> findCatsByChatId(@PathVariable Long chatId) {
    return catService.findCatsByChatId(chatId);
  }

  @DeleteMapping("/{id}")
  public void deleteCat(@PathVariable Long id) {
    catService.deleteCat(id);
  }

  @PostMapping("/{id}/like")
  public void likeCat(@PathVariable Long id) {
    catService.likeCat(id);
  }

  @PostMapping("/{id}/dislike")
  public void dislikeCat(@PathVariable Long id) {
    catService.dislikeCat(id);
  }

  @PostMapping("/{id}/heart")
  public void heartCat(@PathVariable Long id) {
    catService.heartCat(id);
  }

  @PostMapping("/{id}/fire")
  public void fireCat(@PathVariable Long id) {
    catService.fireCat(id);
  }
}
