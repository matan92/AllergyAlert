package com.example.allergyalert;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.allergyalert.R;

public class AboutUsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_about_us, container, false);
        Button button= v.findViewById(R.id.termsBtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                termsData();
            }
        });
        return v;
    }

    private void termsData() {
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());

        TextView title = new TextView(getActivity());
        title.setText("Important Information");
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setTextSize(20);

        builder.setCustomTitle(title);

        LinearLayout linearLayout= new LinearLayout(getActivity());
        linearLayout.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        linearLayout.setPadding(10, 10, 10, 10);

        TextView content= new TextView(getActivity());
        content.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        content.setText("The binding product data is only on the packaging of the product. Before consuming, rely only on what is stated in them." +
                "The details on the product components must not be relied on, there may be errors or discrepancies in the information, the exact data appearing on the product. The data must be checked again on the product packaging before use.");
        linearLayout.addView(content);

        builder.setView(linearLayout);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }
}
