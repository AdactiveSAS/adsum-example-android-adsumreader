package com.adactive.AdsumReader;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.widget.Toast;

import com.adactive.nativeapi.MapView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.quinny898.library.persistentsearch.SearchBox;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;

public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    private MapView map;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Force local to english
        Locale locale2 = new Locale("en");
        Locale.setDefault(locale2);
        Configuration config2 = new Configuration();
        config2.locale = locale2;
        getBaseContext().getResources().updateConfiguration(
                config2, getBaseContext().getResources().getDisplayMetrics());

        setContentView(R.layout.activity_main);

        map = new MapView(getApplicationContext());
        map.update();

        if(map.isMapDataAvailable()) {
            map.start();
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, PlaceholderFragment.newInstance(position, map)).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            restoreActionBar();
        }

        return super.onCreateOptionsMenu(menu);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
        }
    }

    @Override
    public void onDestroy() {
        map.destroy();
        super.onDestroy();
    }

    public void onSectionAttached(int number) {
        mTitle = (getResources().getStringArray(R.array.sections))[number];
    }



    public boolean isNavigationDrawerOpen() {
        return mNavigationDrawerFragment.isDrawerOpen();
    }

    public SearchBox getSearchBox() {
        return (SearchBox) findViewById(R.id.searchbox);
    }

    @Override
    public void onBackPressed() {
        SearchBox search = (SearchBox) findViewById(R.id.searchbox);
        if(search.isShown()) {
            search.toggleSearch();
        }
        else {
            super.onBackPressed();
        }
    }

    public void handleQRCode(int requestCode, int resultCode, Intent data) {
        //Parsing xzing result
        IntentResult res = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if( res != null ){

            if(checkXml(res.getContents())) {
                //Delete old map
                if (map != null)
                    map.destroy();

                //Delete old files
                clearFiles();

                //Write config.xml
                if (writeConfigFile(res.getContents())) {
                    this.map = new MapView(getApplicationContext());

                    map.update();

                    onNavigationDrawerItemSelected(1);
                } else {
                    Toast.makeText(this, "Failed to write new config file",
                            Toast.LENGTH_LONG).show();
                }
            }
            else
            {
                Toast.makeText(this, "The QRCode does not contain valid config data",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Failed to read QRCode",
                    Toast.LENGTH_LONG).show();
        }

    }
    private boolean checkXml(String content)
    {
        boolean ret = false;
        XmlPullParser parser =  Xml.newPullParser();
        try {
            parser.setInput(new StringReader(content));
            parser.nextTag();
            if(parser.getName().equals("Adsum"))
            {
                String siteId = "";
                String kioskId = "";

                int currentEvent = parser.next();
                while (currentEvent != XmlPullParser.END_DOCUMENT) {

                    if(currentEvent == XmlPullParser.START_TAG) {
                        String name = parser.getName();
                        // Starts by looking for the entry tag
                        if (name.equals("siteId")) {
                            siteId = parser.nextText();
                        } else if (name.equals("kioskId")) {
                            kioskId = parser.nextText();
                        }
                    }
                    currentEvent = parser.next();
                }
                siteId = (siteId == null) ? "" : siteId;
                kioskId = (kioskId == null) ? "" : kioskId;

                Toast.makeText(getApplicationContext(), "Loading site " + siteId + " (kioskId " + kioskId +")", Toast.LENGTH_LONG).show();

                ret = true;
            }
        }
        catch (Exception e)
        {
            return false;
        }

        return ret;
    }

    private void clearFiles()
    {
        File dir = getFilesDir();
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (String aChildren : children) {
                if(!new File(dir, aChildren).delete())
                    Log.d("SCAN","Failed to delete " + aChildren);

            }
        }
    }
    private boolean writeConfigFile(String content) {
        try {
            File path = getFilesDir();
            File file = new File(path, "config.xml");
            FileOutputStream outputStreamWriter = new FileOutputStream(file);
            outputStreamWriter.write(content.getBytes());
            outputStreamWriter.close();
            return true;
        }
        catch (IOException e) {
            Log.e("Scanning", "File write failed: " + e.toString());
            return false;
        }
    }
    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public static PlaceholderFragment newInstance(int sectionNumber, MapView map) {
            PlaceholderFragment fragment;

            switch (sectionNumber) {
                case 0:
                    fragment = DescriptionFragment.newInstance();
                    break;
                case 1:
                    fragment = MapFragment.newInstance(map);
                    break;
                default:
                    fragment = DescriptionFragment.newInstance();
                    break;
            }

            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
        }

    }

}
