package com.teamcaffeine.hotswap.messaging.models;

import java.util.List;

/**
 * @author agrawroh
 * @version v1.0
 */
public class Subscriptions {
    private List<String> channel;

    public Subscriptions() {
        /* Do Nothing */
    }

    public Subscriptions(final List<String> channel) {
        this.channel = channel;
    }

    public List<String> getChannel() {
        return channel;
    }

    public void setChannel(List<String> channel) {
        this.channel = channel;
    }
}
