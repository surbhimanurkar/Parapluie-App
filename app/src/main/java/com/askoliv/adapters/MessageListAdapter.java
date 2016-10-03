package com.askoliv.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.askoliv.app.FullscreenImageActivity;
import com.askoliv.model.Message;
import com.askoliv.app.R;
import com.askoliv.utils.Constants;
import com.bumptech.glide.Glide;
import com.google.firebase.database.Query;

import java.text.SimpleDateFormat;

/**
 * Created by surbhimanurkar on 10-03-2016.
 * Adapter handling chat messages
 */
public class MessageListAdapter extends FirebaseListAdapter<Message> {

    private static final String TAG = MessageListAdapter.class.getSimpleName();
    private Activity mActivity;
    private static final String label = "Parapluie";

    public MessageListAdapter(Query ref, Activity activity, int layout) {
        super(ref, Message.class, layout, activity);
        this.mActivity = activity;
    }

    /**
     * Bind an instance of the <code>Chat</code> class to our view. This method is called by <code>FirebaseListAdapter</code>
     * when there is a data change, and we are given an instance of a View that corresponds to the layout that we passed
     * to the constructor, as well as a single <code>Chat</code> instance that represents the current data to bind.
     *
     * @param view A view instance corresponding to the layout we passed to the constructor.
     * @param message An instance representing the current state of a chat message
     */
    @Override
    @SuppressLint("NewApi")
    protected void populateView(View view, final Message message, String key) {
        // Counting unread messages
        if(message.isUnread()){

        }

        // Map a Chat object to an entry in our listview
        CardView activeCard;
        CardView unusedCard;
        TextView activeTimeStamp;
        TextView activeImageTimeStamp;
        TextView activeTextImageTimestamp;
        TextView activeMessage;
        ImageView activeImage;
        if(message.getAuthor() == Constants.SENDER_USER){
            activeCard = (CardView) view.findViewById(R.id.cardview_query);
            unusedCard = (CardView) view.findViewById(R.id.cardview_reply);
            activeTimeStamp = (TextView) view.findViewById(R.id.query_timestamp);
            activeImageTimeStamp = (TextView) view.findViewById(R.id.query_image_timestamp);
            activeTextImageTimestamp = (TextView) view.findViewById(R.id.query_text_image_timestamp);
            activeMessage = (TextView) view.findViewById(R.id.query);
            activeImage = (ImageView) view.findViewById(R.id.queryImage);
        }else{
            activeCard = (CardView) view.findViewById(R.id.cardview_reply);
            unusedCard = (CardView) view.findViewById(R.id.cardview_query);
            activeTimeStamp = (TextView) view.findViewById(R.id.reply_timestamp);
            activeImageTimeStamp = (TextView) view.findViewById(R.id.reply_image_timestamp);
            activeTextImageTimestamp = (TextView) view.findViewById(R.id.reply_text_image_timestamp);
            activeMessage = (TextView) view.findViewById(R.id.reply);
            activeImage = (ImageView) view.findViewById(R.id.replyImage);
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
        activeCard.setVisibility(View.VISIBLE);
        unusedCard.setVisibility(View.GONE);
        setLongClickListenerforCard(message,activeCard);

        Log.d(TAG, "Text:" + message.getMessage() + " Image:" + message.getImage());
        if(message.getMessage()!=null){
            activeMessage.setText(message.getMessage());
            if(message.getImage()!=null){
                Glide.with(mActivity).load(message.getImage()).centerCrop().into(activeImage);
                activeImage.setVisibility(View.VISIBLE);
                setClickListenerforImage(message.getImage(),activeCard);
                activeTextImageTimestamp.setVisibility(View.VISIBLE);
                activeTextImageTimestamp.setText(simpleDateFormat.format(message.getTime()));
                activeTimeStamp.setVisibility(View.GONE);
            }else{
                activeImage.setVisibility(View.GONE);
                activeTimeStamp.setVisibility(View.VISIBLE);
                activeTimeStamp.setText(simpleDateFormat.format(message.getTime()));
                activeTextImageTimestamp.setVisibility(View.GONE);
            }
            activeMessage.setVisibility(View.VISIBLE);
            activeImageTimeStamp.setVisibility(View.GONE);
        }else if(message.getImage()!=null){
            Glide.with(mActivity).load(message.getImage()).centerCrop().placeholder(new ColorDrawable(Color.DKGRAY)).into(activeImage);
            activeMessage.setVisibility(View.GONE);
            activeImage.setVisibility(View.VISIBLE);
            activeTimeStamp.setVisibility(View.GONE);
            activeImageTimeStamp.setVisibility(View.VISIBLE);
            activeImageTimeStamp.setText(simpleDateFormat.format(message.getTime()));
            activeTextImageTimestamp.setVisibility(View.GONE);
            setClickListenerforImage(message.getImage(),activeCard);
        }
    }

    private void setClickListenerforImage(final String image, CardView activeCard){
        activeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = mActivity.getSharedPreferences(Constants.SHARED_PREFERENCE_IMAGE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Constants.IMAGE_URL, image);
                editor.apply();
                Intent intent = new Intent(mActivity, FullscreenImageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mActivity.startActivity(intent);
            }
        });
    }

    private void setLongClickListenerforCard(final Message message, CardView activeCard){
        activeCard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(message.getMessage()!=null){
                    Toast.makeText(mActivity,mActivity.getResources().getString(R.string.toast_text_copied), Toast.LENGTH_SHORT).show();
                    ClipboardManager clipboard = (ClipboardManager) mActivity.getSystemService(mActivity.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(label, message.getMessage());
                    clipboard.setPrimaryClip(clip);
                    return true;
                }
                return false;
            }
        });
    }
}
