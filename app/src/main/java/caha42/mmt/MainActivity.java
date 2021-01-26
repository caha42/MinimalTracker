package caha42.mmt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Calendars;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    private static final String CALENDER_OWNER = "MinimalTracker";
    private String TRACKER_NAME;

    private long calID = -1;
    private CalendarView calendarView;
    private List<Calendar> calendars = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR);

        if ((ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_CALENDAR) ==
                PackageManager.PERMISSION_DENIED) || (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_CALENDAR) ==
                PackageManager.PERMISSION_DENIED)) {
            requestPermissions(new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR,},
                    42);
        } else {
            startTracker();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.new_game:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);

                return true;
            case R.id.help:
                Toast.makeText(calendarView.getContext(),
                        "Define what to track via 'Manage tracker' option. Click a date to track " +
                                "or untrack a date.",
                        Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case 42:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startTracker();
                } else {
                    Toast.makeText(calendarView.getContext(),
                            "MinimalTracker does not work without calendar access. " +
                                    "It needs to create a calendar to store tracked events. Please restart app",
                            Toast.LENGTH_LONG).show();
                }
        }
    }

    private void startTracker() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        TRACKER_NAME = prefs.getString("tracker_name", "Migraine");
        Toast.makeText(this, TRACKER_NAME, Toast.LENGTH_SHORT).show(); // shows true if the SwitchPreferenceCompat is ON, and false if OFF.

        getCalenderId();
        if (calID == -1) {
            createCalender();
        }

        calendarView = (CalendarView) findViewById(R.id.calendarView);

        calendarView.setDisabledDays(getDatesUntilEndOfMonth());

        // fill calendar with Events if calender exists
        if (calID != -1) {
            ContentResolver cr = getContentResolver();
            Uri uri = Events.CONTENT_URI;
            String selection = "((" + Events.CALENDAR_ID + " = ?) AND ("
                    + Events.TITLE + " = ?))";
            String[] selectionArgs = new String[] {String.valueOf(calID), TRACKER_NAME};

            Cursor cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

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

        // set handler
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
    }

    /**
     *
     * @return -1 if calender does not exist
     */
    private void getCalenderId() {
        // Search for calender
        ContentResolver cr = getContentResolver();
        Uri uri = Calendars.CONTENT_URI;
        String selection = "((" + Calendars.CALENDAR_DISPLAY_NAME + " = ?) AND ("
                + Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + Calendars.OWNER_ACCOUNT + " = ?))";
        String[] selectionArgs = new String[] {TRACKER_NAME, CalendarContract.ACCOUNT_TYPE_LOCAL, CALENDER_OWNER};

        // Submit the query and get a Cursor object back.
        Cursor cur = cr.query(uri, CALENDER_PROJECTION, selection, selectionArgs, null);

        int nofCal = cur.getCount();
        if (nofCal == 1) {
            cur.moveToFirst();
            calID = cur.getLong(0);
        } else if (nofCal > 1) {
            Toast.makeText(calendarView.getContext(),
                    "More than one matching calender exist. Get rid of the redundancy and try again.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void createCalender() {
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();

        values.put(Calendars.ACCOUNT_NAME, CALENDER_OWNER);
        values.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        values.put(Calendars.NAME, TRACKER_NAME);
        values.put(Calendars.CALENDAR_DISPLAY_NAME, TRACKER_NAME);
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
        calID = Long.parseLong(result.getLastPathSegment()); // cast to long
    }

    private void addEvent(long calID, EventDay eventDay) {
        Calendar day = eventDay.getCalendar();
        long dateMillis = day.getTimeInMillis();

        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(Events.DTSTART, dateMillis);
        values.put(Events.DTEND, dateMillis);
        values.put(Events.ALL_DAY, true);
        values.put(Events.TITLE, TRACKER_NAME);
        values.put(Events.CALENDAR_ID, calID);
        values.put(Events.EVENT_TIMEZONE, eventDay.getCalendar().getTimeZone().toString());
        cr.insert(Events.CONTENT_URI, values);
    }

    private void deleteEvent(long calID, EventDay eventDay) {
        ContentResolver cr = getContentResolver();
        Uri uri = Events.CONTENT_URI;

        long startMillis = 0;

        Calendar day = eventDay.getCalendar();
        startMillis = day.getTimeInMillis();

        String selection = "((" + Events.CALENDAR_ID + " = ?) AND ("
                + Events.DTSTART + " = ?) AND ("
                + Events.TITLE + " = ?))";
        String[] selectionArgs = new String[] {String.valueOf(calID), String.valueOf(startMillis), TRACKER_NAME};

        int rows = cr.delete(Events.CONTENT_URI, selection, selectionArgs);

        if (rows != 1) {
            if (rows == 0) {
                Toast.makeText(calendarView.getContext(),
                        "The event could not be found. Go scold the developers!.",
                        Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(calendarView.getContext(),
                        "More than one matching event exists for that day. Get rid of the redundancy and try again.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public static List<Calendar> getDatesUntilEndOfMonth() {

        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        long numOfDaysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        List<LocalDate> dates = IntStream.iterate(0, i -> i + 1)
                .limit(numOfDaysBetween)
                .mapToObj(i -> startDate.plusDays(i))
                .collect(Collectors.toList());

        List<Calendar> datesUntilEndOfMonth = new ArrayList<Calendar>();
        for (LocalDate date : dates) {
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.set(date.getYear(), date.getMonthValue()-1, date.getDayOfMonth());
            datesUntilEndOfMonth.add(calendar);
        }

        return datesUntilEndOfMonth;
    }
}