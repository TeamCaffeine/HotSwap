package com.teamcaffeine.hotswap.messaging.models;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.teamcaffeine.hotswap.login.User;

import java.util.Date;

/**
 * @author agrawroh
 * @version v1.0
 */
public class Message implements IMessage {

    private String id;
    private String text;
    private Date createdAt;
    private String status;
    private User user;

    public Message() {
        /* Do Nothing */
    }

    public Message(String id, User user, String text) {
        this(id, user, text, new Date());
    }

    public Message(String id, User user, String text, Date createdAt) {
        this.id = id;
        this.text = text;
        this.user = user;
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
