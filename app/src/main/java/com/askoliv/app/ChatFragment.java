package com.askoliv.app;

import android.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.media.MediaScannerConnection;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.askoliv.adapters.HelpQuestionsAdapter;
import com.askoliv.adapters.MessageListAdapter;
import com.askoliv.model.Message;
import com.askoliv.utils.AndroidUtils;
import com.askoliv.utils.Constants;
import com.askoliv.utils.DialogListItem;
import com.askoliv.utils.FirebaseUtils;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Created by surbhimanurkar on 03-03-2016.
 * Defines chat fragment including viewing previous chats and sending new messages. This is invoked when user goes to chat tab
 */
public class ChatFragment extends Fragment {

    private static final String TAG = ChatFragment.class.getSimpleName();

    /* A reference to the Firebase */
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mChatRef;
    private DatabaseReference mUserRef;
    private DatabaseReference mRootFirebaseRef;
    private FirebaseUser mFirebaseUser;

    private MessageListAdapter mMessageListAdapter;
    //private HelpQuestionsAdapter mHelpQuestionsAdapter;
    private ValueEventListener mConnectedListener;
    private String mUID;

    private View mRootView;
    private ListView listView;
    private EditText inputText;
    private Button sendButton;
    private Button imageButton;

    private AndroidUtils mAndroidUtils = new AndroidUtils();

    public ChatFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_chat, container, false);

        //Colors
        final int primaryColor = ContextCompat.getColor(getActivity(), R.color.colorPrimary);
        final int disabledColor = ContextCompat.getColor(getActivity(), R.color.colorDisabled);
        final int secondaryColor = ContextCompat.getColor(getActivity(), R.color.colorSecondary);

         /* Create the Firebase ref that is used for all authentication with Firebase */
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRootFirebaseRef = mFirebaseDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mUID = mFirebaseUser.getUid();
        Log.d(TAG, "Retrieve UID: " + mUID);
        mChatRef = mRootFirebaseRef.child(Constants.F_NODE_CHAT).child(mUID);
        mUserRef = mRootFirebaseRef.child(Constants.F_NODE_USER).child(mUID);

        listView = (ListView) mRootView.findViewById(R.id.listview_messages);

        //Setting send button
        sendButton = (Button) mRootView.findViewById(R.id.button_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = inputText.getText().toString().trim();
                if(messageText.length()!=0)
                    FirebaseUtils.getInstance().sendMessage(messageText, null,inputText,Constants.SENDER_USER);
            }
        });

        // Setting edittext for messages
        inputText = (EditText) mRootView.findViewById(R.id.edit_text_chat);
        String[] array = getResources().getStringArray(R.array.hint_text_list);
        String randomStr = array[new Random().nextInt(array.length)];
        inputText.setHint(randomStr);
        inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    String messageText = inputText.getText().toString().trim();
                    if(messageText.length()!=0)
                        FirebaseUtils.getInstance().sendMessage(messageText, null, inputText,Constants.SENDER_USER);
                }
                return true;
            }
        });
        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if(charSequence.toString().trim().length()==0){
                    sendButton.setEnabled(false);
                    sendButton.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.send_icon_disabled));
                } else {
                    sendButton.setEnabled(true);
                    sendButton.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.send_icon_active));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        if(inputText.getText()==null || inputText.getText().toString().trim().length()==0){
            sendButton.setEnabled(false);
            sendButton.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.send_icon_disabled));
        }


        //Chat Help Panel - Commenting out help questions feature
        /*helpQuestionsLayout = (LinearLayout) mRootView.findViewById(R.id.help_questions_layout);
        helpQuestionsShadow = mRootView.findViewById(R.id.help_questions_shadow);
        helpButton = (Button) mRootView.findViewById(R.id.button_help_query);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (helpQuestionsLayout.getVisibility() == View.GONE) {
                    setHelpKeyboard(true);
                } else {
                    setHelpKeyboard(false);
                }
            }
        });*/


        //Chat image capture functionality
        imageButton = (Button) mRootView.findViewById(R.id.button_image);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setKeyboard(false);
                AndroidUtils androidUtils = new AndroidUtils();
                if(androidUtils.checkPermission(getActivity(),new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},Constants.PERMISSIONS_REQUEST_STORAGE_IMAGE)){
                    androidUtils.openImageActivity(getActivity());
                }
            }
        });

        return mRootView;
    }

    @Override
    public void onStart() {
        super.onStart();


        // Setting Message List Adapter
        mMessageListAdapter = new MessageListAdapter(mChatRef.orderByChild(Constants.TIME), this.getActivity(), R.layout.chat_message_query);
        listView.setAdapter(mMessageListAdapter);
        mMessageListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                int lastVisibleItem = listView.getLastVisiblePosition();
                Log.d(TAG,"LastVisibleItem:" + lastVisibleItem);
                /*if(lastVisibleItem!=-1){
                    mAndroidUtils.playNotificationSound(getActivity());
                }*/
                listView.setSelection(mMessageListAdapter.getCount() - 1);
            }
        });

        // Setting Help Questions List Adapter
        /*mHelpQuestionsAdapter = new HelpQuestionsAdapter(mRootFirebaseRef.child(Constants.F_NODE_HELP_QUESTIONS).orderByChild(Constants.HELP_QUESTIONS_RELEVANCE).limitToLast(5), this.getActivity(), R.layout.help_question, inputText);
        helpListView.setAdapter(mHelpQuestionsAdapter);
        mHelpQuestionsAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                helpListView.setSelection(mHelpQuestionsAdapter.getCount() - 1);
            }
        });*/

        // Finally, a little indication of connection status
        mConnectedListener = mChatRef.getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean) dataSnapshot.getValue();
                if (connected) {
                    Log.i(TAG, "Connected to Firebase");
                } else {
                    Log.i(TAG, "Disconnected from Firebase");
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e(TAG, firebaseError.getMessage());
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        mChatRef.getRoot().child(".info/connected").removeEventListener(mConnectedListener);
        mMessageListAdapter.cleanup();
    }

    private void setKeyboard(boolean isVisible){
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(isVisible){
            imm.showSoftInput(inputText,InputMethodManager.SHOW_FORCED);
        }else{
            // Check if no view has focus:
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

}
