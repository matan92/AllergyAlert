package com.example.allergyalert;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.allergyalert.R;

import java.util.ArrayList;

public class SettingsFragment extends Fragment {

    private Intent intent;
    private ListView listView;

    @Nullable
    TextView share;
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_settings, container, false);



        listView= view.findViewById(R.id.list_view);

        final ArrayList<String> arrayList= new ArrayList<>();
        arrayList.add("Share");
        arrayList.add("Update");
        arrayList.add("Rate Us");
        arrayList.add("Contact us");

        ArrayAdapter<String> arrayAdapter= new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,arrayList);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(arrayList.get(position).equals("Share"))
                {
                    intent= new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    // what you want to send
                    String sharebody= "Suffering from allergies? Allergy Alert is the solution for you.";
                    // subject
                    String sharesub= "Download App";

                    intent.putExtra(Intent.EXTRA_SUBJECT, sharesub);
                    intent.putExtra(Intent.EXTRA_TEXT, sharebody);
                    // pop-up title
                    startActivity(Intent.createChooser(intent, "Share the app"));
                }
            }
        });

        return view;
    }
}
