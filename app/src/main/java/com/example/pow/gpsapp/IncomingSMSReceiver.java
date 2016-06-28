package com.example.pow.gpsapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class IncomingSMSReceiver extends BroadcastReceiver {
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    String txtLocation= MapsActivity.txtLocation;
    static double latitude,longitude;

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("Test1233");
        if (intent.getAction().equals(SMS_RECEIVED)) {
            System.out.println("Test123");
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus.length == 0) {
                    return;
                }
                SmsMessage[] messages = new SmsMessage[pdus.length];
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    sb.append(messages[i].getMessageBody());
                }
                String sender = messages[0].getOriginatingAddress();
                String message = sb.toString();

                if (message.startsWith("Request"))
                {
                    String txtSMS=txtLocation;
                    Toast.makeText(context, "Sending Location", Toast.LENGTH_SHORT).show();

                    this.abortBroadcast();
                    sendSMS(sender, txtSMS);
                }
                if (message.startsWith("AQOZkasQSM"))
                {
                    String txtSMS="Thanks";
                    Toast.makeText(context, "Received Location", Toast.LENGTH_SHORT).show();

                    this.abortBroadcast();
                    sendSMS(sender, txtSMS);
                    String[] separated = message.split(":");
                    String[] latlong = separated[1].split(",");
                    latitude = Double.parseDouble(latlong[0]);
                    longitude = Double.parseDouble(latlong[1]);
                    System.out.println("ok");
                    LatLng latLng = new LatLng(latitude,longitude);
                    MapsActivity.mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
                    MapsActivity.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    System.out.println("abc");

                }

            }
        }
    }
    // sends your SMS
    private void sendSMS(final String phoneNumber, String message) {


        if(!phoneNumber.isEmpty() && !message.isEmpty()) {

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, null, null);
        }
    }


}