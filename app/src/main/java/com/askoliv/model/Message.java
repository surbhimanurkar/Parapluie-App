package com.askoliv.model;

import java.util.Date;

/**
 * Created by surbhimanurkar on 03-03-2016.
 * Defines message received or sent
 */
public class Message {


    private String message;
    private int author;
    private Object time;

    private Message(){

    }

    public Message(String message, int author, Object time) {
        this.message = message;
        this.author = author;
        this.time = time;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getAuthor() {
        return author;
    }

    public void setAuthor(int author) {
        this.author = author;
    }

    public Object getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

}
