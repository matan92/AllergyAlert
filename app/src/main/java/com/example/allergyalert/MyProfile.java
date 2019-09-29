package com.example.allergyalert;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.HashMap;

import static com.example.allergyalert.MainActivity.imageprofile;
import static com.example.allergyalert.MainActivity.sign_register;

public class MyProfile extends AppCompatActivity {

    TextView username, email;
    Button signOut;
    CircularImageView imageView;

    private Uri filePath;
    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE=300;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE=400;

    private String cameraPermissions[];
    private String storagePermissions[];

    ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        ActionBar actionBar= getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("My Profile");
        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.profileBackgroundColor)));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        cameraPermissions= new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions= new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        storageReference= FirebaseStorage.getInstance().getReference().child("users_photos");
        firebaseAuth= FirebaseAuth.getInstance();

        user= firebaseAuth.getCurrentUser();

        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());

        progressDialog= new ProgressDialog(this);

        username= findViewById(R.id.profile_name);
        email= findViewById(R.id.profile_email);
        imageView= findViewById(R.id.profile_photo);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPic();
            }
        });

        if (user != null) {
            username.setText(user.getDisplayName());
            email.setText(user.getEmail());

            progressDialog.setMessage("Please wait...");
            progressDialog.show();
            filePath=user.getPhotoUrl();
            if(filePath!=null)
            {
                Glide.with(this).load(filePath).into(imageView);
            }
            progressDialog.dismiss();

        }



        signOut= (Button) findViewById(R.id.signOutBtn);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                // We ensure that the user can't go back to this activity by clearing the stack
                finish();
                startActivity(new Intent(MyProfile.this, MainActivity.class));
            }
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        // if the user changed his username
        sign_register.setText(user.getDisplayName());
        //if the user changed his profile picture
        if(filePath!=null)
        {
            Glide.with(this).load(filePath).into(imageprofile);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_user_data_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id= item.getItemId();

        if(id==R.id.edit_username)
        {
            AlertDialog.Builder builder= new AlertDialog.Builder(this);
            builder.setTitle("Update your username");

            LinearLayout linearLayout= new LinearLayout(this);
            linearLayout.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            linearLayout.setPadding(10, 10, 10, 10);

            final EditText editText= new EditText(this);
            editText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            editText.setHint("enter username");
            linearLayout.addView(editText);

            builder.setView(linearLayout);

            builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String value= editText.getText().toString().trim();
                    if(!TextUtils.isEmpty(value))
                    {
                        progressDialog.show();
                        HashMap<String,Object> result= new HashMap<>();
                        result.put("userName",value);

                        databaseReference.updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(value).build();
                                user.updateProfile(profileUpdates);
                                username.setText(value);
                                Toast.makeText(MyProfile.this, "Successfully updated", Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(MyProfile.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });

                    }
                    else
                    {
                        Toast.makeText(MyProfile.this, "Please enter username", Toast.LENGTH_SHORT).show();
                    }

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

        if(id==R.id.edit_email)
        {
            AlertDialog.Builder builder= new AlertDialog.Builder(this);
            builder.setTitle("Update your email");

            LinearLayout linearLayout= new LinearLayout(this);
            linearLayout.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            linearLayout.setPadding(10, 10, 10, 10);

            final EditText editText= new EditText(this);
            editText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            editText.setHint("enter email");
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            linearLayout.addView(editText);

            builder.setView(linearLayout);

            builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String value= editText.getText().toString().trim();
                    if(!Patterns.EMAIL_ADDRESS.matcher(value).matches())
                    {
                        Toast.makeText(MyProfile.this, "Invalid email", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(!TextUtils.isEmpty(value))
                    {
                        progressDialog.show();
                        HashMap<String,Object> result= new HashMap<>();
                        result.put("email",value);

                        databaseReference.updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                user.updateEmail(value);
                                email.setText(value);
                                Toast.makeText(MyProfile.this, "Successfully updated", Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(MyProfile.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });

                    }
                    else
                    {
                        Toast.makeText(MyProfile.this, "Please enter email", Toast.LENGTH_SHORT).show();
                    }

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

        if(id==R.id.edit_password)
        {
            AlertDialog.Builder builder= new AlertDialog.Builder(this);
            builder.setTitle("Update your password");

            LinearLayout linearLayout= new LinearLayout(this);
            linearLayout.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            linearLayout.setPadding(10, 10, 10, 10);

            final EditText editText= new EditText(this);
            editText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            editText.setHint("enter password");
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            linearLayout.addView(editText);

            builder.setView(linearLayout);

            builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String value= editText.getText().toString().trim();
                    if(value.length()<6)
                    {
                        Toast.makeText(MyProfile.this, "Your password must contain at least 6 characters", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(!TextUtils.isEmpty(value))
                    {
                        progressDialog.show();
                        HashMap<String,Object> result= new HashMap<>();
                        result.put("password",value);

                        databaseReference.updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                user.updatePassword(value);
                                Toast.makeText(MyProfile.this, "Successfully updated", Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(MyProfile.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });

                    }
                    else
                    {
                        Toast.makeText(MyProfile.this, "Please enter password", Toast.LENGTH_SHORT).show();
                    }

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

        return super.onOptionsItemSelected(item);
    }

    private boolean checkStoragePermission() {
       return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission()
    {
        //request runtime storage permission
        ActivityCompat.requestPermissions(this,storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result= ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                ==(PackageManager.PERMISSION_GRANTED);

        boolean result1= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission()
    {
        //request runtime storage permission
        ActivityCompat.requestPermissions(this,cameraPermissions, CAMERA_REQUEST_CODE);
    }


    @Override
    // this method called when user press Allow or Deny from permission request Dialog
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            //check if camera and storage permissions allowed or not
            case CAMERA_REQUEST_CODE:
                if(grantResults.length>0)
                {
                    boolean cameraAccepted= grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted= grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted&& writeStorageAccepted)
                    {
                        //permissions enabled
                        pickFromCamera();

                    }
                    else
                    {
                        Toast.makeText(this, "Please enable camera and storage permissions", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_REQUEST_CODE:
                //check if storage permissions allowed or not
                if(grantResults.length>0)
                {
                    boolean writeStorageAccepted= grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted)
                    {
                        //permissions enabled
                        pickFromGallery();

                    }
                    else
                    {
                        Toast.makeText(this, "Please enable storage permissions", Toast.LENGTH_SHORT).show();
                    }
                }
                break;


        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void pickFromGallery() {
        Intent galleryIntent= new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_REQUEST_CODE);

    }

    private void pickFromCamera() {
        ContentValues values= new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp description");
        // put image uri
        filePath= this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // start camera
        Intent cameraIntent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,filePath);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_REQUEST_CODE);



    }

    private void removePhoto()
    {
        AlertDialog.Builder adb= new AlertDialog.Builder(MyProfile.this);
        LinearLayout linearLayout= new LinearLayout(MyProfile.this);
        linearLayout.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        TextView title = new TextView(MyProfile.this);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 60, 0, 0);
        title.setTextSize(20);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        title.setText("Are you sure?");
        adb.setCustomTitle(title);
        adb.setView(linearLayout);
        adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds : dataSnapshot.getChildren())
                        {
                            if(ds.getKey().equals("photo"))
                            {
                                ds.getRef().removeValue();
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setPhotoUri(null).build();
                                user.updateProfile(profileUpdates);
                                // Now the image path and the image in the navigation drawer are null as well
                                filePath=null;
                                imageprofile.setImageURI(filePath);
                                // We set the default drawer because the user has no image anymore
                                imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_account_circle_black_24dp));
                                dialog.cancel();
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        adb.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK)
        {
            if(requestCode==IMAGE_PICK_GALLERY_REQUEST_CODE)
            {
                //image is picked from gallery, get uri of image
                progressDialog.setMessage("Please wait...");
                progressDialog.show();
                filePath= data.getData();
                imageView.setImageURI(filePath);
                final StorageReference mStorageReference= storageReference.child(user.getUid()+".jpg");
                mStorageReference.putFile(filePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            mStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uri) {
                                    HashMap<String,Object> result= new HashMap<>();
                                    result.put("photo",uri.toString());

                                    databaseReference.updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                    .setPhotoUri(uri).build();
                                            user.updateProfile(profileUpdates);
                                            progressDialog.dismiss();
                                            Toast.makeText(MyProfile.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();

                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MyProfile.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });


                        }
                    }
                });

            }
            if(requestCode==IMAGE_PICK_CAMERA_REQUEST_CODE)
            {
                //image is picked from camera, get uri of image
                progressDialog.setMessage("Please wait...");
                progressDialog.show();
                imageView.setImageURI(filePath);
                final StorageReference mStorageReference= storageReference.child(user.getUid()+".jpg");
                mStorageReference.putFile(filePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            mStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uri) {
                                    progressDialog.show();
                                    HashMap<String,Object> result= new HashMap<>();
                                    result.put("photo",uri.toString());

                                    databaseReference.updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                    .setPhotoUri(uri).build();
                                            user.updateProfile(profileUpdates);
                                            progressDialog.dismiss();
                                            Toast.makeText(MyProfile.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();

                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MyProfile.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });


                        }
                    }
                });

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void editPic()
    {
        // We crate a builder for the dialog
        AlertDialog.Builder builder= new AlertDialog.Builder(MyProfile.this);
        View mView= getLayoutInflater().inflate(R.layout.photo_dialog,null);

        TextView title= mView.findViewById(R.id.photo_dialog_tv);
        TextView camera= mView.findViewById(R.id.camera_tv);
        TextView gallery= mView.findViewById(R.id.gallery_tv);
        TextView remove= mView.findViewById(R.id.remove_tv);

        builder.setView(mView);
        // We set the builder into a dialog
        final AlertDialog dialog= builder.create();

        filePath=user.getPhotoUrl();
        // If the user has am image, we allow him to remove it
        if(filePath!=null)
        {
            remove.setVisibility(View.VISIBLE);
            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePhoto();
                }

            });
        }

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Updating Profile picture");
                progressDialog.show();

                if(!checkCameraPermission()){
                    requestCameraPermission();
                }
                else
                {
                    pickFromCamera();
                }

                progressDialog.dismiss();
                dialog.cancel();
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Updating Profile picture");
                progressDialog.show();

                if(!checkStoragePermission()){
                    requestStoragePermission();
                }
                else
                {
                    pickFromGallery();
                }

                progressDialog.dismiss();
                dialog.cancel();
            }
        });

        dialog.show();
    }


    public void preferences(View view) {
        startActivity(new Intent(this,Preferences.class));
    }

    public void favorites(View view) {
        startActivity(new Intent(this,favouritesActivity.class));
    }

    public void reviews(View view) {
        startActivity(new Intent(this,reviewsActivity.class));
    }
}
