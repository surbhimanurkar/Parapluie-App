package com.askoliv.utils;

/**
 * Created by surbhimanurkar on 10-03-2016.
 */
public class Constants {

    //Firebase Analytics Constants
    public static final String TAB_STORIES = "STORIES";
    public static final String TAB_CHAT = "CHAT";
    public static final String TAB_PROFILE = "PROFILE";


    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int SENDER_USER = 0;
    public static final int SENDER_OLIV = 1;
    public static final int SNAPSHOTS = 3;
    public static final String IMAGE_NAME_PREFIX = "PP-";
    public static final String LOCAL_IMAGE_PATH = "Parapluie/Parapluie Images/";
    public static final String SHARE_IMAGE_PATH = "Parapluie/Snapshots";

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
    public static final String F_KEY_STORIES_TIMEPUBLISHED = "timePublished";
    public static final String F_KEY_STORIES_SOCIAL = "social";
    public static final String F_KEY_STORIES_LOVES = "loves";
    public static final int NUM_STORIES_LOADED = 10;

    public static final String SHARED_PREFERENCE_LOGIN = "loginPref";
    public static final String SHARED_PREFERENCE_HISTORY = "historyPref";
    public static final String SHARED_PREFERENCE_STORY = "storyPref";
    public static final String LOGIN_PREF_LOGOUT = "logout";
    public static final String HISTORY_PREF_SELECTED_TAB = "selectedTab";
    public static final String HISTORY_PREF_CURRENT_PHOTO_PATH = "currentPhotoPath";
    public static final String STORY_PREF_TITLE = "storyTitle";
    public static final String STORY_PREF_KEY = "storyKey";
    public static final String STORY_PREF_SHARE_TEXT = "storyShareText";
    public static final String STORY_PREF_SNAPSHOT = "storySnapshot";
    public static final String APP_NAME_FONT = "fonts/GrandHotel-Regular.ttf";

    public static final int REQUEST_CAMERA = 0;
    public static final int REQUEST_GALLERY = 1;

    public static final String SHARED_PREFERENCE_IMAGE = "imagePref";
    public static final String IMAGE_PREF_URL = "imageURL";

    //Permissions
    public static final int PERMISSIONS_REQUEST_STORAGE_IMAGE = 2;
    public static final int PERMISSIONS_REQUEST_STORAGE_SHARE = 1;
}
