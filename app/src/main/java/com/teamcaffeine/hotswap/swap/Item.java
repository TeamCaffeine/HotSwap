package com.teamcaffeine.hotswap.swap;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Item {
    // Force empty initialization of default table fields
    private String name = "";
    private String ownerID = "";
    private String renteeID = "";
    private String description = "";
    private String rentPrice = "";
    private ArrayList<String> tags = new ArrayList<String>();
    private String headerPicture = "";
    private ArrayList<String> additionalPictures = new ArrayList<String>();
    private ArrayList<Date> availableDates = new ArrayList<Date>();
    private String address = "";

    Item() {}

    public Item(String name, String ownerID, String description, ArrayList<String> tags, String headerPicture,
                ArrayList<Date> availableDates) {
        this.name = name;
        this.ownerID = ownerID;
        this.description = description;
        this.tags = tags;
        this.headerPicture = headerPicture;
        this.availableDates = availableDates;
    }

    public Item(Item item) {
        this.name = item.getName();
        this.ownerID = item.getOwnerID();
        this.renteeID = item.getRenteeID();
        this.description = item.getDescription();
        this.tags = item.getTags();
        this.headerPicture = item.getHeaderPicture();
        this.additionalPictures = item.getAdditionalPictures();
        this.availableDates = item.getAvailableDates();
        this.address = item.getAddress();
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("ownerID", ownerID);
        result.put("renteeID", renteeID);
        result.put("description", description);
        result.put("tags", tags);
        result.put("headerPicture", headerPicture);
        result.put("additionalPictures", additionalPictures);
        result.put("address", address);
        return result;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public void setRenteeID(String renteeID) {
        this.renteeID = renteeID;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean addTag(String tag) {
        if (!this.tags.contains(tag)) {
            this.tags.add(tag);
            return true;
        }
        return false;
    }

    public boolean removeTag(String tag) {
        return this.tags.remove(tag);
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

    public void setAvailableDates(ArrayList<Date> availableDates) {
        this.availableDates = availableDates;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public String getRenteeID() {
        return renteeID;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public String getHeaderPicture() {
        return headerPicture;
    }

    public ArrayList<String> getAdditionalPictures() {
        return additionalPictures;
    }

    public ArrayList<Date> getAvailableDates() {
        return availableDates;
    }

    public String getAddress() {
        return address;
    }
}