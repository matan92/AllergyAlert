package com.example.allergyalert;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.allergyalert.HistoryAdapter;
import com.example.allergyalert.HistoryItem;
import com.example.allergyalert.Login;
import com.example.allergyalert.R;
import com.example.allergyalert.Registration;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HistoryFragment extends Fragment {

    private Button signup, login;
    private ProgressBar progressBar;
    private TextView signup_login, not_scanned;
    private ImageView sad;
    private ArrayList<HistoryItem>historyItems;
    private DatabaseReference database;
    private HistoryAdapter adapter;
    private RecyclerView mRecyclerView;
    private FirebaseAuth firebaseAuth;
    private LinearLayout linearLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
       View view= inflater.inflate(R.layout.fragment_history, container, false);

       linearLayout= view.findViewById(R.id.history_layout);

       historyItems= new ArrayList<>();

       firebaseAuth= FirebaseAuth.getInstance();

       progressBar= view.findViewById(R.id.hpb);

       signup_login= view.findViewById(R.id.signUp_login);
       not_scanned= view.findViewById(R.id.not_scanned_yet);

       sad= view.findViewById(R.id.history_image);

       signup= view.findViewById(R.id.historySignUpBtn);
       signup.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               startActivity(new Intent(getActivity(), Registration.class));
               getActivity().finish();
           }
       });

       login= view.findViewById(R.id.historyLoginBtn);
       login.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               startActivity(new Intent(getActivity(), Login.class));
               getActivity().finish();
           }
       });

        mRecyclerView = view.findViewById(R.id.hrv);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        showScans();
        deleteScans();

       return view;
    }
    private void showScans()
    {
        if (firebaseAuth.getCurrentUser()!=null)
        {
            progressBar.setVisibility(View.VISIBLE);
            String user_id= FirebaseAuth.getInstance().getCurrentUser().getUid();
            database=FirebaseDatabase.getInstance().getReference().child("Scans").child(user_id);

            database.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        HistoryItem historyItem= ds.getValue(HistoryItem.class);
                        historyItems.add(historyItem);
                    }
                    adapter= new HistoryAdapter(getActivity(),historyItems);
                    mRecyclerView.setAdapter(adapter);
                    if(adapter.getItemCount()==0)
                    {
                        sad.setVisibility(View.VISIBLE);
                        not_scanned.setVisibility(View.VISIBLE);
                    }
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        else
        {
            signup_login.setVisibility(View.VISIBLE);
            signup.setVisibility(View.VISIBLE);
            login.setVisibility(View.VISIBLE);
        }

    }

    private void deleteScans()
    {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(getActivity()) {
            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                final HistoryItem item= historyItems.get(viewHolder.getAdapterPosition());
                final int deleteindex= viewHolder.getAdapterPosition();
                adapter.removeItem(deleteindex);
                Snackbar snackbar= Snackbar.make(linearLayout,"The scan has been removed",Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adapter.restoreItem(item,deleteindex);
                        if(sad.getVisibility()==View.VISIBLE && not_scanned.getVisibility()==View.VISIBLE)
                        {
                            sad.setVisibility(View.GONE);
                            not_scanned.setVisibility(View.GONE);
                        }
                    }
                });
                snackbar.setActionTextColor(Color.RED);
                snackbar.show();
                if(adapter.getItemCount()==0)
                {
                    sad.setVisibility(View.VISIBLE);
                    not_scanned.setVisibility(View.VISIBLE);
                }
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(mRecyclerView);
    }



}
