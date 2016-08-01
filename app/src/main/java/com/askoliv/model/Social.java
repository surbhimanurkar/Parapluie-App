package com.askoliv.model;

/**
 * Created by surbhimanurkar on 20-06-2016.
 */
public class Social {
    int shares;
    int loves;

    public Social(int shares, int loves) {
        this.shares = shares;
        this.loves = loves;
    }

    public Social() {
    }

    public int getShares() {
        return shares;
    }

    public void setShares(int shares) {
        this.shares = shares;
    }

    public int getLoves() {
        return loves;
    }

    public void setLoves(int loves) {
        this.loves = loves;
    }
}
