package com.askoliv.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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
    //private ListView helpListView;
    //private LinearLayout helpQuestionsLayout;
    //private View helpQuestionsShadow;
    //private Button helpButton;
    private EditText inputText;
    private Button imageButton;

    private AndroidUtils mAndroidUtils = new AndroidUtils();

    public ChatFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_chat, container, false);

         /* Create the Firebase ref that is used for all authentication with Firebase */
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRootFirebaseRef = mFirebaseDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mUID = mFirebaseUser.getUid();
        Log.d(TAG, "Retrieve UID: " + mUID);
        mChatRef = mRootFirebaseRef.child(Constants.F_NODE_CHAT).child(mUID);
        mUserRef = mRootFirebaseRef.child(Constants.F_NODE_USER).child(mUID);

        listView = (ListView) mRootView.findViewById(R.id.listview_messages);
        //helpListView = (ListView) mRootView.findViewById(R.id.listview_help_questions);
        // Setup our input methods. Enter key on the keyboard or pushing the send button
        inputText = (EditText) mRootView.findViewById(R.id.edit_text_chat);
        inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    String messageText = inputText.getText().toString();
                    FirebaseUtils.getInstance().sendMessage(messageText, null, inputText);
                }
                return true;
            }
        });


        mRootView.findViewById(R.id.button_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = inputText.getText().toString();
                FirebaseUtils.getInstance().sendMessage(messageText, null,inputText);
            }
        });

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

        //Chat input text
        /*
        inputText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(v instanceof EditText && v.hasFocus() && helpQuestionsLayout.getVisibility() == View.VISIBLE){
                    setHelpKeyboard(false);
                }
                return false;
            }
        });*/


        //Chat image capture functionality
        imageButton = (Button) mRootView.findViewById(R.id.button_image);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageActivity();
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

    /*private void setHelpKeyboard(boolean setHelpKeyboardVisible){
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
    }*/

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
                        dispatchTakePictureIntent(getImageFileNameSentbyUser());
                    }else if (dialogListItems[itemPosition].getListText().equals(itemTextGallery)) {
                        Log.d(TAG, "Gallery clicked");
                        Intent intent = new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        intent.putExtra("outputY", getResources().getDimensionPixelSize(R.dimen.chat_image_size));
                        intent.putExtra("scale",true);
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

        Bitmap uploadedImageBitmap;
        Log.d(TAG, "onActivityResult: Result Code:" + resultCode + " Request Code:" + requestCode + " Intent:" + intent);

        if (resultCode == getActivity().RESULT_OK)
        {
           switch(requestCode){
               case Constants.REQUEST_CAMERA:
                   Log.d(TAG, "Got image from the Camera");
                   //uploadedImageBitmap = (Bitmap) intent.getExtras().get("data");
                   uploadedImageBitmap = mAndroidUtils.getPicture(getActivity());
                   saveImage(uploadedImageBitmap, requestCode);
                   mAndroidUtils.galleryAddPic(getActivity());
                   break;
               case Constants.REQUEST_GALLERY:
                   Log.d(TAG, "Got image from the gallery");
                   try {
                       uploadedImageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), intent.getData());
                       saveImage(uploadedImageBitmap, requestCode);
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
                   break;
           }
        }
    }

    private void saveImage(Bitmap bitmap, int requestCode){

        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Create a storage reference from our app
        StorageReference storageRef = storage.getReferenceFromUrl(getResources().getString(R.string.firebase_storage_url));

        String fileName = getImageFileNameSentbyUser();
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
                Toast.makeText(getActivity(), "Image could not be sent!", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                if(downloadUrl!=null)
                    FirebaseUtils.getInstance().sendMessage(null, downloadUrl.toString(), inputText);
            }
        });

    }

    private String getImageFileNameSentbyUser(){
        return Constants.IMAGE_NAME_PREFIX + Constants.SENDER_USER + "-" + System.currentTimeMillis() + ".jpg";
    }

    public void dispatchTakePictureIntent(String imageFileName) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = mAndroidUtils.createImageFile(getActivity(),imageFileName);
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d(TAG, ex.getMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.d(TAG, "PhotoFile: Path " + photoFile.getAbsolutePath());
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        "com.askoliv.app.fileprovider",
                        photoFile);
                Log.d(TAG, "PhotoURI:" + photoURI);
                takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, Constants.REQUEST_CAMERA);
            }
        }
    }

}
