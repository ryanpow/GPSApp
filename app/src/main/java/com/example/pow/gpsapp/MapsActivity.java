 package com.example.pow.gpsapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;


 public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Button btnContact= (Button)findViewById(R.id.btnContact);
        btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortPopup(MapsActivity.this,null);
            }
        });
    }

     private void showSortPopup(final Activity context, Point p)
     {
         // Inflate the popup_layout.xml
         RelativeLayout viewGroup = (RelativeLayout) context.findViewById(R.id.ContactLayout);
         LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View layout = layoutInflater.inflate(R.layout.contactpopup, viewGroup);

         // Creating the PopupWindow
         final PopupWindow popup = new PopupWindow(context);
         popup.setContentView(layout);
         popup.setWidth(RelativeLayout.LayoutParams.WRAP_CONTENT);
         popup.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
         popup.setFocusable(true);

         // Some offset to align the popup a bit to the left, and a bit down, relative to button's position.
         int OFFSET_X = -20;
         int OFFSET_Y = 95;

         // Clear the default translucent background
         popup.setBackgroundDrawable(new BitmapDrawable());
         // Displaying the popup at the specified location, + offsets.
         popup.showAtLocation(layout, Gravity.CENTER, OFFSET_X, OFFSET_Y);

         Button Back = (Button) layout.findViewById(R.id.btnBack);
         Back.setOnClickListener(new View.OnClickListener() {

             @Override
             public void onClick(View v) {
                 popup.dismiss();
             }
         });
         // Getting a reference to Close button, and close the popup when clicked.


     }

     public void onZoom(View view)
     {
         if(view.getId() == R.id.btnzoomin)
         {
             mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
         }
         if(view.getId() == R.id.btnzoomout)
         {
             mMap.animateCamera(CameraUpdateFactory.zoomTo(8), 2000, null);
         }
     }

    public void onSearch(View view)
    {
        EditText location_tf = (EditText)findViewById(R.id.txtAddress);
        String location = location_tf.getText().toString();
        List<Address> addressList = null;
        if(location != null || location.equals(""))
        {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude() , address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        LatLng coordinate = new LatLng(1.3468, 103.9326);
        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
        mMap.animateCamera(location);
    }
}
