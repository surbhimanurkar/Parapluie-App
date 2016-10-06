package in.parapluie.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

/**
 * Created by surbhimanurkar on 19-05-2016.
 * Singleton of font to avoid memory leak
 */
public class TitleFont {

    private static final String TAG = TitleFont.class.getSimpleName();
    private static TitleFont instance;
    private static Typeface typeface;

    public static TitleFont getInstance(Context context) {
        synchronized (TitleFont.class) {
            if (instance == null) {
                instance = new TitleFont();
                try{
                    typeface = Typeface.createFromAsset(context.getResources().getAssets(), "fonts/LuloClean.ttf");
                }
                catch (Exception e){
                    Log.e(TAG, e.getMessage());
                }
            }
            return instance;
        }
    }

    public Typeface getTypeFace() {
        return typeface;
    }
}
