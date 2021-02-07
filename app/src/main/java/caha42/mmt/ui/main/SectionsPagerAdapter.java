package caha42.mmt.ui.main;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import caha42.mmt.CalendarController;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private final Context mContext;

    private List<Map.Entry<Integer, String>> trackers;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
        this.trackers = CalendarController.loadTrackers(mContext.getContentResolver());
        if (this.trackers.size() == 0) {
            Toast.makeText(mContext,
                    "No calender exist. Create calendar.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return PlaceholderFragment.newInstance(trackers.get(position).getKey(), trackers.get(position).getValue());
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return trackers.get(position).getValue();
    }

    @Override
    public int getCount() {
        return trackers.size();
    }

}