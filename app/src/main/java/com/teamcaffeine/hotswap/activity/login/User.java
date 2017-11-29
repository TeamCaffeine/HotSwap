package com.teamcaffeine.hotswap.activity.login;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class User {
    private boolean addedDetails;
    private String Uid;
    private String firstName;
    private String lastName;
    private String email;
    private String memberSince;
    private String phoneNumber;

    User() {
    }

    public User(boolean addedDetails, String Uid, String firstName, String lastName,
                String email, String memberSince, String phoneNumber) {
        this.addedDetails = addedDetails;
        this.Uid = Uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.memberSince = memberSince;
        this.phoneNumber = phoneNumber;
    }

    public User(String Uid, String email) {
        this.addedDetails = false;
        this.Uid = Uid;
        this.email = email;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("addedDetails", addedDetails);
        result.put("Uid", Uid);
        result.put("firstName", firstName);
        result.put("lastName", lastName);
        result.put("email", email);
        result.put("memberSince", memberSince);
        result.put("phoneNumber", phoneNumber);
        return result;
    }

    public void setAddedDetails(boolean addedDetails) {
        this.addedDetails = addedDetails;
    }

    public void setUid(String uid) {
        this.Uid = uid;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMemberSince(String memberSince) {
        this.memberSince = memberSince;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean getAddedDetails() {
        return addedDetails;
    }

    public String getUid() {
        return Uid;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getMemberSince() {
        return memberSince;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
