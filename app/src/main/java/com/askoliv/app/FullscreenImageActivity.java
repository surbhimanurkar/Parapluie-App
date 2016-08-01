package com.askoliv.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.askoliv.utils.Constants;
import com.bumptech.glide.Glide;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenImageActivity extends AppCompatActivity {

    private ImageView mFullscreenImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen_image);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null)
            actionBar.hide();
        mFullscreenImage = (ImageView) findViewById(R.id.fullscreen_image);
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_IMAGE, Context.MODE_PRIVATE);
        String imageURL = sharedPreferences.getString(Constants.IMAGE_PREF_URL, null);
        Glide.with(this).load(imageURL).fitCenter().into(mFullscreenImage);


    }



}
