package com.askoliv.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

/**
 * Created by surbhimanurkar on 19-05-2016.
 * Singleton of font to avoid memory leak
 */
public class GrandHotelFont {

    private static final String TAG = GrandHotelFont.class.getSimpleName();
    private static GrandHotelFont instance;
    private static Typeface typeface;

    public static GrandHotelFont getInstance(Context context) {
        synchronized (GrandHotelFont.class) {
            if (instance == null) {
                instance = new GrandHotelFont();
                try{
                    typeface = Typeface.createFromAsset(context.getResources().getAssets(), "fonts/GrandHotel-Regular.ttf");
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
