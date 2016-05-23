package com.askoliv.model;

import java.util.Map;

/**
 * Created by surbhimanurkar on 19-05-2016.
 * Single story can be represented by an object of this class
 */
public class Story {
    private String title;
    private String author;
    private String category;
    private String subtitle;
    private String textInitial;
    private String textEnd;
    private Map<String, Carousel> carousel;

    public Story(){

    }

    public Story(String title, String author, String category, String subtitle, String textInitial, String textEnd, Map<String, Carousel> carousel) {
        this.title = title;
        this.author = author;
        this.category = category;
        this.subtitle = subtitle;
        this.textInitial = textInitial;
        this.textEnd = textEnd;
        this.carousel = carousel;
    }

    public Map<String, Carousel> getCarousel() {
        return carousel;
    }

    public void setCarousel(Map<String, Carousel> carousel) {
        this.carousel = carousel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getTextInitial() {
        return textInitial;
    }

    public void setTextInitial(String textInitial) {
        this.textInitial = textInitial;
    }

    public String getTextEnd() {
        return textEnd;
    }

    public void setTextEnd(String textEnd) {
        this.textEnd = textEnd;
    }
}
