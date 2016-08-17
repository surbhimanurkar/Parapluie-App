package com.askoliv.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.ImageView;

import com.askoliv.app.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by surbhimanurkar on 16-08-2016.
 * Utils for android basic functions
 */
public class AndroidUtils {

    private static final String TAG = AndroidUtils.class.getSimpleName();
    public static final int REQUEST_TAKE_PHOTO = 1;
    private String currentPhotoPath;

    public String getCurrentPhotoPath() {
        return currentPhotoPath;
    }

    public void setCurrentPhotoPath(String currentPhotoPath) {
        this.currentPhotoPath = currentPhotoPath;
    }

    public File createImageFile(Activity activity, String imageFileName) throws IOException {
        // Create an image file
        String localFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File folder = new File(localFilePath, Constants.LOCAL_IMAGE_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File image = new File(folder.getAbsolutePath(),imageFileName);
        image.createNewFile();
        /*File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );*/


        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void galleryAddPic(Activity activity) {
        /*Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        activity.sendBroadcast(mediaScanIntent);*/
        MediaScannerConnection.scanFile(activity,new String[] { currentPhotoPath }, null, null);
    }

    public void setPicture(ImageView imageView) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }

    public Bitmap getPicture(Activity activity) {
        // Get the dimensions of the View
        int targetH = activity.getResources().getDimensionPixelSize(R.dimen.chat_image_size);

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = photoH/targetH;

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Log.d(TAG, "getPicture: CurrentPhotoPath:"+currentPhotoPath);
        //BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        //bmOptions.inJustDecodeBounds = false;
        //bmOptions.inPurgeable = true;
        //BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        return BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
    }
}
