package com.example.allergyalert;


import android.graphics.Typeface;
import android.os.AsyncTask;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.View;

import android.widget.LinearLayout;


import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import io.opencensus.trace.Span;

import static com.example.allergyalert.ProductDetails.tabLayout;


public class RecomMethod extends AsyncTask<Void,Void,Void> {

    private String words;
    private String user_id="";
    private DatabaseReference databaseScans, allergiesDatabase, productsDatabase;
    private FirebaseAuth firebaseAuth;
    private String currentDateTime = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss").format(new Date());
    private Map<String,Object> new_post;
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ProductDetails.progressDialog.show();
    }


    @Override
    protected Void doInBackground(Void... voids) {
        try {
            firebaseAuth=FirebaseAuth.getInstance();
            if (firebaseAuth.getCurrentUser()!=null)
            {
                user_id= FirebaseAuth.getInstance().getCurrentUser().getUid();
                // This database to save the scan
                databaseScans= FirebaseDatabase.getInstance().getReference().child("Scans").child(user_id);
                // This database to look for user allergies
                allergiesDatabase=FirebaseDatabase.getInstance().getReference().child("Allergies").child(user_id);
                // This database to recognize the product
                productsDatabase= FirebaseDatabase.getInstance().getReference().child("Products").child("Barcodes");
            }

            if(ProductDetails.url!=null)
            {
                //Osem Products
                Document doc= Jsoup.connect(ProductDetails.url).get();
                Element element1= doc.getElementById("ingredients");
                Element element2= doc.getElementById("allergens");
                if(element2!=null)
                {
                    words=element1.text()+element2.text();
                }
                else if(element1==null)
                {
                    //another brands
                    Element element= doc.getElementById("NgProductBop");
                    words=element.text();
                }
                else
                    words=element1.text();
            }

            if(ProductDetails.history_url!=null)
            {
                Document doc= Jsoup.connect(ProductDetails.history_url).get();
                Element element1= doc.getElementById("ingredients");
                Element element2= doc.getElementById("allergens");
                if(element2!=null)
                {
                    words=element1.text()+element2.text();
                }
                else if(element1==null)
                {
                    Element element= doc.getElementById("NgProductBop");
                    words=element.text();
                }
                else
                    words=element1.text();
            }

            if(ProductDetails.favourite_url!=null)
            {
                Document doc= Jsoup.connect(ProductDetails.favourite_url).get();
                Element element1= doc.getElementById("ingredients");
                Element element2= doc.getElementById("allergens");
                if(element2!=null)
                {
                    words=element1.text()+element2.text();
                }
                else if(element1==null)
                {
                    Element element= doc.getElementById("NgProductBop");
                    words=element.text();
                }
                else
                    words=element1.text();
            }

            if(ProductDetails.alternative_url!=null)
            {
                Document doc= Jsoup.connect(ProductDetails.alternative_url).get();
                Element element1= doc.getElementById("ingredients");
                Element element2= doc.getElementById("allergens");
                if(element2!=null)
                {
                    words=element1.text()+element2.text();
                }
                else if(element1==null)
                {
                    Element element= doc.getElementById("NgProductBop");
                    words=element.text();
                }
                else
                    words=element1.text();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if(firebaseAuth.getCurrentUser()==null)
        {
            for(String allergy: MainActivity.list)
            {
                if(words.contains(allergy))
                {
                    ProductDetails.block.setVisibility(View.VISIBLE);
                    ProductDetails.not_for_you.setVisibility(View.VISIBLE);
                    break;
                }
                else
                {
                    ProductDetails.vi.setVisibility(View.VISIBLE);
                    ProductDetails.for_you.setVisibility(View.VISIBLE);
                }
            }
        }
        else
        {
            new_post=new HashMap<>();
            allergiesDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                    while(iterator.hasNext())
                    {
                        String allergy= iterator.next().child("allergy").getValue(String.class);
                        if (words.contains(allergy))
                        {
                            ProductDetails.block.setVisibility(View.VISIBLE);
                            ProductDetails.not_for_you.setVisibility(View.VISIBLE);
                            // we hide Feedbacks&Write Feedback Fragments if the user get negative recommendation
                            ((LinearLayout) tabLayout.getTabAt(1).view).setVisibility(View.GONE);
                            ((LinearLayout) tabLayout.getTabAt(2).view).setVisibility(View.GONE);
                            // We are going out of the loop because one of the ingredients of the product causes an allergy to the
                            // user and there is no point in continuing to look for the other allergies
                            break;
                        }
                        if(!iterator.hasNext())
                        {
                            ProductDetails.vi.setVisibility(View.VISIBLE);
                            ProductDetails.for_you.setVisibility(View.VISIBLE);
                        }

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

           if(ProductDetails.id!=null)
           {
               new_post.put("date",currentDateTime);
               productsDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       for (DataSnapshot ds : dataSnapshot.getChildren()) {
                           String barcode = ds.getKey();
                           if (barcode != null)
                           {
                               if (barcode.equals(ProductDetails.id))
                               {
                                   String name = ds.child("name").getValue(String.class);
                                   String image = ds.child("image").getValue(String.class);

                                   if(name!=null && image!=null)
                                   {
                                       new_post.put("product_name",name);
                                       new_post.put("image",image);
                                       databaseScans.push().setValue(new_post);
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

        ProductDetails.progressDialog.dismiss();
    }


}
