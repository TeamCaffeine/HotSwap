package com.teamcaffeine.hotswap.login;

import com.stfalcon.chatkit.commons.models.IUser;

import java.util.HashMap;
import java.util.Map;


public class User implements IUser {
    private boolean addedDetails;
    private String Uid;
    private String firstName;
    private String lastName;
    private String email;
    private String memberSince;
    private String phoneNumber;
    private String avatar;
    private boolean online;
    //TODO: figure out how to store an image

    User() {
    }

    public User(boolean addedDetails, String uid, String firstName, String lastName, String email,
                String memberSince, String phoneNumber, String avatar) {
        this.addedDetails = addedDetails;
        this.Uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.memberSince = memberSince;
        this.phoneNumber = phoneNumber;
        this.avatar = avatar;
        this.online = false;
    }

    public User(String Uid, String email) {
        this.addedDetails = false;
        this.Uid = Uid;
        this.email = email;
    }

    public User(User user) {
        this.addedDetails = user.isAddedDetails();
        this.Uid = user.getUid();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.memberSince = user.getMemberSince();
        this.phoneNumber = user.getPhoneNumber();
        this.avatar = user.getAvatar();
        this.online = user.isOnline();
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
        result.put("avatar", avatar);
        result.put("online", online);
        return result;
    }

    public boolean isAddedDetails() {
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

    public String getAvatar() {
        return avatar;
    }

    public boolean isOnline() {
        return online;
    }

    public void setAddedDetails(boolean addedDetails) {
        this.addedDetails = addedDetails;
    }

    public void setUid(String uid) {
        Uid = uid;
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

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    // Methods that need to be implemented for the IUser class for chatkit
    @Override
    public String getId() {
        return email;
    }

    @Override
    public String getName() {
        return firstName + " " + lastName;
    }

    public void setId(String newEmail) {
        this.email = newEmail;
    }
}
