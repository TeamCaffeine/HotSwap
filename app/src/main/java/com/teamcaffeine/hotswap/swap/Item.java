package com.teamcaffeine.hotswap.swap;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Item implements Parcelable {
    // Force empty initialization of default table fields
    private String itemID ="";
    private String name = "";
    private String ownerID = "";
    private String renteeID = "";
    private String description = "";
    private String rentPrice = "";
    private List<String> tags = new ArrayList<String>();
    private String headerPicture = "";
    private List<String> additionalPictures = new ArrayList<String>();
    private List<Date> availableDates = new ArrayList<Date>();
    private String address = "";

    Item() {}

    public Item(String itemID, String name, String ownerID, String description, String rentPrice, String address) {
        this.itemID = itemID;
        this.name = name;
        this.ownerID = ownerID;
        this.description = description;
        this.rentPrice = rentPrice;
        this.address = address;
    }

    public Item(Item item) {
        this.itemID = item.getItemID();
        this.name = item.getName();
        this.ownerID = item.getOwnerID();
        this.renteeID = item.getRenteeID();
        this.description = item.getDescription();
        this.rentPrice = rentPrice;
        this.tags = item.getTags();
        this.headerPicture = item.getHeaderPicture();
        this.additionalPictures = item.getAdditionalPictures();
        this.availableDates = item.getAvailableDates();
        this.address = item.getAddress();
    }

    protected Item(Parcel in) {
        itemID = in.readString();
        name = in.readString();
        ownerID = in.readString();
        renteeID = in.readString();
        description = in.readString();
        rentPrice = in.readString();
        tags = in.createStringArrayList();
        headerPicture = in.readString();
        additionalPictures = in.createStringArrayList();
        address = in.readString();
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("itemID", itemID);
        result.put("name", name);
        result.put("ownerID", ownerID);
        result.put("renteeID", renteeID);
        result.put("description", description);
        result.put("rentPrice", rentPrice);
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

    public void setRentPrice(String rentPrice) {
        this.rentPrice = rentPrice;
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

    public String getRenteeID() {
        return renteeID;
    }

    public String getDescription() {
        return description;
    }

    public String getRentPrice() {
        return rentPrice;
    }

    public List<String> getTags() {
        return tags;
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

    /**
     * Methods to set Item as a Parcelable class to pass between activities in an intent
     */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(itemID);
        parcel.writeString(name);
        parcel.writeString(ownerID);
        parcel.writeString(renteeID);
        parcel.writeString(description);
        parcel.writeString(rentPrice);
        parcel.writeStringList(tags);
        parcel.writeString(headerPicture);
        parcel.writeStringList(additionalPictures);
        parcel.writeString(address);
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };
}