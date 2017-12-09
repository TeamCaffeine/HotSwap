package com.teamcaffeine.hotswap.swap;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Transaction {

    // Force empty initialization of default table fields
    private String requestUserID = "";
    private String initialMessage = "";
    private double distance = 0.0;
    private List<Date> requestedDates = new ArrayList<>();
    private boolean confirmed = false;

    Transaction() {
    }

    public Transaction(String requestUserID, String initialMessage, double distance, List<Date> requestedDates, boolean confirmed) {
        this.requestUserID = requestUserID;
        this.initialMessage = initialMessage;
        this.distance = distance;
        this.requestedDates = requestedDates;
        this.confirmed = confirmed;
    }

    public String getRequestUserID() {
        return requestUserID;
    }

    public void setRequestUserID(String requestUserID) {
        this.requestUserID = requestUserID;
    }

    public String getInitialMessage() {
        return initialMessage;
    }

    public void setInitialMessage(String initialMessage) {
        this.initialMessage = initialMessage;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public List<Date> getRequestedDates() {
        return requestedDates;
    }

    public Date startDate() {
        if (!requestedDates.isEmpty()) {
            return requestedDates.get(0);
        } else {
            return null;
        }
    }

    public Date endDate() {
        if (!requestedDates.isEmpty()) {
            return requestedDates.get(requestedDates.size() - 1);
        } else {
            return null;
        }
    }

    public void setRequestedDates(List<Date> requestedDates) {
        this.requestedDates = requestedDates;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("requestUserID", requestUserID);
        result.put("initialMessage", initialMessage);
        result.put("distance", distance);
        result.put("requestedDates", requestedDates);
        result.put("confirmed", confirmed);
        return result;
    }

}
