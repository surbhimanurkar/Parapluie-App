package in.parapluie.app;

import android.app.Application;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.multidex.MultiDex;

import com.batch.android.Batch;
import com.batch.android.Config;
import com.facebook.FacebookSdk;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by surbhimanurkar on 03-03-2016.
 */
public class ParapluieApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        String myString = "#f44336";
        FacebookSdk.sdkInitialize(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        MultiDex.install(this);

        //Settings for Batch.com push notifications
        Batch.Push.setGCMSenderId(getResources().getString(R.string.gcm_sender_id));
        Batch.Push.setManualDisplay(true);
        Batch.setConfig(new Config(getResources().getString(R.string.batch_api_key)));
        Batch.Push.setSmallIconResourceId(R.drawable.chat);
        Batch.Push.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        //Batch.Push.setNotificationsColor(Integer.parseInt(myString.replaceFirst("#", ""), 16));
    }
}
