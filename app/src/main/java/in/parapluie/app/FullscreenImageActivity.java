package in.parapluie.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.widget.ImageView;

import in.parapluie.utils.Constants;
import com.bumptech.glide.Glide;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenImageActivity extends BaseActivity {

    private ImageView mFullscreenImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen_image);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null)
            actionBar.hide();
        mFullscreenImage = (ImageView) findViewById(R.id.fullscreen_image);
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_IMAGE, MODE_PRIVATE);
        String imageURL = sharedPreferences.getString(Constants.IMAGE_URL, null);
        Glide.with(this).load(imageURL).fitCenter().into(mFullscreenImage);

    }



}
