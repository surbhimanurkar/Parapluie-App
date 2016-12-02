package in.parapluie.adapters;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

/**
 * Created by surbhimanurkar on 10-03-2016.
 * Adapter handling chat messages
 */
public class MessageListAdapter extends FirebaseRecyclerAdapter<Message,MessageListAdapter.MessageViewHolder> {

    private static final String TAG = MessageListAdapter.class.getSimpleName();
    private Activity mActivity;
    private static final String label = "Parapluie";

    private class ParseURL extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            StringBuffer buffer = new StringBuffer();
                try {
                    /*Document document = null;
                    document = Jsoup.connect(strings[0]).get();*/
                    //Log.d("document",document.absUrl());

                    /*if (document != null){
                        String title = document.title();
                        Log.d("title",title);
                        String description = getMetaTag(document, "description");
                        if (description == null) {
                            description = getMetaTag(document, "og:description");
                        }
                        Log.d("description",description);
                        //message.setMessage(title + "-" + description);
                        String ogImage = getMetaTag(document, "og:image");
                        Log.d("image",ogImage);
                        *//*if(message.getImage() == null) {
                            message.setImage(ogImage);
                        }*//*
                    }*/


                //Log.d("JSwa", "Connecting to [" + strings[0] + "]");
                    Connection con = Jsoup.connect(strings[0]);

    /* this browseragant thing is important to trick servers into sending us the LARGEST versions of the images */
                    //con.userAgent(Constants.BROWSER_USER_AGENT);
                    Document doc = con.get();
                //Log.d("JSwa", "Connected to [" + strings[0] + "]");
// Get document (HTML page) title
                String title = doc.title();
                Log.d("JSwA", "Title [" + title + "]");
                buffer.append("Title: " + title + "rn");

// Get meta info
                    /*Element image = doc.select("img[id~=\"(landingImage)\"]").first(); //img[src~=(?i)\.(jpe?g)]
                    String imageUrl = "";
                    if(image != null)
                        imageUrl = image.attr("src");*/
                    /*Element image = doc.getElementById("landingImage");
                    String imageUrl = "";
                    if(image != null)
                        imageUrl = image.attr("src");*/
                    String imageUrl = "";
                    Elements images = doc.select("img");
                    for(Element image : images){
                        Log.d("testing",image.attr("src"));
                        if(image.attr("id") == "landingImage") {
                            Log.d("1","");
                            Log.d("imageId",image.attr("id"));
                            Log.d("landingImage_",image.attr("src"));
                            imageUrl = image.attr("src");
                        }
                        if(image.attr("class") == "thumbnails-selected-image") {
                            Log.d("2","");
                            Log.d("thumbnails-selected-image",image.attr("src"));
                            imageUrl = image.attr("src");
                        }
                    }
                    ////title[@lang='en']
                    /*String html = doc.html();
                    org.w3c.dom.Document doc1 = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder().parse(new InputSource(new StringReader(html)));

                    //noinspection MalformedXPath
                    XPathExpression xpath = XPathFactory.newInstance()
                            .newXPath().compile("//img[@id=\"landingImage\"");

                    String result = (String) xpath.evaluate(doc1, XPathConstants.STRING);
                    String regex = "/<img.*?src='(.*?)'/";
                    String src = regex.exec(str)[1];*/
                    //Element image = doc.attr("id","landingImage");
                    //String imageUrl = image.attr("src");
                    String imageOgUrl = "";
                    Elements metaOgImage = doc.select("meta[property=og:image]");
                    if (metaOgImage!=null) {
                        imageOgUrl = metaOgImage.attr("content");
                    }
                    Log.d("imageOgUrl", imageOgUrl);
                    Log.d("imageUrl", imageUrl);
                    /*Elements images = doc.select("img[src~=(?i)\\.(jpe?g)]");
                    *//*String image = images.get(0).absUrl("src");
                    Log.d("images", image);*//*
                    for (Element image : images) {
                        Log.d("images", image.attr("src"));
                    }*/
                    /*Elements metaElems = doc.select("meta");
                buffer.append("META DATArn");
                for (Element metaElem : metaElems) {
                    String name = metaElem.attr("name");
                    String content = metaElem.attr("content");
                    buffer.append("name [" + name + "] - content [" + content + "] rn");
                }

                Elements topicList = doc.select("h2.topic");
                buffer.append("Topic listrn");
                for (Element topic : topicList) {
                    String data = topic.text();

                    buffer.append("Data [" + data + "] rn");
                }*/

            } catch (Throwable t) {
                t.printStackTrace();
            }

            Log.d("capturedData",buffer.toString());
            return buffer.toString();
        }
    }

    public MessageListAdapter(Query ref, Activity activity, int layout) {
        super(Message.class, layout, MessageViewHolder.class, ref);
        this.mActivity = activity;
    }

    String getMetaTag(Document document, String attr) {
        Elements elements = document.select("meta[name=" + attr + "]");
        for (Element element : elements) {
            final String s = element.attr("content");
            if (s != null) return s;
        }
        elements = document.select("meta[property=" + attr + "]");
        for (Element element : elements) {
            final String s = element.attr("content");
            if (s != null) return s;
        }
        return null;
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
        boolean linkExists = false;
        //gruber v2
        //final String URL_REGEX = "#(?i)\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))#iS";
        //diegoperini
        // final String URL_REGEX = "_^(?:(?:https?|ftp)://)(?:\\S+(?::\\S*)?@)?(?:(?!10(?:\\.\\d{1,3}){3})(?!127(?:\\.\\d{1,3}){3})(?!169\\.254(?:\\.\\d{1,3}){2})(?!192\\.168(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\x{00a1}-\\x{ffff}0-9]+-?)*[a-z\\x{00a1}-\\x{ffff}0-9]+)(?:\\.(?:[a-z\\x{00a1}-\\x{ffff}0-9]+-?)*[a-z\\x{00a1}-\\x{ffff}0-9]+)*(?:\\.(?:[a-z\\x{00a1}-\\x{ffff}]{2,})))(?::\\d{2,5})?(?:/[^\\s]*)?$_iuS";
        //example-simple
        final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";

        // Map a Chat object to an entry in our listview
        if(message.getAuthor() == Constants.SENDER_USER){
            query = true;
        }

        if(message.getMessage()!=null && !message.getMessage().trim().equals("")){
            textExists = true;
            Log.d("text",message.getMessage().trim());

            Pattern p = Pattern.compile(URL_REGEX);
            Matcher m = p.matcher(message.getMessage().trim());
            Log.d("Matched",m.toString());
            if(m.find()) {
                String group = m.group();
                Log.d("Match Found",group);
                linkExists = true;
                ParseURL urlParser = new ParseURL();
                urlParser.execute(group);
                /*Document document = null;
                try {
                    document = Jsoup.connect(group).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (document != null){
                    String title = document.title();
                    Log.d("title",title);
                    String description = getMetaTag(document, "description");
                    if (description == null) {
                        description = getMetaTag(document, "og:description");
                    }
                    Log.d("description",description);
                    message.setMessage(title + "-" + description);
                    String ogImage = getMetaTag(document, "og:image");
                    Log.d("image",ogImage);
                    if(message.getImage() == null) {
                        message.setImage(ogImage);
                    }
                }*/
                /*Document doc = null;
                String text = "";
                Elements metaOgTitle = null;
                String imageUrl = null;
                Elements metaOgImage = null;
                try {
                    doc = Jsoup.connect(message.getMessage().trim()).get();
                    metaOgTitle = doc.select("meta[property=og:title]");
                    metaOgImage = doc.select("meta[property=og:image]");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //this browseragant thing is important to trick servers into sending us the LARGEST versions of the images

                if (metaOgTitle!=null) {
                    text = metaOgTitle.attr("content");
                }
                else {
                    text = doc != null ? doc.title() : null;
                }
                if (text!=null) {
                    text = text + message.getMessage().trim();
                    message.setMessage(text);
                }

                if (metaOgImage!=null) {
                    imageUrl = metaOgImage.attr("content");
                }

                if (imageUrl!=null) {
                    if (message.getImage()!=null && !message.getImage().trim().equals("")){
                    } else {
                        message.setImage(imageUrl);
                    }
                }*/


            }
        }

        if(message.getImage()!=null && !message.getImage().trim().equals("")){
            imageExists = true;
            Log.d("image",message.getImage().trim());
        }

        messageViewHolder.setCardVisibility(query,textExists,imageExists,linkExists);
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

        public void setCardVisibility(boolean query, boolean textExists, boolean imageExists, boolean linkExists){
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
