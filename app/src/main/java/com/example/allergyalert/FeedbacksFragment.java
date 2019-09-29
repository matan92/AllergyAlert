package com.example.allergyalert;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.allergyalert.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


public class FeedbacksFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference feedbacksDatabase;
    private DatabaseReference productsDatabase;
    private ArrayList<ReviewItem> reviewItems;
    private ArrayList<ReviewItem> temparray;
    private RecyclerView mRecyclerView;
    private ReviewAdapter adapter;

    private RatingBar ratingBar;
    private TextView averageTv;
    private Spinner sort;
    private Spinner stars;
    private View feedbacksDivider;

    private float sum=0;
    private int counter=0;
    private float average=0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v =inflater.inflate(R.layout.fragment_feedbacks, container, false);

        firebaseAuth= FirebaseAuth.getInstance();

        productsDatabase= FirebaseDatabase.getInstance().getReference().child("Products").child("Barcodes");

        reviewItems= new ArrayList<>();
        temparray= new ArrayList<>();

        ratingBar= v.findViewById(R.id.feedbacksRateBar);
        averageTv= v.findViewById(R.id.average_tv);
        feedbacksDivider= v.findViewById(R.id.feedbacks_divider);

        sort= v.findViewById(R.id.sort_spinner);
        sortSpinner();

        stars= v.findViewById(R.id.stars_spinner);
        stars_spinner();

        mRecyclerView= v.findViewById(R.id.feedbacks_rv);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        feedbacksDatabase= FirebaseDatabase.getInstance().getReference().child("Reviews");

        return v;
    }

    private void showFeedbacks() {

        feedbacksDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String date = ds.child("date").getValue(String.class);
                    String feedback = ds.child("feedback").getValue(String.class);
                    String product_name = ds.child("product_name").getValue(String.class);
                    Long rating= ds.child("rating").getValue(Long.class);
                    String username = ds.child("username").getValue(String.class);
                    if(date!=null && feedback!=null && product_name!=null&& rating!=0.0 && username!=null)
                    {
                        temparray.add(new ReviewItem(null,username,rating,date,product_name,feedback));
                    }

                }

                productsDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds: dataSnapshot.getChildren())
                        {
                            String name = ds.child("name").getValue(String.class);
                            String barcode= ds.getKey();
                            for(ReviewItem item: temparray)
                            {
                                String temp= item.getProduct_name();
                                if(ProductDetails.id!=null)
                                {
                                    if(temp.equals(name)&& barcode.equals(ProductDetails.id))
                                    {
                                        reviewItems.add(new ReviewItem(null,item.getUsername(),item.getRating(), item.getDate(),item.getProduct_name(),item.getFeedback()));
                                    }
                                }
                                if(ProductDetails.alternative_id!=null)
                                {
                                    if(temp.equals(name)&& barcode.equals(ProductDetails.alternative_id))
                                    {
                                        reviewItems.add(new ReviewItem(null,item.getUsername(),item.getRating(), item.getDate(),item.getProduct_name(),item.getFeedback()));
                                    }
                                }
                                if(ProductDetails.favourite_id!=null)
                                {
                                    if(temp.equals(name)&& barcode.equals(ProductDetails.favourite_id))
                                    {
                                        reviewItems.add(new ReviewItem(null,item.getUsername(),item.getRating(), item.getDate(),item.getProduct_name(),item.getFeedback()));
                                    }
                                }
                                if(ProductDetails.history_id!=null)
                                {
                                    if(temp.equals(name)&& barcode.equals(ProductDetails.history_id))
                                    {
                                        reviewItems.add(new ReviewItem(null,item.getUsername(),item.getRating(), item.getDate(),item.getProduct_name(),item.getFeedback()));
                                    }
                                }
                                if(ProductDetails.id!=null)
                                {
                                    if(temp.equals(name)&& barcode.equals(ProductDetails.id))
                                    {
                                        reviewItems.add(new ReviewItem(null,item.getUsername(),item.getRating(), item.getDate(),item.getProduct_name(),item.getFeedback()));
                                    }
                                }
                                sum+=item.getRating();
                                counter++;
                            }
                        }
                        average=sum/counter;
                        adapter= new ReviewAdapter(getActivity(),reviewItems);
                        mRecyclerView.setAdapter(adapter);
                        ratingBar.setRating(average);

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


    }

    private void sortSpinner()
    {
        List<String> list = new ArrayList<String>();
        list.add("Sort by default");
        list.add("Sort by latest");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sort.setAdapter(dataAdapter);

        sort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object item= parent.getItemAtPosition(position);

                if(item.equals("Sort by default") && stars.getSelectedItem().equals("All Stars"))
                {
                        reviewItems.clear();
                        showFeedbacks();
                }

                if(item.equals("Sort by latest"))
                {
                    reviewItems.clear();
                    feedbacksDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for(DataSnapshot ds : dataSnapshot.getChildren()) {
                                String date = ds.child("date").getValue(String.class);
                                String product_name = ds.child("product_name").getValue(String.class);
                                String feedback = ds.child("feedback").getValue(String.class);
                                Long rating= ds.child("rating").getValue(Long.class);
                                String username = ds.child("username").getValue(String.class);


                                if(date!=null && feedback!=null && rating!=0.0 && username!=null)
                                {
                                    reviewItems.add(new ReviewItem(null,username,rating,date,product_name,feedback));
                                }

                            }
                            Collections.sort(reviewItems,new Sortbydate());
                            adapter= new ReviewAdapter(getActivity(),reviewItems);
                            mRecyclerView.setAdapter(adapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void stars_spinner()
    {
        List<String> list = new ArrayList<String>();
        list.add("All Stars");
        list.add("5 Stars");
        list.add("4 Stars");
        list.add("3 Stars");
        list.add("2 Stars");
        list.add("1 Stars");
        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stars.setAdapter(dataAdapter);

        stars.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object item= parent.getItemAtPosition(position);
                if(item.equals("5 Stars"))
                {
                    reviewItems.clear();
                    feedbacksDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for(DataSnapshot ds : dataSnapshot.getChildren()) {
                                String date = ds.child("date").getValue(String.class);
                                String feedback = ds.child("feedback").getValue(String.class);
                                String product_name = ds.child("product_name").getValue(String.class);
                                Long rating= ds.child("rating").getValue(Long.class);
                                String username = ds.child("username").getValue(String.class);

                                if(date!=null && feedback!=null && rating==5 && username!=null)
                                {
                                    reviewItems.add(new ReviewItem(null,username,rating,date,product_name,feedback));
                                }
                            }
                            adapter= new ReviewAdapter(getActivity(),reviewItems);
                            mRecyclerView.setAdapter(adapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                if(item.equals("4 Stars"))
                {
                    reviewItems.clear();
                    feedbacksDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for(DataSnapshot ds : dataSnapshot.getChildren()) {
                                String date = ds.child("date").getValue(String.class);
                                String feedback = ds.child("feedback").getValue(String.class);
                                String product_name = ds.child("product_name").getValue(String.class);
                                Long rating= ds.child("rating").getValue(Long.class);
                                String username = ds.child("username").getValue(String.class);

                                if(date!=null && feedback!=null && rating==4 && username!=null)
                                {
                                    reviewItems.add(new ReviewItem(null,username,rating,date,product_name,feedback));
                                }
                            }
                            adapter= new ReviewAdapter(getActivity(),reviewItems);
                            mRecyclerView.setAdapter(adapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                if(item.equals("3 Stars"))
                {
                    reviewItems.clear();
                    feedbacksDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for(DataSnapshot ds : dataSnapshot.getChildren()) {
                                String date = ds.child("date").getValue(String.class);
                                String feedback = ds.child("feedback").getValue(String.class);
                                String product_name = ds.child("product_name").getValue(String.class);
                                Long rating= ds.child("rating").getValue(Long.class);
                                String username = ds.child("username").getValue(String.class);

                                if(date!=null && feedback!=null && rating==3 && username!=null)
                                {
                                    reviewItems.add(new ReviewItem(null,username,rating,date,product_name,feedback));
                                }
                            }
                            adapter= new ReviewAdapter(getActivity(),reviewItems);
                            mRecyclerView.setAdapter(adapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                if(item.equals("2 Stars"))
                {
                    reviewItems.clear();
                    feedbacksDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for(DataSnapshot ds : dataSnapshot.getChildren()) {
                                String date = ds.child("date").getValue(String.class);
                                String feedback = ds.child("feedback").getValue(String.class);
                                String product_name = ds.child("product_name").getValue(String.class);
                                Long rating= ds.child("rating").getValue(Long.class);
                                String username = ds.child("username").getValue(String.class);

                                if(date!=null && feedback!=null && rating==2 && username!=null)
                                {
                                    reviewItems.add(new ReviewItem(null,username,rating,date,product_name,feedback));
                                }
                            }
                            adapter= new ReviewAdapter(getActivity(),reviewItems);
                            mRecyclerView.setAdapter(adapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                if(item.equals("1 Stars"))
                {
                    reviewItems.clear();
                    feedbacksDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for(DataSnapshot ds : dataSnapshot.getChildren()) {
                                String date = ds.child("date").getValue(String.class);
                                String feedback = ds.child("feedback").getValue(String.class);
                                String product_name = ds.child("product_name").getValue(String.class);
                                Long rating= ds.child("rating").getValue(Long.class);
                                String username = ds.child("username").getValue(String.class);

                                if(date!=null && feedback!=null && rating==1 && username!=null)
                                {
                                    reviewItems.add(new ReviewItem(null,username,rating,date,product_name,feedback));
                                }
                            }
                            adapter= new ReviewAdapter(getActivity(),reviewItems);
                            mRecyclerView.setAdapter(adapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private class Sortbydate implements Comparator<ReviewItem>
    {

        public int compare(ReviewItem a, ReviewItem b)
        {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date d1 = null;
            Date d2 = null;
            try {
                d1 = formatter.parse(a.getDate());
                d2 = formatter.parse(b.getDate());
            } catch (ParseException pe) {
                pe.printStackTrace();
            }

            return d2.compareTo(d1);
        }
    }

}
