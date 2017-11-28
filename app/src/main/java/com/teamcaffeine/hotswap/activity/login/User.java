package com.teamcaffeine.hotswap.activity.login;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class User {
    private String Uid;
    private String userName;
    private String password;
    private String dateCreated;
    private String firstName;
    private String lastName;

    private ArrayList<String> items;
    private ArrayList<String> currentlyRenting;
    private ArrayList<String> currentlyLending;
    private ArrayList<String> paymentOptions;
    private ArrayList<String> linkedAccounts;

    User() {
    }

    public User(String Uid, String dateCreated, String firstName, String lastName, ArrayList<String> items,
                ArrayList<String> currentlyRenting, ArrayList<String> currentlyLending, ArrayList<String> paymentOptions,
                ArrayList<String> linkedAccounts) {
        this.Uid = Uid;
        this.dateCreated = dateCreated;
        this.firstName = firstName;
        this.lastName = lastName;
        this.items = items;
        this.currentlyRenting = currentlyRenting;
        this.currentlyLending = currentlyLending;
        this.paymentOptions = paymentOptions;
        this.linkedAccounts = linkedAccounts;
    }

    public User(String Uid) {
        this.Uid = Uid;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("Uid", Uid);
        result.put("dateCreated", dateCreated);
        result.put("firstName", firstName);
        result.put("lastName", lastName);
        result.put("items", items);
        result.put("currentlyRenting", currentlyRenting);
        result.put("currentlyLending", currentlyLending);
        result.put("paymentOptions", paymentOptions);
        result.put("linkedAccounts", linkedAccounts);
        return result;
    }

    public void setUid(String uid) {
        this.Uid = uid;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setItems(ArrayList<String> tests) {
        this.items = tests;
    }

    public void setCurrentlyRenting(ArrayList<String> currentlyRenting) {
        this.currentlyRenting = currentlyRenting;
    }

    public void setCurrentlyLending(ArrayList<String> currentlyLending) {
        this.currentlyLending = currentlyLending;
    }

    public void setPaymentOptions(ArrayList<String> paymentOptions) {
        this.paymentOptions = paymentOptions;
    }

    public void setLinkedAccounts(ArrayList<String> linkedAccounts) {
        this.linkedAccounts = linkedAccounts;
    }

    public String getUid() {
        return Uid;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public ArrayList<String> getItems() {
        return items;
    }

    public ArrayList<String> getCurrentlyRenting() {
        return currentlyRenting;
    }

    public ArrayList<String> getCurrentlyLending() {
        return currentlyLending;
    }

    public ArrayList<String> getPaymentOptions() {
        return paymentOptions;
    }

    public ArrayList<String> getLinkedAccounts() {
        return linkedAccounts;
    }
}
