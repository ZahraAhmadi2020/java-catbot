package com.zahracatbot.cats;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zahracatbot.common.model.Cat;

public interface CatRepository extends JpaRepository<Cat, Long> {
  List<Cat> findByChatId(Long chatId);
}
