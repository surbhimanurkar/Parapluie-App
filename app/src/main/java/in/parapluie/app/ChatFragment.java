package in.parapluie.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import in.parapluie.adapters.MessageListAdapter;
import in.parapluie.utils.AndroidUtils;
import in.parapluie.utils.Constants;
import in.parapluie.utils.FirebaseUtils;
import in.parapluie.utils.UsageAnalytics;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    private UsageAnalytics mUsageAnalytics;

    private MessageListAdapter mMessageListAdapter;
    private LinearLayoutManager mLayoutManager;
    //private HelpQuestionsAdapter mHelpQuestionsAdapter;
    private ValueEventListener mConnectedListener;

    private View mRootView;
    private RecyclerView mRecyclerView;
    private EditText inputText;
    private Button sendButton;
    private Button imageButton;

    private int lastVisibleItem = 0;
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
        String uid = "";
        if(mFirebaseUser != null) {
            uid = mFirebaseUser.getUid();
        }
        Log.d(TAG, "Retrieve UID: " + uid);

        mUsageAnalytics = new UsageAnalytics();
        mUsageAnalytics.initTracker(this.getContext());

        mChatRef = mRootFirebaseRef.child(Constants.F_NODE_CHAT).child(uid);
        mUserRef = mRootFirebaseRef.child(Constants.F_NODE_USER).child(uid);


        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerview_messages);
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        //mLayoutManager.setStackFromEnd(true);
        mLayoutManager.setSmoothScrollbarEnabled(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setScrollbarFadingEnabled(true);
        //mRecyclerView.setEmptyView(mRootView.findViewById(R.id.empty));
        /*mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView RecyclerView, int dx, int dy) {
                Log.d("Scrolled", "Yes");
                Log.d(TAG, "findLastVisibleItemPosition" + mLayoutManager.findLastVisibleItemPosition());
                Log.d(TAG, "getItemCount" + mMessageListAdapter.getItemCount());

                lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
                Log.d(TAG,"lastVisibleItem " +lastVisibleItem);
                super.onScrolled(RecyclerView, dx, dy);
            }
        });*/
        /*if (lastVisibleItem == (mMessageListAdapter.getItemCount() - 1)) {
            mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                    //mRecyclerView.scrollToPosition(mLayoutManager.findLastCompletelyVisibleItemPosition());
                    //mRecyclerView.scrollToPosition(lastVisibleItem);
                    mRecyclerView.scrollToPosition(mMessageListAdapter.getItemCount() - 1);
                }
            });
        }*/
        /*mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                Log.d("Last visible item", "" + mLayoutManager.findLastVisibleItemPosition());
                //Log.d("layout changed", "yes");
                //mRecyclerView.scrollToPosition(mLayoutManager.findLastVisibleItemPosition());
                //mRecyclerView.scrollToPosition(lastVisibleItem);
                mRecyclerView.scrollToPosition(mMessageListAdapter.getItemCount()-1);
            }
        });*/

        //Setting send button
        sendButton = (Button) mRootView.findViewById(R.id.button_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = inputText.getText().toString().trim();
                if (messageText.length() != 0){
                    FirebaseUtils.getInstance().sendMessagebyUser(messageText, null, inputText, mUsageAnalytics);

                }
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
                    if (messageText.length() != 0) {
                        FirebaseUtils.getInstance().sendMessagebyUser(messageText, null, inputText, mUsageAnalytics);

                    }
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
                if (charSequence.toString().trim().length() == 0) {
                    sendButton.setEnabled(false);
                    sendButton.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.send_icon_disabled));
                } else {
                    sendButton.setEnabled(true);
                    sendButton.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.send_icon_active));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        if (inputText.getText() == null || inputText.getText().toString().trim().length() == 0) {
            sendButton.setEnabled(false);
            sendButton.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.send_icon_disabled));
        }

        //Chat image capture functionality
        imageButton = (Button) mRootView.findViewById(R.id.button_image);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setKeyboard(false);
                AndroidUtils androidUtils = new AndroidUtils();
                if (androidUtils.checkPermission(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.PERMISSIONS_REQUEST_STORAGE_IMAGE)) {
                    androidUtils.openImageActivity(getActivity());
                }
            }
        });

        //Disable All if chat is not allowed
        /*if (!isChatAllowed()) {
            (mRootView.findViewById(R.id.female_only_message)).setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            inputText.setVisibility(View.GONE);
            imageButton.setVisibility(View.GONE);
            sendButton.setVisibility(View.GONE);
        }*/

        return mRootView;
    }

    @Override
    public void onStart() {
        super.onStart();


        // Setting Message List Adapter
        mMessageListAdapter = new MessageListAdapter(mChatRef.orderByChild(Constants.TIME), this.getActivity(), R.layout.chat_message_query);
        if (mMessageListAdapter.getItemCount() == 0){

        }
        mMessageListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (mMessageListAdapter.getItemCount() > 0){
                    (mRootView.findViewById(R.id.empty_message)).setVisibility(View.GONE);
                    //if (isChatAllowed()) {
                        mRecyclerView.setVisibility(View.VISIBLE);
                    //}
                }
                super.onItemRangeInserted(positionStart, itemCount);
                /*int lastVisibleItem = mLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisibleItem != -1) {
                    if (!getUserVisibleHint()) {
                        FirebaseUtils.getInstance().increaseUnreadChatMessageCount(getActivity());
                    }
                    mAndroidUtils.playNotificationSound(getActivity());
                }
                Log.d("onItemRangeInserted ", "yes");*/
                mRecyclerView.scrollToPosition(mMessageListAdapter.getItemCount() - 1);
                lastVisibleItem = mMessageListAdapter.getItemCount() - 1;
            }

        });
        mRecyclerView.setAdapter(mMessageListAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView RecyclerView, int dx, int dy) {
                //Log.d("Scrolled", "Yes");
                Log.d(TAG, "addOnScrollListener : " + mLayoutManager.findLastVisibleItemPosition());
                Log.d(TAG, "addOnScrollListener : " + mMessageListAdapter.getItemCount());

                lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
                Log.d("addOnScrollListener","lastVisibleItem: " +lastVisibleItem);
                super.onScrolled(RecyclerView, dx, dy);
            }
        });
        //lastVisibleItem = mMessageListAdapter.getItemCount() - 1;
        /*Log.d("getItemCount", " " + (mMessageListAdapter.getItemCount() - 1));
        Log.d("","lastVisibleItem :" + lastVisibleItem);

        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                //mRecyclerView.scrollToPosition(mLayoutManager.findLastCompletelyVisibleItemPosition());
                //mRecyclerView.scrollToPosition(lastVisibleItem);
                Log.d("addOnLayoutChangeListener: findLastVisibleItemPosition", "" + mLayoutManager.findLastVisibleItemPosition());
                Log.d("addOnLayoutChangeListener: findLastCompletelyVisibleItemPosition ", " " + mLayoutManager.findLastCompletelyVisibleItemPosition());
                Log.d("addOnLayoutChangeListener: getItemCount", " " + (mMessageListAdapter.getItemCount() - 1));
                Log.d("addOnLayoutChangeListener","lastVisibleItem :" + lastVisibleItem);
                if (lastVisibleItem == (mMessageListAdapter.getItemCount() - 1)) {
                    //mRecyclerView.scrollToPosition(lastVisibleItem);
                }
                //mRecyclerView.scrollToPosition(lastVisibleItem);
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

    private void setKeyboard(boolean isVisible) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (isVisible) {
            imm.showSoftInput(inputText, InputMethodManager.SHOW_FORCED);
        } else {
            // Check if no view has focus:
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    /*private boolean isChatAllowed() {
        boolean isChatAllowed = true;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCE_LOGIN, Context.MODE_PRIVATE);
        isChatAllowed = sharedPreferences.getBoolean(Constants.LOGIN_PREF_ISCHATALLOWED, true);
        Log.d("isChatAllowed",isChatAllowed +"");
        return isChatAllowed;
    }*/

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (FirebaseUtils.getInstance().getUnreadChatMessages() > 0) {
                FirebaseUtils.getInstance().readAllChatMessages(getActivity());
            }
        }
    }

    public void scrollToBottom() {
        mRecyclerView.scrollToPosition(mMessageListAdapter.getItemCount() - 1);
    }

    public void scrollToBottomOnKeyboardUp() {
        /*Log.d(TAG,"scrollToBottomOnKeyboardUp : getItemCount : "+ (mMessageListAdapter.getItemCount() - 1));
        Log.d(TAG,"scrollToBottomOnKeyboardUp : lastVisibleItem : "+ lastVisibleItem);
        if (lastVisibleItem == (mMessageListAdapter.getItemCount() - 1)) {
            mRecyclerView.scrollToPosition(lastVisibleItem);
        }*/
        if (lastVisibleItem == (mMessageListAdapter.getItemCount() - 1)) {
            mRecyclerView.scrollToPosition(mMessageListAdapter.getItemCount() - 1);
        }
    }


}
