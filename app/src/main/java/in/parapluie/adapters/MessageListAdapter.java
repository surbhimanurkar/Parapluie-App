package in.parapluie.adapters;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import in.parapluie.app.FullscreenImageActivity;
import in.parapluie.model.Message;
import in.parapluie.app.R;
import in.parapluie.utils.Constants;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

import java.text.SimpleDateFormat;

/**
 * Created by surbhimanurkar on 10-03-2016.
 * Adapter handling chat messages
 */
public class MessageListAdapter extends FirebaseRecyclerAdapter<Message,MessageListAdapter.MessageViewHolder> {

    private static final String TAG = MessageListAdapter.class.getSimpleName();
    private Activity mActivity;
    private static final String label = "Parapluie";

    public MessageListAdapter(Query ref, Activity activity, int layout) {
        super(Message.class, layout, MessageViewHolder.class, ref);
        this.mActivity = activity;
    }
    /**
     * Bind an instance of the <code>Messages</code> class to our view. This method is called by <code>FirebaseListAdapter</code>
     * when there is a data change, and we are given an instance of a View that corresponds to the layout that we passed
     * to the constructor, as well as a single <code>Message</code> instance that represents the current data to bind.
     *
     * @param messageViewHolder A viewholder instance corresponding to the layout we passed to the constructor.
     * @param message An instance representing the current state of the message
     * @param position Position of the model in the list
     */
    @Override
    protected void populateViewHolder(final MessageViewHolder messageViewHolder, final Message message, int position) {


        boolean query = false;
        boolean textExists = false;
        boolean imageExists = false;
        // Map a Chat object to an entry in our listview
        if(message.getAuthor() == Constants.SENDER_USER){
            query = true;
        }

        if(message.getMessage()!=null && !message.getMessage().trim().equals("")){
            textExists = true;
        }

        if(message.getImage()!=null && !message.getImage().trim().equals("")){
            imageExists = true;
        }

        messageViewHolder.setCardVisibility(query,textExists,imageExists);
        messageViewHolder.setMessage(query,message.getMessage());
        messageViewHolder.setImage(mActivity,query,message.getImage(),new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = mActivity.getSharedPreferences(Constants.SHARED_PREFERENCE_IMAGE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Constants.IMAGE_URL, message.getImage());
                editor.apply();
                Intent intent = new Intent(mActivity, FullscreenImageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mActivity.startActivity(intent);
            }
        });
        messageViewHolder.setTimestamp(query,textExists,imageExists,message.getTime());
        messageViewHolder.setCardViewClickListener(query,new View.OnLongClickListener() {
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


    public static class MessageViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public MessageViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setCardVisibility(boolean query, boolean textExists, boolean imageExists){
            if(query){
                //Managing card visibility
                mView.findViewById(R.id.cardview_query).setVisibility(View.VISIBLE);
                mView.findViewById(R.id.cardview_reply).setVisibility(View.GONE);
                if(textExists && imageExists){
                    //Timestamps
                    mView.findViewById(R.id.query_text_image_timestamp).setVisibility(View.VISIBLE);
                    mView.findViewById(R.id.query_timestamp).setVisibility(View.GONE);
                    mView.findViewById(R.id.query_image_timestamp).setVisibility(View.GONE);

                    //Content Visibility
                    mView.findViewById(R.id.query).setVisibility(View.VISIBLE);
                    mView.findViewById(R.id.queryImage).setVisibility(View.VISIBLE);
                }else if(imageExists){
                    //Timestamps
                    mView.findViewById(R.id.query_text_image_timestamp).setVisibility(View.GONE);
                    mView.findViewById(R.id.query_timestamp).setVisibility(View.GONE);
                    mView.findViewById(R.id.query_image_timestamp).setVisibility(View.VISIBLE);

                    //Content Visibility
                    mView.findViewById(R.id.query).setVisibility(View.GONE);
                    mView.findViewById(R.id.queryImage).setVisibility(View.VISIBLE);
                }else if(textExists){
                    //Timestamps
                    mView.findViewById(R.id.query_text_image_timestamp).setVisibility(View.GONE);
                    mView.findViewById(R.id.query_timestamp).setVisibility(View.VISIBLE);
                    mView.findViewById(R.id.query_image_timestamp).setVisibility(View.GONE);

                    //Content Visibility
                    mView.findViewById(R.id.query).setVisibility(View.VISIBLE);
                    mView.findViewById(R.id.queryImage).setVisibility(View.GONE);
                }
            }else{
                //Managing card visibility
                mView.findViewById(R.id.cardview_reply).setVisibility(View.VISIBLE);
                mView.findViewById(R.id.cardview_query).setVisibility(View.GONE);
                if(textExists && imageExists){
                    //Timestamps
                    mView.findViewById(R.id.reply_text_image_timestamp).setVisibility(View.VISIBLE);
                    mView.findViewById(R.id.reply_timestamp).setVisibility(View.GONE);
                    mView.findViewById(R.id.reply_image_timestamp).setVisibility(View.GONE);

                    //Content Visibility
                    mView.findViewById(R.id.reply).setVisibility(View.VISIBLE);
                    mView.findViewById(R.id.replyImage).setVisibility(View.VISIBLE);
                }else if(imageExists){
                    //Timestamps
                    mView.findViewById(R.id.reply_text_image_timestamp).setVisibility(View.GONE);
                    mView.findViewById(R.id.reply_timestamp).setVisibility(View.GONE);
                    mView.findViewById(R.id.reply_image_timestamp).setVisibility(View.VISIBLE);

                    //Content Visibility
                    mView.findViewById(R.id.reply).setVisibility(View.GONE);
                    mView.findViewById(R.id.replyImage).setVisibility(View.VISIBLE);
                }else if(textExists){
                    //Timestamps
                    mView.findViewById(R.id.reply_text_image_timestamp).setVisibility(View.GONE);
                    mView.findViewById(R.id.reply_timestamp).setVisibility(View.VISIBLE);
                    mView.findViewById(R.id.reply_image_timestamp).setVisibility(View.GONE);

                    //Content Visibility
                    mView.findViewById(R.id.reply).setVisibility(View.VISIBLE);
                    mView.findViewById(R.id.replyImage).setVisibility(View.GONE);
                }

            }
        }

        public void setMessage(boolean query, String messageText){
            if(query){
                ((TextView)mView.findViewById(R.id.query)).setText(messageText);
            }else{
                ((TextView)mView.findViewById(R.id.reply)).setText(messageText);
            }
        }

        public void setImage(Activity activity, boolean query, String imageUrl, View.OnClickListener onClickListener){
            ImageView imageView;
            if(query){
                imageView = (ImageView) mView.findViewById(R.id.queryImage);
            }else{
                imageView = (ImageView) mView.findViewById(R.id.replyImage);
            }
            Glide.with(activity).load(imageUrl).centerCrop().dontAnimate().placeholder(ContextCompat.getDrawable(activity,R.drawable.placeholder_image_loading)).into(imageView);
            imageView.setOnClickListener(onClickListener);
        }

        public void setTimestamp(boolean query, boolean textExists, boolean imageExists, Object time){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
            String timeObject = simpleDateFormat.format(time);
            if(query){
                if(textExists && imageExists){
                    ((TextView)mView.findViewById(R.id.query_text_image_timestamp)).setText(timeObject);
                }else if(imageExists){
                    ((TextView)mView.findViewById(R.id.query_image_timestamp)).setText(timeObject);
                }else if(textExists){
                    ((TextView)mView.findViewById(R.id.query_timestamp)).setText(timeObject);
                }
            }else{
                if(textExists && imageExists){
                    ((TextView)mView.findViewById(R.id.reply_text_image_timestamp)).setText(timeObject);
                }else if(imageExists){
                    ((TextView)mView.findViewById(R.id.reply_image_timestamp)).setText(timeObject);
                }else if(textExists){
                    ((TextView)mView.findViewById(R.id.reply_timestamp)).setText(timeObject);
                }
            }
        }

        public void setCardViewClickListener(boolean query, View.OnLongClickListener onLongClickListener){
            if(query){
                mView.findViewById(R.id.cardview_query).setOnLongClickListener(onLongClickListener);
            }else{
                mView.findViewById(R.id.cardview_reply).setOnLongClickListener(onLongClickListener);
            }
        }


    }
}
