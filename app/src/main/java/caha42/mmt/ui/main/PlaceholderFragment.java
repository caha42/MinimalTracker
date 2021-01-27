package caha42.mmt.ui.main;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

import caha42.mmt.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {

    private static final String ARG_CALENDAR_ID = "calendar_id";
    private static final String ARG_TRACKER_NAME = "tracker_namer";

    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    private static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.CALENDAR_TIME_ZONE
    } ;

    private static final String CALENDER_OWNER = "MinimalTracker";

    private long calId;
    private String trackerName;
    private List<Calendar> events = new ArrayList<>();


    public static PlaceholderFragment newInstance(int calendarId, String trackerName) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_CALENDAR_ID, calendarId);
        bundle.putString(ARG_TRACKER_NAME, trackerName);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calId = getArguments().getInt(ARG_CALENDAR_ID);
        trackerName =  getArguments().getString(ARG_TRACKER_NAME);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startTracker();
    }

    private void startTracker() {
        CalendarView calendarView = (CalendarView) getView().findViewById(R.id.calendarView);
        calendarView.setDisabledDays(getDatesUntilEndOfMonth());

        // fill calendar with Events if calender exists

        ContentResolver cr = getActivity().getApplicationContext().getContentResolver();
        Uri uri = CalendarContract.Events.CONTENT_URI;
        String selection = "((" + CalendarContract.Events.CALENDAR_ID + " = ?) AND ("
                + CalendarContract.Events.TITLE + " = ?))";
        String[] selectionArgs = new String[] {String.valueOf(calId), trackerName};

        Cursor cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

        while (cur.moveToNext()) {
            long startMillis = 0;
            String timeZone = "";

            startMillis = cur.getLong(0);
            timeZone = cur.getString(1);

            Calendar event = Calendar.getInstance();
            event.setTimeInMillis(startMillis);
            event.setTimeZone(TimeZone.getTimeZone(timeZone));
            events.add(event);
        }
        calendarView.setSelectedDates(events);


        // set handler
        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                Calendar event = eventDay.getCalendar();
                if (events.contains(eventDay.getCalendar())) {
                    deleteEvent(event);
                } else {
                    addEvent(event);
                }
            }
        });
    }

//    private void createCalender() {
//        ContentResolver cr = getActivity().getApplicationContext().getContentResolver();
//        ContentValues values = new ContentValues();
//
//        values.put(CalendarContract.Calendars.ACCOUNT_NAME, CALENDER_OWNER);
//        values.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
//        values.put(CalendarContract.Calendars.NAME, trackerName);
//        values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, trackerName);
//        values.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
//        values.put(CalendarContract.Calendars.VISIBLE, 1);
//        values.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
//        values.put(CalendarContract.Calendars.OWNER_ACCOUNT, CALENDER_OWNER);
//        values.put(CalendarContract.Calendars.DIRTY, 1);
//        values.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().getID());
//
//        Uri calUri = CalendarContract.Calendars.CONTENT_URI;
//
//        calUri = calUri.buildUpon()
//                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
//                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, CALENDER_OWNER)
//                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
//                .build();
//        Uri result = cr.insert(calUri, values);
//        calId = Long.parseLong(result.getLastPathSegment()); // cast to long
//    }

    private void addEvent(Calendar event) {
        long dateMillis = event.getTimeInMillis();

        ContentResolver cr = getActivity().getApplicationContext().getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, dateMillis);
        values.put(CalendarContract.Events.DTEND, dateMillis);
        values.put(CalendarContract.Events.ALL_DAY, true);
        values.put(CalendarContract.Events.TITLE, trackerName);
        values.put(CalendarContract.Events.CALENDAR_ID, calId);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, event.getTimeZone().toString());
        cr.insert(CalendarContract.Events.CONTENT_URI, values);
    }

    private void deleteEvent(Calendar event) {
        ContentResolver cr = getActivity().getApplicationContext().getContentResolver();
        Uri uri = CalendarContract.Events.CONTENT_URI;

        long startMillis = 0;

        startMillis = event.getTimeInMillis();

        String selection = "((" + CalendarContract.Events.CALENDAR_ID + " = ?) AND ("
                + CalendarContract.Events.DTSTART + " = ?) AND ("
                + CalendarContract.Events.TITLE + " = ?))";
        String[] selectionArgs = new String[] {String.valueOf(calId), String.valueOf(startMillis), trackerName};

        int rows = cr.delete(CalendarContract.Events.CONTENT_URI, selection, selectionArgs);

        if (rows != 1) {
            if (rows == 0) {
                Toast.makeText(getContext(),
                        "The event could not be found. Go scold the developers!.",
                        Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getContext(),
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