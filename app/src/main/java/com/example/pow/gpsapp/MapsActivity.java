 package com.example.pow.gpsapp;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.SmsManager;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.pow.gpsapp.SelectUserAdapter;
import com.example.pow.gpsapp.SelectUser;
import com.google.android.gms.common.ConnectionResult;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



 public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{


     public static GoogleMap mMap;
     TextView txt_wifi;
     private static final int LOCATION_REQUEST_CODE = 101;
     ArrayList<SelectUser> selectUsers;
     List<SelectUser> temp;
     ListView listView;
     Cursor phones, email;
     ContentResolver resolver;
     SearchView search;
     SelectUserAdapter adapter;
     private Location mLastLocation;
     public static TextView mainLabel;
     static String txtLocation,txtWifi;
     public LocationManager mLocationManager;
     String wifiresult=IncomingSMSReceiver.wifiresult;
     /**
      * ATTENTION: This was auto-generated to implement the App Indexing API.
      * See https://g.co/AppIndexing/AndroidStudio for more information.
      */
     private GoogleApiClient client;
     static double latitude= IncomingSMSReceiver.latitude;
     static double longitude= IncomingSMSReceiver.longitude;
     WifiManager mainWifi;
     WifiReceiver receiverWifi;
     List<ScanResult> wifiList;
     StringBuilder sb = new StringBuilder();

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_maps);
         requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                 LOCATION_REQUEST_CODE);
         SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                 .findFragmentById(R.id.map);
         mapFragment.getMapAsync(this);
         System.out.println("Test");
         mainLabel = (TextView) findViewById(R.id.mainLabel);
         client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
         int LOCATION_REFRESH_TIME = 0;
         int LOCATION_REFRESH_DISTANCE = 0;

         mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
         mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                 LOCATION_REFRESH_DISTANCE, mLocationListener);
         mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
         receiverWifi = new WifiReceiver();
         registerReceiver(receiverWifi, new IntentFilter(
                 WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
         mainWifi.startScan();
     }
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.add(0, 0, 0, "Refresh");
         return super.onCreateOptionsMenu(menu);}
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
         mainWifi.startScan();
         return super.onMenuItemSelected(featureId, item);}
     protected void onPause() {
         unregisterReceiver(receiverWifi);
         super.onPause();


     }
     class WifiReceiver extends BroadcastReceiver {
         public void onReceive(Context c, Intent intent) {
             sb = new StringBuilder();
             wifiList = mainWifi.getScanResults();
             int count=0;
                 for (ScanResult result : wifiList) {
                     if(count>2)
                     {
                         break;
                     }
                     sb.append((result.SSID).toString());
                     sb.append(",");
                     sb.append((result.BSSID).toString());
                     sb.append(",");
                     sb.append(String.valueOf(result.level));
                     sb.append("\n");
                     count++;

             }
             System.out.println(sb);
             txtWifi="dHJjhnsjJ@"+sb;
             System.out.println(txtWifi);
         }
     }
     private final android.location.LocationListener mLocationListener = new android.location.LocationListener() {
         @Override
         public void onLocationChanged(Location location) {
             //code
             System.out.println("onLocationChanged");

             mLastLocation = location;

             txtLocation = ("AQOZkasQSM"+":"+
                      String.valueOf(location.getLatitude()) + "," +
                      String.valueOf(location.getLongitude()));
             mainLabel.setText(txtLocation);
         }

         @Override
         public void onStatusChanged(String provider, int status, Bundle extras) {
             System.out.println("onStatusChanged");
         }

         @Override
         public void onProviderEnabled(String provider) {
             System.out.println("onProviderEnabled");
         }

         @Override
         public void onProviderDisabled(String provider) {
             System.out.println("onProviderDisabled");
             //turns off gps services
         }
     };
     public void wifi_list(){
         setContentView(R.layout.wifi_list);
         txt_wifi = (TextView) findViewById(R.id.txt_wifi);
         txt_wifi.setText(wifiresult);

     }

     public void contactpopup() {
         setContentView(R.layout.contactpopup);
         ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.READ_CONTACTS);
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


                 return false;
             }

             @Override
             public boolean onQueryTextChange(String newText) {

                 adapter.filter(newText);
                 return false;
             }
         });
         (findViewById(R.id.btnSelect)).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // user BoD suggests using Intent.ACTION_PICK instead of .ACTION_GET_CONTENT to avoid the chooser
                 Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                 // BoD con't: CONTENT_TYPE instead of CONTENT_ITEM_TYPE
                 intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                 startActivityForResult(intent, 1);
             }
         });

     }

     @Override
     public void onStart() {
         super.onStart();

         // ATTENTION: This was auto-generated to implement the App Indexing API.
         // See https://g.co/AppIndexing/AndroidStudio for more information.
         client.connect();
         Action viewAction = Action.newAction(
                 Action.TYPE_VIEW, // TODO: choose an action type.
                 "Maps Page", // TODO: Define a title for the content shown.
                 // TODO: If you have web page content that matches this app activity's content,
                 // make sure this auto-generated web page URL is correct.
                 // Otherwise, set the URL to null.
                 Uri.parse("http://host/path"),
                 // TODO: Make sure this auto-generated app URL is correct.
                 Uri.parse("android-app://com.example.pow.gpsapp/http/host/path")
         );
         AppIndex.AppIndexApi.start(client, viewAction);
     }

     @Override
     public void onStop() {
         super.onStop();

         // ATTENTION: This was auto-generated to implement the App Indexing API.
         // See https://g.co/AppIndexing/AndroidStudio for more information.
         Action viewAction = Action.newAction(
                 Action.TYPE_VIEW, // TODO: choose an action type.
                 "Maps Page", // TODO: Define a title for the content shown.
                 // TODO: If you have web page content that matches this app activity's content,
                 // make sure this auto-generated web page URL is correct.
                 // Otherwise, set the URL to null.
                 Uri.parse("http://host/path"),
                 // TODO: Make sure this auto-generated app URL is correct.
                 Uri.parse("android-app://com.example.pow.gpsapp/http/host/path")
         );
         AppIndex.AppIndexApi.end(client, viewAction);
         client.disconnect();
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

     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (data != null) {
             Uri uri = data.getData();
             String number = "";
             if (uri != null) {
                 Cursor c = null;
                 try {
                     c = getContentResolver().query(uri, new String[]{
                                     ContactsContract.CommonDataKinds.Phone.NUMBER,
                                     ContactsContract.CommonDataKinds.Phone.TYPE},
                             null, null, null);

                     if (c != null && c.moveToFirst()) {
                         number = c.getString(0);
                         int type = c.getInt(1);
                         showSelectedNumber(type, number);
                     }

                     Log.i("Send SMS", "");
                     String message = "Request";
                     TextView textview = (TextView) findViewById(R.id.txtNumber);
                     textview.setText(number);
                     try {
                         SmsManager smsManager = SmsManager.getDefault();
                         smsManager.sendTextMessage(number, null, message, null, null);
                         Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
                     } catch (Exception e) {
                         Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
                         e.printStackTrace();
                     }
                 } finally {
                     if (c != null) {
                         c.close();
                     }
                 }
             }

         }

     }

     public void showSelectedNumber(int type, String number) {
         Toast.makeText(this, type + ": " + number, Toast.LENGTH_LONG).show();
     }

     public void onSwitch(View view) {
         if (view.getId() == R.id.btnBack) {
             setContentView(R.layout.contact_map);
             SupportMapFragment mapFragment2 = (SupportMapFragment) getSupportFragmentManager()
                     .findFragmentById(R.id.map2);
             //mapFragment2.getMapAsync(this);
         }
         if (view.getId() == R.id.btnContact) {
             setContentView(R.layout.contactpopup);
             contactpopup();
         }
         if (view.getId() == R.id.btnWifi) {
             setContentView(R.layout.wifi_list);
             wifi_list();
         }
         if (view.getId() == R.id.btnBack2) {
             setContentView(R.layout.contact_map);
             SupportMapFragment mapFragment2 = (SupportMapFragment) getSupportFragmentManager()
                     .findFragmentById(R.id.map2);
             //mapFragment2.getMapAsync(this);
         }
     }

     public void Map2(GoogleMap googleMap2, double x, double y) {
         mMap = googleMap2;
         ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
         ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
         mMap.setMyLocationEnabled(true);
         LatLng coordinate = new LatLng(x, y);
         CameraUpdate location = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
         mMap.animateCamera(location);
     }

     public void onZoom(View view) {
         if (view.getId() == R.id.btnzoomin) {
             mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
         }
         if (view.getId() == R.id.btnzoomout) {
             mMap.animateCamera(CameraUpdateFactory.zoomTo(8), 2000, null);
         }
     }

     public void onZoom2(View view) {
         if (view.getId() == R.id.btnzoomin2) {
             mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
         }
         if (view.getId() == R.id.btnzoomout2) {
             mMap.animateCamera(CameraUpdateFactory.zoomTo(8), 2000, null);
         }
     }

     public void onSearch(View view) {
         EditText location_tf = (EditText) findViewById(R.id.txtAddress);
         String location = location_tf.getText().toString();
         List<Address> addressList = null;
         if (location != null || location.equals("")) {
             Geocoder geocoder = new Geocoder(this);
             try {
                 addressList = geocoder.getFromLocationName(location, 1);
             } catch (IOException e) {
                 e.printStackTrace();
             }
             Address address = addressList.get(0);
             LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
             mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
             mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
         }
     }
     public static void onLocation(View view){

         LatLng latLng = new LatLng(latitude,longitude);
         mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
         mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
     }

     public void onSearch2(View view) {
         EditText location_tf = (EditText) findViewById(R.id.txtAddress2);
         String location = location_tf.getText().toString();
         List<Address> addressList = null;
         if (location != null || location.equals("")) {
             Geocoder geocoder = new Geocoder(this);
             try {
                 addressList = geocoder.getFromLocationName(location, 1);
             } catch (IOException e) {
                 e.printStackTrace();
             }
             Address address = addressList.get(0);
             LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
             mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
             mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
         }
     }
     protected void requestPermission(String permissionType, int
             requestCode) {
         int permission = ContextCompat.checkSelfPermission(this,
                 permissionType);
         if (permission != PackageManager.PERMISSION_GRANTED) {
             ActivityCompat.requestPermissions(this,
                     new String[]{permissionType}, requestCode
             );
         }
     }

     @Override
     public void onRequestPermissionsResult(int requestCode,
                                            String permissions[], int[]
                                                    grantResults) {
         switch (requestCode) {
             case LOCATION_REQUEST_CODE: {
                 if (grantResults.length == 0
                         || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                 {
                     Toast.makeText(this, "Unable to show location - permission required", Toast.LENGTH_LONG).show();
                 }
                 return;
             }
         }
     }

     @Override
     public void onMapReady(GoogleMap googleMap) {
         mMap = googleMap;
         if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
             return;
         }
         mMap.setMyLocationEnabled(true);
         LatLng coordinate = new LatLng(1.3468, 103.9326);
         CameraUpdate location = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
         mMap.animateCamera(location);
     }
     @Override
     public void onConnected(Bundle bundle) {

     }

     @Override
     public void onConnectionSuspended(int i) {

     }

     @Override
     public void onConnectionFailed(ConnectionResult connectionResult) {

     }
 }
