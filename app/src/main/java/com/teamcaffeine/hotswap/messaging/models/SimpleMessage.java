package com.teamcaffeine.hotswap.messaging.models;

/**
 * @author agrawroh
 * @version v1.0
 */

public class SimpleMessage {
    private String message;
    private String user;
    private long timestamp;

    public SimpleMessage() {
        /* Do Nothing */
    }

    public SimpleMessage(String message, String user, long timestamp) {
        this.message = message;
        this.user = user;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
