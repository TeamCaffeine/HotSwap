package com.teamcaffeine.hotswap.messaging.models;

import java.util.HashMap;
import java.util.Map;

/**
 * @author agrawroh
 * @version v1.0
 */
public class Channel {
    private String channel;
    private Subscriptions subscriptions;

    public Channel() {
        /* Do Nothing */
    }

    public Channel(final String channel, final Subscriptions subscriptions) {
        this.channel = channel;
        this.subscriptions = subscriptions;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("channel", channel);
        result.put("subscriptions", subscriptions);
        return result;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Subscriptions getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Subscriptions subscriptions) {
        this.subscriptions = subscriptions;
    }

    public void addSubscription(String subscription) {
        this.subscriptions.addChannel(subscription);
    }
}
