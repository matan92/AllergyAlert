package com.example.allergyalert;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.viewHolder> {

    private Context context;
    private ArrayList<ReviewItem> items;

    public ReviewAdapter(Context c, ArrayList<ReviewItem> r_items)
    {
        context=c;
        items=r_items;
    }

    @NonNull
    @Override
    public ReviewAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ReviewAdapter.viewHolder(LayoutInflater.from(context).inflate(R.layout.review_item,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapter.viewHolder viewHolder, int i) {
        viewHolder.t1.setText(items.get(i).getUsername());
        viewHolder.t2.setText(items.get(i).getDate());
        viewHolder.t3.setText(items.get(i).getFeedback());
        viewHolder.t4.setText(items.get(i).getProduct_name());
        viewHolder.ratingBar.setRating(items.get(i).getRating());


    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void removeItem(int position) {
        final ReviewItem selectedItem = items.get(position);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Reviews");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    dataSnapshot = iterator.next();
                    ReviewItem reviewItem = dataSnapshot.getValue(ReviewItem.class);
                    if (reviewItem.getDate().equals(selectedItem.getDate())
                            && reviewItem.getFeedback().equals(selectedItem.getFeedback())
                            && reviewItem.getRating().equals(selectedItem.getRating())
                            && reviewItem.getUsername().equals(selectedItem.getUsername())
                            &&reviewItem.getProduct_name().equals(selectedItem.getProduct_name())
                            && reviewItem.getUserId().equals(selectedItem.getUserId()))
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

    public void restoreItem(ReviewItem item, int position)
    {
        HashMap<String,Object> repost= new HashMap<>();
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("Reviews");

        repost.put("product_name",reviewsActivity.productName);
        repost.put("username",item.getUsername());
        repost.put("id",item.getUserId());
        repost.put("rating",item.getRating());
        repost.put("date",item.getDate());
        repost.put("feedback",item.getFeedback());

        databaseReference.push().setValue(repost);

        items.add(position,item);
        notifyItemInserted(position);
    }

    class viewHolder extends RecyclerView.ViewHolder
    {
        TextView t1,t2, t3, t4;
        RatingBar ratingBar;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            t1= itemView.findViewById(R.id.username_view);
            t2= itemView.findViewById(R.id.date_view);
            t3= itemView.findViewById(R.id.feedback_view);
            t4= itemView.findViewById(R.id.productName);
            ratingBar= itemView.findViewById(R.id.ratingbar_view);
        }
    }
}
