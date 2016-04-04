package com.askoliv.oliv;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.askoliv.adapters.HelpQuestionsAdapter;
import com.askoliv.adapters.MessageListAdapter;
import com.askoliv.model.Message;
import com.askoliv.utils.Constants;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.Calendar;

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

        inputText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(v instanceof EditText && v.hasFocus() && helpQuestionsLayout.getVisibility() == View.VISIBLE){
                    setHelpKeyboard(false);
                }
                return false;
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
            Message message = new Message(input, Constants.SENDER_USER, Calendar.getInstance().getTime());
            // Create a new, auto-generated child of that chat location, and save our chat data there
            mChatRef.push().setValue(message);
            inputText.setText("");
            mUserRef.child(Constants.FIREBASE_USER_KEY_RESOLVED).setValue(false);
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


}
