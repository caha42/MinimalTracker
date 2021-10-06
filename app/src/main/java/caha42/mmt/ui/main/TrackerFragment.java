package caha42.mmt.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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
public class TrackerFragment extends Fragment {

    private static final String ARG_CALENDAR_ID = "calendar_id";
    private static final String ARG_TRACKER_NAME = "tracker_namer";

    private TrackerViewModel pageViewModel;

    public static TrackerFragment newInstance(int calendarId, String trackerName) {
        TrackerFragment fragment = new TrackerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_CALENDAR_ID, calendarId);
        bundle.putString(ARG_TRACKER_NAME, trackerName);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(TrackerViewModel.class);

        int calId = getArguments().getInt(ARG_CALENDAR_ID);
        pageViewModel.setCalendarId(calId);

        String trackerName = getArguments().getString(ARG_TRACKER_NAME);
        pageViewModel.setTrackerName(trackerName);

        pageViewModel.loadData(getActivity().getContentResolver());
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tracker, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startTracker();
    }

    private void startTracker() {
        CalendarView calendarView = (CalendarView) getView().findViewById(R.id.calendarView);
        calendarView.setMaximumDate(Calendar.getInstance());

        List<Calendar> trackedDays = pageViewModel.getTrackedDays();
        calendarView.setSelectedDates(trackedDays);
        calendarView.setHighlightedDays(trackedDays);
        // set handler
        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                Calendar day = eventDay.getCalendar();
                if (eventDay.getCalendar().before(Calendar.getInstance())) {
                    pageViewModel.toggleTracking(day);
                }
            }
        });
    }
}