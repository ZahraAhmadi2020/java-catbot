package com.zahracatbot.common.model;

import java.io.Serializable;

public class CatAction implements Serializable {
    private final String action;
    private final Long chatId;
    private final Long catId;
    private final String fileId;

    public CatAction(String action, Long chatId, Long catId, String fileId) {
        this.action = action;
        this.chatId = chatId;
        this.catId = catId;
        this.fileId = fileId;
    }

    public String getAction() {
        return action;
    }

    public Long getChatId() {
        return chatId;
    }

    public Long getCatId() {
        return catId;
    }

    public String getFileId() {
        return fileId;
    }
}
