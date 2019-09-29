package com.example.allergyalert;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Preferences extends AppCompatActivity {


    private ListView allergiesList;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference database;
    private DatabaseReference listOfAllergies;
    private CheckBox checkBox;
    private TextView add;
    ProgressDialog progressDialog;
    private String allergy="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        ActionBar actionBar= getSupportActionBar();
        actionBar.setTitle("Preferences");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        progressDialog= new ProgressDialog(this);

        firebaseAuth= FirebaseAuth.getInstance();

        listOfAllergies= FirebaseDatabase.getInstance().getReference("AllergiesList");

        allergiesList= findViewById(R.id.allergies_list);

        arrayList= new ArrayList<>();

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
                        Preferences.this,R.layout.row,R.id.rowCheckBox,arrayList
                );

                allergiesList.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        add= findViewById(R.id.addAllergy);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAllergy();
            }
        });

        show_list();

    }

    private void addAllergy() {

        LinearLayout linearLayout= new LinearLayout(this);
        linearLayout.setPadding(10, 10, 10, 10);

        final EditText editText= new EditText(this);
        editText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        editText.setHint("enter your Allergy");
        linearLayout.addView(editText);

        final AlertDialog dialog = new AlertDialog.Builder(this)
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
                            Toast.makeText(Preferences.this, "Please enter your allergy", Toast.LENGTH_SHORT).show();
                        }
                        else if( arrayList.contains(value))
                        {
                            Toast.makeText(Preferences.this, "This allergy is already exists on the list", Toast.LENGTH_SHORT).show();
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


    // This method shows the search view on the screen
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // We are preparing our search menu layout implementation in the activity
        MenuInflater inflater= getMenuInflater();
        inflater.inflate(R.menu.search_menu,menu);
        MenuItem item= menu.findItem(R.id.allergy_search);
        androidx.appcompat.widget.SearchView searchView= (androidx.appcompat.widget.SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // search view shows the allergies list
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        startActivity(new Intent(this,MyProfile.class));
        finish();
        return super.onSupportNavigateUp();
    }


    public void show_list()
    {
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        String user_id= firebaseAuth.getCurrentUser().getUid();
        database=FirebaseDatabase.getInstance().getReference().child("Allergies").child(user_id);
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean flag= false;
                //This loop goes over all the allergies of the user
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    //In every iteration, we keep every allergy
                    String allergy= ds.child("allergy").getValue(String.class);
                    // We are looking for this allergy in the list
                    for (int i = 0; i <allergiesList.getChildCount();i++)
                    {
                        // we save his checkbox
                        checkBox= allergiesList.getChildAt(i).findViewById(R.id.rowCheckBox);
                        if(allergy!=null)
                        {
                            if(allergiesList.getItemAtPosition(i).toString().contains(allergy))
                            {
                                checkBox.setChecked(true);
                            }
                        }

                        if(checkBox.isChecked())
                        {
                            flag=true;
                        }

                    }
                }
                progressDialog.dismiss();

                if(!flag)
                {
                   user_dialog();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void save_list(View view)
    {
        String user_id= firebaseAuth.getCurrentUser().getUid();
        final DatabaseReference current_user_allergies= FirebaseDatabase.getInstance().getReference().child("Allergies").child(user_id);
        final Map<String,Object> new_post= new HashMap<>();
        //We set up an array to keep all of the user's allergies and not the entire list that appears to them
        final ArrayList<String> arrayList= new ArrayList<>();
        current_user_allergies.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                // if user preferences are empty
                if(!dataSnapshot.exists())
                {
                    for (int i = 0; i <allergiesList.getChildCount();i++)
                    {
                        checkBox= allergiesList.getChildAt(i).findViewById(R.id.rowCheckBox);
                        allergy= allergiesList.getItemAtPosition(i).toString();
                        if(checkBox.isChecked())
                        {
                            new_post.put("allergy",allergy);
                            current_user_allergies.push().setValue(new_post);
                        }

                    }
                }

                else
                {

                    for (int i = 0; i <allergiesList.getChildCount();i++)
                    {
                        checkBox= allergiesList.getChildAt(i).findViewById(R.id.rowCheckBox);
                        allergy= allergiesList.getItemAtPosition(i).toString();
                        if(checkBox.isChecked())
                        {
                            arrayList.add(allergy);
                        }
                    }

                    // The user has deleted all the allergies from his/her list
                    if(arrayList.size()==0)
                    {
                        AlertDialog.Builder builder= new AlertDialog.Builder(Preferences.this);

                        TextView title = new TextView(Preferences.this);
                        title.setText("Warning");
                        title.setPadding(10, 10, 10, 10);
                        title.setGravity(Gravity.CENTER);
                        title.setTextColor(Color.BLACK);
                        title.setTextSize(20);

                        builder.setCustomTitle(title);

                        LinearLayout linearLayout= new LinearLayout(Preferences.this);
                        linearLayout.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                        linearLayout.setPadding(10, 10, 10, 10);

                        TextView content= new TextView(Preferences.this);
                        content.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        content.setText("If you decide to delete your entire list, you will not be able to scan more products");
                        linearLayout.addView(content);

                        builder.setView(linearLayout);

                        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dataSnapshot.getRef().removeValue();
                                Toast.makeText(Preferences.this, "The changes were successfully saved", Toast.LENGTH_SHORT).show();
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

                    // adding method
                    if(arrayList.size()>=dataSnapshot.getChildrenCount())
                    {
                        for (String item: arrayList)
                        {
                            Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                            while (iterator.hasNext())
                            {
                                String current_allergy = iterator.next().child("allergy").getValue(String.class);

                                if(!item.equals(current_allergy)&& !iterator.hasNext())
                                {
                                    new_post.put("allergy",item);
                                    current_user_allergies.push().setValue(new_post);
                                }
                                if(item.equals(current_allergy))
                                {
                                    break;
                                }
                            }

                        }

                    }

                    //Delete method
                    //If the user added and deleted allergy/ies from his/her list
                    //We should enter again to the database and delete them
                    current_user_allergies.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getChildrenCount()>arrayList.size())
                            {
                                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                                while (iterator.hasNext())
                                {
                                    dataSnapshot= iterator.next().child("allergy");
                                    String current_allergy = dataSnapshot.getValue(String.class);
                                    for(int i=0; i<arrayList.size(); i++)
                                    {
                                        if(!current_allergy.equals(arrayList.get(i)) && i==arrayList.size()-1)
                                        {
                                            dataSnapshot.getRef().removeValue();
                                        }
                                        if(current_allergy.equals(arrayList.get(i)))
                                        {
                                            break;
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        Toast.makeText(Preferences.this, "The changes were successfully saved", Toast.LENGTH_SHORT).show();




    }

    private void user_dialog()
    {
        AlertDialog.Builder builder= new AlertDialog.Builder(Preferences.this);

        TextView title = new TextView(Preferences.this);
        title.setText("Empty List");
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setTextSize(20);

        builder.setCustomTitle(title);

        LinearLayout linearLayout= new LinearLayout(Preferences.this);
        linearLayout.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        linearLayout.setPadding(10, 10, 10, 10);

        TextView content= new TextView(Preferences.this);
        content.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        content.setText("Please select at least one allergy to scan products");
        linearLayout.addView(content);

        builder.setView(linearLayout);

        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

}
