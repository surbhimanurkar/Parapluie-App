package in.parapluie.app;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import in.parapluie.utils.Constants;
import in.parapluie.utils.FirebaseUtils;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = ProfileFragment.class.getSimpleName();

    private View mRootView;
    private FirebaseUser mFirebaseUser;
    private String mUID = null;

    public ProfileFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_profile, container, false);

        //Populating user data
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mFirebaseUser!=null){
            mUID = mFirebaseUser.getUid();
            //Populating profile data
            Uri displayPictureUrl = mFirebaseUser.getPhotoUrl();
            String displayNameText = mFirebaseUser.getDisplayName();

            if(displayPictureUrl==null || displayNameText==null){
                for (UserInfo userInfo : mFirebaseUser.getProviderData()) {
                    if (displayNameText == null && userInfo.getDisplayName() != null) {
                        displayNameText = userInfo.getDisplayName();
                    }
                    if (displayPictureUrl == null && userInfo.getPhotoUrl() != null) {
                        displayPictureUrl = userInfo.getPhotoUrl();
                    }
                }

            }


            //Populating display picture
            final ImageView displayPicture = (ImageView)mRootView.findViewById(R.id.display_image);
            if(displayPictureUrl!=null){
                Glide.with(getActivity()).load(displayPictureUrl).asBitmap().listener(new RequestListener<Uri, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<Bitmap> target, boolean isFirstResource) {
                        e.printStackTrace();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Uri model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                }).centerCrop().into(new BitmapImageViewTarget(displayPicture) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getActivity().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        displayPicture.setImageDrawable(circularBitmapDrawable);
                    }
                });

            }

            //Populating display name
            TextView displayName = (TextView) mRootView.findViewById(R.id.display_name);
            displayName.setText(displayNameText);

            //Populating activity data
            if(mUID!=null){
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference rootReference = firebaseDatabase.getReference();
                final String profileTrialEnds = getActivity().getResources().getString(R.string.profile_trial_ends);

                //Get subscription end date
                DatabaseReference trackingReference = rootReference.child(Constants.F_NODE_USER).child(mUID).child(Constants.F_KEY_USER_TRACKING);
                trackingReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists() && dataSnapshot.child(Constants.F_KEY_USER_TIMESTAMP_START).exists() && dataSnapshot.child(Constants.F_KEY_USER_SUBSCRIPTION_PERIOD).exists()){
                            Long startTimestamp = (Long) dataSnapshot.child(Constants.F_KEY_USER_TIMESTAMP_START).getValue();
                            Long subscriptionPeriod = (Long) dataSnapshot.child(Constants.F_KEY_USER_SUBSCRIPTION_PERIOD).getValue();
                            Long endTimestamp = startTimestamp + subscriptionPeriod;
                            SimpleDateFormat sfd = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                            String endDate = sfd.format(new Date(endTimestamp));
                            String endString = profileTrialEnds + " " + endDate;
                            TextView endTextView = (TextView) mRootView.findViewById(R.id.subscriptionEndDate);
                            endTextView.setText(endString);
                        }else{
                            FirebaseUtils.getInstance().saveUserTrackingWithDefault();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                DatabaseReference activityReference = rootReference.child(Constants.F_NODE_USER).child(mUID).child(Constants.F_KEY_USER_ACTIVITY);
                DatabaseReference lovesReference = activityReference.child(Constants.F_KEY_USER_LOVES);
                lovesReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long lovesCount = 0;
                        for(DataSnapshot lovesSnapshot : dataSnapshot.getChildren()){
                            Boolean hasLoved = lovesSnapshot.getValue(Boolean.class);
                            if(hasLoved)
                                lovesCount++;
                        }
                        TextView lovesTextView = (TextView) mRootView.findViewById(R.id.totalloves);
                        lovesTextView.setText(lovesCount + "");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG,databaseError.getMessage());
                    }
                });

                DatabaseReference sharesReference = activityReference.child(Constants.F_KEY_USER_SHARES);
                sharesReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long sharesCount = 0;
                        for(DataSnapshot sharesSnapshot : dataSnapshot.getChildren()){
                            Long storyLoved = sharesSnapshot.getValue(Long.class);
                            if(storyLoved>0)
                                sharesCount = sharesCount + storyLoved;
                        }
                        TextView lovesTextView = (TextView) mRootView.findViewById(R.id.totalshares);
                        lovesTextView.setText(sharesCount + "");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG,databaseError.getMessage());
                    }
                });
            }
        }


        return mRootView;
    }

}
