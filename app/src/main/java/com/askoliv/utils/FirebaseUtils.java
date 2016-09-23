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
import com.askoliv.model.Message;
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

    private FirebaseUtils() {
    }

    public static FirebaseUtils getInstance(){
        if(mFirebaseUtils==null){
            mFirebaseUtils = new FirebaseUtils();
            mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();
        }
        return mFirebaseUtils;
    }

    public void sendMessage(final String messageText, String messageImage, final EditText inputText, int sender) {
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
                        mUserRef.child(Constants.F_KEY_USER_STATUS).setValue(Constants.F_VALUE_USER__OPEN);
                    }
                });
            }
        }

    }


    public void saveImage(final Activity activity, Bitmap bitmap, int requestCode) {

        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Create a storage reference from our app
        StorageReference storageRef = storage.getReferenceFromUrl(activity.getResources().getString(R.string.firebase_storage_url));

        AndroidUtils androidUtils = new AndroidUtils();
        String fileName = androidUtils.getImageFileNameSentbyUser();
        String firebaseFilePath = Constants.F_NODE_CHAT + "/" + mUID + "/" + fileName;
        String localFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();

        // Create a reference to image
        StorageReference imageRef = storageRef.child(firebaseFilePath);

        //Saving image to Firebase
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();


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
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                if (downloadUrl != null)
                    sendMessage(null, downloadUrl.toString(), null, Constants.SENDER_USER);
            }
        });

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
                    long newShares = ((long) dataSnapshot.getValue()) + 1;
                    if(firebaseUser!=null) {
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

}
