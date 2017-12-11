package com.teamcaffeine.hotswap.swap;

import java.util.Date;

public class ActiveTransactionInfo {

    private Item item;
    private String renterId;
    private Date date;

    public ActiveTransactionInfo(Item item, String renterId, Date date) {
        this.item = item;
        this.renterId = renterId;
        this.date = date;
    }

    public String toKey() {
        return item.getName() + renterId + date.toString();
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getRenterId() {
        return renterId;
    }

    public void setRenterId(String renterId) {
        this.renterId = renterId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
