package caha42.mmt.ui.main;

import android.content.ContentResolver;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import caha42.mmt.io.CalendarController;

public class TrackerViewModel extends ViewModel {

    private ContentResolver cr;

    private int calId;
    private String trackerName;
    private List<Calendar> events;

    public TrackerViewModel() {
        super();
    }

    public void setCalendarId(int calId) {
        this.calId = calId;
    }

    public void setTrackerName(String trackerName) {
        this.trackerName = trackerName;
    }

    public void loadData(ContentResolver cr) {
        this.cr = cr;
        this.events = CalendarController.loadTrackedDays(this.cr, this.calId, this.trackerName);
    }

    public void toggleTracking(Calendar day) {
        if (this.events.contains(day)) {
            CalendarController.untrackDay(this.cr, this.calId, this.trackerName, day);
            this.events.remove(day);
        } else {
            CalendarController.trackDay(this.cr, this.calId, this.trackerName, day);
            this.events.add(day);
        }
    }

    public List<Calendar> getTrackedDays() {
        return this.events;
    }
}