 package com.example.pow.gpsapp;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.pow.gpsapp.SelectUserAdapter;
import com.example.pow.gpsapp.SelectUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


 public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
     ArrayList<SelectUser> selectUsers;
     List<SelectUser> temp;
     ListView listView;
     Cursor phones, email;
     ContentResolver resolver;
     SearchView search;
     SelectUserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

     public void contactpopup()
     {
         setContentView(R.layout.contactpopup);

         selectUsers = new ArrayList<SelectUser>();
         resolver = this.getContentResolver();
         listView = (ListView) findViewById(R.id.contacts_list);

         phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
         LoadContact loadContact = new LoadContact();
         loadContact.execute();

         search = (SearchView) findViewById(R.id.searchView);

         //*** setOnQueryTextListener ***
         search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

             @Override
             public boolean onQueryTextSubmit(String query) {
                 // TODO Auto-generated method stub

                 return false;
             }

             @Override
             public boolean onQueryTextChange(String newText) {
                 // TODO Auto-generated method stub
                 adapter.filter(newText);
                 return false;
             }
         });
     }

     // Load data on background
     class LoadContact extends AsyncTask<Void, Void, Void> {
         @Override
         protected void onPreExecute() {
             super.onPreExecute();

         }

         @Override
         protected Void doInBackground(Void... voids) {
             // Get Contact list from Phone

             if (phones != null) {
                 Log.e("count", "" + phones.getCount());
                 if (phones.getCount() == 0) {
                     Toast.makeText(MapsActivity.this, "No contacts in your contact list.", Toast.LENGTH_LONG).show();
                 }

                 while (phones.moveToNext()) {
                     String id = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                     String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                     String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                     String EmailAddr = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA2));
                     SelectUser selectUser = new SelectUser();
                     selectUser.setName(name);
                     selectUser.setPhone(phoneNumber);
                     selectUser.setEmail(id);
                     selectUser.setCheckedBox(false);
                     selectUsers.add(selectUser);
                 }
             } else {
                 Log.e("Cursor close 1", "----------------");
             }
             //phones.close();
             return null;
         }

         @Override
         protected void onPostExecute(Void aVoid) {
             super.onPostExecute(aVoid);
             adapter = new SelectUserAdapter(selectUsers, MapsActivity.this);
             listView.setAdapter(adapter);

             // Select item on listclick
             listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                 @Override
                 public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                     Log.e("search", "here---------------- listener");

                     SelectUser data = selectUsers.get(i);
                 }
             });

             listView.setFastScrollEnabled(true);
         }
     }

     @Override
     protected void onStop() {
         super.onStop();
         phones.close();
     }

     public void contact_map(View view)
     {
         setContentView(R.layout.activity_maps);
         SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                 .findFragmentById(R.id.map2);
         mapFragment.getMapAsync(this);
     }
     public void onSwitch(View view)
     {
         if(view.getId() == R.id.btnBack)
         {
             setContentView(R.layout.contact_map);
         }
         if(view.getId() == R.id.btnContact)
         {
             setContentView(R.layout.contactpopup);
             contactpopup();
         }
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
     public void onZoom2(View view)
     {
         if(view.getId() == R.id.btnzoomin2)
         {
             mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
         }
         if(view.getId() == R.id.btnzoomout2)
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
     public void onSearch2(View view)
     {
         EditText location_tf = (EditText)findViewById(R.id.txtAddress2);
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
