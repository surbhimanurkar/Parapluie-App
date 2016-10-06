package in.parapluie.app;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.batch.android.Batch;

/**
 * Created by surbhimanurkar on 01-09-2016.
 */
public class PushService extends IntentService {

    private final String TAG = PushService.class.getSimpleName();

    public PushService()
    {
        super("MyPushService");
    }


    @Override
    protected void onHandleIntent(Intent intent)
    {
        try
        {
            Log.d(TAG,"onHandleIntent isVisible:" + BaseActivity.isVisible());
            if(!BaseActivity.isVisible() && Batch.Push.shouldDisplayPush(this, intent)) {
                Batch.Push.dismissNotifications();
                Batch.Push.displayNotification(this,intent);
            }
        }
        finally
        {
            PushReceiver.completeWakefulIntent(intent);
        }
    }
}
