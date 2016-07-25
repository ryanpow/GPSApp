package com.example.pow.gpsapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.common.ConnectionResult;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    public static GoogleMap mMap;
    private static final int LOCATION_REQUEST_CODE = 101;
    ListView friendList;
    ContentResolver resolver;
    SearchView search;
    private Location mLastLocation;
    public static TextView txtCode,mainLabel,txtAdd,txtSSID1, txtSSID2, txtSSID3, txtMAC1, txtMAC2, txtMAC3, txtlevel1, txtlevel2, txtlevel3,usernameText,passwordText,usernametxt,passwordtxt;
    static String txtLocation, txtWifi, txtusername, txtpassword, sqlStatement, getSQLUsername, getSQLPassword,randomID,UserID,usernametxt2,passwordtxt2;
    public LocationManager mLocationManager;
    boolean login=false;
    private GoogleApiClient client;
    Double latitude = IncomingSMSReceiver.latitude;
    Double longitude = IncomingSMSReceiver.longitude;
    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    StringBuilder sb = new StringBuilder();
    private java.util.Random rndGenerator = new java.util.Random();
    private int testID;
    public final static int NUMBER_OF_VALUES = 9999;
    SimpleAdapter ADAhere;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                    LOCATION_REQUEST_CODE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_SMS}, 123);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 123);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS}, 123);
        }
        setContentView(R.layout.loginmenu);
        login=false;
        login();
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                LOCATION_REQUEST_CODE);
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
     public void onDestroyView() {
         FragmentManager fm = getSupportFragmentManager();
         Fragment fragment = (fm.findFragmentById(R.id.map));
         FragmentTransaction ft = fm.beginTransaction();
         ft.remove(fragment);
         ft.commit();
     }
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.add(0, 0, 0, "Refresh");
         return super.onCreateOptionsMenu(menu);}
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
         mainWifi.startScan();
         return super.onMenuItemSelected(featureId, item);}

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
                     sb.append("!");
                     count++;

             }
             txtWifi="dHJjhnsjJ@"+sb;
             sqlStatement = "UPDATE GPSInfo set Wifi='"+sb+"' where UserID='"+UserID+"'";
             new WriteDatabase().execute();
         }
     }
    public void Register(){
        usernametxt = (TextView) findViewById(R.id.usernametxt);
        passwordtxt = (TextView) findViewById(R.id.passwordtxt);
        Button btnRegister = (Button) findViewById(R.id.btnsaveregister);
        testID = rndGenerator.nextInt(NUMBER_OF_VALUES);
        randomID=String.format("%04d", testID);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sqlStatement = "insert into GPSAccount values('"+randomID+"','"+usernametxt.getText()+"','"+passwordtxt.getText()+"');insert into GPSInfo values('"+randomID+"','null','null')";
                new WriteDatabase().execute();
                setContentView(R.layout.loginmenu);
                login();
            }
        });
    }
     private final android.location.LocationListener mLocationListener = new android.location.LocationListener() {
         @Override
         public void onLocationChanged(Location location) {
             //code
             System.out.println("onLocationChanged");

             mLastLocation = location;

             txtLocation = (String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude()));
             sqlStatement = "UPDATE GPSInfo set Location='"+txtLocation+"' where UserID='"+UserID+"'";
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
         txtSSID1 = (TextView) findViewById(R.id.txtSSID1);
         txtSSID2 = (TextView) findViewById(R.id.txtSSID2);
         txtSSID3 = (TextView) findViewById(R.id.txtSSID3);
         txtMAC1 = (TextView) findViewById(R.id.txtMAC1);
         txtMAC2 = (TextView) findViewById(R.id.txtMAC2);
         txtMAC3 = (TextView) findViewById(R.id.txtMAC3);
         txtlevel1 = (TextView) findViewById(R.id.txtlevel1);
         txtlevel2 = (TextView) findViewById(R.id.txtlevel2);
         txtlevel3 = (TextView) findViewById(R.id.txtlevel3);
         txtSSID1.setText(IncomingSMSReceiver.SSID1);
         txtSSID2.setText(IncomingSMSReceiver.SSID2);
         txtSSID3.setText(IncomingSMSReceiver.SSID3);
         txtMAC1.setText(IncomingSMSReceiver.MAC1);
         txtMAC2.setText(IncomingSMSReceiver.MAC2);
         txtMAC3.setText(IncomingSMSReceiver.MAC3);
         txtlevel1.setText(IncomingSMSReceiver.level1);
         txtlevel2.setText(IncomingSMSReceiver.level2);
         txtlevel3.setText(IncomingSMSReceiver.level3);
         if (txtSSID1.getText().equals("")){
             txtSSID1.setText("(No SSID)");
         }
         if (txtSSID2.getText().equals("")){
             txtSSID2.setText("(No SSID)");
         }
         if (txtSSID3.getText().equals("")){
             txtSSID3.setText("(No SSID)");
         }
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


     public void onSwitch(View view) {
         if (view.getId() == R.id.btnBack2) {
             generatemap();
         }
         if (view.getId() == R.id.btnFriend) {
             setContentView(R.layout.friendlist);
             friendlist();
             onDestroyView();
         }
         if (view.getId() == R.id.btnWifi) {
             setContentView(R.layout.wifi_list);
             onDestroyView();
             wifi_list();
         }
         if (view.getId() == R.id.btnBackLogin) {
             setContentView(R.layout.loginmenu);
             login();
         }
         if (view.getId() == R.id.btnregister) {
             setContentView(R.layout.registermenu);
             onDestroyView();
             Register();
         }
         if (view.getId() == R.id.btnCode) {
             setContentView(R.layout.codemenu);
             onDestroyView();
             codemenu();
         }
         if (view.getId() == R.id.btnBack3) {
             generatemap();
         }
         if (view.getId() == R.id.btnBack4) {
             generatemap();

         }
     }
    public void codemenu(){
        setContentView(R.layout.codemenu);
        txtCode = (TextView) findViewById(R.id.txtCode);
        txtAdd = (TextView) findViewById(R.id.txtAdd);
        Button btnAdd = (Button) findViewById(R.id.btnAdd);
        txtCode.setText(UserID);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sqlStatement = "insert into GPSFriend values('"+UserID+"','"+txtAdd.getText()+"')";
                new WriteDatabase().execute();
            }
        });
    }
    public void login(){
        usernameText = (TextView) findViewById(R.id.usernameText);
        passwordText = (TextView) findViewById(R.id.passwordText);
        Button btnLogin = (Button) findViewById(R.id.btnlogin);
        login=false;
        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sqlStatement = "select * from GPSAccount where Username='"+usernameText.getText()+"' AND Password='"+passwordText.getText()+"'";
                txtusername = String.valueOf(usernameText.getText());
                txtpassword = String.valueOf(passwordText.getText());
                new ReadAccountDatabase().execute();
            }
        });


    }
    public void generatemap(){
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

     public void onZoom(View view) {
         if (view.getId() == R.id.btnzoomin) {
             mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
         }
         if (view.getId() == R.id.btnzoomout) {
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
    public void friendlist(){
        setContentView(R.layout.friendlist);
        friendList = (ListView) findViewById(R.id.friendList);
        sqlStatement = "select * from GPSFriend where UserID='"+UserID+"'";
        new ReadFriendDatabase().execute();
        //friendList.setAdapter(ADAhere);
        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                HashMap<String, Object> obj = (HashMap<String, Object>) ADAhere
                        .getItem(position);
                String VehicleId = (String) obj.get("A");
                Toast.makeText(MapsActivity.this, VehicleId, Toast.LENGTH_LONG)
                        .show();
            }
        });

    }
    private class ReadFriendDatabase extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(MapsActivity.this, "Loading",
                    "Please Wait", true);
        }
        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        @Override
        protected Void doInBackground(Void... arg0) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            String url = "jdbc:jtds:sqlserver://182.50.133.109:1433;DatabaseName=tps";
            String driver = "net.sourceforge.jtds.jdbc.Driver";
            String userName = "RyanPow";
            String password = "password123";
            // Declare the JDBC objects.
            Connection con = null;
            Statement stmt = null;
            ResultSet rs = null;
            try
            {
                // Establish the connection.
                Class.forName(driver);
                con = DriverManager.getConnection(url, userName, password);
                // Create and execute an SQL statement that returns some data.
                String SQL = sqlStatement;
                stmt = con.createStatement();
                rs = stmt.executeQuery(SQL);
                List<Map<String, String>> data = null;
                data = new ArrayList<Map<String, String>>();

                // Iterate through the data in the result set and display it.
                while (rs.next()) {
                    Map<String, String> datanum = new HashMap<String, String>();
                    datanum.put("A", rs.getString("FriendUserID"));
                    data.add(datanum);
                }
                String[] fromwhere = { "A" };
                int[] viewswhere = { R.id.lblname };
                ADAhere = new SimpleAdapter(MapsActivity.this, data,
                        R.layout.listadapter, fromwhere, viewswhere);
                Log.w("My Activity", "SQL No Error");
            }
            catch(Exception ex)
            {
                Log.w("My Activity", "SQL Error: "+ ex.toString());
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
            friendList.setAdapter(ADAhere);
        }

    }
    private class WriteDatabase extends AsyncTask<Void, Void, Void> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            @TargetApi(Build.VERSION_CODES.GINGERBREAD)
            @Override
        protected Void doInBackground(Void... arg0) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            String url = "jdbc:jtds:sqlserver://182.50.133.109:1433;DatabaseName=tps";
            String driver = "net.sourceforge.jtds.jdbc.Driver";
            String userName = "RyanPow";
            String password = "password123";
            // Declare the JDBC objects.
            Connection con = null;
            Statement stmt = null;
            ResultSet rs = null;
            try
            {
                // Establish the connection.
                Class.forName(driver);
                con = DriverManager.getConnection(url, userName, password);
                // Create and execute an SQL statement that returns some data.
                String SQL = sqlStatement;
                stmt = con.createStatement();
                stmt.executeUpdate(SQL);

                Log.w("My Activity", "SQL No Error");
            }
            catch(Exception ex)
            {
                Log.w("My Activity", "SQL Error: "+ ex.toString());
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
        }

    }
    private class ReadAccountDatabase extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        @Override
        protected Void doInBackground(Void... arg0) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            String url = "jdbc:jtds:sqlserver://182.50.133.109:1433;DatabaseName=tps";
            String driver = "net.sourceforge.jtds.jdbc.Driver";
            String userName = "RyanPow";
            String password = "password123";
            // Declare the JDBC objects.
            Connection con = null;
            Statement stmt = null;
            ResultSet rs = null;
            try
            {
                // Establish the connection.
                Class.forName(driver);
                con = DriverManager.getConnection(url, userName, password);
                // Create and execute an SQL statement that returns some data.
                String SQL = sqlStatement;
                stmt = con.createStatement();
                rs = stmt.executeQuery(SQL);

                /* Iterate through the data in the result set and display it. */
                while (rs.next()) {
                    getSQLUsername= rs.getString("username") ;
                    getSQLPassword=rs.getString("password");
                    UserID=rs.getString("userid");
                    if(getSQLUsername.equals(txtusername)&& getSQLPassword.equals(txtpassword)){
                        login=true;
                    }
                }
                Log.w("My Activity", "SQL No Error");
            }
            catch(Exception ex)
            {
                Log.w("My Activity", "SQL Error: "+ ex.toString());
            }
            try {
                rs.close();
                con.close();
            }
            catch (Exception e){

            }

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            if (login==true){
                generatemap();
            }
            else{
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
        if((IncomingSMSReceiver.latitude==null)&&(IncomingSMSReceiver.longitude==null)) {
            LatLng coordinate = new LatLng(1.3468, 103.9326);
            CameraUpdate location = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
            mMap.animateCamera(location);
        }
         else
        {
            LatLng latLng = new LatLng(IncomingSMSReceiver.latitude,IncomingSMSReceiver.longitude);
            MapsActivity.mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
            MapsActivity.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
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
