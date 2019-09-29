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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class favouritesActivity extends AppCompatActivity {

    private LinearLayout linearLayout;
    private RecyclerView mRecyclerView;
    private ProgressBar progressBar;
    private DatabaseReference database;

    ArrayList<FavouriteItem> favouriteItems;
    FavouriteAdapter adapter;

    private ImageView imageView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        ActionBar actionBar= getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Favourites");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        linearLayout= findViewById(R.id.favourites_layout);

        imageView= findViewById(R.id.favourite_imageview);
        textView= findViewById(R.id.fav_tv);

        progressBar= findViewById(R.id.fpb);
        favouriteItems= new ArrayList<>();

        mRecyclerView = findViewById(R.id.frv);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        String user_id= FirebaseAuth.getInstance().getCurrentUser().getUid();
        database= FirebaseDatabase.getInstance().getReference().child("Favourites").child(user_id);

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
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    FavouriteItem favouriteItem= ds.getValue(FavouriteItem.class);
                    favouriteItems.add(favouriteItem);
                }
                adapter= new FavouriteAdapter(favouritesActivity.this,favouriteItems);
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
                final FavouriteItem item= favouriteItems.get(viewHolder.getAdapterPosition());
                final int deleteindex= viewHolder.getAdapterPosition();
                adapter.removeItem(deleteindex);
                Snackbar snackbar= Snackbar.make(linearLayout,"The item has been removed",Snackbar.LENGTH_LONG);
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
