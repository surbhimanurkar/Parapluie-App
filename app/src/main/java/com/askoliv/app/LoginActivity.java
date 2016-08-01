package com.askoliv.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.askoliv.utils.Constants;
import com.askoliv.utils.TitleFont;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;



public class LoginActivity extends AppCompatActivity{


    private static final String TAG = LoginActivity.class.getSimpleName();

    /* *************************************
     *              GENERAL                *
     ***************************************/

    /*Appname textview*/
    private TextView mAppName;

    /* A dialog that is presented until the Firebase authentication finished. */
    private ProgressDialog mAuthProgressDialog;

    /* A reference to the Firebase */
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFirebaseRef;

    /* A reference to the Firebase User data */
    private DatabaseReference mUserRef;

    /* Data from the authenticated user */
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    /* Listener for Firebase session changes */
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    /* *************************************
     *              FACEBOOK               *
     ***************************************/
    /* The login button for Facebook */
    private LoginButton mFacebookLoginButton;
    /* The callback manager for Facebook */
    private CallbackManager mFacebookCallbackManager;
    /* Used to track user logging in/out off Facebook */
    private AccessTokenTracker mFacebookAccessTokenTracker;
    /*Login Manager*/
    private LoginManager mFacebookLoginManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

         /* Create the Firebase ref that is used for all authentication with Firebase */
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseRef = mFirebaseDatabase.getReference();
        mUserRef = mFirebaseRef.child(Constants.F_NODE_USER);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        /*Facebook initializations*/
        mFacebookCallbackManager = CallbackManager.Factory.create();
        mFacebookLoginManager = LoginManager.getInstance();

        if(mFirebaseUser!=null)
        {
            SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_LOGIN,Context.MODE_PRIVATE);
            Boolean logoutAttempt = sharedPreferences.getBoolean(Constants.LOGIN_PREF_LOGOUT, false);
            Log.d(TAG, "Logout Attempt: " + logoutAttempt + " Shared Pref:" + sharedPreferences.toString());
            if(logoutAttempt){
                mFirebaseAuth.signOut();
                mFacebookLoginManager.logOut();
            }else{
                Log.d(TAG, "User is logged in already");
                redirectUserToMain();
            }
        }

        //Styling the app title
        //getSupportActionBar().hide();
        mAppName = (TextView) findViewById(R.id.app_name);
        mAppName.setTypeface(TitleFont.getInstance(this).getTypeFace());
        //appName.setTextScaleX(0.8f);


        int loginTextSize = getResources().getDimensionPixelSize(R.dimen.abc_text_size_button_material);
        /* *************************************
         *              FACEBOOK               *
         ***************************************/
        /* Load the Facebook login button and set up the tracker to monitor access token changes */
        mFacebookLoginButton = (LoginButton) findViewById(R.id.login_with_facebook);
        customizeFacebookButton(mFacebookLoginButton, loginTextSize);
        mFacebookLoginButton.setReadPermissions("email", "public_profile");
        mFacebookLoginButton.registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                setLoadingScreen(true);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });



        /* *************************************
         *               GENERAL               *
         ***************************************/


        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
        mAuthProgressDialog = new ProgressDialog(this, R.style.LoadingSpinnerTheme);
        mAuthProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mAuthProgressDialog.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.circular_progress_bar));
        mAuthProgressDialog.setCancelable(false);
        mAuthProgressDialog.show();
        mAuthProgressDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mAuthProgressDialog.dismiss();
                mFirebaseUser = firebaseAuth.getCurrentUser();
                if (mFirebaseUser != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + mFirebaseUser.getUid());
                    setLoadingScreen(true);
                    setAuthenticatedUser();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    setLoadingScreen(false);
                }
            }
        };
        /* Check if the user is authenticated with Firebase already. If this is the case we can set the authenticated
         * user and hide hide any login buttons */
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    /**
     * This method fires when any startActivityForResult finishes. The requestCode maps to
     * the value passed into startActivityForResult.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * Once a user is logged in, take the mAuthData provided from Firebase and "use" it.
     */
    private void setAuthenticatedUser() {
        Log.d(TAG, "SetAuthenticatedUser");
        if (mFirebaseUser != null) {
            /* Hide all the login buttons */
            mFacebookLoginButton.setVisibility(View.GONE);
            saveUserData();
            redirectUserToMain();
        }
        /* invalidate options menu to hide/show the logout button */
        supportInvalidateOptionsMenu();
    }

    /**
     * Show errors to users
     */
    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    /* ************************************
     *             FACEBOOK               *
     **************************************
     */
    protected void customizeFacebookButton(LoginButton signInButton, int textSize) {

        //Styling Text
        signInButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        signInButton.setTypeface(null, Typeface.NORMAL);
        signInButton.setAllCaps(true);
    }
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    /*
     * GENERAL
     */

    public void redirectUserToMain(){
        Log.d(TAG, "Redirecting to main");
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_LOGIN,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.LOGIN_PREF_LOGOUT, false);
        editor.commit();
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
        startActivity(intent);
    }

    /**
     * Save User data in firebase
     */
    public void saveUserData(){
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.F_KEY_USER_PROVIDER, mFirebaseUser.getProviderId());
        if(mFirebaseUser.getDisplayName()!=null) {
            map.put(Constants.F_KEY_USER_USERNAME, mFirebaseUser.getDisplayName().toString());
        }
        mUserRef.child(mFirebaseUser.getUid()).updateChildren(map);
    }

    public void setLoadingScreen(boolean show){
        if(show){
            mFacebookLoginButton.setVisibility(View.GONE);
            mAppName.setVisibility(View.GONE);
        }else{
            mFacebookLoginButton.setVisibility(View.VISIBLE);
            mAppName.setVisibility(View.VISIBLE);
        }
    }

}
