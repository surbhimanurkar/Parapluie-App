package com.askoliv.utils;

import android.content.Context;
import android.os.Bundle;

import com.askoliv.model.Story;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by surbhimanurkar on 09-08-2016.
 */
public class UsageAnalytics {

    private FirebaseAnalytics analytics;

    private boolean isActive() {
        //return BuildConfig.ENABLE_USAGE_ANALYTICS;
        return true;
    }

    public void initTracker(Context context) {
        analytics = FirebaseAnalytics.getInstance(context);
    }

    public void trackTab(String tab) {
        if (isActive() && analytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, tab);
            analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }
    }

    public void trackStory(Story story) {
        if (isActive() && analytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, story.getKey());
            bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, story.getCategory());
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, story.getTitle());
            analytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
        }
    }

    public void trackEvent(String action) {
        if (isActive() && analytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, action);
            analytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
        }
    }

    public void trackLogin(String uid, String username) {
        if (isActive() && analytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, uid);
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, username);
            analytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
        }
    }

    public void trackTipsCarouselEvent(String tipName,int tipNumber, Story story) {
        if (isActive() && analytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, tipNumber + "");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, tipName);
            bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, story.getKey());
            analytics.logEvent("TipsCarousel", bundle);
        }
    }

    public void trackShareEvent(Story story) {
        if (isActive() && analytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, story.getKey());
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, story.getTitle());
            analytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle);
        }
    }

    public void trackLikeEvent(Story story) {
        if (isActive() && analytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, story.getKey());
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, story.getTitle());
            analytics.logEvent("Like", bundle);
        }
    }

}
