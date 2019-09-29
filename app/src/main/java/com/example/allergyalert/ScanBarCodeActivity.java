package com.example.allergyalert;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class ScanBarCodeActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView scannerView;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private String product_url="", website="";
    private URL url;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        ActionBar actionBar= getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Product scan");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        databaseReference= FirebaseDatabase.getInstance().getReference().child("Products").child("Barcodes");

        progressDialog= new ProgressDialog(this);

        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);
        int currentApiVersion = Build.VERSION.SDK_INT;

        if(!(currentApiVersion >=  Build.VERSION_CODES.M))
        {
                requestPermission();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private boolean checkPermission()
    {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission()
    {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    @Override
    public void onResume() {
        super.onResume();

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                if(scannerView == null) {
                    scannerView = new ZXingScannerView(this);
                    setContentView(scannerView);
                }
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            } else {
                requestPermission();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scannerView.setAutoFocus(false);
        scannerView.stopCamera();
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(!cameraAccepted) {
                        Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access and camera", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{CAMERA},
                                                            REQUEST_CAMERA);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new androidx.appcompat.app.AlertDialog.Builder(ScanBarCodeActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    // This method checks the scanned product
    @Override
    public void handleResult(final Result result) {

        // Barcode number
        final String num= result.getText();

        progressDialog.setMessage("Please wait...");
        /*new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                progressDialog.show();
            }
        },100);*/

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                for(DataSnapshot ds: dataSnapshot.getChildren())
                {
                    String barcode= ds.getKey();
                    if(barcode!=null)
                    {
                        if(barcode.equals(num))
                        {
                            try {

                                product_url= ds.child("url").getValue(String.class);
                                // This class checks if the url is valid
                                url= new URL(product_url);
                                website= url.toString();
                                intent= new Intent(ScanBarCodeActivity.this, ProductDetails.class);
                                intent.putExtra("url",website);
                                intent.putExtra("id",num);
                                startActivity(intent);
                            }
                            catch (MalformedURLException e)
                            {
                                Log.d("Invalid url",e.getMessage());
                            }

                        }
                    }

                }

                // If the barcode number is not in the database
                if(product_url.equals(""))
                {
                    Toast.makeText(ScanBarCodeActivity.this, "We couldn't find this product. Please try again later.", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        progressDialog.dismiss();

        /*
        @SuppressLint("StaticFieldLeak")
        class getPage extends AsyncTask<Void,Void,Void>
        {
           private String url;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Document document= Jsoup.connect("https://m.rami-levy.co.il/default.asp?ItemType=1&pageIndex=1&pageCount=1&strCurSearch=&strSearch=" +result.getText()).get();
                    Element link = document.selectFirst("div.prodDesc clearfix > a");
                    url = link.attr("href");

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                intent= new Intent(ScanBarCodeActivity.this, ProductDetails.class);
                intent.putExtra("url",website);
                intent.putExtra("id",result.getText());
                startActivity(intent);

            }
        }

        new getPage().execute();*/


    }


}
