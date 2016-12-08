package in.parapluie.utils;

/**
 * Created by surbhimanurkar on 10-03-2016.
 */
public class Constants {

    //Firebase Analytics Constants
    public static final String TAB_STORIES = "STORIES";
    public static final String TAB_CHAT = "CHAT";
    public static final String TAB_PROFILE = "PROFILE";
    public static final int FRAGMENT_POSITION_STORIES = 0;
    public static final int FRAGMENT_POSITION_CHAT = 1;
    public static final int FRAGMENT_POSITION_PROFILE = 2;

    //Dynamic links constants
    public static final String DEEP_LINK_LINK = "link";
    public static final String DEEP_LINK_PACKAGE = "apn";
    public static final String DEEP_LINK_FALLBACK = "afl";
    public static final String DEEP_LINK_MAIN = "main";
    public static final String DEEP_LINK_FRAGMENT = "fragment";
    public static final String DEEP_LINK_STORY = "story";

    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int SENDER_USER = 0;
    public static final int SENDER_PARAPLUIE = 1;
    public static final int SNAPSHOTS = 3;
    public static final String IMAGE_NAME_PREFIX = "PP-";
    public static final String LOCAL_IMAGE_PATH = "Parapluie/Parapluie Images/";
    public static final String SHARE_IMAGE_PATH = "Parapluie/Snapshots";

    public static final String F_URL = "https://co.in.parapluie.firebaseio.com";
    public static final String F_NODE_CHAT = "chat";
    public static final String F_NODE_QUERY = "query";
    public static final String F_NODE_HELP_QUESTIONS = "help_questions";
    public static final String F_NODE_USER = "user";
    public static final String F_NODE_USER_APP = "app";
    public static final String F_NODE_USER_FB = "facebook";
    public static final String F_KEY_USER_PROVIDER = "provider";
    public static final String F_KEY_USER_USERNAME = "username";
    public static final String F_KEY_USER_RESOLVED = "resolved";
    public static final String F_KEY_USER_ACTIVEQID = "activeQid";
    public static final String F_KEY_USER_GENDER = "gender";
    public static final String F_KEY_USER_AGE_RANGE = "ageRange";
    public static final String F_KEY_USER_EMAIL = "email";
    public static final String F_KEY_USER_STATUS = "status";
    public static final String F_KEY_USER_ACTIVITY = "activity";
    public static final String F_KEY_USER_LOVES = "loves";
    public static final String F_KEY_USER_SHARES = "shares";
    public static final String F_KEY_USER_UNREAD_CHAT_MESSAGES = "unreadChatMessages";
    public static final String F_VALUE_USER__OPEN = "OPEN";
    public static final String TIME = "time";
    public static final String HELP_QUESTIONS_RELEVANCE = "relevance";
    public static final String F_NODE_CONFIG = "config";

    //Firebase Stories Constants
    public static final String F_NODE_STORIES = "stories";
    public static final String F_KEY_STORIES_TIMEPUBLISHED = "timePublished";
    public static final String F_NODE_SOCIAL = "social";
    public static final String F_KEY_STORIES_LOVES = "loves";
    public static final String F_KEY_STORIES_SHARES = "shares";
    public static final String F_NODE_STORIES_PUBLISHED = "published";
    public static final int NUM_STORIES_LOADED = 100;

    public static final String SHARED_PREFERENCE_LOGIN = "loginPref";
    public static final String SHARED_PREFERENCE_HISTORY = "historyPref";
    public static final String SHARED_PREFERENCE_STORY = "storyPref";
    public static final String ONBOARDING_SETTING = "logout";
    public static final String LOGIN_PREF_LOGOUT = "logout";
    public static final String LOGIN_PREF_ISCHATALLOWED = "isChatAllowed";
    public static final String HISTORY_PREF_SELECTED_TAB = "selectedTab";
    public static final String HISTORY_PREF_CURRENT_PHOTO_PATH = "currentPhotoPath";
    public static final String STORY_PREF_TITLE = "storyTitle";
    public static final String STORY_PREF_KEY = "storyKey";
    public static final String STORY_PREF_SHARE_TEXT = "storyShareText";
    public static final String STORY_PREF_SNAPSHOT = "storySnapshot";
    public static final String HISTORY_PREF_STORY_KEY = "storyKey";
    public static final String APP_NAME_FONT = "fonts/GrandHotel-Regular.ttf";

    public static final int REQUEST_CAMERA = 0;
    public static final int REQUEST_GALLERY = 1;
    public static final int REQUEST_SHARE = 2;

    public static final String SHARED_PREFERENCE_IMAGE = "imagePref";
    public static final String IMAGE_URL = "imageURL";
    public static final String IMAGE_BITMAP = "imagebitmap";
    public static final String IMAGE_REQUEST_CODE = "requestCode";

    //Permissions
    public static final int PERMISSIONS_REQUEST_STORAGE_IMAGE = 2;
    public static final int PERMISSIONS_REQUEST_STORAGE_SHARE = 1;

    //FB profile variable names
    public static final String FB_PROFILE_GENDER = "gender";
    public static final String FB_PROFILE_AGE_RANGE = "age_range";
    public static final String FB_PROFILE_EMAIL = "email";
    public static final String FB_PROFILE_GENDER_MALE = "male";
    public static final String FB_PROFILE_GENDER_FEMALE = "female";

    //yet to categorize constants
    public static final String BROWSER_USER_AGENT = "Mozilla"; ///5.0 (Windows NT 5.1; rv:19.0) Gecko/20100101 Firefox/19.0
}
