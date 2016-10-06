package in.parapluie.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.batch.android.Batch;

/**
 * Created by surbhimanurkar on 02-08-2016.
 */
public class BaseActivity extends AppCompatActivity {

    protected static boolean isVisible = false;

    protected static boolean isVisible() {
        return isVisible;
    }

    private static void setIsVisible(boolean isVisible) {
        BaseActivity.isVisible = isVisible;
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        Batch.onStart(this);
    }

    @Override
    protected void onStop()
    {
        Batch.onStop(this);

        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        Batch.onDestroy(this);

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        setIsVisible(true);
        super.onResume();
    }

    @Override
    protected void onPause() {
        setIsVisible(false);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        Batch.onNewIntent(this, intent);

        super.onNewIntent(intent);
    }
}

