package com.example.pow.gpsapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ArrayAdapter;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.common.ConnectionResult;


import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    public static GoogleMap mMap;
    private static final int LOCATION_REQUEST_CODE = 101;
    ListView friendList,deleteList;
    ContentResolver resolver;
    private Location mLastLocation;
    Marker marker;
    public static TextView txtCode,txtAdd,txtSSID1, txtSSID2, txtSSID3, txtMAC1, txtMAC2, txtMAC3, txtlevel1, txtlevel2, txtlevel3,usernameText,passwordText,usernametxt,passwordtxt;
    static String SSID1,SSID2,SSID3,MAC1,MAC2,MAC3,level1,level2,level3,friendtxt,locationtxt,wifitxt,deletetxt,AddCode,passwordregister,usernameregister,CodeUsername,txtLocation, txtusername, txtpassword, sqlStatement, getSQLUsername, getSQLPassword,randomID,UserID,usernametxt2,passwordtxt2;
    public LocationManager mLocationManager;
    boolean login=false,account=false,codecheck=false,codereal=false,friendcheck=false;
    private GoogleApiClient client;
    Double latitude;
    Double longitude;
    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    StringBuilder sb = new StringBuilder();
    ArrayList<String> ReadUserID = new ArrayList<>();
    ArrayList<String> ReadUsername = new ArrayList<>();
    ArrayList<String> ReadPassword = new ArrayList<>();
    ArrayList<String> ReadLocation = new ArrayList<>();
    ArrayList<String> ReadWifi = new ArrayList<>();
    ArrayList<String> ReadUserIDFriend = new ArrayList<>();
    ArrayList<String> ReadFriendList = new ArrayList<>();
    private java.util.Random rndGenerator = new java.util.Random();
    private int testID;
    public final static int NUMBER_OF_VALUES = 9999;
    List<Map<String, String>> data = null;
//    These 2 URLs are needed to tell the code which PHP files to connect
    String ReadGPSAccountURL="http://gpsapp-ryanpow.rhcloud.com/phpXML.php";
    String ReadGPSFriendURL="http://gpsapp-ryanpow.rhcloud.com/FriendXML.php";



//    This method is used the moment the application starts.
//    It will call the login() method to generate the login page.
//    Request permission if needed.
//    It will also start scanning for information.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requesting GPS permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                    LOCATION_REQUEST_CODE);
        }
        setContentView(R.layout.loginmenu);
        login=false;
        login();
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                LOCATION_REQUEST_CODE);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        int LOCATION_REFRESH_TIME = 0;
        int LOCATION_REFRESH_DISTANCE = 0;
//      Starts scanning for GPS Info
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);
//       Starts scanning for WiFi info
         mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
         receiverWifi = new WifiReceiver();
         registerReceiver(receiverWifi, new IntentFilter(
                 WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
         mainWifi.startScan();
     }
//    This method is called to generate the login page at the start of the application
//    It also calls the LoginGPSAccount() method to execute the login command
    public void login(){
        usernameText = (TextView) findViewById(R.id.usernameText);
        passwordText = (TextView) findViewById(R.id.passwordText);
        Button btnLogin = (Button) findViewById(R.id.btnlogin);
        login=false;
        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getBaseContext(),"Logging In",Toast.LENGTH_SHORT).show();
                new LoginGPSAccount().execute();

            }
        });
    }
//    This method is for the Register page
//    It will save the username and password into variables
//    It will also run CodeRegister() to execute the registering sequence
    public void Register(){
        usernametxt = (TextView) findViewById(R.id.usernametxt);
        passwordtxt = (TextView) findViewById(R.id.passwordtxt);
        Button btnRegister = (Button) findViewById(R.id.btnsaveregister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                usernameregister = String.valueOf(usernametxt.getText());
                passwordregister = String.valueOf(passwordtxt.getText());
                account=false;
                codecheck=false;
                new CodeRegister().execute();
            }
        });
    }
//    This class is used when the user registers
//    This is for assigning a unique code to the account
//    If the same code has been assigned, it will run the class again to generate a new code
    private class CodeRegister extends AsyncTask<String, Void, String> {
        @Override
//      This method will connect to the XML file with the URL
//      It calls the ReadGPSAccountXML method for connection
//      It will also generate a 4-digit code to put into the variable 'randomID'
        protected String doInBackground(String... urls) {
            try {
                testID = rndGenerator.nextInt(NUMBER_OF_VALUES);
                randomID=String.format("%04d", testID);
                return ReadGPSAccountXML(ReadGPSAccountURL);
            } catch (IOException e) {
                return ("IO Error: " + e);
            } catch (XmlPullParserException e) {
                return ("XML Error: " + e);
            }
        }
//    This method runs once connection with the XML file is complete
//    It will also check if the code is unique,if not, it will loop the class until it is unique
//    If it is unique, it runs the CheckRegister() class
        @Override
        protected void onPostExecute(String result) {
            for(int x=0; x<ReadUserID.size(); x++){
                if(String.valueOf(ReadUserID.get(x)).equals(randomID))
                {
                    codecheck=true;
                    break;
                }
            }
            if (codecheck==true)
            {
                new CodeRegister().execute();
            }
            else
            {
                new CheckRegister().execute();
            }
        }
    }
//    This class is used to check if the username has been taken
    private class CheckRegister extends AsyncTask<String, Void, String> {//
        @Override
//      This method will connect to the XML file with the URL
//      It calls the ReadGPSAccountXML method for connection
        protected String doInBackground(String... urls) {
            try {
                return ReadGPSAccountXML(ReadGPSAccountURL);
            } catch (IOException e) {

                return ("IO Error: " + e);
            } catch (XmlPullParserException e) {
                return ("XML Error: " + e);
            }
        }
        @Override
//        This method runs once connection with the XML file is complete
//        It checks if the username that has been entered is a duplicate
//        If it is a duplicate, it generates an error message showing that the username has been taken
//        If it is not, it runs the Registerxml() class
        protected void onPostExecute(String result) {
            for(int x=0; x<ReadUserID.size(); x++){
                if(String.valueOf(ReadUsername.get(x)).equals(usernameregister))
                {
                    account=true;
                    break;
                }
            }
            if (account==true)
            {
                new AlertDialog.Builder(MapsActivity.this)
                        .setTitle("Error")
                        .setMessage("This username has been taken")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
            else
            {
                new Registerxml().execute();
            }
        }
    }
//    This class is used when the user logins
    private class LoginGPSAccount extends AsyncTask<String, Void, String> {
        @Override
//      This method will connect to the XML file with the URL
//      It calls the ReadGPSAccountXML method for connection
        protected String doInBackground(String... urls) {
            try {
                return ReadGPSAccountXML(ReadGPSAccountURL);
            } catch (IOException e) {
                System.out.println("IO Error: " + e);
                return ("IO Error: " + e);
            } catch (XmlPullParserException e) {
                return ("XML Error: " + e);
            }
        }
        @Override
//        This method runs once connection with the XML file is complete
//        It checks to see if the input info matched with any of the info in the database
//        If it matches, the main page will be generated
//        If it does not, it will show an error message showing that the info is wrong
        protected void onPostExecute(String result) {
            for(int x=0; x<ReadUserID.size(); x++){
                if((String.valueOf(ReadUsername.get(x)).equals(String.valueOf(usernameText.getText())))&&(String.valueOf(ReadPassword.get(x)).equals(String.valueOf(passwordText.getText()))))
                {
                        UserID=ReadUserID.get(x);
                        login=true;
                        break;

                }
            }
            if (login==false)
            {
                new AlertDialog.Builder(MapsActivity.this)
                        .setTitle("Error")
                        .setMessage("Incorrect Username or Password")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
            else
            {
                generatemap();
            }

        }
    }
//    This method is called whenever a connection with the GPSAccount database is needed
//    It stores the ReadGPSAccount class variables that was received from the server
    private String ReadGPSAccountXML(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        // Instantiate the parser
        ReadGPSAccount stackOverflowXmlParser = new ReadGPSAccount();
        List<ReadGPSAccount.gpsEntry> entries = null;
        StringBuilder htmlString = new StringBuilder();

        try {
            stream = downloadUrl(urlString);
            entries = stackOverflowXmlParser.parse(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        // Store Objects
        for (ReadGPSAccount.gpsEntry gps : entries) {
            ReadUserID.add(gps.UserID);
            System.out.println(gps.UserID);
        }
        for (ReadGPSAccount.gpsEntry gps : entries) {
            ReadUsername.add(gps.Username);
        }
        for (ReadGPSAccount.gpsEntry gps : entries) {
            ReadPassword.add(gps.Password);
        }
        for (ReadGPSAccount.gpsEntry gps : entries) {
            ReadLocation.add(gps.Location);
        }
        for (ReadGPSAccount.gpsEntry gps : entries) {
            ReadWifi.add(gps.Wifi);
        }
        return htmlString.toString();
    }
//    This method is called whenever a connection with the GPSFriend database is needed
//    It stores the ReadGPSFriend class variables that was received from the server
    private String ReadGPSFriendXML(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        // Instantiate the parser
        ReadGPSFriend stackOverflowXmlParser = new ReadGPSFriend();
        List<ReadGPSFriend.gpsEntry> entries = null;
        StringBuilder htmlString = new StringBuilder();

        try {
            stream = downloadUrl(urlString);
            entries = stackOverflowXmlParser.parse(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        // Store Objects
        for (ReadGPSFriend.gpsEntry gps : entries) {
            if (gps.UserID.equals(UserID))
            {
                ReadUserIDFriend.add(gps.UserID);
            };
        }
        for (ReadGPSFriend.gpsEntry gps : entries) {
            if (gps.UserID.equals(UserID))
            {
                ReadFriendList.add(gps.FriendList);
            }

        }
        return htmlString.toString();
    }

    //Downloads the XML\\
    private InputStream downloadUrl(String urlString) throws IOException {
        java.net.URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        return conn.getInputStream();
    }
//    This class is called once all the register checking has been completed
//    It connects with the PHP file and saves the input data into the GPSAccount database
    private class Registerxml extends AsyncTask<Void, Void, Void> {
        @Override
//        This method takes the stored objects and name them different variables for the PHP file to understand
        protected Void doInBackground(Void... arg0) {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://gpsapp-ryanpow.rhcloud.com/Register.php");
            // add start end and booking fac
            httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);


                nameValuePairs.add(new BasicNameValuePair("UserID", randomID));
                nameValuePairs.add(new BasicNameValuePair("Username", usernameregister));
                nameValuePairs.add(new BasicNameValuePair("Password", passwordregister));
                nameValuePairs.add(new BasicNameValuePair("Location", "null"));
                nameValuePairs.add(new BasicNameValuePair("Wifi", "null"));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);
                System.out.println("HTTP Response: "+response);
            } catch (ClientProtocolException e) {
                System.out.println("HTTP Response1 : "+e);
            } catch (IOException e) {
                System.out.println("HTTP Response2 : "+e);
            }
            return null;
        }
        @Override
//        This method runs once the storing of data has been completed
//        It will generate the login page again after registering
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            setContentView(R.layout.loginmenu);
            login();
        }
    }
//    This class is called whenever GPS or Wi-Fi information has been changed in the phone
//    It connects with the PHP file and saves the input data into the GPSAccount database
//    It will update the past info with the latest info into the database
    private class Update extends AsyncTask<Void, Void, Void> {
        @Override
//        This method takes the stored objects and name them different variables for the PHP file to understand
        protected Void doInBackground(Void... arg0) {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://gpsapp-ryanpow.rhcloud.com/add.php");
            // add start end and booking fac
            httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("UserID", UserID));
                nameValuePairs.add(new BasicNameValuePair("Location", txtLocation));
                nameValuePairs.add(new BasicNameValuePair("Wifi", String.valueOf(sb)));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);
                System.out.println("HTTP Response: "+response);
            } catch (ClientProtocolException e) {
                System.out.println("HTTP Response1 : "+e);
            } catch (IOException e) {
                System.out.println("HTTP Response2 : "+e);
            }
            return null;
        }
    }
//    This method is called to destroy the map fragment, preventing overload of fragments
     public void onDestroyView() {
         FragmentManager fm = getSupportFragmentManager();
         Fragment fragment = (fm.findFragmentById(R.id.map));
         FragmentTransaction ft = fm.beginTransaction();
         ft.remove(fragment);
         ft.commit();
     }
//    This method is called whenever the layout changes to the main page
//    It generates the map and the main page
    public void generatemap(){
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
//    This class is used when the application starts
//    It is for detecting WiFi information
     class WifiReceiver extends BroadcastReceiver {
//    This method runs when WiFi information gets updated
//    It will convert the WiFi info to string for storing of info
//    Then it will run Update() to update the info to the database
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
             new Update().execute();
         }
     }
//         This allows the GPS info to be received
     private final android.location.LocationListener mLocationListener = new android.location.LocationListener() {
         @Override
//         This method gets called when the GPS info changes
//         It converts the info into string coordinates for storing
//         Then it will run Update() to update the info to the database
         public void onLocationChanged(Location location) {
             //code
             System.out.println("onLocationChanged");
             mLastLocation = location;
             txtLocation = (String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude()));
             new Update().execute();
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
//    This method runs when the user wants to view the WiFi page
//    It will generate the WiFi page and assign the stored external WiFi info to the text fields
//    If there is no SSID from the stored WiFi external info, "No SSID" will be displayed
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
         txtSSID1.setText(SSID1);
         txtSSID2.setText(SSID2);
         txtSSID3.setText(SSID3);
         txtMAC1.setText(MAC1);
         txtMAC2.setText(MAC2);
         txtMAC3.setText(MAC3);
         txtlevel1.setText(level1);
         txtlevel2.setText(level2);
         txtlevel3.setText(level3);
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
//    This method is called whenever a button is pressed that does not involve database connections
//    It will generate the map and destroy it if needed
//    It also empties list to prevent duplicated generating information
//    It assigns the buttons to their appropriate methods
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
             friendList.setAdapter(null);
             generatemap();
         }
         if (view.getId() == R.id.btnBack5) {
             deleteList.setAdapter(null);
             setContentView(R.layout.friendlist);
             friendlist();
         }
         if (view.getId() == R.id.btnDelete) {
             friendList.setAdapter(null);
             setContentView(R.layout.deletelist);
             deletelist();
         }
     }
//    This method is called when the user wants to add a friend into their friend list
//    It generates the codemenu layout and it displays the users code for adding
//    It checks if the code that is added is the user's own code
//    If it is, an error message is displayed
//    If it is not, ConvertCode() runs for checking and adding a friend
    public void codemenu(){
        setContentView(R.layout.codemenu);
        ReadUserIDFriend.clear();
        ReadFriendList.clear();
        txtCode = (TextView) findViewById(R.id.txtCode);
        txtAdd = (TextView) findViewById(R.id.txtAdd);
        Button btnAdd = (Button) findViewById(R.id.btnAdd);
        txtCode.setText(UserID);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AddCode = String.valueOf(txtAdd.getText());
                if (UserID.equals(AddCode))
                {
                    new AlertDialog.Builder(MapsActivity.this)
                            .setTitle("Error")
                            .setMessage("You cannot add yourself")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                else
                {
                    new ConvertCode().execute();
                }
            }
        });
    }
//    This class is called after the user tries to add a friend
//    This class is for checking if the code exists and for converting the code into a username
    private class ConvertCode extends AsyncTask<String, Void, String> {
//      This method will connect to the XML file with the URL
//      It calls the ReadGPSAccountXML method for connection
    protected String doInBackground(String... urls) {
        try {
            return ReadGPSAccountXML(ReadGPSAccountURL);
        } catch (IOException e) {
            System.out.println("IO Error: " + e);
            return ("IO Error: " + e);
        } catch (XmlPullParserException e) {
            return ("XML Error: " + e);
        }
    }
    @Override
//     This method runs once connection with the XML file is complete
//     It checks if the code exists
//     If it does, it will store the username that belongs to the code into a variable called 'CodeUsername' and run CheckList()
//     If it does not, It will run an error message telling the user that the code is incorrect
        protected void onPostExecute(String result) {
            for(int x=0; x<ReadUserID.size(); x++){
                if(String.valueOf(ReadUserID.get(x)).equals(AddCode))
                {
                    CodeUsername=ReadUsername.get(x);
                    codereal=true;
                    new CheckList().execute();
                    break;
                }
            }
            if (codereal==false)
            {
                new AlertDialog.Builder(MapsActivity.this)
                        .setTitle("Error")
                        .setMessage("This code is incorrect")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
    }
//  This class is to check if the friend has not already been added in the friend list
    private class CheckList extends AsyncTask<String, Void, String> {
        @Override
//      This method will connect to the XML file with the URL
//      It calls the ReadGPSFriendXML method for connection
        protected String doInBackground(String... urls) {
            try {
                return ReadGPSFriendXML(ReadGPSFriendURL);
            } catch (IOException e) {

                return ("IO Error: " + e);
            } catch (XmlPullParserException e) {
                return ("XML Error: " + e);
            }
        }
        @Override
//     This method runs once connection with the XML file is complete
//     It compares the user's friend list with the 'CodeUsername', checking if the variable already exists in the friend list
//     If it exists, display an error message telling the user that the person is already in the friend list
//     If it does not, it will execute AddFriend()
        protected void onPostExecute(String result) {
            for(int x=0; x<ReadUserIDFriend.size(); x++){
                if((String.valueOf(ReadUserIDFriend.get(x)).equals(UserID))&&(String.valueOf(ReadFriendList.get(x)).equals(CodeUsername)))
                {
                    friendcheck=true;
                    break;
                }
            }
            if (friendcheck==false)
            {
                new AddFriend().execute();
            }
            else
            {
                new AlertDialog.Builder(MapsActivity.this)
                        .setTitle("Error")
                        .setMessage("This person is already in your friend list")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
    }
//   This method is for the zoom buttons
//   It allows the map to zoom in and zoom out with the buttons
     public void onZoom(View view) {
         if (view.getId() == R.id.btnzoomin) {
             mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
         }
         if (view.getId() == R.id.btnzoomout) {
             mMap.animateCamera(CameraUpdateFactory.zoomTo(8), 2000, null);
         }
     }

//   This method is for the search address bar
//   It connects to the google map database with geocoder
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
//    This method is for requesting permission for location for Android Version 6.0
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
//    This method is for requesting permission for location for Android Version 6.0
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
//    This method gets called when the user wants to view the friend list from the main page
//    It calls CreateList() to generate the friend list
//    When a friend is selected FindFriend() is called to receive the friend info from database
    public void friendlist(){
        setContentView(R.layout.friendlist);
        friendList = (ListView) findViewById(R.id.friendList);
        ReadUserIDFriend.clear();
        ReadFriendList.clear();
        new CreateList().execute();
        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> av, View view, int i, long l) {
                String selectedFromList = ((String)av.getItemAtPosition(i));
                friendtxt=selectedFromList;
                new FindFriend().execute();
                Toast.makeText(getBaseContext(),selectedFromList,Toast.LENGTH_SHORT).show();
            }
        });
    }
//    This class is called when the user chooses a friend from the friend list
//    It receives and stores the friend's info from the database
    private class FindFriend extends AsyncTask<String, Void, String> {//
        @Override
//      This method will connect to the XML file with the URL
//      It calls the ReadGPSFriendXML method for connection
        protected String doInBackground(String... urls) {
            try {
                return ReadGPSAccountXML(ReadGPSAccountURL);
            } catch (IOException e) {

                return ("IO Error: " + e);
            } catch (XmlPullParserException e) {
                return ("XML Error: " + e);
            }
        }
        @Override
//     This method runs once connection with the XML file is complete
//     It splits and stores the friend's  external info into variables
//     If the requested info is unavaliable, it will display a toast message, talling the user that it is unavaliable
//     Once done, it will generate the main page and run the UpdateInfo() method to update the external info
        protected void onPostExecute(String result) {
            for(int x=0; x<ReadUserID.size(); x++){
                if(String.valueOf(ReadUsername.get(x)).equals(friendtxt))
                {
                    locationtxt = ReadLocation.get(x);
                    wifitxt = ReadWifi.get(x);
                }
            }
            if (locationtxt.equals("null"))
            {
                Toast.makeText(getBaseContext(),"Location Unavaliable",Toast.LENGTH_SHORT).show();
            }
            else
            {
                String[] latlong = locationtxt.split(",");
                latitude = Double.parseDouble(latlong[0]);
                longitude = Double.parseDouble(latlong[1]);
            }
            if (wifitxt.equals("null"))
            {
                Toast.makeText(getBaseContext(),"Wifi Unavaliable",Toast.LENGTH_SHORT).show();
            }
            else
            {
                String[] wifiinfo = wifitxt.split("!");
                String wifi1 = wifiinfo[0];
                String wifi2 = wifiinfo[1];
                String wifi3 = wifiinfo[2];
                String[] wifiinfo1 = wifi1.split(",");
                SSID1 = wifiinfo1[0];
                MAC1 = wifiinfo1[1];
                level1 = wifiinfo1[2];
                String[] wifiinfo2 = wifi2.split(",");
                SSID2 = wifiinfo2[0];
                MAC2 = wifiinfo2[1];
                level2 = wifiinfo2[2];
                String[] wifiinfo3 = wifi3.split(",");
                SSID3 = wifiinfo3[0];
                MAC3 = wifiinfo3[1];
                level3 = wifiinfo3[2];
            }
            generatemap();
            new UpdateInfo().execute();
        }
    }
//    This class runs on a loop and it is called once a friend is chosen
//    This class is meant to continuously receive the friend's info from the database
//    and updating the variables in the phone when the info changes
    private class UpdateInfo extends AsyncTask<String, Void, String> {//
        @Override
//      This method will connect to the XML file with the URL
//      It calls the ReadGPSFriendXML method for connection
        protected String doInBackground(String... urls) {
            try {
                return ReadGPSAccountXML(ReadGPSAccountURL);
            } catch (IOException e) {

                return ("IO Error: " + e);
            } catch (XmlPullParserException e) {
                return ("XML Error: " + e);
            }
        }
    @Override
//     This method runs once connection with the XML file is complete
//     It runs on a constantly, checking for updates in the friend's info
//     It moves the marker on the map, following where the GPS coordinates point to
//     and updates the WiFi page when the info changes
        protected void onPostExecute(String result) {
            for(int x=0; x<ReadUserID.size(); x++){
                if(String.valueOf(ReadUsername.get(x)).equals(friendtxt))
                {
                    locationtxt = ReadLocation.get(x);
                    wifitxt = ReadWifi.get(x);
                }
            }
            if (locationtxt.equals("null"))
            {
            }
            else
            {
                String[] latlong = locationtxt.split(",");
                latitude = Double.parseDouble(latlong[0]);
                longitude = Double.parseDouble(latlong[1]);
                LatLng latLng = new LatLng(latitude,longitude);
                marker.remove();
                marker=MapsActivity.mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
            }
            if (wifitxt.equals("null"))
            {
            }
            else
            {
                String[] wifiinfo = wifitxt.split("!");
                String wifi1 = wifiinfo[0];
                String wifi2 = wifiinfo[1];
                String wifi3 = wifiinfo[2];
                String[] wifiinfo1 = wifi1.split(",");
                SSID1 = wifiinfo1[0];
                MAC1 = wifiinfo1[1];
                level1 = wifiinfo1[2];
                String[] wifiinfo2 = wifi2.split(",");
                SSID2 = wifiinfo2[0];
                MAC2 = wifiinfo2[1];
                level2 = wifiinfo2[2];
                String[] wifiinfo3 = wifi3.split(",");
                SSID3 = wifiinfo3[0];
                MAC3 = wifiinfo3[1];
                level3 = wifiinfo3[2];
            }
            new UpdateInfo().execute();
        }
    }
//    This method runs when the user wants to delete a friend
//    It will clear all the list and generate the friend list on the delete page with CreateDeleteList()
//    When a friend is selected, DeleteFriend() is called
    public void deletelist(){
        setContentView(R.layout.deletelist);
        deleteList = (ListView) findViewById(R.id.deleteList);
        ReadUserIDFriend.clear();
        ReadFriendList.clear();
        new CreateDeleteList().execute();
        deleteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> av, View view, int i, long l) {
                String selectedFromList = ((String)av.getItemAtPosition(i));
                Toast.makeText(getBaseContext(),selectedFromList,Toast.LENGTH_SHORT).show();
                deletetxt = selectedFromList.toString();
                new DeleteFriend().execute();
            }
        });
    }
//    This class is called when the user opens the delete page
//    It creates the user's friend list on the delete page
    private class CreateDeleteList extends AsyncTask<String, Void, String> {
    @Override
//      This method will connect to the XML file with the URL
//      It calls the ReadGPSFriendXML method for connection
    protected String doInBackground(String... urls) {
        try {
            return ReadGPSAccountXML(ReadGPSAccountURL);
        } catch (IOException e) {

            return ("IO Error: " + e);
        } catch (XmlPullParserException e) {
            return ("XML Error: " + e);
        }
    }
    @Override
//     This method runs once connection with the XML file is complete
//     It populates the listview with the friends list
        protected void onPostExecute(String result) {
            for(int x=0; x<ReadUserIDFriend.size(); x++){
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MapsActivity.this,android.R.layout.simple_list_item_1,ReadFriendList );
            deleteList.setAdapter(arrayAdapter);
        }
    }
//    This class is called when the user opens the friend list page
//    It creates the user's friend list on the friend list page
    private class CreateList extends AsyncTask<String, Void, String> {
        @Override
//      This method will connect to the XML file with the URL
//      It calls the ReadGPSFriendXML method for connection
        protected String doInBackground(String... urls) {
            try {
                return ReadGPSFriendXML(ReadGPSFriendURL);
            } catch (IOException e) {

                return ("IO Error: " + e);
            } catch (XmlPullParserException e) {
                return ("XML Error: " + e);
            }
        }
        @Override
//     This method runs once connection with the XML file is complete
//     It populates the listview with the friends list
        protected void onPostExecute(String result) {
            for(int x=0; x<ReadUserIDFriend.size(); x++){
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MapsActivity.this,android.R.layout.simple_list_item_1,ReadFriendList );
            friendList.setAdapter(arrayAdapter);
        }
    }
//    This class is called whenever a friend is selected in the delete friend list
//    It deletes the friend from the friend list
    private class DeleteFriend extends AsyncTask<Void, Void, Void> {
//        This method takes the stored objects and name them different variables for the PHP file to understand
        @Override
        protected Void doInBackground(Void... arg0) {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://gpsapp-ryanpow.rhcloud.com/DeleteList.php");
            // add start end and booking fac
            httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);


                nameValuePairs.add(new BasicNameValuePair("UserID", UserID));
                nameValuePairs.add(new BasicNameValuePair("FriendList", deletetxt));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);
                System.out.println("HTTP Response: "+response);
            } catch (ClientProtocolException e) {
                System.out.println("HTTP Response1 : "+e);
            } catch (IOException e) {
                System.out.println("HTTP Response2 : "+e);
            }
            return null;
        }
//    This method is called once the variables has been sent to the PHP files, for the friend to be deleted
//    It runs the moves the layout back to the friend list showing that the friend is deleted
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            deleteList.setAdapter(null);
            friendList.setAdapter(null);
            friendlist();
        }

    }
//    This class is called once all the checking is done
//    It communicates with the PHP file to add a friend into the user's friend list
    private class AddFriend extends AsyncTask<Void, Void, Void> {
//        This method takes the stored objects and name them different variables for the PHP file to understand
        @Override
        protected Void doInBackground(Void... arg0) {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://gpsapp-ryanpow.rhcloud.com/AddCode.php");
            // add start end and booking fac
            httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);


                nameValuePairs.add(new BasicNameValuePair("UserID", UserID));
                nameValuePairs.add(new BasicNameValuePair("FriendList", CodeUsername));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);
                System.out.println("HTTP Response: "+response);
            } catch (ClientProtocolException e) {
                System.out.println("HTTP Response1 : "+e);
            } catch (IOException e) {
                System.out.println("HTTP Response2 : "+e);
            }
            return null;
        }
//    This method is called once the variables has been sent to the PHP files, for the friend to be added
//    It will go back to the main page with the map
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            generatemap();
        }

    }


     @Override
     public void onMapReady(GoogleMap googleMap) {
         mMap = googleMap;
         if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
             return;
         }
         mMap.setMyLocationEnabled(true);
        if((latitude==null)&&(longitude==null)) {
            LatLng coordinate = new LatLng(1.3468, 103.9326);
            CameraUpdate location = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
            mMap.animateCamera(location);
        }
         else
        {
            LatLng latLng = new LatLng(latitude,longitude);
            marker=MapsActivity.mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
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
