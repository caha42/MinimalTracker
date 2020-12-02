package caha42.mmt;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    public static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
//            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
//            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
//            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
//    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
//    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
//    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

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
                    events.remove(eventDay);
                    calendars.remove(eventDay.getCalendar());
                } else {
                    events.add(eventDay);
                    calendars.add(eventDay.getCalendar());
                }
                calendarView.setHighlightedDays(calendars);
            }
        });
    }

    private long getCalenderId() {
        // Search for calender
        ContentResolver cr = getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "((" + CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
        String[] selectionArgs = new String[] {CALENDER_NAME, CalendarContract.ACCOUNT_TYPE_LOCAL,
                CALENDER_OWNER};

        // Submit the query and get a Cursor object back.
        Cursor cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

        long calID;

        if (cur.isFirst() && cur.isLast()) {
            calID = cur.getLong(PROJECTION_ID_INDEX);
        } else if (!cur.isFirst()) {
            calID = createCalender();
        } else {
            Toast.makeText(calendarView.getContext(),
                    "More than one matching calender exist. Get rid of the redundancy and try again.",
                    Toast.LENGTH_LONG).show();
            calID = -1;
        }

        return calID;
    }

    private long createCalender() {
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();

        values.put(CalendarContract.Calendars.ACCOUNT_NAME, CALENDER_OWNER);
        values.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        values.put(CalendarContract.Calendars.NAME, CALENDER_NAME);
        values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDER_NAME);
        values.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        values.put(CalendarContract.Calendars.VISIBLE, 1);
        values.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        values.put(CalendarContract.Calendars.OWNER_ACCOUNT, CALENDER_OWNER);
        values.put(CalendarContract.Calendars.DIRTY, 1);
        values.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().getID());

        Uri calUri = CalendarContract.Calendars.CONTENT_URI;

        calUri = calUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, CALENDER_OWNER)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
                .build();
        Uri result = cr.insert(calUri, values);
        long calID = Long.parseLong(result.getLastPathSegment()); // cast to long
        return calID;
    }
}