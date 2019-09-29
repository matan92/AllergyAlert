package com.example.allergyalert;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class IngredientsFragment extends Fragment {

    public static TextView textView;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v= inflater.inflate(R.layout.fragment_ingredients, container, false);

        textView= v.findViewById(R.id.ingredients_tv);
        new IngredientsData().execute();

        return v;
    }

}
