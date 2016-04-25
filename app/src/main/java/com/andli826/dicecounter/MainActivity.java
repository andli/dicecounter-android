package com.andli826.dicecounter;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity
        implements StatsFragment.OnStatsInteractionListener,
        CaptureImageFragment.OnCaptureImageListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new StatsFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stats, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStatsUpdate(Uri uri) {

    }

    @Override
    public void requestCapture() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.container, new CaptureImageFragment());
        ft.commit();
        Log.d("dclog.main", "Back in Activity from Stats fragment.");
    }

    @Override
    public void onImageCaptured() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.container, new StatsFragment());
        ft.commit();
        Log.d("dclog.main", "Back in Activity from Capture fragment.");
    }
}
