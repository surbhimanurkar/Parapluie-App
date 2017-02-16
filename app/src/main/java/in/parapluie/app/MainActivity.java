package in.parapluie.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import in.parapluie.adapters.MessageListAdapter;
import in.parapluie.utils.AndroidUtils;
import in.parapluie.utils.Constants;
import in.parapluie.utils.CustomViewPager;
import in.parapluie.utils.FirebaseUtils;
import in.parapluie.utils.Global;
import in.parapluie.utils.OnboardingUtils;
import in.parapluie.utils.TitleFont;
import in.parapluie.utils.UsageAnalytics;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.Stack;

public class MainActivity extends BaseActivity {

    /**
     * The {@link CustomViewPager} that will host the section contents.
     */
    private static final String TAG = MainActivity.class.getSimpleName();
    private CustomViewPager mViewPager;
    private int secondaryColor;
    private int primaryColor;
    private int baseColor;
    private TabLayout mTabLayout;

    /*
     *  Firebase Variables
     */
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    /*
     * Tracking history
     */
    private Stack<Integer> mTabHistory = new Stack<>();
    private boolean doubleBackToExitPressedOnce = false;
    private SharedPreferences mHistorySharedPreferences;

    private UsageAnalytics mUsageAnalytics;
    private AndroidUtils mAndroidUtils = new AndroidUtils();

    public static final String PREF_USER_FIRST_TIME = "user_first_time";
    boolean isUserFirstTime;


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client2;
    private boolean isKeyboardUp;
    private boolean isKeyboardDown = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Initializing Analytics
        //Obtain the FirebaseAnalytics instance.
        mUsageAnalytics = new UsageAnalytics();
        mUsageAnalytics.initTracker(this);

        Log.d(TAG, "Activity Launched");

        //isUserFirstTime = Boolean.valueOf(OnboardingUtils.readSharedSetting(MainActivity.this, PREF_USER_FIRST_TIME, "true"));
        isUserFirstTime = true;

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        /*if (mFirebaseUser == null) {
            Log.d("isUserFirstTime", ""+ isUserFirstTime);
            if (isUserFirstTime) {
                startActivity(introIntent);
            } else {

            }
            //redirectUserToLogin();
        }*/
        if (mFirebaseUser == null) {
            Intent introIntent = new Intent(MainActivity.this, PagerActivity.class);
            introIntent.putExtra(PREF_USER_FIRST_TIME, isUserFirstTime);
            startActivity(introIntent);
            finish();
        }

        //Populating Config variables
        FirebaseUtils.getInstance().populatingConfigVariables();

        //Setting unread Chat message
        FirebaseUtils.getInstance().setUnreadChatMessages(this);

        //Initializing Resources
        Resources resources = getResources();
        primaryColor = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
        secondaryColor = ContextCompat.getColor(getApplicationContext(), R.color.colorSecondary);
        baseColor = ContextCompat.getColor(getApplicationContext(), R.color.colorBase);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHistorySharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_HISTORY, Context.MODE_PRIVATE);

        //Setting UI elements
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbar.setLogo(R.drawable.high_res_logo);
        setSupportActionBar(toolbar);
        //if(getSupportActionBar()!=null){
          //  getSupportActionBar().setDisplayShowHomeEnabled(true);
            //getSupportActionBar().setDisplayShowTitleEnabled(true);
        //}

        //Finding title in the toolbar
        TextView title;
        View child = null;
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            child = toolbar.getChildAt(i);
            if (child instanceof TextView) {
                title = (TextView) child;
                title.setTypeface(TitleFont.getInstance(this).getTypeFace());
                title.setAllCaps(true);
                title.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.title_text_size));
                //title.setTextScaleX(0.8f);
                title.setTextColor(baseColor);
                break;
            }
        }

        //Adding overflow icon
        Drawable overflowIcon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_overflow);
        overflowIcon.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN);
        toolbar.setOverflowIcon(overflowIcon);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        /*
      The {@link PagerAdapter} that will provide
      fragments for each of the sections. We use a
      {@link FragmentPagerAdapter} derivative, which will keep every
      loaded fragment in memory. If this becomes too memory intensive, it
      may be best to switch to a
      {@link FragmentStatePagerAdapter}.
     */
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (CustomViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPagingEnabled(false);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setSelectedTabIndicatorColor(primaryColor);
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(mSectionsPagerAdapter.getTabView(i));
            }
            Log.d(TAG, "Setting custom view for Tab:" + i);
        }
        mViewPager.setCurrentItem(Constants.FRAGMENT_POSITION_CHAT);

        //Changing icon color on selection
        mTabLayout.addOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {

                    ImageView iconImage;
                    TextView tabTitle;

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);

                        //Usage Analytics
                        if (tab.getPosition() == 0)
                            mUsageAnalytics.trackTab(Constants.TAB_STORIES);
                        else if (tab.getPosition() == 1)
                            mUsageAnalytics.trackTab(Constants.TAB_CHAT);
                        else
                            mUsageAnalytics.trackTab(Constants.TAB_PROFILE);

                        //Tracking History
                        SharedPreferences historySharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_HISTORY, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = historySharedPreferences.edit();
                        editor.putInt(Constants.HISTORY_PREF_SELECTED_TAB, tab.getPosition());
                        Log.d(TAG, "Selected Tab:" + tab.getPosition());
                        editor.apply();
                        if (mTabHistory.empty()){
                            mTabHistory.push(1);
                        }


                        if (mTabHistory.contains(tab.getPosition())) {
                            mTabHistory.remove(mTabHistory.indexOf(tab.getPosition()));
                            mTabHistory.push(tab.getPosition());
                            Log.d(TAG, "Selected TabHistory:" + mTabHistory);
                        } else {
                            mTabHistory.push(tab.getPosition());
                        }
                        //Adjusting UI
                        if (tab.getCustomView() != null) {
                            iconImage = (ImageView) tab.getCustomView().findViewById(R.id.tab_icon);
                            iconImage.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN);
                            tabTitle = (TextView) tab.getCustomView().findViewById(R.id.tab_title);
                            tabTitle.setTextColor(primaryColor);
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        if (tab.getCustomView() != null) {
                            iconImage = (ImageView) tab.getCustomView().findViewById(R.id.tab_icon);
                            iconImage.setColorFilter(secondaryColor, PorterDuff.Mode.SRC_IN);
                            tabTitle = (TextView) tab.getCustomView().findViewById(R.id.tab_title);
                            tabTitle.setTextColor(secondaryColor);
                        }
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        super.onTabReselected(tab);
                    }
                }
        );

        //Selecting the correct tab
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_HISTORY, Context.MODE_PRIVATE);
        int selectTabPosition = sharedPreferences.getInt(Constants.HISTORY_PREF_SELECTED_TAB, 0);
        Log.d(TAG, "Select Tab Position:" + selectTabPosition);
        mViewPager.setCurrentItem(selectTabPosition);

        Intent deepLinkIntent = getIntent();
        Uri data = deepLinkIntent.getData();
        Log.d(TAG, "onCreate parseIntent:Data:" + data);
        if (data != null && data.getQueryParameter(Constants.DEEP_LINK_FRAGMENT) != null) {
            int fragment = Integer.parseInt(data.getQueryParameter(Constants.DEEP_LINK_FRAGMENT));
            if (fragment == Constants.FRAGMENT_POSITION_STORIES) {
                mViewPager.setCurrentItem(Constants.FRAGMENT_POSITION_STORIES);
                String storyKey = data.getQueryParameter(Constants.DEEP_LINK_STORY);
                SharedPreferences.Editor editor = mHistorySharedPreferences.edit();
                editor.putString(Constants.HISTORY_PREF_STORY_KEY, storyKey);
                editor.apply();
            } else {
                mViewPager.setCurrentItem(fragment);
                //There is no selected story
                SharedPreferences.Editor editor = mHistorySharedPreferences.edit();
                editor.remove(Constants.HISTORY_PREF_STORY_KEY);
                editor.apply();
            }
        } else {
            //There is no selected story
            SharedPreferences.Editor editor = mHistorySharedPreferences.edit();
            editor.remove(Constants.HISTORY_PREF_STORY_KEY);
            editor.apply();
        }

        //Listener on softkeyboard that hides tabs when softkeyboard is visible and shows tabs when softkeyboard is gone
        final View rootView = findViewById(R.id.main_content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                int navigationBarHeight = 0;
                int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    navigationBarHeight = getResources().getDimensionPixelSize(resourceId);
                }

                // status bar height
                int statusBarHeight = 0;
                resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    statusBarHeight = getResources().getDimensionPixelSize(resourceId);
                }

                // display window size for the app layout
                Rect rect = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);

                // screen height - (user app height + status + nav) ..... if non-zero, then there is a soft keyboard
                int keyboardHeight = rootView.getRootView().getHeight() - (statusBarHeight + navigationBarHeight + rect.height());
                /*InputMethodManager imm = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);

                if (imm.isAcceptingText()) {
                    Log.d("keyboard", "keyboard UP");
                    mTabLayout.setVisibility(View.GONE);
                } else {
                    Log.d("keyboard", "keyboard DOWN");
                    mTabLayout.setVisibility(View.VISIBLE);
                }*/

                if (keyboardHeight <= 0) {
                    /*Log.d("1", "" + rootView.getRootView().getHeight());
                    Log.d("1", "" + (statusBarHeight + navigationBarHeight + rect.height()));*/
                    Log.d("keyboard", "keyboard DOWN");
                    mTabLayout.setVisibility(View.VISIBLE);
                    if (chatFragment != null && isKeyboardUp) {
                        isKeyboardUp = false;
                        isKeyboardDown = true;
                        chatFragment.scrollToBottom();
                        Log.d("keyboard", "keyboard DOWN!!!");
                    }
                } else {
                    Log.d("2", "" + rootView.getRootView().getHeight());
                    Log.d("2", "" + statusBarHeight + navigationBarHeight + rect.height());
                    if (chatFragment != null && isKeyboardDown) {
                        isKeyboardDown = false;
                        isKeyboardUp = true;
                        chatFragment.scrollToBottom();
                        Log.d("keyboard", "keyboard UP!!!");
                    }
                    Log.d("keyboard", "keyboard UP");
                    mTabLayout.setVisibility(View.GONE);
                }
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2 = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }
    @Override
    public void onPause(){
        SharedPreferences historySharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_HISTORY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = historySharedPreferences.edit();
        editor.putInt(Constants.HISTORY_PREF_SELECTED_TAB, 1);
        Log.d(TAG, "Selected Tab Set to Home on Pause");
        editor.apply();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mTabHistory.size() > 1) {
            mTabHistory.pop();
            mViewPager.setCurrentItem(mTabHistory.lastElement());
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://in.parapluie.app/http/host/path")
        );
        AppIndex.AppIndexApi.start(client2, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://in.parapluie.app/http/host/path")
        );
        AppIndex.AppIndexApi.end(client2, viewAction);
        client2.disconnect();
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */

    public ChatFragment chatFragment = null;
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            if (position == 0) {
                return new StoriesFragment();
            } else if (position == 1) {
                chatFragment = new ChatFragment();
                return chatFragment;
            } else {
                return new ProfileFragment();
            }
        }

        /**
         * Returns custom view with icon and text for different tabs
         *
         * @param position
         * @return View
         */
        public View getTabView(int position) {
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.custom_tab, null);
            TextView title = (TextView) view.findViewById(R.id.tab_title);
            ImageView icon = (ImageView) view.findViewById(R.id.tab_icon);
            LinearLayout tabBadge = (LinearLayout) view.findViewById(R.id.tab_badge);
            Drawable iconDrawable;
            switch (position) {
                case 0:
                    title.setText(getResources().getString(R.string.tab_bar_stories_title));
                    title.setTextColor(primaryColor);
                    iconDrawable = (ContextCompat.getDrawable(getApplicationContext(), R.drawable.news));
                    iconDrawable.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN);
                    icon.setImageDrawable(iconDrawable);
                    tabBadge.setVisibility(View.GONE);
                    break;
                case 1:
                    title.setText(getResources().getString(R.string.tab_bar_chat_title));
                    iconDrawable = (ContextCompat.getDrawable(getApplicationContext(), R.drawable.chat));
                    iconDrawable.setColorFilter(secondaryColor, PorterDuff.Mode.SRC_IN);
                    icon.setImageDrawable(iconDrawable);
                    if (FirebaseUtils.getInstance().getUnreadChatMessages() > 0) {
                        tabBadge.setVisibility(View.VISIBLE);
                        TextView tabBadgeText = (TextView) view.findViewById(R.id.tab_badge_text);
                        tabBadgeText.setText("" + FirebaseUtils.getInstance().getUnreadChatMessages());
                    } else {
                        tabBadge.setVisibility(View.GONE);
                    }
                    break;
                case 2:
                    title.setText(getResources().getString(R.string.tab_bar_profile_title));
                    iconDrawable = (ContextCompat.getDrawable(getApplicationContext(), R.drawable.user));
                    iconDrawable.setColorFilter(secondaryColor, PorterDuff.Mode.SRC_IN);
                    icon.setImageDrawable(iconDrawable);
                    tabBadge.setVisibility(View.GONE);
                    break;
            }

            return view;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }


    }

    public void setUnreadChatMessagesBadge(long unreadChatMessages) {
        if (mTabLayout != null && mTabLayout.getTabAt(1) != null && (mTabLayout.getTabAt(1).getCustomView()) != null) {
            if (unreadChatMessages > 0) {
                View customView = (mTabLayout.getTabAt(1).getCustomView());
                TextView tabBadgeText = (TextView) customView.findViewById(R.id.tab_badge_text);
                tabBadgeText.setText(unreadChatMessages + "");
                LinearLayout tabBadge = (LinearLayout) customView.findViewById(R.id.tab_badge);
                tabBadge.setVisibility(View.VISIBLE);
            } else if (unreadChatMessages == 0) {
                View customView = (mTabLayout.getTabAt(1).getCustomView());
                LinearLayout tabBadge = (LinearLayout) customView.findViewById(R.id.tab_badge);
                tabBadge.setVisibility(View.GONE);
            }
        }
    }

    private void logout() {
        SharedPreferences loginSharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_LOGIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = loginSharedPreferences.edit();
        editor.putBoolean(Constants.LOGIN_PREF_LOGOUT, true);
        editor.apply();
        Log.d(TAG, "Putting Boolean");
        Intent logoutIntent = new Intent(this, LoginActivity.class);
        startActivity(logoutIntent);
        finish();
    }

    public void redirectUserToLogin() {
        Log.d(TAG, "Redirecting to login");
        /*SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_LOGIN,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.LOGIN_PREF_LOGOUT, false);
        editor.commit();*/
        Intent intent = new Intent(this, LoginActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        switch (requestCode) {
            case Constants.PERMISSIONS_REQUEST_STORAGE_SHARE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_STORY, Context.MODE_PRIVATE);
                    String storyShareText = sharedPreferences.getString(Constants.STORY_PREF_SHARE_TEXT, null);
                    String storyTitle = sharedPreferences.getString(Constants.STORY_PREF_TITLE, null);
                    String storyKey = sharedPreferences.getString(Constants.STORY_PREF_KEY, null);
                    //String storySnapshot = sharedPreferences.getString(Constants.STORY_PREF_SNAPSHOT, null);
                    boolean success = mAndroidUtils.shareStory(this, storyShareText, Global.currentStorySnapshot, storyKey);
                    if (success) {
                        FirebaseUtils.getInstance().increaseShareCount(storyKey);
                        mUsageAnalytics.trackShareEvent(storyKey, storyTitle);
                    } else
                        Toast.makeText(this, getResources().getString(R.string.story_share_error_generic), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.story_share_error_permission), Toast.LENGTH_SHORT).show();
                }
                break;
            case Constants.PERMISSIONS_REQUEST_STORAGE_IMAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mAndroidUtils.openImageActivity(this);
                } else {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.image_error_permission), Toast.LENGTH_SHORT).show();
                }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        Bitmap uploadedImageBitmap;
        Log.d(TAG, "onActivityResult: Result Code:" + resultCode + " Request Code:" + requestCode + " Intent:" + intent);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constants.REQUEST_CAMERA:
                    Log.d(TAG, "Got image from the Camera");
                    //uploadedImageBitmap = (Bitmap) intent.getExtras().get("data");
                    String currentPhotoPath = mHistorySharedPreferences.getString(Constants.HISTORY_PREF_CURRENT_PHOTO_PATH, null);
                    uploadedImageBitmap = mAndroidUtils.getPicture(this, currentPhotoPath);
                    FirebaseUtils.getInstance().saveImage(this, uploadedImageBitmap);
                    mAndroidUtils.galleryAddPic(this, currentPhotoPath);
                    break;
                case Constants.REQUEST_GALLERY:
                    Log.d(TAG, "Got image from the gallery");
                    try {
                        Intent imageConfirmationIntent = new Intent(this, ImageConfirmationActivity.class);
                        Global.imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), intent.getData());
                        imageConfirmationIntent.putExtra(Constants.IMAGE_URL, intent.getData());
                        startActivity(imageConfirmationIntent);
                        finish();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case Constants.REQUEST_SHARE:
                    Log.d(TAG, "Shared successfully");
//                    String key = intent.getExtras().getString("key");
  //                  Log.d("onActivityResult","story Key:" + key);
                    //FirebaseUtils.getInstance().increaseShareCount(key);
                    break;
            }
        }
    }
}
