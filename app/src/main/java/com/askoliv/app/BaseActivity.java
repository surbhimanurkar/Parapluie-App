package com.askoliv.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.batch.android.Batch;

/**
 * Created by surbhimanurkar on 02-08-2016.
 */
public class BaseActivity extends AppCompatActivity {

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
    protected void onNewIntent(Intent intent)
    {
        Batch.onNewIntent(this, intent);

        super.onNewIntent(intent);
    }
}

