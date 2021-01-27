package caha42.mmt.ui.main;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private final Context mContext;

    private List<String> trackerNames = new ArrayList<String>();
    private List<Integer> calenderIds = new ArrayList<Integer>();

    private static final String[] CALENDER_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
    };
    private static final String CALENDER_OWNER = "MinimalTracker";


    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
        getTrackers();
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return PlaceholderFragment.newInstance(calenderIds.get(position ), trackerNames.get(position));
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return trackerNames.get(position);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return trackerNames.size();
    }

    private void getTrackers() {
        // Search for calender
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
        String[] selectionArgs = new String[] {CalendarContract.ACCOUNT_TYPE_LOCAL, CALENDER_OWNER};

        // Submit the query and get a Cursor object back.
        Cursor cur = cr.query(uri, CALENDER_PROJECTION, selection, selectionArgs, null);

        int nofCal = cur.getCount();

        while (cur.moveToNext()) {
            calenderIds.add(cur.getInt(0));
            trackerNames.add(cur.getString(1));
        }

        if (nofCal == 0) {
              Toast.makeText(mContext,
                    "No calender exist. Create calendar.",
                    Toast.LENGTH_LONG).show();
        }
    }
}