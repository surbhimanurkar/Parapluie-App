package in.parapluie.app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import in.parapluie.utils.Constants;
import in.parapluie.utils.FirebaseUtils;
import in.parapluie.utils.Global;
import com.bumptech.glide.Glide;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ImageConfirmationActivity extends AppCompatActivity {


    private Uri mImageURL;
    private EditText mEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_confirmation);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
            actionBar.setHomeAsUpIndicator(ContextCompat.getDrawable(this,R.drawable.back));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.image_confirmation_title));
        }

        mImageURL = getIntent().getParcelableExtra(Constants.IMAGE_URL);
        ImageView imageView = (ImageView) findViewById(R.id.image_for_sending);
        Glide.with(this).load(mImageURL).into(imageView);

        Button confirmButton = (Button) findViewById(R.id.image_confirm);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendImage();
                goBacktoMainActivity();
            }
        });

        mEditText = (EditText) findViewById(R.id.sendImageText);

        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    sendImage();
                }
                return true;
            }
        });

    }

    @Override
    public void onBackPressed() {
        goBacktoMainActivity();
    }

    private void goBacktoMainActivity(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            goBacktoMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendImage(){
        String caption = mEditText.getText().toString().trim();
        FirebaseUtils.getInstance().saveImagewithCaption(this, Global.imageBitmap,caption);
    }

}
