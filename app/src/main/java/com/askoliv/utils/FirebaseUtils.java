package com.askoliv.utils;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.EditText;

import com.askoliv.model.Message;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

/**
 * Created by surbhimanurkar on 29-07-2016.
 */
public class FirebaseUtils {

    private static final String TAG = FirebaseUtils.class.getSimpleName();

    private static FirebaseUtils mFirebaseUtils;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mChatRef;
    private DatabaseReference mUserRef;
    private FirebaseUser mFirebaseUser;
    private String mUID;

    private FirebaseUtils() {
    }

    public static FirebaseUtils getInstance(){
        if(mFirebaseUtils==null){
            mFirebaseUtils = new FirebaseUtils();
        }
        return mFirebaseUtils;
    }

    public void sendMessage(final String messageText, String messageImage, final EditText inputText) {
        Log.d(TAG, "SendMessage started");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mFirebaseUser!=null){
            mUID = mFirebaseUser.getUid();
            Log.d(TAG, "Retrieve UID: " + mUID);
            mChatRef = mFirebaseDatabase.getReference().child(Constants.F_NODE_CHAT).child(mUID);
            mUserRef = mFirebaseDatabase.getReference().child(Constants.F_NODE_USER).child(mUID);
            if(messageText!=null || messageImage!=null){
                // Create our 'model', a Chat object
                Log.d(TAG, "UID: " + mUID);
                Message message = new Message(messageText, messageImage, Constants.SENDER_USER, ServerValue.TIMESTAMP);
                // Create a new, auto-generated child of that chat location, and save our chat data there
                mChatRef.push().setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(messageText!=null && inputText!=null){
                            inputText.setText("");
                        }
                        mUserRef.child(Constants.F_KEY_USER_RESOLVED).setValue(false);
                        mUserRef.child(Constants.F_KEY_USER_STATUS).setValue(Constants.F_VALUE_USER__OPEN);
                    }
                });
            }
        }

    }

    public File downloadFilefromFirebaseURL(String urlString){
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(urlString);

        String fileName = Constants.IMAGE_NAME_PREFIX + Constants.SNAPSHOTS + "-" + System.currentTimeMillis() + ".jpg";
        try {
            String localFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            File folder = new File(localFilePath, Constants.LOCAL_IMAGE_PATH);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File localFile = new File(folder.getAbsolutePath(),fileName);
            localFile.createNewFile();

            storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "Local file successfully created:" + taskSnapshot.getBytesTransferred());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.d(TAG, "Error in creating temp file:"+ exception.getMessage());
                }
            });
            return localFile;
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return null;

    }
}
