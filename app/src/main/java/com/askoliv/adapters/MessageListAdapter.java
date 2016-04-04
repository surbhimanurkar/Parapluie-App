package com.askoliv.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.askoliv.model.Message;
import com.askoliv.oliv.R;
import com.askoliv.utils.Constants;
import com.firebase.client.Query;

import java.text.SimpleDateFormat;

/**
 * Created by surbhimanurkar on 10-03-2016.
 */
public class MessageListAdapter extends FirebaseListAdapter<Message> {

    private Activity activity;

    public MessageListAdapter(Query ref, Activity activity, int layout) {
        super(ref, Message.class, layout, activity);
        this.activity = activity;
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
    protected void populateView(View view, Message message) {
        // Map a Chat object to an entry in our listview
        CardView queryCard = (CardView) view.findViewById(R.id.cardview_query);
        CardView replyCard = (CardView) view.findViewById(R.id.cardview_reply);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
        if(message.getAuthor() == Constants.SENDER_USER){
            queryCard.setVisibility(View.VISIBLE);
            replyCard.setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.query)).setText(message.getMessage());
            ((TextView) view.findViewById(R.id.query_timestamp)).setText(simpleDateFormat.format(message.getTime()));
        }else{
            queryCard.setVisibility(View.GONE);
            replyCard.setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.reply)).setText(message.getMessage());
            ((TextView) view.findViewById(R.id.reply_timestamp)).setText(simpleDateFormat.format(message.getTime()));
        }
    }
}
