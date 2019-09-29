package com.example.allergyalert;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class reviewsActivity extends AppCompatActivity {

    private LinearLayout linearLayout;
    private RecyclerView mRecyclerView;
    private ProgressBar progressBar;
    private DatabaseReference database;

    private ArrayList<ReviewItem> reviewItems;

    private ReviewAdapter adapter;

    static String productName="";
    private ImageView imageView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        linearLayout= findViewById(R.id.reviews_layout);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        ActionBar actionBar= getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Reviews");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        imageView= findViewById(R.id.reviews_imageview);
        textView= findViewById(R.id.reviews_tv);

        progressBar= findViewById(R.id.rpb);

        reviewItems= new ArrayList<>();

        mRecyclerView = findViewById(R.id.rrv);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        showRecyclerView();
        deleteRecyclerViewItem();

    }

    @Override
    public boolean onSupportNavigateUp() {
        startActivity(new Intent(this,MyProfile.class));
        finish();
        return super.onSupportNavigateUp();
    }

    public void showRecyclerView()
    {
        progressBar.setVisibility(View.VISIBLE);
        final FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        database= FirebaseDatabase.getInstance().getReference().child("Reviews");
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for(DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        productName= ds.child("product_name").getValue(String.class);
                        String date = ds.child("date").getValue(String.class);
                        String feedback = ds.child("feedback").getValue(String.class);
                        String id=  ds.child("id").getValue(String.class);
                        Long rating= ds.child("rating").getValue(Long.class);
                        String username = ds.child("username").getValue(String.class);
                        if(id!=null)
                        {
                            assert user != null;
                            if(id.equals(user.getUid()))
                            {
                                reviewItems.add(new ReviewItem(id,username,rating,date,productName,feedback));
                            }
                        }
                    }

                }
                adapter= new ReviewAdapter(reviewsActivity.this,reviewItems);
                mRecyclerView.setAdapter(adapter);
                if(adapter.getItemCount()==0)
                {
                    imageView.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.VISIBLE);
                }
                progressBar.setVisibility(View.GONE);

            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void deleteRecyclerViewItem() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final ReviewItem item= reviewItems.get(viewHolder.getAdapterPosition());
                final int deleteindex= viewHolder.getAdapterPosition();
                adapter.removeItem(deleteindex);
                Snackbar snackbar= Snackbar.make(linearLayout,"The feedback has been removed",Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adapter.restoreItem(item,deleteindex);
                        if(imageView.getVisibility()==View.VISIBLE && textView.getVisibility()==View.VISIBLE)
                        {
                            imageView.setVisibility(View.GONE);
                            textView.setVisibility(View.GONE);
                        }
                    }
                });
                snackbar.setActionTextColor(Color.RED);
                snackbar.show();
                if(adapter.getItemCount()==0)
                {
                    imageView.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.VISIBLE);
                }
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(mRecyclerView);
    }


}
