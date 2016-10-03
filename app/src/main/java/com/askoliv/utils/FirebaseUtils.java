package com.askoliv.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.askoliv.adapters.StoriesListAdapter;
import com.askoliv.app.R;
import com.askoliv.model.Config;
import com.askoliv.model.Message;
import com.askoliv.model.Story;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by surbhimanurkar on 29-07-2016.
 */
public class FirebaseUtils {

    private static final String TAG = FirebaseUtils.class.getSimpleName();

    private static FirebaseUtils mFirebaseUtils;
    private static DatabaseReference mFirebaseDatabase;
    private DatabaseReference mChatRef;
    private DatabaseReference mUserRef;
    private FirebaseUser mFirebaseUser;
    private String mUID;
    private boolean mSuccess;
    /*
    * Config variables
     */
    private static Config mConfig;

    private FirebaseUtils() {
    }

    public static FirebaseUtils getInstance(){
        if(mFirebaseUtils==null){
            mFirebaseUtils = new FirebaseUtils();
            mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();
        }
        return mFirebaseUtils;
    }

    public void sendMessagebyUser(String messageText, String messageImage, EditText inputText){
        sendMessage(messageText,messageImage,inputText,Constants.SENDER_USER,true);
    }

    public void sendMessageChatRelatedtoStories(Activity activity, Story story, String key){
        AndroidUtils androidUtils = new AndroidUtils();
        String messageText = androidUtils.getShareStoryBody(activity,story,key, false);
        sendMessage(messageText,null,null,Constants.SENDER_PARAPLUIE,true);
    }

    private void sendMessage(final String messageText, String messageImage, final EditText inputText, final int sender, final boolean userTriggered) {
        Log.d(TAG, "SendMessage started");
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mFirebaseUser!=null){
            mUID = mFirebaseUser.getUid();
            Log.d(TAG, "Retrieve UID: " + mUID);
            mChatRef = mFirebaseDatabase.child(Constants.F_NODE_CHAT).child(mUID);
            mUserRef = mFirebaseDatabase.child(Constants.F_NODE_USER).child(mUID);
            if(messageText!=null || messageImage!=null){
                // Create our 'model', a Chat object
                Log.d(TAG, "UID: " + mUID);
                if(messageText!=null && inputText!=null){
                    inputText.setText("");
                }
                Message message = new Message(messageText, messageImage, sender, ServerValue.TIMESTAMP, true);
                // Create a new, auto-generated child of that chat location, and save our chat data there
                mChatRef.push().setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mUserRef.child(Constants.F_KEY_USER_RESOLVED).setValue(false);
                        //mUserRef.child(Constants.F_KEY_USER_STATUS).setValue(Constants.F_VALUE_USER__OPEN);
                        if(!isActive() && userTriggered){
                            sendMessage(getInactiveMessage(),null,null,Constants.SENDER_PARAPLUIE,false);
                        }
                    }
                });
            }
        }

    }

    public void saveImagewithCaption(final Activity activity, Bitmap bitmap, final String caption){
        final long t = System.currentTimeMillis();
        Log.d(TAG,"Send image 1 t=" + (System.currentTimeMillis()-t));

        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Create a storage reference from our app
        StorageReference storageRef = storage.getReferenceFromUrl(activity.getResources().getString(R.string.firebase_storage_url));

        AndroidUtils androidUtils = new AndroidUtils();
        String fileName = androidUtils.getImageFileNameSentbyUser();
        String firebaseFilePath = Constants.F_NODE_CHAT + "/" + mUID + "/" + fileName;
        String localFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.d(TAG,"Send image 2 t=" + (System.currentTimeMillis()-t));

        // Create a reference to image
        StorageReference imageRef = storageRef.child(firebaseFilePath);

        //Saving image to Firebase
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] data = baos.toByteArray();
        Log.d(TAG,"Send image 3 t=" + (System.currentTimeMillis()-t));


        //Uploading image to firebase storage
        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(activity, "Image could not be sent!", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Log.d(TAG,"Send image 4 t=" + (System.currentTimeMillis()-t));
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                if (downloadUrl != null) {
                    if(caption!=null && !caption.equals("")){
                        sendMessagebyUser(caption, downloadUrl.toString(), null);
                    }else{
                        sendMessagebyUser(null, downloadUrl.toString(), null);
                    }
                }
                Log.d(TAG,"Send image 5 t=" + (System.currentTimeMillis()-t));
            }
        });
    }


    public void saveImage(final Activity activity, Bitmap bitmap) {
        saveImagewithCaption(activity,bitmap,null);
    }

    public int getStoryPositionfromKey(StoriesListAdapter storiesListAdapter, String key){
        int count = storiesListAdapter.getItemCount();
        int position = count-1;
        for (int p=(count-1); p>=0; p--){
            if(key.equals(storiesListAdapter.getRef(p).getKey())){
                position = p;
                break;
            }
        }
        Log.d(TAG,"Calculated Position:"+position + " Count:" + count);
        return position;
    }

    public void increaseShareCount(final String key){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null){
            final DatabaseReference sharesRef = mFirebaseDatabase.child(Constants.F_NODE_SOCIAL)
                    .child(Constants.F_NODE_STORIES).child(key)
                    .child(Constants.F_KEY_STORIES_SHARES)
                    .child(firebaseUser.getUid());
            sharesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot!=null && firebaseUser!=null) {
                        long newShares = 0;
                        if(dataSnapshot.getValue()!=null)
                            newShares = ((long) dataSnapshot.getValue());
                        newShares++;
                        sharesRef.setValue(newShares);
                        mFirebaseDatabase.child(Constants.F_NODE_USER).child(firebaseUser.getUid())
                                .child(Constants.F_KEY_USER_ACTIVITY).child(Constants.F_KEY_STORIES_SHARES)
                                .child(key).setValue(newShares);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG,"onCancelled" + databaseError.getMessage());
                }
            });
        }
    }

    public void populatingConfigVariables(){
        DatabaseReference mConfigRef = mFirebaseDatabase.child(Constants.F_NODE_CONFIG);
        if(mConfigRef!=null){
            ValueEventListener configListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mConfig = dataSnapshot.getValue(Config.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG,"Config Retrieval cancelled" + databaseError.getMessage());
                }
            };
            mConfigRef.addValueEventListener(configListener);
        }

    }

    public boolean isActive(){
        return mConfig.isActive();
    }

    public String getInactiveMessage(){
        return mConfig.getInactiveMessage();
    }

}
