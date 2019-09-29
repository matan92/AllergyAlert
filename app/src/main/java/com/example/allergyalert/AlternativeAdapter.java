package com.example.allergyalert;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Iterator;

public class AlternativeAdapter extends RecyclerView.Adapter<AlternativeAdapter.viewHolder> {

    private Context context;
    private ArrayList<AlternativeItem> items;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private String website="";
    private String barcode="";
    private String product="";

    public AlternativeAdapter(Context c, ArrayList<AlternativeItem> a_items)
    {
        context=c;
        items=a_items;
    }

    @NonNull
    @Override
    public AlternativeAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new AlternativeAdapter.viewHolder(LayoutInflater.from(context).inflate(R.layout.alternative_item,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final AlternativeAdapter.viewHolder viewHolder, int i) {
        viewHolder.t1.setText(items.get(i).getName());
        Glide.with(context).load(items.get(i).getImage()).into(viewHolder.imageView);
        viewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog= new ProgressDialog(context);
                progressDialog.setMessage("Please wait...");
                progressDialog.show();
                product= viewHolder.t1.getText().toString().trim();
                databaseReference=FirebaseDatabase.getInstance().getReference().child("Products").child("Barcodes");
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds: dataSnapshot.getChildren())
                        {
                            barcode=ds.getKey();
                            website= ds.child("url").getValue(String.class);
                            String name= ds.child("name").getValue(String.class);
                            if(name!=null)
                            {
                                if(product.equals(name))
                                {
                                    Intent intent= new Intent(context, ProductDetails.class);
                                    intent.putExtra("alternative_url",website);
                                    intent.putExtra("alternative_id",barcode);
                                    context.startActivity(intent);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                progressDialog.dismiss();
            }

        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    class viewHolder extends RecyclerView.ViewHolder
    {
        TextView t1;
        ImageView imageView;
        RelativeLayout relativeLayout;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            t1=  itemView.findViewById(R.id.altertextView);
            imageView= itemView.findViewById(R.id.altView);
            relativeLayout= itemView.findViewById(R.id.arl);
        }
    }
}
