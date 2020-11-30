package caha42.mmt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    CalendarView calendarView;
    List<EventDay> events = new ArrayList<>();
    List<Calendar> calendars = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendarView = (CalendarView) findViewById(R.id.calendarView);

        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                if (calendars.contains(eventDay.getCalendar())) {
                    events.remove(eventDay);
                    calendars.remove(eventDay.getCalendar());
                }
                else {
                    events.add(eventDay);
                    calendars.add(eventDay.getCalendar());
                }
                calendarView.setHighlightedDays(calendars);
            }
        });
    }
}