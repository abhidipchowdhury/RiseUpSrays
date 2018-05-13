package com.abhidip.strays.model;

/**
 * Created by admin on 5/4/2018.
 */

public class Comments {
    private int id;
    private String chatMessageId;
    private String uid;
    private String description;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getChatMessageId() {
        return chatMessageId;
    }

    public void setChatMessageId(String chatMessageId) {
        this.chatMessageId = chatMessageId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
