package com.askoliv.oliv;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.askoliv.adapters.HelpQuestionsAdapter;
import com.askoliv.adapters.MessageListAdapter;
import com.askoliv.model.Message;
import com.askoliv.utils.Constants;
import com.askoliv.utils.DialogListItem;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;

/**
 * Created by surbhimanurkar on 03-03-2016.
 */
public class ChatFragment extends Fragment {

    private static final String TAG = ChatFragment.class.getSimpleName();

    /* A reference to the Firebase */
    private Firebase mChatRef;
    private Firebase mUserRef;
    private Firebase mRootFirebaseRef;

    private MessageListAdapter mMessageListAdapter;
    private HelpQuestionsAdapter mHelpQuestionsAdapter;
    private ValueEventListener mConnectedListener;
    private String mUID;
    private ListView listView;
    private ListView helpListView;
    private LinearLayout helpQuestionsLayout;
    private View helpQuestionsShadow;
    private Button helpButton;
    private EditText inputText;
    private Button imageButton;

    public ChatFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

         /* Create the Firebase ref that is used for all authentication with Firebase */
        mRootFirebaseRef = new Firebase(getResources().getString(R.string.firebase_url));
        mUID = mRootFirebaseRef.getAuth().getUid();
        Log.d(TAG, "Retrieve UID: " + mUID);
        mChatRef = mRootFirebaseRef.child(Constants.FIREBASE_CHAT_NODE).child(mUID);
        mUserRef = mRootFirebaseRef.child(Constants.FIREBASE_USER_NODE).child(mUID);

        listView = (ListView) rootView.findViewById(R.id.listview_messages);
        helpListView = (ListView) rootView.findViewById(R.id.listview_help_questions);
        // Setup our input methods. Enter key on the keyboard or pushing the send button
        inputText = (EditText) rootView.findViewById(R.id.edit_text_chat);
        inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    sendMessage(rootView);
                }
                return true;
            }
        });

        rootView.findViewById(R.id.button_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(rootView);
            }
        });

        //Chat Help Panel
        helpQuestionsLayout = (LinearLayout) rootView.findViewById(R.id.help_questions_layout);
        helpQuestionsShadow = rootView.findViewById(R.id.help_questions_shadow);
        helpButton = (Button) rootView.findViewById(R.id.button_help_query);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (helpQuestionsLayout.getVisibility() == View.GONE) {
                    setHelpKeyboard(true);
                } else {
                    setHelpKeyboard(false);
                }
            }
        });

        //Chat input text
        inputText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(v instanceof EditText && v.hasFocus() && helpQuestionsLayout.getVisibility() == View.VISIBLE){
                    setHelpKeyboard(false);
                }
                return false;
            }
        });

        //Chat image capture functionality
        imageButton = (Button) rootView.findViewById(R.id.button_image);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageActivity();
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();


        // Setting Message List Adapter
        mMessageListAdapter = new MessageListAdapter(getChatsforUser(mChatRef), this.getActivity(), R.layout.chat_message_query);
        listView.setAdapter(mMessageListAdapter);
        mMessageListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(mMessageListAdapter.getCount() - 1);
            }
        });

        // Setting Help Questions List Adapter
        mHelpQuestionsAdapter = new HelpQuestionsAdapter(mRootFirebaseRef.child(Constants.FIREBASE_HELP_QUESTIONS_NODE).orderByChild(Constants.HELP_QUESTIONS_RELEVANCE).limitToLast(5), this.getActivity(), R.layout.help_question, inputText);
        helpListView.setAdapter(mHelpQuestionsAdapter);
        mHelpQuestionsAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                helpListView.setSelection(mHelpQuestionsAdapter.getCount() - 1);
            }
        });

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
            public void onCancelled(FirebaseError firebaseError) {
                // No-op
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        mChatRef.getRoot().child(".info/connected").removeEventListener(mConnectedListener);
        mMessageListAdapter.cleanup();
    }


    private void sendMessage(View rootView) {
        EditText inputText = (EditText) rootView.findViewById(R.id.edit_text_chat);
        String input = inputText.getText().toString();
        if (!input.equals("")) {
            // Create our 'model', a Chat object
            Log.d(TAG, "UID: " + mUID);
            Message message = new Message(input, Constants.SENDER_USER, ServerValue.TIMESTAMP);
            // Create a new, auto-generated child of that chat location, and save our chat data there
            mChatRef.push().setValue(message);
            inputText.setText("");
            mUserRef.child(Constants.FIREBASE_USER_KEY_RESOLVED).setValue(false);
            mUserRef.child(Constants.FIREBASE_USER_KEY_STATUS).setValue(Constants.FIREBASE_USER_VALUE_OPEN);
        }
    }

    public Query getChatsforUser(Firebase firebaseRef){
        return firebaseRef.orderByChild(Constants.TIME);
    }

    private void setHelpKeyboard(boolean setHelpKeyboardVisible){
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(setHelpKeyboardVisible){
            helpQuestionsLayout.setVisibility(View.VISIBLE);
            helpQuestionsShadow.setVisibility(View.VISIBLE);
            // Check if no view has focus:
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            helpButton.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getActivity(), R.drawable.ic_keyboard), null, null, null);
        }else{
            helpQuestionsLayout.setVisibility(View.GONE);
            helpQuestionsShadow.setVisibility(View.GONE);
            helpButton.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getActivity(), R.drawable.ic_faq), null, null, null);
            imm.showSoftInput(inputText,InputMethodManager.SHOW_FORCED);
        }
    }

    private void openImageActivity(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String itemTextCamera = getResources().getString(R.string.list_item_camera);
        final String itemTextGallery = getResources().getString(R.string.list_item_gallery);
        final DialogListItem[] dialogListItems = {
                new DialogListItem(itemTextCamera,R.drawable.ic_photo_camera_24dp),
                new DialogListItem(itemTextGallery,R.drawable.ic_photo_library_24dp)
        };

        ListAdapter adapter = new ArrayAdapter<DialogListItem>(getActivity(), R.layout.custom_list_dialog, R.id.text1, dialogListItems){

            public View getView(int position, View convertView, ViewGroup parent) {
                //Use super class to create the View
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView)v.findViewById(R.id.text1);
                tv.setText(dialogListItems[position].getListText());

                //Put the image on the TextView
                tv.setCompoundDrawablesWithIntrinsicBounds(dialogListItems[position].getListIcon(), 0, 0, 0);

                //Add margin between image and text (support various screen densities)
                int marginSize = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
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
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, Constants.REQUEST_CAMERA);
                    }else if (dialogListItems[itemPosition].getListText().equals(itemTextGallery)) {
                        Log.d(TAG, "Gallery clicked");
                        Intent intent = new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(
                                Intent.createChooser(intent, "Select File"),
                                Constants.REQUEST_GALLERY);
                    }
                }
            }
        });
        View customTitleView = getActivity().getLayoutInflater().inflate(R.layout.custom_list_title, null);
        TextView titleTextView = (TextView) customTitleView.findViewById(R.id.alertTitle);
        titleTextView.setText(getResources().getString(R.string.title_dialog_select_image));
        builder.setCustomTitle(customTitleView);
        builder.show();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == getActivity().RESULT_OK)
        {
           switch(requestCode){
               case Constants.REQUEST_CAMERA:
                   Log.d(TAG, "Got image from the Camera");
                   break;
               case Constants.REQUEST_GALLERY:
                   Log.d(TAG, "Got image from the gallery");
                   break;
           }
        }
    }

}
