package com.teamcaffeine.hotswap.swap;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Item {
    // Force empty initialization of default table fields
    private String itemID = "";
    private String name = "";
    private String ownerID = "";
    private String description = "";
    private String rentPrice = "";
    private String tag = "";
    private String headerPicture = "";
    private List<String> additionalPictures = new ArrayList<String>();
    private List<Date> availableDates = new ArrayList<Date>();
    private String address = "";
    private ArrayList<Transaction> transactions = new ArrayList<>();

    Item() {
    }

    public Item(String itemID, String name, String ownerID, String description, String rentPrice, String address, String headerPicture, String tag) {
        this.itemID = itemID;
        this.name = name;
        this.ownerID = ownerID;
        this.description = description;
        this.rentPrice = rentPrice;
        this.address = address;
        this.headerPicture = headerPicture;
        this.tag = tag;
    }

    public Item(String itemID, String name, String ownerID, String description, String rentPrice, String address, ArrayList<Transaction> transactions) {
        this.itemID = itemID;
        this.name = name;
        this.ownerID = ownerID;
        this.description = description;
        this.rentPrice = rentPrice;
        this.address = address;
        this.transactions = transactions;
    }

    public Item(Item item) {
        this.itemID = item.getItemID();
        this.name = item.getName();
        this.ownerID = item.getOwnerID();
        this.description = item.getDescription();
        this.rentPrice = rentPrice;
        this.tag = item.getTag();
        this.headerPicture = item.getHeaderPicture();
        this.additionalPictures = item.getAdditionalPictures();
        this.availableDates = item.getAvailableDates();
        this.address = item.getAddress();
        this.transactions = item.getTransactions();
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("itemID", itemID);
        result.put("name", name);
        result.put("ownerID", ownerID);
        result.put("description", description);
        result.put("rentPrice", rentPrice);
        result.put("tag", tag);
        result.put("headerPicture", headerPicture);
        result.put("additionalPictures", additionalPictures);
        result.put("address", address);
        result.put("transactions", transactions);
        return result;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTag(String tag){
        this.tag = tag;
    }

    public String getTag(){
        return tag;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRentPrice(String rentPrice) {
        this.rentPrice = rentPrice;
    }

    public void setHeaderPicture(String headerPicture) {
        this.headerPicture = headerPicture;
    }

    public boolean addAdditionalPicture(String picture) {
        if (!this.additionalPictures.contains(picture)) {
            this.additionalPictures.add(picture);
            return true;
        }
        return false;
    }

    public boolean removeAdditionalPicture(String picture) {
        return this.additionalPictures.remove(picture);
    }

    public void setAvailableDates(List<Date> availableDates) {
        this.availableDates = availableDates;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getItemID() {
        return itemID;
    }

    public String getName() {
        return name;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public String getDescription() {
        return description;
    }

    public String getRentPrice() {
        return rentPrice;
    }

    public String getHeaderPicture() {
        return headerPicture;
    }

    public List<String> getAdditionalPictures() {
        return additionalPictures;
    }

    public List<Date> getAvailableDates() {
        return availableDates;
    }

    public String getAddress() {
        return address;
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }

    public boolean addTransaction(Transaction transaction) {
        for (Transaction t : transactions) {
            if (t.getRequestUserID().equals(transaction.getRequestUserID()) && t.getRequestedDates().equals(transaction.getRequestedDates())) {
                //TODO this doesn't handle overlap, just checks for equal dates. Future work to check for any overlap and notify user
                return false;
            }
        }
        transactions.add(transaction);
        return true;
    }
}