package com.cmpe277.mobileninjas.eventshare;

import com.cmpe277.mobileninjas.eventshare.Albums.AlbumDS;
import com.cmpe277.mobileninjas.eventshare.Albums.AlbumsActivity;
import com.cmpe277.mobileninjas.eventshare.Images.Upload.ServiceAlarmSetter;
import com.cmpe277.mobileninjas.eventshare.adapter.NavDrawerListAdapter;
import com.cmpe277.mobileninjas.eventshare.model.NavDrawerItem;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.raweng.built.Built;
import com.raweng.built.BuiltApplication;
import com.raweng.built.BuiltError;
import com.raweng.built.BuiltObject;
import com.raweng.built.BuiltQuery;
import com.raweng.built.QueryResult;
import com.raweng.built.QueryResultsCallBack;
import com.raweng.built.utilities.BuiltConstant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener{
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private GoogleApiClient mGoogleApiClient;

    // nav drawer title
    private CharSequence mDrawerTitle;

    // used to store app title
    private CharSequence mTitle;

    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;

    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;

    public  static String userName;
    public static  String userId;

    public ArrayList<AlbumDS> eventNames = new ArrayList<AlbumDS>();
    public static HashMap<String, ArrayList<String>> imageUrls = new HashMap<>();
    public Database db = new Database(MainActivity.this);
    BuiltApplication builtApplication;



    public static  String getUserName(){
        return userName;
    }

    public static String getUserID(){
        return userId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mGoogleApiClient.connect();


        userId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("userName");

        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("USER_ID", userId);
        editor.commit();


        //startBackgroundProcess();

        try {
            builtApplication = Built.application(getApplicationContext(), "bltc86a080623f49dda");
        } catch (Exception e) {
            e.printStackTrace();
        }



        //Calling Location Tracking Service -- Bhuvan Start

       if(isMyServiceRunning(MyService.class)) {

            stopService(new Intent(MainActivity.this, MyService.class));

       }

        startService(new Intent(MainActivity.this, MyService.class));


        // -- Bhuvan End

        mTitle = mDrawerTitle = getTitle();

        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        // nav drawer icons from resources
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);


        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        navDrawerItems = new ArrayList<NavDrawerItem>();

        // adding nav drawer items to array

        // Current Event
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        // Create Event
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        // Pending Request
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        // Accepted Requests
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
        // Albums
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
        // Log Out
        navDrawerItems.add(new NavDrawerItem( navMenuTitles[5]+"\n("+ userName+")", navMenuIcons.getResourceId(5, -1)));


        // Recycle the typed array
        navMenuIcons.recycle();

        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);

        //TextView textView = new TextView(getApplicationContext());
        //textView.setText("Bhuvan Teja");
        //textView.setTextSize(30);

        //mDrawerList.addHeaderView(textView);
        mDrawerList.setAdapter(adapter);

        // enabling action bar app icon and behaving it as toggle button
		/*getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                //getActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);
                getSupportActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            // on first time display view for first nav item
            displayView(1);
        }
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    //Location Services Check  --start

    public boolean isLocationServiceOn() {

        LocationManager lm = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            Toast.makeText(getApplicationContext(), "Please enable the Location Services!! ", Toast.LENGTH_LONG)
                    .show();
            return false;

        }
        return true;
    }



    // Location Services Check --end





    /**
     * Slide menu item click listener
     * */
    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // display view for selected nav drawer item
            displayView(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* *
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("Connection Failed", "onConnectionFailed:" + connectionResult);
    }

    /**
     * Diplaying fragment view for selected nav drawer list item
     * */
    private void displayView(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new CurrentEventFragment();
                break;
            case 1:
                fragment = new CreateEventFragment();
                break;
            case 2:
                fragment = new EventRequestsFragment();
                break;
            case 3:
                fragment = new ApprovedEventFragment();
                break;
            case 4:
                startBackgroundProcess();
                startActivity(new Intent(this, AlbumsActivity.class));
                break;
            case 5:
                //fragment = new LogoutFragment();
                logout();
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment).commit();

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(navMenuTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        //getActionBar().setTitle(mTitle);
        getSupportActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    private void startBackgroundProcess() {
        try {
            eventNames = db.registeredEvents(userId);
            db.close();
            if (eventNames != null) {
                new DownloadImages().execute();
            }
        }
        catch (Exception e) {
            Log.d("PM", "No user events");
        }
    }

    public class DownloadImages extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            BuiltApplication builtApplication = null;
            try {
                builtApplication = Built.application(getApplicationContext(), "bltc86a080623f49dda");
            }
            catch (Exception e) {
                Log.d("PM", "Built.io connection error" + e.getMessage());
            }
            BuiltQuery projectQuery = builtApplication.classWithUid("images").query();
            projectQuery.containedIn("event_id", db.registeredEventIds(userId));
            projectQuery.ascending("event_id");
            projectQuery.execInBackground(new QueryResultsCallBack() {
                @Override
                public void onCompletion(BuiltConstant.ResponseType responseType, QueryResult queryResultObject, BuiltError error) {
                    if (error == null) {
                        List<BuiltObject> objects = queryResultObject.getResultObjects();
                        Log.d("PM", "Data fetched successfully" + objects.toArray().length);
                        String temp = "TEMP";
                        int i = 0;
                        ArrayList<String> indivImageUrls = new ArrayList<String>();

                        for (BuiltObject object : objects) {
                            i++;
                            try {
                                if(temp.equals(object.getString("event_id")) || temp.equals("TEMP")) {
                                    indivImageUrls.add(object.getJSONObject("image").get("url").toString());
                                }
                                else {
                                    imageUrls.put(temp, indivImageUrls);
                                    indivImageUrls = new ArrayList<String>();
                                    indivImageUrls.add(object.getJSONObject("image").get("url").toString());
                                }
                                temp = object.getString("event_id");
                                if(objects.toArray().length == i) {
                                    imageUrls.put(temp, indivImageUrls);
                                }
                                Log.d("PM", "URL: " + object.getJSONObject("image").get("url").toString() +" " +object.getString("event_id"));
                            }
                            catch (Exception e) {
                                Log.d("PM", "Exception: " + e.getMessage());
                            }
                        }
                    }
                    else {
                        Log.d("PM", "Fetch error: " + error.toString());
                    }
                }
            });
            return "Fetched";
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("PM", "Done " + result);
        }
    }


    public void logout() {
        if (mGoogleApiClient!=null) {
            if (mGoogleApiClient.isConnected()) {
                Log.d("PM", "in logout");
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        finish();
                    }
                });
            }
        }
    }
}