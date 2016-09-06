package com.askoliv.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.askoliv.app.R;
import com.askoliv.model.Story;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by surbhimanurkar on 16-08-2016.
 * Utils for android basic functions
 */
public class AndroidUtils {

    private static final String TAG = AndroidUtils.class.getSimpleName();

    public static final int REQUEST_TAKE_PHOTO = 1;

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
        SharedPreferences historySharedPreferences = activity.getSharedPreferences(Constants.SHARED_PREFERENCE_HISTORY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = historySharedPreferences.edit();
        editor.putString(Constants.HISTORY_PREF_CURRENT_PHOTO_PATH, image.getAbsolutePath());
        editor.apply();
        return image;
    }

    public void galleryAddPic(Activity activity, String currentPhotoPath) {
        /*Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        activity.sendBroadcast(mediaScanIntent);*/
        MediaScannerConnection.scanFile(activity,new String[] { currentPhotoPath }, null, null);
    }

    public void setPicture(ImageView imageView, String currentPhotoPath) {
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

    public Bitmap getPicture(Activity activity, String currentPhotoPath) {
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public boolean checkPermission(final Activity activity, String[] permissionsRequired, final int requestCode)
    {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=android.os.Build.VERSION_CODES.M)
        {
            ArrayList<String> permissionRequest = new ArrayList<>();
            for(int i=0; i < permissionsRequired.length; i++){
                if(ContextCompat.checkSelfPermission(activity, permissionsRequired[i]) != PackageManager.PERMISSION_GRANTED) {
                    permissionRequest.add(permissionsRequired[i]);
                }
            }
            if (permissionRequest.size()!=0) {
                permissionsRequired = permissionRequest.toArray(permissionsRequired);
                ActivityCompat.requestPermissions(activity, permissionsRequired,requestCode);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public boolean shareStory(Activity activity,String text,String snapshot){
        File imageFile = FirebaseUtils.getInstance().downloadFilefromFirebaseURL(snapshot);
        if(imageFile!=null){
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("image/*");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, text);
            Uri imageUri = Uri.fromFile(imageFile);
            sharingIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivity(Intent.createChooser(sharingIntent, "Share"));
            return true;
        }else{
            return false;
        }
    }

    public String getShareStoryBody(Activity activity,Story story, String key, boolean external){
        String shareBody = story.getTitle() + "\n\n" + activity.getResources().getString(R.string.share_message_app_name);
        if(external){
            String deepLink = activity.getResources().getString(R.string.mobile_web_link)
                    + "/" + Constants.DEEP_LINK_MAIN
                    + "?" + Constants.DEEP_LINK_FRAGMENT + "=" + Constants.FRAGMENT_POSITION_STORIES
                    + "&" + Constants.DEEP_LINK_STORY + "=" + key;
            String deepLinkEncoded = deepLink;
            try {
                deepLinkEncoded = URLEncoder.encode(deepLink,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                return shareBody;
            }
            String shareLink = activity.getResources().getString(R.string.firebase_dynamic_link_domain)
                    + "?" + Constants.DEEP_LINK_LINK + "=" + deepLinkEncoded
                    + "&" + Constants.DEEP_LINK_PACKAGE + "=" + activity.getPackageName()
                    + "&" + Constants.DEEP_LINK_FALLBACK + "=" + activity.getResources().getString(R.string.mobile_fallback_link);
            shareBody = story.getTitle() + "\n" + shareLink + "\n\n" + activity.getResources().getString(R.string.share_message_app_name);
        }else{
            shareBody = activity.getResources().getString(R.string.text_seeking_help_with_story) + " \"" + story.getTitle() + "\" " + activity.getResources().getString(R.string.text_seeking_help_with_story_suffix);
        }
        return shareBody;
    }

    public void openImageActivity(final Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final String itemTextCamera = activity.getResources().getString(R.string.list_item_camera);
        final String itemTextGallery = activity.getResources().getString(R.string.list_item_gallery);
        final DialogListItem[] dialogListItems = {
                new DialogListItem(itemTextCamera,R.drawable.ic_photo_camera_24dp),
                new DialogListItem(itemTextGallery,R.drawable.ic_photo_library_24dp)
        };

        ListAdapter adapter = new ArrayAdapter<DialogListItem>(activity, R.layout.custom_list_dialog, R.id.text1, dialogListItems){

            public View getView(int position, View convertView, ViewGroup parent) {
                //Use super class to create the View
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView)v.findViewById(R.id.text1);
                tv.setText(dialogListItems[position].getListText());

                //Put the image on the TextView
                tv.setCompoundDrawablesWithIntrinsicBounds(dialogListItems[position].getListIcon(), 0, 0, 0);

                //Add margin between image and text (support various screen densities)
                int marginSize = activity.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
                tv.setCompoundDrawablePadding(marginSize);

                return v;
            }
        };
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int itemPosition) {
                if(dialogListItems[itemPosition]!=null){
                    if (dialogListItems[itemPosition].getListText().equals(itemTextCamera)) {
                        Log.d(TAG, "Camera clicked");
                        dispatchTakePictureIntent(activity,getImageFileNameSentbyUser());
                    }else if (dialogListItems[itemPosition].getListText().equals(itemTextGallery)) {
                        Log.d(TAG, "Gallery clicked");
                        Intent intent = new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        intent.putExtra("outputY", activity.getResources().getDimensionPixelSize(R.dimen.chat_image_size));
                        intent.putExtra("scale",true);
                        activity.startActivityForResult(
                                Intent.createChooser(intent, "Select File"),
                                Constants.REQUEST_GALLERY);
                    }
                }
            }
        });
        View customTitleView = activity.getLayoutInflater().inflate(R.layout.custom_list_title, null);
        TextView titleTextView = (TextView) customTitleView.findViewById(R.id.alertTitle);
        titleTextView.setText(activity.getResources().getString(R.string.title_dialog_select_image));
        builder.setCustomTitle(customTitleView);
        builder.show();
    }

    public void dispatchTakePictureIntent(Activity activity,String imageFileName) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(activity,imageFileName);
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d(TAG, ex.getMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.d(TAG, "PhotoFile: Path " + photoFile.getAbsolutePath());
                Uri photoURI = FileProvider.getUriForFile(activity,
                        "com.askoliv.app.fileprovider",
                        photoFile);
                Log.d(TAG, "PhotoURI:" + photoURI);
                takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoURI);
                activity.startActivityForResult(takePictureIntent, Constants.REQUEST_CAMERA);
            }
        }
    }

    public String getImageFileNameSentbyUser(){
        return Constants.IMAGE_NAME_PREFIX + Constants.SENDER_USER + "-" + System.currentTimeMillis() + ".jpg";
    }

    public void playNotificationSound(Activity activity){
        try {
            Uri notification = Uri.parse("android.resource://" + activity.getPackageName() + "/" + R.raw.inapp_message_notification);
            Ringtone r = RingtoneManager.getRingtone(activity, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
