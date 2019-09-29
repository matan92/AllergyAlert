package com.example.allergyalert;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class IngredientsData extends AsyncTask<Void,Void,Void> {

    private String words;
    private DatabaseReference allergiesDatabase;
    private SpannableString spannableString;
    private FirebaseUser user;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        user= FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null)
        {
            allergiesDatabase= FirebaseDatabase.getInstance().getReference().child("Allergies")
                    .child(user.getUid());
        }

        try {

            if(ProductDetails.url!=null)
            {
                Document doc= Jsoup.connect(ProductDetails.url).get();
                Element element1= doc.getElementById("ingredients");
                Element element2= doc.getElementById("allergens");
                if(element2!=null)
                {
                    words=element1.text()+" "+element2.text();
                }
                else if(element1==null)
                {
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
                    words=element1.text()+" "+element2.text();
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
                    words=element1.text()+" "+element2.text();
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
                    words=element1.text()+" "+element2.text();
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

        final String[] text= words.split("[ |.,]");

        if(user!=null)
        {
            //Highlight and paint the user's allergies
            allergiesDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for(DataSnapshot ds: dataSnapshot.getChildren())
                    {
                        int index=0;
                        spannableString= new SpannableString(words);
                        String allergy= ds.child("allergy").getValue(String.class);

                        for(int i=0; i<text.length; i++)
                        {
                            if(text[i].contains(allergy))
                            {
                                spannableString.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),index , index+text[i].length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                spannableString.setSpan(new ForegroundColorSpan(Color.RED),index , index+text[i].length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }

                            index+= text[i].length()+1;

                        }
                    }

                    if(spannableString!=null)
                    {
                        IngredientsFragment.textView.setText(spannableString);
                    }
                    else
                    {
                        IngredientsFragment.textView.setText(words);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        //Highlight and paint the host allergies
        else
        {
            for(String allergy: MainActivity.list)
            {
                int index=0;
                spannableString= new SpannableString(words);


                for(int i=0; i<text.length; i++)
                {
                    if(text[i].contains(allergy))
                    {
                        spannableString.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),index , index+text[i].length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spannableString.setSpan(new ForegroundColorSpan(Color.RED),index , index+text[i].length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    index+= text[i].length()+1;

                }
            }

            if(spannableString!=null)
            {
                IngredientsFragment.textView.setText(spannableString);
            }
            else
            {
                IngredientsFragment.textView.setText(words);
            }

        }
        ProductDetails.progressDialog.dismiss();

    }
}
