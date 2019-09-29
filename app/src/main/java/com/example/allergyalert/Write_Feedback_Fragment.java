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
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

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


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.example.allergyalert.MainActivity.imageprofile;

public class Write_Feedback_Fragment extends Fragment {

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference database;
    private Map<String,Object> new_post;

    private Button share;
    private EditText feedback;
    private RatingBar ratingBar;

    private String currentDateTime = new SimpleDateFormat("dd/MM/YYYY").format(new Date());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v= inflater.inflate(R.layout.fragment_write__feedback, container, false);

        mFirebaseAuth= FirebaseAuth.getInstance();
        new_post= new HashMap<>();

        ratingBar= v.findViewById(R.id.rateBar);
        feedback= v.findViewById(R.id.feedback_et);
        share= v.findViewById(R.id.share_btn);

        if(mFirebaseAuth.getCurrentUser()!=null)
        {
            database= FirebaseDatabase.getInstance().getReference().child("Reviews");
        }

        else
        {
            ratingBar.setVisibility(View.GONE);
            feedback.setVisibility(View.GONE);
            share.setVisibility(View.GONE);
        }

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save_feedback();
            }
        });

        return v;
    }

    private void save_feedback()
    {
        final String review= feedback.getText().toString().trim();
        final float rate= ratingBar.getRating();
        if(TextUtils.isEmpty(review))
        {
            feedback.setError("Please enter feedback");
            feedback.setFocusable(true);
            return;
        }
        if(rate==0.0)
        {
            Toast.makeText(getActivity(), "Please rate the product", Toast.LENGTH_SHORT).show();
            return;
        }


        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                new_post.put("id",mFirebaseAuth.getCurrentUser().getUid());
                new_post.put("username",mFirebaseAuth.getCurrentUser().getDisplayName());
                new_post.put("rating",rate);
                new_post.put("date",currentDateTime);
                new_post.put("feedback",review);

                DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("Products").child("Barcodes");
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            String barcode = ds.getKey();
                            if (barcode != null)
                            {
                                if (barcode.equals(ProductDetails.id) || barcode.equals(ProductDetails.alternative_id)|| barcode.equals(ProductDetails.favourite_id) || barcode.equals(ProductDetails.history_id))
                                {
                                    String name = ds.child("name").getValue(String.class);
                                    if(name!=null)
                                    {
                                        new_post.put("product_name",name);
                                        break;
                                    }
                                }
                            }
                        }
                        database.push().setValue(new_post);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Toast.makeText(getActivity(), "Your feedback has been published successfully", Toast.LENGTH_SHORT).show();
    }

}
