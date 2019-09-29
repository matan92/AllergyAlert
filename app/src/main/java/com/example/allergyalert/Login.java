package com.example.allergyalert;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class Login extends AppCompatActivity {



    private EditText emailEt,passEt;
    private Button signInbtn;
    private SignInButton googlebtn;
    private LoginButton facebookbtn;

    private static final String TAGF="FACELOG";
    private static final String TAGG="REGISTRATION";
    private static final int RC_SIGN_IN=100;
    private CallbackManager mCallbackManager;
    private GoogleSignInClient mGoogleSignInClient;

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        ActionBar actionBar= getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Login");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        progressDialog= new ProgressDialog(this);
        progressDialog.setMessage("Sign in...");

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient= GoogleSignIn.getClient(this,gso);


        firebaseAuth= FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()!=null)
        {
            startActivity(new Intent(Login.this, MyProfile.class));
        }

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        facebookbtn = findViewById(R.id.login_facebook_btn);
        //facebookbtn.setReadPermissions("email", "public_profile");
        facebookbtn.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
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

        googlebtn= findViewById(R.id.login_google_btn);

        googlebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        emailEt= findViewById(R.id.email_et);
        passEt= findViewById(R.id.login_password);

        signInbtn= findViewById(R.id.loginbtn);
        signInbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    userLogin();
            }
        });

        GradientDrawable gd = new GradientDrawable();

        // Set the gradient drawable background to transparent
        gd.setColor(Color.parseColor("#00ffffff"));

        gd.setStroke(2,Color.BLACK);

        emailEt.setBackground(gd);
        passEt.setBackground(gd);
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
                            Toast.makeText(Login.this, "Authentication failed", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(Login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI();

                        }

                    }
                });
    }

    private void userLogin()
    {
        String email= emailEt.getText().toString().trim();
        String password= passEt.getText().toString().trim();

        if(TextUtils.isEmpty(email)&& TextUtils.isEmpty(password))
        {
            emailEt.setError("Please enter your email");
            passEt.setError("Please enter your password");

            emailEt.setFocusable(true);
            passEt.setFocusable(true);
            return;
        }

        if(TextUtils.isEmpty(email))
        {
            emailEt.setError("Please enter your email");
            emailEt.setFocusable(true);
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            emailEt.setError("Invalid Email");
            emailEt.setFocusable(true);
            return;
        }

        if(TextUtils.isEmpty(password))
        {
            passEt.setError("Please enter your password");
            passEt.setFocusable(true);
            return;
        }

        if(password.length()<6)
        {
            passEt.setError("Your password must contain at least 6 characters");
            passEt.setFocusable(true);
            return;
        }

        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful())
                        {
                            progressDialog.dismiss();
                            startActivity(new Intent(Login.this, MyProfile.class));
                        }
                        else
                        {
                            Toast.makeText(Login.this, "Couldn't login.Please check one or more of the fields", Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    }
                });


    }

    public void register(View view) {
        startActivity(new Intent(Login.this,Registration.class));
    }

    public void forgot(View view) {

        AlertDialog.Builder builder= new AlertDialog.Builder(this);

        TextView title = new TextView(this);
        title.setText("Reset your password");
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setTextSize(20);

        builder.setCustomTitle(title);

        LinearLayout linearLayout= new LinearLayout(this);
        linearLayout.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        linearLayout.setPadding(10, 10, 10, 10);

        final EditText editText= new EditText(this);
        editText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        editText.setHint("enter your email");
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        builder.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String value= editText.getText().toString().trim();
                if(TextUtils.isEmpty(value))
                {
                    editText.setError("Please enter your email");
                    editText.setFocusable(true);
                    return;
                }

                if(!Patterns.EMAIL_ADDRESS.matcher(value).matches())
                {
                    editText.setError("Invalid Email");
                    editText.setFocusable(true);
                    return;
                }
                firebaseAuth.sendPasswordResetEmail(value).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Login.this, "Email Sent", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}
