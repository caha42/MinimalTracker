package caha42.mmt;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Calendars;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    private static final String[] CALENDER_PROJECTION = new String[] {
            Calendars._ID
    };
    private static final String[] EVENT_PROJECTION = new String[] {
            Events.DTSTART,
            Events.CALENDAR_TIME_ZONE
    } ;

    private static final String CALENDER_NAME = "Migraine";
    private static final String CALENDER_OWNER = "MinimalMigraineTracker";

    private CalendarView calendarView;
    private List<EventDay> events = new ArrayList<>();
    private List<Calendar> calendars = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        long calID = getCalenderId();

        calendarView = (CalendarView) findViewById(R.id.calendarView);

        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                if (calendars.contains(eventDay.getCalendar())) {
                    calendars.remove(eventDay.getCalendar());
                    deleteEvent(calID, eventDay);
                } else {
                    calendars.add(eventDay.getCalendar());
                    addEvent(calID, eventDay);
                }
                calendarView.setHighlightedDays(calendars);

            }
        });

        // get Events if calender exists
        if (calID != -1) {
            ContentResolver cr = getContentResolver();
            Uri uri = Events.CONTENT_URI;

            // Submit the query and get a Cursor object back.
            Cursor cur = cr.query(uri, EVENT_PROJECTION, null, null, null);

            System.out.println("eventCursor count="+cur.getCount());
            while (cur.moveToNext()) {
                long startMillis = 0;
                String timeZone = "";

                startMillis = cur.getLong(0);
                timeZone = cur.getString(1);

                Calendar event = Calendar.getInstance();
                event.setTimeInMillis(startMillis);
                event.setTimeZone(TimeZone.getTimeZone(timeZone));
                calendars.add(event);
            }
            calendarView.setHighlightedDays(calendars);
        }
    }

    private void deleteEvent(long calID, EventDay eventDay) {
    }

    private void addEvent(long calID, EventDay eventDay) {
        long startMillis = 0;
        long endMillis = 0;

        Calendar day = eventDay.getCalendar();
        startMillis = day.getTimeInMillis();
        endMillis = day.getTimeInMillis();

        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(Events.DTSTART, startMillis);
        values.put(Events.DTEND, endMillis);
        values.put(Events.ALL_DAY, true);
        values.put(Events.TITLE, CALENDER_NAME);
        values.put(Events.CALENDAR_ID, calID);
        values.put(Events.EVENT_TIMEZONE, eventDay.getCalendar().getTimeZone().toString());
        Uri uri = cr.insert(Events.CONTENT_URI, values);

        // get the event ID that is the last element in the Uri
        long eventID = Long.parseLong(uri.getLastPathSegment());
    }

    private long getCalenderId() {
        // Search for calender
        ContentResolver cr = getContentResolver();
        Uri uri = Calendars.CONTENT_URI;
        String selection = "((" + Calendars.CALENDAR_DISPLAY_NAME + " = ?) AND ("
                + Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + Calendars.OWNER_ACCOUNT + " = ?))";
        String[] selectionArgs = new String[] {CALENDER_NAME, CalendarContract.ACCOUNT_TYPE_LOCAL, CALENDER_OWNER};

        // Submit the query and get a Cursor object back.
        Cursor cur = cr.query(uri, CALENDER_PROJECTION, selection, selectionArgs, null);

        long calID = -1;

        int nofCal = cur.getCount();
        if (nofCal == 1) {
            cur.moveToFirst();
            calID = cur.getLong(0);
        } else if (nofCal == 0) {
            calID = createCalender();
        } else {
            Toast.makeText(calendarView.getContext(),
                    "More than one matching calender exist. Get rid of the redundancy and try again.",
                    Toast.LENGTH_LONG).show();
        }

        return calID;
    }

    private long createCalender() {
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();

        values.put(Calendars.ACCOUNT_NAME, CALENDER_OWNER);
        values.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        values.put(Calendars.NAME, CALENDER_NAME);
        values.put(Calendars.CALENDAR_DISPLAY_NAME, CALENDER_NAME);
        values.put(Calendars.SYNC_EVENTS, 1);
        values.put(Calendars.VISIBLE, 1);
        values.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
        values.put(Calendars.OWNER_ACCOUNT, CALENDER_OWNER);
        values.put(Calendars.DIRTY, 1);
        values.put(Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().getID());

        Uri calUri = Calendars.CONTENT_URI;

        calUri = calUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(Calendars.ACCOUNT_NAME, CALENDER_OWNER)
                .appendQueryParameter(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
                .build();
        Uri result = cr.insert(calUri, values);
        long calID = Long.parseLong(result.getLastPathSegment()); // cast to long
        return calID;
    }
}