package com.teamcaffeine.hotswap.activity.messaging;

/**
 * Created by william on 26/11/2017.
 */

public class conversation {
    private int convoProfilePic;
    private String convoName;
    private String convoLastMessage;

    public conversation(int convoProfilePic, String convoName, String convoLastMessage) {
        this.convoProfilePic = convoProfilePic;
        this.convoName = convoName;
        this.convoLastMessage = convoLastMessage;
    }

    public int getConvoProfilePic() {
        return convoProfilePic;
    }

    public String getConvoName() {
        return convoName;
    }

    public String getConvoLastMessage() {
        return convoLastMessage;
    }

    public void setConvoProfilePic(int convoProfilePic) {
        this.convoProfilePic = convoProfilePic;
    }

    public void setConvoName(String convoName) {
        this.convoName = convoName;
    }

    public void setConvoLastMessage(String convoLastMessage) {
        this.convoLastMessage = convoLastMessage;
    }
}
