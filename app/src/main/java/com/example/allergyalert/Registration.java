package com.example.allergyalert;


import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class Registration extends AppCompatActivity implements View.OnClickListener {

    private EditText username_et,email_et,password_et;
    private TextView already;
    private Button signUp;
    private SignInButton googlebtn;

    private ProgressDialog progressDialog;
    private static final String TAGF="FACELOG";
    private static final String TAGG="REGISTRATION";
    private static final int RC_SIGN_IN=100;
    private FirebaseAuth firebaseAuth;
    private CallbackManager mCallbackManager;
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        ActionBar actionBar= getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Registration");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient= GoogleSignIn.getClient(this,gso);

        firebaseAuth= FirebaseAuth.getInstance();

        //these lines of codes generate the latest hashkey for facebook
         try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.example.allergyalert",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : ((PackageInfo) info).signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.facebookbtn);
        //loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAGF, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAGF, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAGF, "facebook:onError", error);
            }
        });

        googlebtn= findViewById(R.id.googlebtn);

        googlebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        progressDialog= new ProgressDialog(this);
        progressDialog.setMessage("Registering user...");

        GradientDrawable gd = new GradientDrawable();

        // Set the gradient drawable background to transparent
        gd.setColor(Color.parseColor("#00ffffff"));

        gd.setStroke(2,Color.BLACK);

        username_et= findViewById(R.id.username_et);
        email_et= findViewById(R.id.email_et);
        password_et= findViewById(R.id.password);

        username_et.setBackground(gd);
        email_et.setBackground(gd);
        password_et.setBackground(gd);

        already= findViewById(R.id.already_tv);
        already.setOnClickListener(this);

        signUp= findViewById(R.id.signUpBtn);
        signUp.setOnClickListener(this);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if(account!=null)
                {
                    firebaseAuthWithGoogle(account);
                }

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAGG, "Google sign in failed", e);
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }


    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account)
    {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAGG, "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAGG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(Registration.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser!=null)
        {
            updateUI();
        }

    }

    private void updateUI() {
        startActivity(new Intent(this,MyProfile.class));
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAGF, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAGF, "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUI();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAGF, "signInWithCredential:failure", task.getException());
                            Toast.makeText(Registration.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI();

                        }


                    }
                });
    }

    private void registerUser()
    {
        final String username= username_et.getText().toString().trim();
        final String email = email_et.getText().toString().trim();
        final String password = password_et.getText().toString().trim();

        if(TextUtils.isEmpty(username) && TextUtils.isEmpty(email) && TextUtils.isEmpty(password))
        {
            username_et.setError("Please enter username");
            email_et.setError("Please enter your email");
            password_et.setError("Please enter your password");

            username_et.setFocusable(true);
            email_et.setFocusable(true);
            password_et.setFocusable(true);

            return;
        }
        
        if(TextUtils.isEmpty(username))
        {
            username_et.setError("Please enter username");
            username_et.setFocusable(true);
            return;
        }

        if(TextUtils.isEmpty(email))
        {
            email_et.setError("Please enter your email");
            email_et.setFocusable(true);
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            email_et.setError("Invalid Email");
            email_et.setFocusable(true);
            return;
        }

        if(TextUtils.isEmpty(password))
        {
            password_et.setError("Please enter your password");
            password_et.setFocusable(true);
            return;
        }
        
        if(password.length()<6)
        {
            password_et.setError("Your password must contain at least 6 characters");
            password_et.setFocusable(true);
            return;
        }

        if(!password.matches("^[a-zA-Z0-9]+$"))
        {
            password_et.setError("Your password must contain at least one letter and one number");
            password_et.setFocusable(true);
            return;
        }


        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email,password).
                addOnCompleteListener(Registration.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        try {

                            if(task.isSuccessful())
                            {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username).build();

                                user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(Registration.this, "Registration Successfully",
                                                Toast.LENGTH_SHORT).show();
                                        String user_id= firebaseAuth.getCurrentUser().getUid();
                                        DatabaseReference current_user_db= FirebaseDatabase.getInstance().getReference()
                                                .child("Users").child(user_id);

                                        Map<String,Object> new_post= new HashMap<>();
                                        new_post.put("userName",username);
                                        new_post.put("email",email);
                                        new_post.put("password",password);

                                        current_user_db.setValue(new_post);
                                        progressDialog.dismiss();
                                        startActivity(new Intent(Registration.this, Preferences.class));
                                    }
                                });

                            }

                            throw task.getException();
                        }

                        catch (FirebaseAuthUserCollisionException e)
                        {
                            email_et.setError("This email is already exists");
                            email_et.setFocusable(true);
                        }

                        catch (Exception e)
                        {
                            e.getMessage();
                        }

                        progressDialog.cancel();

                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.signUpBtn:
                registerUser();
                break;

            case R.id.already_tv:
                login(v);
                break;
        }

    }

    public void login(View view) {
        startActivity(new Intent(this, Login.class));
    }




}
