package com.askoliv.model;

/**
 * Created by surbhimanurkar on 28-09-2016.
 */
public class Config {
    private boolean active;
    private String inactiveMessage;


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getInactiveMessage() {
        return inactiveMessage;
    }

    public void setInactiveMessage(String inactiveMessage) {
        this.inactiveMessage = inactiveMessage;
    }
}
