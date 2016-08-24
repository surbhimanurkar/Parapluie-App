package com.askoliv.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.askoliv.utils.AndroidUtils;
import com.askoliv.utils.Constants;
import com.askoliv.utils.CustomViewPager;
import com.askoliv.utils.FirebaseUtils;
import com.askoliv.utils.TitleFont;
import com.askoliv.utils.UsageAnalytics;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

    private UsageAnalytics mUsageAnalytics;
    private AndroidUtils mAndroidUtils = new AndroidUtils();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Initializing Analytics
        //Obtain the FirebaseAnalytics instance.
        mUsageAnalytics = new UsageAnalytics();
        mUsageAnalytics.initTracker(this);

        Log.d(TAG, "Activity Launched");

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            redirectUserToLogin();
        }

        //Initializing Resources
        Resources resources = getResources();
        primaryColor = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
        secondaryColor = ContextCompat.getColor(getApplicationContext(), R.color.colorSecondary);
        baseColor = ContextCompat.getColor(getApplicationContext(), R.color.colorBase);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Setting UI elements
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        //Changing icon color on selection
        mTabLayout.addOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {

                    ImageView iconImage;
                    TextView tabTitle;

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);

                        //Usage Analytics
                        if(tab.getPosition()==0)
                            mUsageAnalytics.trackTab(Constants.TAB_STORIES);
                        else if(tab.getPosition()==1)
                            mUsageAnalytics.trackTab(Constants.TAB_CHAT);
                        else
                            mUsageAnalytics.trackTab(Constants.TAB_PROFILE);

                        //Tracking History
                        SharedPreferences historySharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_HISTORY, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = historySharedPreferences.edit();
                        editor.putInt(Constants.HISTORY_PREF_SELECTED_TAB, tab.getPosition());
                        Log.d(TAG, "Selected Tab:" + tab.getPosition());
                        editor.apply();
                        if (mTabHistory.empty())
                            mTabHistory.push(0);

                        if (mTabHistory.contains(tab.getPosition())) {
                            mTabHistory.remove(mTabHistory.indexOf(tab.getPosition()));
                            mTabHistory.push(tab.getPosition());
                        } else {
                            mTabHistory.push(tab.getPosition());
                        }

                        //Adjusting UI
                        if(tab.getCustomView()!=null){
                            iconImage = (ImageView) tab.getCustomView().findViewById(R.id.tab_icon);
                            iconImage.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN);
                            tabTitle = (TextView) tab.getCustomView().findViewById(R.id.tab_title);
                            tabTitle.setTextColor(primaryColor);
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        if(tab.getCustomView()!=null){
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

                if (keyboardHeight <= 0) {
                    Log.d("keyboard", "keyboard DOWN");
                    mTabLayout.setVisibility(View.VISIBLE);
                } else {
                    Log.d("keyboard", "keyboard UP");
                    mTabLayout.setVisibility(View.GONE);
                }
            }
        });

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



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
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
                return new ChatFragment();
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
            Drawable iconDrawable;
            switch (position) {
                case 0:
                    title.setText(getResources().getString(R.string.tab_bar_stories_title));
                    title.setTextColor(primaryColor);
                    iconDrawable = (ContextCompat.getDrawable(getApplicationContext(), R.drawable.news));
                    iconDrawable.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN);
                    icon.setImageDrawable(iconDrawable);
                    break;
                case 1:
                    title.setText(getResources().getString(R.string.tab_bar_chat_title));
                    iconDrawable = (ContextCompat.getDrawable(getApplicationContext(), R.drawable.chat));
                    iconDrawable.setColorFilter(secondaryColor, PorterDuff.Mode.SRC_IN);
                    icon.setImageDrawable(iconDrawable);
                    break;
                case 2:
                    title.setText(getResources().getString(R.string.tab_bar_profile_title));
                    iconDrawable = (ContextCompat.getDrawable(getApplicationContext(), R.drawable.user));
                    iconDrawable.setColorFilter(secondaryColor, PorterDuff.Mode.SRC_IN);
                    icon.setImageDrawable(iconDrawable);
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
        Log.d(TAG,"onRequestPermissionsResult");
        switch (requestCode) {
            case Constants.PERMISSIONS_REQUEST_STORAGE_SHARE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_STORY, Context.MODE_PRIVATE);
                    String storyShareText = sharedPreferences.getString(Constants.STORY_PREF_SHARE_TEXT, null);
                    String storyTitle = sharedPreferences.getString(Constants.STORY_PREF_TITLE, null);
                    String storyKey = sharedPreferences.getString(Constants.STORY_PREF_KEY, null);
                    String storySnapshot = sharedPreferences.getString(Constants.STORY_PREF_SNAPSHOT, null);
                    mAndroidUtils.shareStory(this,storyShareText,storySnapshot);
                    mUsageAnalytics.trackShareEvent(storyKey,storyTitle);
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied! Can't share story!", Toast.LENGTH_SHORT).show();
                }
                break;
            case Constants.PERMISSIONS_REQUEST_STORAGE_IMAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mAndroidUtils.openImageActivity(this);
                }else{
                    Toast.makeText(MainActivity.this, "Permission Denied! Can't send image!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        Bitmap uploadedImageBitmap;
        Log.d(TAG, "onActivityResult: Result Code:" + resultCode + " Request Code:" + requestCode + " Intent:" + intent);

        if (resultCode == RESULT_OK)
        {
            switch(requestCode){
                case Constants.REQUEST_CAMERA:
                    Log.d(TAG, "Got image from the Camera");
                    //uploadedImageBitmap = (Bitmap) intent.getExtras().get("data");
                    SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_HISTORY, Context.MODE_PRIVATE);
                    String currentPhotoPath = sharedPreferences.getString(Constants.HISTORY_PREF_CURRENT_PHOTO_PATH, null);
                    uploadedImageBitmap = mAndroidUtils.getPicture(this,currentPhotoPath);
                    FirebaseUtils.getInstance().saveImage(this,uploadedImageBitmap, requestCode);
                    mAndroidUtils.galleryAddPic(this,currentPhotoPath);
                    break;
                case Constants.REQUEST_GALLERY:
                    Log.d(TAG, "Got image from the gallery");
                    try {
                        uploadedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), intent.getData());
                        FirebaseUtils.getInstance().saveImage(this,uploadedImageBitmap, requestCode);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }
}
