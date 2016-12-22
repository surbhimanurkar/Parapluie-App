package in.parapluie.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import in.parapluie.adapters.StoriesListAdapter;
import in.parapluie.app.MainActivity;
import in.parapluie.model.Config;
import in.parapluie.model.Message;
import in.parapluie.model.Story;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by surbhimanurkar on 29-07-2016.
 */
public class FirebaseUtils {

    private static final String TAG = FirebaseUtils.class.getSimpleName();

    private static FirebaseUtils mFirebaseUtils;
    private static DatabaseReference mFirebaseDatabase;
    private DatabaseReference mChatRef;
    private static DatabaseReference mUserRef;
    private static DatabaseReference mResolvedRef;
    private DatabaseReference mQueryRef;
    private static FirebaseUser mFirebaseUser;
    private static String mUID;
    private boolean mSuccess;
    private UsageAnalytics mUsageAnalytics;

    /*private static String mMessageText;
    private static String mMessageImage;
    private static EditText mInputText;*/
    /*
    * Config variables
     */
    private static Config mConfig;
    private static long unreadChatMessages;

    private FirebaseUtils() {
    }

    public static FirebaseUtils getInstance(){
        if(mFirebaseUtils==null){
            mFirebaseUtils = new FirebaseUtils();
            mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();
        }
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mFirebaseUser!=null) {
            mUID = mFirebaseUser.getUid();
            Log.d(TAG, "Retrieve UID: " + mUID);
            mUserRef = mFirebaseDatabase.child(Constants.F_NODE_USER).child(mUID);
            mUserRef.keepSynced(true);
        }
        return mFirebaseUtils;
    }

    public void sendMessagebyUser( final String messageText, final String messageImage, final EditText inputText, final UsageAnalytics mUsageAnalytics){
        /*mMessageText = messageText;
        mMessageImage = messageImage;
        mInputText = inputText;*/
        String queryId = "";
        //final boolean[] resolved = {true};//new boolean[1];
        /*mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mFirebaseUser!=null) {
            mUID = mFirebaseUser.getUid();
            Log.d(TAG, "Retrieve UID: " + mUID);
            mUserRef = mFirebaseDatabase.child(Constants.F_NODE_USER).child(mUID).child("resolved");
            mUserRef.keepSynced(true);
        }*/
        /*mUserRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                // Return passed in data
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if (databaseError != null || !b || dataSnapshot == null) {
                    System.out.println("Failed to get DataSnapshot");
                } else {
                    System.out.println("Successfully get DataSnapshot");
                    //handle data here
                    boolean[] resolved = {true};
                    if(dataSnapshot!=null && dataSnapshot.getValue()!=null){
                        resolved[0] = (boolean) dataSnapshot.getValue();
                    }
                    if (resolved[0] == true){
                        sendMessage(messageText,messageImage,inputText,Constants.SENDER_USER,true,true, mUsageAnalytics);
                    } else {
                        sendMessage(messageText,messageImage,inputText,Constants.SENDER_USER,true,false, null);
                    }
                }
            }
        });*/
        mResolvedRef = mUserRef.child("resolved");
        mResolvedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean[] resolved = {true};
                if(dataSnapshot!=null && dataSnapshot.getValue()!=null){
                    resolved[0] = (boolean) dataSnapshot.getValue();
                }
                if (resolved[0] == true){
                    sendMessage(messageText,messageImage,inputText,Constants.SENDER_USER,true,true, mUsageAnalytics);
                } else {
                    sendMessage(messageText,messageImage,inputText,Constants.SENDER_USER,true,false, mUsageAnalytics);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /*Log.d("12", "23");
        Log.d("resolved",""+ resolved[0]);*/


    }

    /*public void sendMessage(int sender, boolean userTriggered, boolean markUnResolved){
        sendMessage(mMessageText,mMessageImage,mInputText,sender,userTriggered,markUnResolved);
    }*/

    public void sendMessageChatRelatedtoStories(Activity activity, Story story, String key){
        AndroidUtils androidUtils = new AndroidUtils();
        String messageText = androidUtils.getShareStoryBody(activity,story,key, false);
        sendMessage(messageText,null,null,Constants.SENDER_PARAPLUIE,true,true,mUsageAnalytics);
    }

    private void sendMessage(final String messageText, final String messageImage, final EditText inputText, final int sender, final boolean userTriggered, final boolean markUnResolved, final UsageAnalytics mUsageAnalytics) {
        Log.d(TAG, "SendMessage started");
        //mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mFirebaseUser!=null){
            mUID = mFirebaseUser.getUid();
            Log.d(TAG, "Retrieve UID: " + mUID);
            mChatRef = mFirebaseDatabase.child(Constants.F_NODE_CHAT).child(mUID);
            mUserRef = mFirebaseDatabase.child(Constants.F_NODE_USER).child(mUID);
            mQueryRef = mFirebaseDatabase.child(Constants.F_NODE_QUERY).child(mUID);
            if(messageText!=null || messageImage!=null){
                // Create our 'model', a Chat object
                Log.d(TAG, "UID: " + mUID);
                if(messageText!=null && inputText!=null){
                    inputText.setText("");
                }
                if (markUnResolved) {
                    mUserRef.child(Constants.F_KEY_USER_RESOLVED).setValue(false);
                    mQueryRef.push().setValue(true, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                System.out.println("Data could not be saved " + databaseError.getMessage());
                            } else {
                                System.out.println("Data saved successfully.");
                                String[] queryId = {""};
                                queryId[0] = databaseReference.getKey();
                                mUsageAnalytics.trackQDAU(queryId[0]);
                                mUserRef.child(Constants.F_KEY_USER_ACTIVEQID).setValue(queryId[0]);
                                Message message = new Message(messageText, messageImage, sender, ServerValue.TIMESTAMP, true, queryId[0]);
                                mChatRef.push().setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        new BatchNotificationUtils().execute();
                                        //mUserRef.child(Constants.F_KEY_USER_RESOLVED).setValue(false);
                                        //mUserRef.child(Constants.F_KEY_USER_STATUS).setValue(Constants.F_VALUE_USER__OPEN);
                                        if(!isActive() && userTriggered){
                                            sendMessage(getInactiveMessage(),null,null,Constants.SENDER_PARAPLUIE,false,false,null);
                                        }
                                    }
                                });
                            }
                        }
                    });


                } else {
                    mUserRef.child(Constants.F_KEY_USER_ACTIVEQID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String[] queryId = {""};
                            queryId[0] = (String) dataSnapshot.getValue();
                            Message message = new Message(messageText, messageImage, sender, ServerValue.TIMESTAMP, true, queryId[0]);
                            mChatRef.push().setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //mUserRef.child(Constants.F_KEY_USER_RESOLVED).setValue(false);
                                    //mUserRef.child(Constants.F_KEY_USER_STATUS).setValue(Constants.F_VALUE_USER__OPEN);
                                    if(!isActive() && userTriggered){
                                        sendMessage(getInactiveMessage(),null,null,Constants.SENDER_PARAPLUIE,false,false,null);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }

                /*Message message = new Message(messageText, messageImage, sender, ServerValue.TIMESTAMP, true, queryId);
                // Create a new, auto-generated child of that chat location, and save our chat data there
                mChatRef.push().setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //mUserRef.child(Constants.F_KEY_USER_RESOLVED).setValue(false);
                        //mUserRef.child(Constants.F_KEY_USER_STATUS).setValue(Constants.F_VALUE_USER__OPEN);
                        if(!isActive() && userTriggered){
                            sendMessage(getInactiveMessage(),null,null,Constants.SENDER_PARAPLUIE,false,false);
                        }
                    }
                });*/
            }
        }

    }

    public void saveImagewithCaption(final Activity activity, Bitmap bitmap, final String caption){
        final long t = System.currentTimeMillis();
        Log.d(TAG,"Send image 1 t=" + (System.currentTimeMillis()-t));

        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Create a storage reference from our app
        StorageReference storageRef = storage.getReferenceFromUrl(activity.getResources().getString(in.parapluie.app.R.string.firebase_storage_url));
        mUsageAnalytics = new UsageAnalytics();
        mUsageAnalytics.initTracker(activity);
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
                        sendMessagebyUser(caption, downloadUrl.toString(), null, mUsageAnalytics);
                    }else{
                        sendMessagebyUser(null, downloadUrl.toString(), null, mUsageAnalytics);
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
        //final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mFirebaseUser!=null){
            final DatabaseReference sharesRef = mFirebaseDatabase.child(Constants.F_NODE_SOCIAL)
                    .child(Constants.F_NODE_STORIES).child(key)
                    .child(Constants.F_KEY_STORIES_SHARES)
                    .child(mFirebaseUser.getUid());
            sharesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot!=null && mFirebaseUser!=null) {
                        long newShares = 0;
                        if(dataSnapshot.getValue()!=null)
                            newShares = ((long) dataSnapshot.getValue());
                        newShares++;
                        sharesRef.setValue(newShares);
                        mFirebaseDatabase.child(Constants.F_NODE_USER).child(mFirebaseUser.getUid())
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

    public String getTokens() {
        HashMap<String, String> tokens = mConfig.getStylistTokens();
        String[] keys = new String[tokens.size()];
        String[] values = new String[tokens.size()];
        int index = 0;
        for (HashMap.Entry<String, String> mapEntry : tokens.entrySet()) {
            keys[index] = mapEntry.getKey();
            values[index] = mapEntry.getValue();
            index++;
        }
        //Object[] tokensArray = tokens.values().toArray();
        String tokensString = Arrays.toString(values);
        StringBuilder builder = new StringBuilder();
        for(int i=0;i<values.length;i++) {
            if(i == 0) {
                builder.append(values[i]);
            } else {
                builder.append("\", \""+ values[i]);
            }
        }
        Log.d("tokens1", builder.toString());
        return "\"" + builder.toString() + "\"";
    }

    public String getInactiveMessage(){
        return mConfig.getInactiveMessage();
    }

    public void increaseUnreadChatMessageCount(Activity activity){
        unreadChatMessages++;
        //mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mFirebaseUser!=null) {
            mUID = mFirebaseUser.getUid();
            DatabaseReference ref = mFirebaseDatabase.child(Constants.F_NODE_USER).child(mUID);
            ref.child(Constants.F_NODE_USER_APP).child(Constants.F_KEY_USER_UNREAD_CHAT_MESSAGES).setValue(unreadChatMessages);
            ((MainActivity) activity).setUnreadChatMessagesBadge(unreadChatMessages);
        }
    }

    public void setUnreadChatMessages(final Activity activity) {
        //mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mFirebaseUser!=null) {
            mUID = mFirebaseUser.getUid();
            DatabaseReference ref = mFirebaseDatabase.child(Constants.F_NODE_USER).child(mUID);
            ref.child(Constants.F_NODE_USER_APP).child(Constants.F_KEY_USER_UNREAD_CHAT_MESSAGES).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot!=null && dataSnapshot.getValue()!=null) {
                        unreadChatMessages = (long) dataSnapshot.getValue();
                        ((MainActivity) activity).setUnreadChatMessagesBadge(unreadChatMessages);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG,databaseError.getMessage());
                }
            });
        }
    }

    public long getUnreadChatMessages() {
        return unreadChatMessages;
    }

    public void readAllChatMessages(Activity activity){
        unreadChatMessages = 0;
        //mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mFirebaseUser!=null) {
            ((MainActivity) activity).setUnreadChatMessagesBadge(unreadChatMessages);
            mUID = mFirebaseUser.getUid();
            DatabaseReference ref = mFirebaseDatabase.child(Constants.F_NODE_USER).child(mUID);
            ref.child(Constants.F_NODE_USER_APP).child(Constants.F_KEY_USER_UNREAD_CHAT_MESSAGES).setValue(0);
        }
    }
}
