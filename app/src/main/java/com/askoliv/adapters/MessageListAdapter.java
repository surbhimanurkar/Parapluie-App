package com.askoliv.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
        // Map a Chat object to an entry in our listview
        CardView activeCard;
        CardView unusedCard;
        TextView activeTimeStamp;
        TextView activeImageTimeStamp;
        TextView activeMessage;
        ImageView activeImage;
        if(message.getAuthor() == Constants.SENDER_USER){
            activeCard = (CardView) view.findViewById(R.id.cardview_query);
            unusedCard = (CardView) view.findViewById(R.id.cardview_reply);
            activeTimeStamp = (TextView) view.findViewById(R.id.query_timestamp);
            activeImageTimeStamp = (TextView) view.findViewById(R.id.query_image_timestamp);
            activeMessage = (TextView) view.findViewById(R.id.query);
            activeImage = (ImageView) view.findViewById(R.id.queryImage);
        }else{
            activeCard = (CardView) view.findViewById(R.id.cardview_reply);
            unusedCard = (CardView) view.findViewById(R.id.cardview_query);
            activeTimeStamp = (TextView) view.findViewById(R.id.reply_timestamp);
            activeImageTimeStamp = (TextView) view.findViewById(R.id.reply_image_timestamp);
            activeMessage = (TextView) view.findViewById(R.id.reply);
            activeImage = (ImageView) view.findViewById(R.id.replyImage);
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
        activeCard.setVisibility(View.VISIBLE);
        unusedCard.setVisibility(View.GONE);
        Log.d(TAG, "Text:" + message.getMessage() + " Image:" + message.getImage());
        if(message.getMessage()!=null){
            activeMessage.setText(message.getMessage());
            activeImage.setVisibility(View.GONE);
            activeMessage.setVisibility(View.VISIBLE);
            activeImageTimeStamp.setVisibility(View.GONE);
            activeTimeStamp.setVisibility(View.VISIBLE);
            activeTimeStamp.setText(simpleDateFormat.format(message.getTime()));
        }else if(message.getImage()!=null){
            Glide.with(mActivity).load(message.getImage()).centerCrop().into(activeImage);
            activeMessage.setVisibility(View.GONE);
            activeImage.setVisibility(View.VISIBLE);
            activeTimeStamp.setVisibility(View.GONE);
            activeImageTimeStamp.setVisibility(View.VISIBLE);
            activeImageTimeStamp.setText(simpleDateFormat.format(message.getTime()));
            activeCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences sharedPreferences = mActivity.getSharedPreferences(Constants.SHARED_PREFERENCE_IMAGE, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Constants.IMAGE_PREF_URL, message.getImage());
                    editor.apply();
                    Intent intent = new Intent(mActivity, FullscreenImageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mActivity.startActivity(intent);
                }
            });
        }
    }
}
