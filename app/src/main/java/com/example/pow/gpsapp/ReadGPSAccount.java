package com.example.pow.gpsapp;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
// This class is called when the application wants to connect to the GPSAccount server
public class ReadGPSAccount {
    private static final String ns = null;

    public List parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }
    private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List entries = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "GPSAccount");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("user")) {
                entries.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }
    public static class gpsEntry {
        public final String UserID;
        public final String Username;
        public final String Password;
        public final String Location;
        public final String Wifi;

        private gpsEntry(String UserID, String Username, String Password, String Location, String Wifi ) {
            this.UserID = UserID;
            this.Username = Username;
            this.Password = Password;
            this.Location = Location;
            this.Wifi = Wifi;
        }
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
    private gpsEntry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "user");
        String UserID2 = null;
        String Username2 = null;
        String Password2 = null;
        String Location2 = null;
        String Wifi2 = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("UserID")) {
                UserID2= readUserID(parser);
            } else if (name.equals("Username")) {
                Username2= readUsername(parser);
            } else if (name.equals("Password")) {
                Password2= readPassword(parser);
            } else if (name.equals("Location")) {
                Location2= readLocation(parser);
            } else if (name.equals("Wifi")) {
                Wifi2 = readWifi(parser);
            }

            else {
                skip(parser);
            }
        }
        return new gpsEntry(UserID2, Username2, Password2, Location2, Wifi2 );
    }

    // Processes title tags in the database.
    private String readUserID(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "UserID");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "UserID");
        return title;
    }

    private String readUsername(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "Username");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "Username");
        return title;
    }

    private String readPassword(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "Password");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "Password");
        return title;
    }

    private String readLocation(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "Location");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "Location");
        return title;
    }

    private String readWifi(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "Wifi");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "Wifi");
        return title;
    }

    // For the titles, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

}
