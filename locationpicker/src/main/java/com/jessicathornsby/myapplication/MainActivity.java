package com.jessicathornsby.myapplication;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;


public class MainActivity extends AppCompatActivity {

    TextView placeName;
    TextView placeAddress;
    Button pickPlaceButton;
    private final static int FINE_LOCATION = 100;
    private final static int PLACE_PICKER_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();



        placeName = (TextView) findViewById(R.id.placeName);
        placeAddress = (TextView) findViewById(R.id.placeAddress);
        pickPlaceButton = (Button) findViewById(R.id.pickPlaceButton);
        pickPlaceButton.setOnClickListener(new View.OnClickListener() {

//Add a click handler that’ll start the place picker//

            @Override
            public void onClick(View view) {

//Use PlacePicker.IntentBuilder() to construct an Intent//

                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    Intent intent = builder.build(MainActivity.this);

//Create a PLACE_PICKER_REQUEST constant that we’ll use to obtain the selected place//

                    startActivityForResult(intent, PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void requestPermission() {

//Check whether our app has the fine location permission, and request it if necessary//

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION);
            }
        }
    }

//Handle the result of the permission request//

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case FINE_LOCATION:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "This app requires location permissions to detect your location!", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }

//Retrieve the results from the place picker dialog//

    @Override

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {

//If the resultCode is OK...//

        if (resultCode == RESULT_OK) {

//...then retrieve the Place object, using PlacePicker.getPlace()//

            Place place = PlacePicker.getPlace(this, data);

//Extract the place’s name and display it in the TextView//

            placeName.setText(place.getName());

//Extract the place’s address, and display it in the TextView//

            placeAddress.setText(place.getAddress());

//If the user exited the dialog without selecting a place...//

        } else if (resultCode == RESULT_CANCELED) {

//...then display the following toast//

            Toast.makeText(getApplicationContext(), "No place selected", Toast.LENGTH_LONG).show();

        }
    }
}

