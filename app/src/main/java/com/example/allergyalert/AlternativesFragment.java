package com.example.allergyalert;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.allergyalert.AlternativeAdapter;
import com.example.allergyalert.AlternativeItem;
import com.example.allergyalert.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class AlternativesFragment extends Fragment {
    private ArrayList<AlternativeItem>items, alternativeItems;
    private DatabaseReference databaseProducts, scannedProduct;
    AlternativeAdapter adapter;
    private RecyclerView mRecyclerView;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v= inflater.inflate(R.layout.fragment_alternatives, container, false);

        progressDialog= new ProgressDialog(getActivity());
        progressDialog.setMessage("Please wait...");

        items= new ArrayList<>();
        alternativeItems= new ArrayList<>();

        mRecyclerView = v.findViewById(R.id.arv);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        showAlternatives();

        return v;
    }

    private void showAlternatives()
    {
        if(ProductDetails.id!=null)
        {
            scannedProduct = FirebaseDatabase.getInstance().getReference().child("Products").child("Barcodes").child(ProductDetails.id);
        }
        else if(ProductDetails.history_id!=null)
        {
            scannedProduct = FirebaseDatabase.getInstance().getReference().child("Products").child("Barcodes").child(ProductDetails.history_id);
        }
        else if(ProductDetails.favourite_id!=null)
        {
            scannedProduct = FirebaseDatabase.getInstance().getReference().child("Products").child("Barcodes").child(ProductDetails.favourite_id);
        }
        else
        {
            scannedProduct = FirebaseDatabase.getInstance().getReference().child("Products").child("Barcodes").child(ProductDetails.alternative_id);
        }

            // All the products that stored in the database
            databaseProducts= FirebaseDatabase.getInstance().getReference().child("Products").child("Barcodes");
            databaseProducts.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    progressDialog.show();

                    for(DataSnapshot ds: dataSnapshot.getChildren())
                    {
                        AlternativeItem alternativeItem= ds.getValue(AlternativeItem.class);
                        items.add(alternativeItem);
                    }

                    scannedProduct.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(AlternativeItem item: items)
                            {
                                for(DataSnapshot ds: dataSnapshot.getChildren())
                                {
                                    String name = ds.getValue(String.class);
                                    // we enter each word to a cell in the array
                                    String [] arr = name.split(" ");
                                    if(item.getName().contains(arr[0])&& !item.getName().equals(name))
                                    {
                                        alternativeItems.add(item);
                                    }
                                }
                            }
                            adapter= new AlternativeAdapter(getActivity(),alternativeItems);
                            mRecyclerView.setAdapter(adapter);
                            progressDialog.dismiss();
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

}
