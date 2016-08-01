package com.askoliv.utils;

/**
 * Created by surbhimanurkar on 10-03-2016.
 */
public class Constants {

    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int SENDER_USER = 0;
    public static final int SENDER_OLIV = 1;
    public static final String IMAGE_NAME_PREFIX = "OLV-";
    public static final String LOCAL_IMAGE_PATH = "Oliv/Oliv Images/";

    public static final String F_URL = "https://askoliv.firebaseio.com";
    public static final String F_NODE_CHAT = "chat";
    public static final String F_NODE_HELP_QUESTIONS = "help_questions";
    public static final String F_NODE_USER = "user";
    public static final String F_KEY_USER_PROVIDER = "provider";
    public static final String F_KEY_USER_USERNAME = "username";
    public static final String F_KEY_USER_RESOLVED = "resolved";
    public static final String F_KEY_USER_STATUS = "status";
    public static final String F_KEY_USER_ACTIVITY = "activity";
    public static final String F_KEY_USER_LOVES = "loves";
    public static final String F_VALUE_USER__OPEN = "OPEN";
    public static final String TIME = "time";
    public static final String HELP_QUESTIONS_RELEVANCE = "relevance";

    //Firebase Stories Constants
    public static final String F_NODE_STORIES = "stories";
    public static final String F_KEY_STORIES_INVERSETIMEPUBLISHED = "timePublishedInverse";
    public static final String F_KEY_STORIES_SOCIAL = "social";
    public static final String F_KEY_STORIES_LOVES = "loves";
    public static final int NUM_STORIES_LOADED = 10;

    public static final String SHARED_PREFERENCE_LOGIN = "loginPref";
    public static final String LOGIN_PREF_LOGOUT = "logout";
    public static final String APP_NAME_FONT = "fonts/GrandHotel-Regular.ttf";

    public static final int REQUEST_CAMERA = 0;
    public static final int REQUEST_GALLERY = 1;

    public static final String SHARED_PREFERENCE_IMAGE = "imagePref";
    public static final String IMAGE_PREF_URL = "imageURL";
}
