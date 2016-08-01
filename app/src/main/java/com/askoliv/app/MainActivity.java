package com.askoliv.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.askoliv.utils.Constants;
import com.askoliv.utils.CustomViewPager;
import com.askoliv.utils.TitleFont;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link CustomViewPager} that will host the section contents.
     */
    private static final String TAG = MainActivity.class.getSimpleName();
    private CustomViewPager mViewPager;
    private int secondaryColor;
    private int primaryColor;
    private int baseColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Initializing Resources
        Resources resources = getResources();
        primaryColor = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
        secondaryColor = ContextCompat.getColor(getApplicationContext(),R.color.colorSecondary);
        baseColor = ContextCompat.getColor(getApplicationContext(),R.color.colorBase);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Finding title in the toolbar
        TextView title = null;
        View child = null;
        for(int i=0; i < toolbar.getChildCount(); i++){
            child = toolbar.getChildAt(i);
            if(child instanceof TextView){
                title = (TextView) child;
                title.setTypeface(TitleFont.getInstance(this).getTypeFace());
                title.setAllCaps(true);
                title.setTextSize(TypedValue.COMPLEX_UNIT_PX,resources.getDimensionPixelSize(R.dimen.title_text_size));
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
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (CustomViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPagingEnabled(false);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setSelectedTabIndicatorColor(primaryColor);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(mSectionsPagerAdapter.getTabView(i));
            Log.d(TAG, "Setting custom view for Tab:" + i);
        }
        tabLayout.getTabAt(0).getCustomView().setSelected(true);

        //Changing icon color on selection
        tabLayout.setOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {

                    ImageView iconImage;
                    TextView tabTitle;
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        iconImage = (ImageView) tab.getCustomView().findViewById(R.id.tab_icon);
                        iconImage.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN);
                        tabTitle = (TextView) tab.getCustomView().findViewById(R.id.tab_title);
                        tabTitle.setTextColor(primaryColor);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        iconImage = (ImageView) tab.getCustomView().findViewById(R.id.tab_icon);
                        iconImage.setColorFilter(secondaryColor, PorterDuff.Mode.SRC_IN);
                        tabTitle = (TextView) tab.getCustomView().findViewById(R.id.tab_title);
                        tabTitle.setTextColor(secondaryColor);
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        super.onTabReselected(tab);
                    }
                }
        );

        //Listener on softkeyboard that hides tabs when softkeyboard is visible and shows tabs when softkeyboard is gone
        final View rootView = findViewById(R.id.main_content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();

                if (heightDiff > 100) {
                    Log.d("keyboard", "keyboard UP");
                    tabLayout.setVisibility(View.GONE);
                } else {
                    Log.d("keyboard", "keyboard DOWN");
                    tabLayout.setVisibility(View.VISIBLE);
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

        if(id == R.id.action_logout){
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
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
            // Return a PlaceholderFragment (defined as a static inner class below).
            if(position == 0)
                return new StoriesFragment();
            else if(position == 1)
                return new ChatFragment();
            else
                return PlaceholderFragment.newInstance(position + 1);
        }

        /**
         * Returns custom view with icon and text for different tabs
         * @param position
         * @return View
         */
        public View getTabView(int position) {
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.custom_tab, null);
            TextView title = (TextView) view.findViewById(R.id.tab_title);
            ImageView icon = (ImageView) view.findViewById(R.id.tab_icon);
            Drawable iconDrawable;
            switch (position){
                case 0:
                    title.setText(getResources().getString(R.string.tab_bar_stories_title));
                    title.setTextColor(primaryColor);
                    iconDrawable = (ContextCompat.getDrawable(getApplicationContext(),R.drawable.news));
                    iconDrawable.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN);
                    icon.setImageDrawable(iconDrawable);
                    break;
                case 1:
                    title.setText(getResources().getString(R.string.tab_bar_chat_title));
                    iconDrawable = (ContextCompat.getDrawable(getApplicationContext(),R.drawable.chat));
                    iconDrawable.setColorFilter(secondaryColor, PorterDuff.Mode.SRC_IN);
                    icon.setImageDrawable(iconDrawable);
                    break;
                case 2:
                    title.setText(getResources().getString(R.string.tab_bar_profile_title));
                    iconDrawable = (ContextCompat.getDrawable(getApplicationContext(),R.drawable.user));
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

    private void logout(){
        SharedPreferences loginSharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_LOGIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = loginSharedPreferences.edit();
        editor.putBoolean(Constants.LOGIN_PREF_LOGOUT, true);
        editor.commit();
        Log.d(TAG, "Putting Boolean");
        Intent logoutIntent = new Intent(this, LoginActivity.class);
        startActivity(logoutIntent);
        finish();
    }

}
