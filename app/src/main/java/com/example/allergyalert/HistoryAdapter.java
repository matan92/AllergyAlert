package com.example.allergyalert;

import android.app.ProgressDialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.HashMap;
import java.util.Iterator;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.viewHolder> {

   private Context context;
   private ArrayList<HistoryItem> items;
   private DatabaseReference databaseReference;
    private String website="";
    private String barcode="";
    private String product="";

   public HistoryAdapter(Context c, ArrayList<HistoryItem> h_items)
   {
       context=c;
       items=h_items;
   }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new viewHolder(LayoutInflater.from(context).inflate(R.layout.history_item,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final viewHolder viewHolder, int i) {
        viewHolder.t1.setText(items.get(i).getProduct_name());
        viewHolder.t2.setText(items.get(i).getDate());
        Glide.with(context).load(items.get(i).getImage()).into(viewHolder.imageView);
        viewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog progressDialog= new ProgressDialog(context);
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
                                    intent.putExtra("history_url",website);
                                    intent.putExtra("history_id",barcode);
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

    public void removeItem(int position) {
        final HistoryItem selectedItem= items.get(position);
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("Scans")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext())
                {
                    dataSnapshot= iterator.next();
                    HistoryItem historyItem = dataSnapshot.getValue(HistoryItem.class);
                    if(historyItem.getProduct_name().equals(selectedItem.getProduct_name())&& historyItem.getImage().equals(selectedItem.getImage())
                    && historyItem.getDate().equals(selectedItem.getDate()))
                    {
                        dataSnapshot.getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

       items.remove(position);
       notifyItemRemoved(position);
    }

    public void restoreItem(HistoryItem item, int position)
    {
        HashMap<String,Object> repost= new HashMap<>();
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("Scans")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        repost.put("date",item.getDate());
        repost.put("product_name",item.getProduct_name());
        repost.put("image",item.getImage());

        databaseReference.push().setValue(repost);

        items.add(position,item);
        notifyItemInserted(position);
    }

    class viewHolder extends RecyclerView.ViewHolder
   {
       TextView t1,t2;
       ImageView imageView;
       RelativeLayout relativeLayout;
       public viewHolder(@NonNull View itemView) {
           super(itemView);
           t1=(TextView)itemView.findViewById(R.id.historytextView);
           t2=(TextView)itemView.findViewById(R.id.historytextView2);
           imageView=(ImageView)itemView.findViewById(R.id.historyView);
           relativeLayout= itemView.findViewById(R.id.hrl);
       }
   }


}
