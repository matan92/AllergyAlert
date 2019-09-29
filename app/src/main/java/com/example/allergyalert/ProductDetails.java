package com.example.allergyalert;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ProductDetails extends AppCompatActivity {


    private FirebaseAuth firebaseAuth;
    private DatabaseReference favouritesDatabase, productsDatabase;
    private Map<String,Object> new_post;

    static TabLayout tabLayout;
    TabItem tabIngredients;
    TabItem tabFeedbacks;
    TabItem tabWriteFeedback;
    TabItem tabAlternatives;

    SectionsPageAdapter mSectionsPageAdapter;
    ViewPager mvViewPager;

    public static ImageView vi,block;
    public static TextView for_you,not_for_you;
    public static String url,id, history_url, history_id,
            favourite_url, favourite_id, alternative_url, alternative_id;
    public static ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        ActionBar actionBar= getSupportActionBar();
        actionBar.setTitle("Recommendation");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);


        firebaseAuth=FirebaseAuth.getInstance();
        productsDatabase= FirebaseDatabase.getInstance().getReference().child("Products").child("Barcodes");

        tabLayout= findViewById(R.id.tabs);

        tabIngredients= findViewById(R.id.tabIngredients);
        tabFeedbacks= findViewById(R.id.tabFeedbacks);
        tabWriteFeedback= findViewById(R.id.tabWrite_Feedback);
        tabAlternatives= findViewById(R.id.tabAlternatives);

        mvViewPager= findViewById(R.id.viewPager);

        mSectionsPageAdapter= new SectionsPageAdapter(getSupportFragmentManager(),tabLayout.getTabCount());
        mvViewPager.setAdapter(mSectionsPageAdapter);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mvViewPager.setCurrentItem(tab.getPosition());
                if(firebaseAuth.getCurrentUser()==null)
                {
                    if(tab.getPosition()==2)
                    {
                      showDialog();
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mvViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // This code disables viewpager swiping

        mvViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        vi= findViewById(R.id.vi_image);
        block= findViewById(R.id.not_image);

        for_you= findViewById(R.id.vi_tv);
        not_for_you= findViewById(R.id.not_tv);

        url=getIntent().getExtras().getString("url");
        //barcode number
        id=getIntent().getExtras().getString("id");

        history_url=getIntent().getExtras().getString("history_url");
        history_id= getIntent().getExtras().getString("history_id");

        favourite_url=getIntent().getExtras().getString("favourite_url");
        favourite_id= getIntent().getExtras().getString("favourite_id");

        alternative_url=getIntent().getExtras().getString("alternative_url");
        alternative_id= getIntent().getExtras().getString("alternative_id");


        progressDialog= new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        new RecomMethod().execute();




    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }


    // favourites method
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater= getMenuInflater();
        inflater.inflate(R.menu.favourite_item,menu);
        MenuItem item= menu.findItem(R.id.favourite_item);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (firebaseAuth.getCurrentUser()!=null)
                {
                    new_post= new HashMap<>();
                    String user_id= FirebaseAuth.getInstance().getCurrentUser().getUid();
                    favouritesDatabase= FirebaseDatabase.getInstance().getReference().child("Favourites").child(user_id);
                    productsDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // We are looking for the product that the user wants to add to his favourites list
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                String barcode = ds.getKey();
                                if (barcode != null)
                                {
                                    if(id!=null)
                                    {
                                        if (barcode.equals(id))
                                        {
                                            String name = ds.child("name").getValue(String.class);
                                            String image = ds.child("image").getValue(String.class);

                                            if(name!=null && image!=null)
                                            {
                                                new_post.put("product_name",name);
                                                new_post.put("image",image);
                                                break;
                                            }
                                        }
                                    }
                                    else if(history_id!=null)
                                    {
                                        if (barcode.equals(history_id))
                                        {
                                            String name = ds.child("name").getValue(String.class);
                                            String image = ds.child("image").getValue(String.class);

                                            if(name!=null && image!=null)
                                            {
                                                new_post.put("product_name",name);
                                                new_post.put("image",image);
                                                break;
                                            }
                                        }
                                    }

                                    else if(favourite_id!=null)
                                    {
                                        if (barcode.equals(favourite_id))
                                        {
                                            String name = ds.child("name").getValue(String.class);
                                            String image = ds.child("image").getValue(String.class);

                                            if(name!=null && image!=null)
                                            {
                                                new_post.put("product_name",name);
                                                new_post.put("image",image);
                                                break;
                                            }
                                        }
                                    }

                                    else
                                    {
                                        if (barcode.equals(alternative_id))
                                        {
                                            String name = ds.child("name").getValue(String.class);
                                            String image = ds.child("image").getValue(String.class);

                                            if(name!=null && image!=null)
                                            {
                                                new_post.put("product_name",name);
                                                new_post.put("image",image);
                                                break;
                                            }
                                        }
                                    }

                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                    favouritesDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // If the user's list is empty
                            if(!dataSnapshot.exists())
                            {
                                favouritesDatabase.push().setValue(new_post);
                                Toast.makeText(ProductDetails.this, "The product was added to your Favorites list", Toast.LENGTH_LONG).show();
                            }
                            else
                            {
                                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                                while(iterator.hasNext()) {
                                    String name = iterator.next().child("product_name").getValue(String.class);
                                    if (new_post.containsValue(name))
                                    {
                                        Toast.makeText(ProductDetails.this, "This product is already in your favorites list", Toast.LENGTH_LONG).show();
                                        break;
                                    }
                                    if (!iterator.hasNext())
                                    {
                                        if (!new_post.containsValue(name))
                                        {
                                            favouritesDatabase.push().setValue(new_post);
                                            Toast.makeText(ProductDetails.this, "The product was added to your Favorites list", Toast.LENGTH_LONG).show();
                                        }
                                    }

                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

                else
                {
                  showDialog();
                }

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void showDialog()
    {
        AlertDialog.Builder mBuilder= new AlertDialog.Builder(ProductDetails.this);
        View mView= getLayoutInflater().inflate(R.layout.register_login_dialog,null);

        TextView title= mView.findViewById(R.id.title_dialog);
        TextView text= mView.findViewById(R.id.context_tv);
        Button signup= mView.findViewById(R.id.register_dialog_btn);
        Button login= mView.findViewById(R.id.login_dialog_btn);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProductDetails.this,Registration.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProductDetails.this,Login.class));
            }
        });

        mBuilder.setView(mView);
        AlertDialog dialog= mBuilder.create();
        dialog.show();
    }

}
