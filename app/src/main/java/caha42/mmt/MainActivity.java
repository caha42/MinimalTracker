package caha42.mmt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;

import caha42.mmt.ui.main.TrackePagerAdapter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);
        TrackePagerAdapter sectionsPagerAdapter = new TrackePagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR);

        if ((ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_CALENDAR) ==
                PackageManager.PERMISSION_DENIED) || (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_CALENDAR) ==
                PackageManager.PERMISSION_DENIED)) {
            requestPermissions(new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR,},
                    42);
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
                //Toast.makeText(calendarView.getContext(),
                //        "Define what to track via 'Manage tracker' option. Click a date to track " +
                //                "or untrack a date.",
                //        Toast.LENGTH_LONG).show();
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
                //    startTracker();
                //} else {
                //    Toast.makeText(calendarView.getContext(),
                //            "MinimalTracker does not work without calendar access. " +
                 //                   "It needs to create a calendar to store tracked events. Please restart app",
                 //           Toast.LENGTH_LONG).show();
                }
        }
    }


}