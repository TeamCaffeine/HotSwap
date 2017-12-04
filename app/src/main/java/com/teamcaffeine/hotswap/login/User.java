package com.teamcaffeine.hotswap.login;

import com.stfalcon.chatkit.commons.models.IUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User implements IUser{
    // Force empty initialization of default table fields
    private String Uid = "";
    private String firstName = "";
    private String lastName = "";
    private String email = "";
    private String memberSince = "";
    private String phoneNumber = "";
    private String profilePicture = "";
    private boolean online = false;

    User() {}

    public User(String Uid, String email) {
        this.Uid = Uid;
        this.email = email;
    }

    public User(User user) {
        this.Uid = user.getUid();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.memberSince = user.getMemberSince();
        this.phoneNumber = user.getPhoneNumber();
        this.profilePicture = user.getProfilePicture();
        this.online = user.getOnline();
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("Uid", Uid);
        result.put("firstName", firstName);
        result.put("lastName", lastName);
        result.put("email", email);
        result.put("memberSince", memberSince);
        result.put("phoneNumber", phoneNumber);
        result.put("profilePicture", profilePicture);
        result.put("online", online);
        return result;
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

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setOnline(boolean online) { this.online = online; }

    public String getUid() { return Uid; }

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

    public String getProfilePicture() { return profilePicture; }

    public boolean getOnline() { return online; }

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
