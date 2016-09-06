package com.askoliv.model;

import java.util.Map;

/**
 * Created by surbhimanurkar on 20-06-2016.
 */
public class Social {
    Map<String,Integer> shares;
    Map<String,Boolean> loves;

    public Social(Map<String,Integer> shares, Map<String,Boolean> loves) {
        this.shares = shares;
        this.loves = loves;
    }

    public Social() {
    }

    public Map<String,Integer> getShares() {
        return shares;
    }

    public void setShares(Map<String,Integer> shares) {
        this.shares = shares;
    }

    public Map<String,Boolean> getLoves() {
        return loves;
    }

    public void setLoves(Map<String,Boolean> loves) {
        this.loves = loves;
    }
}
