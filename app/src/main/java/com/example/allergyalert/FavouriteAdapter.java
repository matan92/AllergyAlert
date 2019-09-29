package com.example.allergyalert;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

public class FavouriteAdapter extends RecyclerView.Adapter<FavouriteAdapter.viewHolder> {

    private Context context;
    private ArrayList<FavouriteItem> items;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private String website="";
    private String barcode="";
    private String product="";

    public FavouriteAdapter(Context c, ArrayList<FavouriteItem> f_items)
    {
        context=c;
        items=f_items;
    }

    @NonNull
    @Override
    public FavouriteAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new FavouriteAdapter.viewHolder(LayoutInflater.from(context).inflate(R.layout.favourite_item,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final FavouriteAdapter.viewHolder viewHolder, int i) {
        viewHolder.textView.setText(items.get(i).getProduct_name());
        Glide.with(context).load(items.get(i).getImage()).into(viewHolder.imageView);
        viewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog= new ProgressDialog(context);
                progressDialog.setMessage("Please wait...");
                progressDialog.show();
                product= viewHolder.textView.getText().toString().trim();
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
                                    intent.putExtra("favourite_url",website);
                                    intent.putExtra("favourite_id",barcode);
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
        final FavouriteItem selectedItem= items.get(position);
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("Favourites")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext())
                {
                    dataSnapshot= iterator.next();
                    FavouriteItem favouriteItem = dataSnapshot.getValue(FavouriteItem.class);
                    if(favouriteItem.getProduct_name().equals(selectedItem.getProduct_name())&&
                            favouriteItem.getImage().equals(selectedItem.getImage()))
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

    public void restoreItem(FavouriteItem item, int position)
    {
        HashMap<String,Object> repost= new HashMap<>();
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("Favourites")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        repost.put("product_name",item.getProduct_name());
        repost.put("image",item.getImage());

        databaseReference.push().setValue(repost);

        items.add(position,item);
        notifyItemInserted(position);
    }


    class viewHolder extends RecyclerView.ViewHolder
    {
        TextView textView;
        ImageView imageView;
        RelativeLayout relativeLayout;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            textView= itemView.findViewById(R.id.favourite_textView);
            imageView= itemView.findViewById(R.id.favouriteView);
            relativeLayout= itemView.findViewById(R.id.frl);
        }
    }


}
