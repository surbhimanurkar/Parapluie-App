package com.askoliv.oliv;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.askoliv.utils.Constants;
import com.firebase.client.AuthData;

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
     * The {@link ViewPager} that will host the section contents.
     */
    private static final String TAG = MainActivity.class.getSimpleName();
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Initializing Resources
        Resources resources = getResources();
        final int primaryColor = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
        final int secondaryColor = ContextCompat.getColor(getApplicationContext(),R.color.colorSecondary);
        final int whiteColor = ContextCompat.getColor(getApplicationContext(),R.color.colorWhite);

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
                break;
            }
        }

        //Styling title
        Typeface logoFont = Typeface.createFromAsset(getAssets(), Constants.APP_NAME_FONT);
        title.setTypeface(logoFont);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX,resources.getDimensionPixelSize(R.dimen.title_text_size));
        title.setTextScaleX(0.8f);

        //Adding overflow icon
        Drawable overflowIcon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_overflow);
        overflowIcon.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        toolbar.setOverflowIcon(overflowIcon);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setSelectedTabIndicatorColor(primaryColor);
        tabLayout.setupWithViewPager(mViewPager);

        //setting icons on tablayout
        for(int i = 0; i < tabLayout.getTabCount(); i++){
            Drawable iconDrawable;
            switch (i){
                case 0:
                    iconDrawable = (ContextCompat.getDrawable(getApplicationContext(),R.drawable.chat));
                    iconDrawable.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN);
                    tabLayout.getTabAt(0).setIcon(iconDrawable);
                case 1:
                    iconDrawable = (ContextCompat.getDrawable(getApplicationContext(),R.drawable.news));
                    iconDrawable.setColorFilter(secondaryColor, PorterDuff.Mode.SRC_IN);
                    tabLayout.getTabAt(1).setIcon(iconDrawable);
                case 2:
                    iconDrawable = (ContextCompat.getDrawable(getApplicationContext(),R.drawable.user));
                    iconDrawable.setColorFilter(secondaryColor, PorterDuff.Mode.SRC_IN);
                    tabLayout.getTabAt(2).setIcon(iconDrawable);
            }
        }

        //Changing icon color on selection
        tabLayout.setOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        tab.getIcon().setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        tab.getIcon().setColorFilter(secondaryColor, PorterDuff.Mode.SRC_IN);
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        super.onTabReselected(tab);
                    }
                }
        );


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
                return new ChatFragment();
            else
                return PlaceholderFragment.newInstance(position + 1);
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
