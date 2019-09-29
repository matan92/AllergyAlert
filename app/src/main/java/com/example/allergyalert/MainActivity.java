package com.example.allergyalert;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private ImageView main_image;
    ImageButton scan;
    CheckBox checkBox;
    private ProgressBar progressBar;
    private ListView allergiesList;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;
    public static CircularImageView imageprofile;
    private Uri uri;
    private FirebaseAuth firebaseAuth;
    public static TextView sign_register;
    public static ArrayList<String> list= new ArrayList<>();

    private SharedPreferences prefs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("com.example.allergyalert", MODE_PRIVATE);

        progressBar= findViewById(R.id.progressBarDialog);

        firebaseAuth= FirebaseAuth.getInstance();

        Toolbar toolbar= findViewById(R.id.toolbar);
        NavigationView navigationView= findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        onlineNavigationView();

        main_image= findViewById(R.id.main_image);

        // these codes below show the project name
        drawer= findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle= new ActionBarDrawerToggle(this, drawer,toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


    }

    // how looks navigation view if the user is online
    private void onlineNavigationView()
    {
        NavigationView navigationView= findViewById(R.id.nav_view);
        View view= navigationView.getHeaderView(0);
        imageprofile= view.findViewById(R.id.nav_img);
        sign_register= view.findViewById(R.id.signRegister);
        firebaseAuth= FirebaseAuth.getInstance();
        FirebaseUser user= firebaseAuth.getCurrentUser();
        if (user!=null) {
            sign_register.setText("Hi "+user.getDisplayName());
            if(user.getPhotoUrl()!=null)
            {
                uri=user.getPhotoUrl();
                Glide.with(this).load(uri).into(imageprofile);
            }

            // header height changes
            ViewGroup.LayoutParams layoutParams= view.getLayoutParams();
            layoutParams.height = 450;
            view.setLayoutParams(layoutParams);


        }
    }

    // what's happening after the user selects an item in the navigation view
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        scan= findViewById(R.id.btn_scan);
        switch (menuItem.getItemId())
        {
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new mainFragment()).commit();
                scan.setVisibility(View.VISIBLE);
                main_image.setVisibility(View.VISIBLE);
                break;

            case R.id.nav_history:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HistoryFragment()).commit();
                scan.setVisibility(View.GONE);
                main_image.setVisibility(View.GONE);
                break;

            case R.id.nav_settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
                scan.setVisibility(View.GONE);
                main_image.setVisibility(View.GONE);
                break;

            case R.id.nav_about:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutUsFragment()).commit();
                scan.setVisibility(View.GONE);
                main_image.setVisibility(View.GONE);
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {

        if(drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }

        else
        {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // This method checks if this is the first time the user enters to the app
        if (prefs.getBoolean("firstrun", true)) {
            // Do first run stuff here then set 'firstrun' as false
            // The user gets alert dialog of terms of use
            termsOfUse();
            // using the following line to edit/commit prefs
            prefs.edit().putBoolean("firstrun", false).apply();
        }
    }

    public void productScan(View view) {

        progressBar.setVisibility(View.VISIBLE);
        if(firebaseAuth.getCurrentUser()!=null)
        {
            final Dialog myDialog= new Dialog(this);
            WindowManager.LayoutParams lp = myDialog.getWindow().getAttributes();
            myDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            lp.dimAmount = 0.75f;
            myDialog.getWindow().setAttributes(lp);
            myDialog.show();

            DatabaseReference database= FirebaseDatabase.getInstance().getReference().child("Allergies").child(firebaseAuth.getCurrentUser().getUid());
            database.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(!dataSnapshot.exists())
                    {
                        Toast.makeText(MainActivity.this, "Your preferences list is empty", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        myDialog.dismiss();

                    }
                    else
                    {
                        myDialog.dismiss();
                        progressBar.setVisibility(View.GONE);
                        startActivity(new Intent(MainActivity.this,ScanBarCodeActivity.class));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
        else
        {
            AlertDialog.Builder mBuilder= new AlertDialog.Builder(this);
            View mView= getLayoutInflater().inflate(R.layout.main_activity_dialog,null);

            ImageView imageView= mView.findViewById(R.id.clear_dialog);
            Button signup= mView.findViewById(R.id.register_dialog);
            Button login= mView.findViewById(R.id.login_dialog);
            Button proceed= mView.findViewById(R.id.proceed_dialog);

            mBuilder.setView(mView);
            final AlertDialog dialog= mBuilder.create();

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });

            signup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this,Registration.class));
                }
            });

            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this,Login.class));
                }
            });

            proceed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder mBuilder= new AlertDialog.Builder(MainActivity.this);
                    View mView= getLayoutInflater().inflate(R.layout.scan_dialog,null);

                    final DatabaseReference listOfAllergies= FirebaseDatabase.getInstance().getReference("AllergiesList");
                    progressBar.setVisibility(View.VISIBLE);

                    ImageView imageView= mView.findViewById(R.id.scan_clear_dialog);
                    TextView textView= mView.findViewById(R.id.new_allergy);
                    Button button= mView.findViewById(R.id.scanning_btn);

                    allergiesList = mView.findViewById(R.id.scanning_list);
                    arrayList = new ArrayList<>();
                    listOfAllergies.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for(DataSnapshot ds: dataSnapshot.getChildren())
                            {
                                String allergy= ds.child("allergy").getValue(String.class);
                                if(allergy!=null)
                                    arrayList.add(allergy);
                                else
                                    arrayList.add(ds.getValue().toString());
                            }
                            adapter= new ArrayAdapter<>(
                                    MainActivity.this,R.layout.row,R.id.rowCheckBox,arrayList
                            );

                            allergiesList.setAdapter(adapter);
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                    mBuilder.setView(mView);
                    final AlertDialog dialog= mBuilder.create();

                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.cancel();
                        }
                    });

                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            LinearLayout linearLayout= new LinearLayout(MainActivity.this);
                            linearLayout.setPadding(10, 10, 10, 10);

                            final EditText editText= new EditText(MainActivity.this);
                            editText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            editText.setHint("enter your Allergy");
                            linearLayout.addView(editText);

                            final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                                    .setView(linearLayout)
                                    .setTitle("New Allergy")
                                    .setPositiveButton("Add", null) //Set to null. We override the onclick
                                    .setNegativeButton("Cancel", null)
                                    .create();

                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                                @Override
                                public void onShow(DialogInterface dialogInterface) {

                                    Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                    button.setOnClickListener(new View.OnClickListener() {

                                        @Override
                                        public void onClick(View view) {
                                            final String value= editText.getText().toString().trim();
                                            if(TextUtils.isEmpty(value))
                                            {
                                                Toast.makeText(MainActivity.this, "Please enter your allergy", Toast.LENGTH_SHORT).show();
                                            }
                                            else if( arrayList.contains(value))
                                            {
                                                Toast.makeText(MainActivity.this, "This allergy is already exists on the list", Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                            {
                                                final Map<String,Object> new_post= new HashMap<>();
                                                listOfAllergies.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        new_post.put("allergy",value);
                                                        listOfAllergies.push().setValue(new_post);
                                                        adapter.add(value);
                                                        allergiesList.setAdapter(adapter);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                                dialog.dismiss();
                                            }

                                        }
                                    });
                                }
                            });
                            dialog.show();
                        }
                    });

                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // We clear the list because if the user rescans with another allergies, he will still
                            // receive the same recommendation
                            list.clear();
                            for (int i = 0; i < allergiesList.getChildCount(); i++) {
                                checkBox = allergiesList.getChildAt(i).findViewById(R.id.rowCheckBox);
                                if (checkBox.isChecked()) {
                                    list.add(allergiesList.getItemAtPosition(i).toString());
                                }
                            }

                            if (list.isEmpty()) {
                                Toast.makeText(MainActivity.this, "You must choose at least one allergy", Toast.LENGTH_LONG).show();
                            } else {
                                startActivity(new Intent(MainActivity.this, ScanBarCodeActivity.class));
                            }

                        }
                    });
                    dialog.show();
                }
            });

            dialog.show();

        }

    }

    // whats happening after the user clicks on the pic in the header
    public void myProfile(View view)
    {
        if(firebaseAuth.getCurrentUser()==null)
        {
            startActivity(new Intent(MainActivity.this,Login.class));
        }
        else
        {
            startActivity(new Intent(this,MyProfile.class));
        }
    }

    private void termsOfUse() {
        AlertDialog.Builder builder= new AlertDialog.Builder(this);

        TextView title = new TextView(this);
        title.setText("Welcome to Allergy Alert");
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setTextSize(20);

        builder.setCustomTitle(title);

        LinearLayout linearLayout= new LinearLayout(this);
        linearLayout.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        linearLayout.setPadding(10, 10, 10, 10);

        TextView content= new TextView(this);
        content.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        content.setText("The binding product data is only on the packaging of the product. Before consuming, rely only on what is stated in them." +
                "The details on the product components must not be relied on, there may be errors or discrepancies in the information, the exact data appearing on the product. The data must be checked again on the product packaging before use.");
        linearLayout.addView(content);

        builder.setView(linearLayout);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

}
