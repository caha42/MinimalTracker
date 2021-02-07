package caha42.mmt.ui.main;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PageViewModel extends ViewModel {

    private int calId;
    private String trackerName;
    private List<Calendar> events;

    public PageViewModel() {
        super();
        this.events = new ArrayList<>();
    }

    public void setCalendarId(int calId) {
        this.calId = calId;
    }

    public void setTrackerName(String trackerName) {
        this.trackerName = trackerName;
    }

    public void trackDay(Calendar day) {
        this.events.add(day);
    }

    public boolean isEventTracked(Calendar day) {
        return this.events.contains(day);
    }

    public int getCalendarId() {
        return this.calId;
    }

    public String getTrackerName() {
        return this.trackerName;
    }


}