package com.askoliv.utils;

import android.util.Log;
import android.widget.EditText;

import com.askoliv.model.Message;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

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
}
