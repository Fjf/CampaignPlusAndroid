package com.example.dndapp.Playthrough;

public class PlayerArrayElement {
    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    private String playerName;
    private String userName;
    private String className;

    public PlayerArrayElement(String playerName, String userName, String className) {
        this.playerName = playerName;
        this.userName = userName;
        this.className = className;
    }
}
