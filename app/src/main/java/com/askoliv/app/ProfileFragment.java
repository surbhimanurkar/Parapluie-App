package com.askoliv.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = ProfileFragment.class.getSimpleName();

    private View mRootView;
    private FirebaseUser mFirebaseUser;
    private String mUID;

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
            //Populating profile data
            Uri displayPictureUrl = mFirebaseUser.getPhotoUrl();
            String displayNameText = mFirebaseUser.getDisplayName();

            if(/*displayPictureUrl==null ||*/ displayNameText==null){
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
            Glide.with(getActivity()).load(displayPictureUrl).asBitmap().centerCrop().into(new BitmapImageViewTarget(displayPicture) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(getActivity().getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    displayPicture.setImageDrawable(circularBitmapDrawable);
                }
            });

            //Populating display name
            TextView displayName = (TextView) mRootView.findViewById(R.id.display_name);
            displayName.setText(displayNameText);

            ImageView placeholderImage = (ImageView) mRootView.findViewById(R.id.placeholder_image);
            Glide.with(getActivity()).load(getActivity().getResources().getString(R.string.placeholder_image)).centerCrop().into(placeholderImage);

        }



        return mRootView;
    }

}
