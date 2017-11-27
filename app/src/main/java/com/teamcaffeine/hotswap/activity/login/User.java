package com.teamcaffeine.hotswap.activity.login;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class User {
    private String Uid;
    private String userName;
    private String password;
    private String firstName;
    private String lastName;

    private ArrayList<String> tests;

    User() {
    }

    public User(String Uid, String userName, String password, String firstName, String lastName, ArrayList<String> tests) {
        this.Uid = Uid;
        this.userName = userName;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.tests = tests;
    }

    public User(String Uid) {
        this.Uid = Uid;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("Uid", Uid);
        result.put("userName", userName);
        result.put("password", password);
        result.put("firstName", firstName);
        result.put("lastName", lastName);
        result.put("tests", tests);
        return result;
    }

    public void setUid(String uid) {
        this.Uid = uid;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setTests(ArrayList<String> tests) {
        this.tests = tests;
    }

    public String getUid() {
        return Uid;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public ArrayList<String> getTests() {
        return tests;
    }
}
