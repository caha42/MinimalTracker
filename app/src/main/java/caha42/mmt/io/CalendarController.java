package caha42.mmt.io;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.format.DateUtils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class CalendarController {

    private static final String CALENDER_OWNER = "MinimalTracker";

    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    private static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.CALENDAR_TIME_ZONE
    } ;

    private static final String[] CALENDER_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
    };

    public static List<Map.Entry<Integer, String>> loadTrackers(ContentResolver cr) {
        // Search for calender
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
        String[] selectionArgs = new String[] {CalendarContract.ACCOUNT_TYPE_LOCAL, CALENDER_OWNER};

        // Submit the query and get a Cursor object back.
        Cursor cur = cr.query(uri, CALENDER_PROJECTION, selection, selectionArgs, null);

        List<Map.Entry<Integer, String>> trackers = new ArrayList<Map.Entry<Integer, String>>();
        while (cur.moveToNext()) {
            Map.Entry<Integer, String> entry = new AbstractMap.SimpleEntry<Integer, String>(cur.getInt(0), cur.getString(1));
            trackers.add(entry);
        }
        return trackers;
    }

    public static int createCalender(ContentResolver cr, String trackerName) {
        ContentValues values = new ContentValues();

        values.put(CalendarContract.Calendars.ACCOUNT_NAME, CALENDER_OWNER);
        values.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        values.put(CalendarContract.Calendars.NAME, trackerName);
        values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, trackerName);
        values.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        values.put(CalendarContract.Calendars.VISIBLE, 1);
        values.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        values.put(CalendarContract.Calendars.OWNER_ACCOUNT, CALENDER_OWNER);
        values.put(CalendarContract.Calendars.DIRTY, 1);
        values.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().getID());

        System.out.print(TimeZone.getDefault());

        Uri calUri = CalendarContract.Calendars.CONTENT_URI;

        calUri = calUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, CALENDER_OWNER)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
                .build();
        Uri result = cr.insert(calUri, values);
        return Integer.parseInt(result.getLastPathSegment());
    }

    public static List<Calendar> loadTrackedDays(ContentResolver cr, int calendarId, String trackerName) {
        Uri uri = CalendarContract.Events.CONTENT_URI;
        String selection = "((" + CalendarContract.Events.CALENDAR_ID + " = ?) AND ("
                + CalendarContract.Events.TITLE + " = ?))";
        String[] selectionArgs = new String[] {String.valueOf(calendarId), trackerName};

        Cursor cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

        List<Calendar> trackedDays = new ArrayList<Calendar>();
        while (cur.moveToNext()) {
            long startMillis = 0;
            String timeZone = "";

            startMillis = cur.getLong(0);
            timeZone = cur.getString(1);

            Calendar day = Calendar.getInstance();
            day.setTimeInMillis(startMillis);
            day.setTimeZone(TimeZone.getTimeZone(timeZone));
            trackedDays.add(day);
        }
        return trackedDays;
    }

    public static void trackDay(ContentResolver cr, int calendarId, String trackerName, Calendar day) {
        long dateMillis = day.getTimeInMillis();

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, dateMillis);
        values.put(CalendarContract.Events.DTEND, dateMillis);
        values.put(CalendarContract.Events.ALL_DAY, true);
        values.put(CalendarContract.Events.TITLE, trackerName);
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, day.getTimeZone().getID());
        cr.insert(CalendarContract.Events.CONTENT_URI, values);
    }

    public static void untrackDay(ContentResolver cr, int calendarId, String trackerName, Calendar day) {
        Uri uri = CalendarContract.Events.CONTENT_URI;

        long startMillis = 0;

        startMillis = day.getTimeInMillis();

        String selection = "((" + CalendarContract.Events.CALENDAR_ID + " = ?) AND ("
                + CalendarContract.Events.DTSTART + " = ?) AND ("
                + CalendarContract.Events.TITLE + " = ?))";
        String[] selectionArgs = new String[] {String.valueOf(calendarId), String.valueOf(startMillis), trackerName};

        int rows = cr.delete(CalendarContract.Events.CONTENT_URI, selection, selectionArgs);

//        if (rows != 1) {
//            if (rows == 0) {
//                Toast.makeText(getContext(),
//                        "The day could not be found. Go scold the developers!.",
//                        Toast.LENGTH_LONG).show();
//            }
//            else {
//                Toast.makeText(getContext(),
//                        "More than one matching day exists for that day. Get rid of the redundancy and try again.",
//                        Toast.LENGTH_LONG).show();
//            }
//        }
    }

}
