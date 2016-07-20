package com.cmpe277.mobileninjas.eventshare;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cmpe277.mobileninjas.eventshare.model.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class CurrentEventFragment extends Fragment {


    private GoogleMap mMap;
    List<Marker> currentMarkers = new ArrayList<Marker>();
    private boolean islocationSet = false;
    private LatLng currentLocation;
    public String eventID;
    private Handler handler = new Handler();
    Marker current;
    private String userName;
    private String userEmail;
    SupportMapFragment mSupportMapFragment;

    private MapView mapView;
    private boolean mapsSupported = true;

    Context context;


    public Float[] mrkrClrs = new Float[]{0.0f, 270.0f, 120.0f, 30.0f, 60.0f, 180.0f, 300.0f};

    String url; //Rest API URI

    String tag = "com.travelcompanion.mapsactivity";

    private Bundle mBundle;

    public CurrentEventFragment() {
    }


    private void setUpMapIfNeeded(View inflatedView) {

        if (mMap == null) {

            mMap = ((MapView) inflatedView.findViewById(R.id.map)).getMap();

            if (mMap != null) {

                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                setUpMap();
            } else {
                Toast.makeText(context, "Maps failed to load!!! ", Toast.LENGTH_LONG)
                        .show();

            }
        }


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        context = getActivity().getApplicationContext();

        url = Constants.url;


        userName = MainActivity.getUserName();
        userEmail = MainActivity.getUserID();


        //userName = "Bhuvan"; // for simulation
        //userEmail = "1"; // for simulation

        Database db = new Database(context);

        eventID = db.isEventCreated(userEmail);


        // eventID = "3";// for simulation purpose

        db.close();

        if (eventID != null && !eventID.equals("")) {

            Toast.makeText(context, "Event fetched" + eventID, Toast.LENGTH_LONG).show();

            updateServerWithCurrentLocation.run();

            getOtherUsersLocation.run();

        } else {

           // Toast.makeText(context, "Event Id returned null!!!", Toast.LENGTH_LONG).show();

        }


        View inflatedView = inflater.inflate(R.layout.activity_maps, container, false);
        try {
            MapsInitializer.initialize(this.getActivity());
        } catch (Exception e) {
            // TODO handle this situation
        }

        mapView = (MapView) inflatedView.findViewById(R.id.map);
        mapView.onCreate(mBundle);
        setUpMapIfNeeded(inflatedView);

        return inflatedView;


    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    private void setUpMap() {

        mMap.setMyLocationEnabled(true);

        int MY_PERMISSION_ACCESS_COURSE_LOCATION = 1000;

        // Enable MyLocation Layer of Google Map
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_ACCESS_COURSE_LOCATION);
        }

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //return 0 ;
        }

        // mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setMyLocationEnabled(true);

        mMap.setOnMyLocationChangeListener(myLocationChangeListener);

        // Get LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);




        // Get Current Location
       // Location myLocation = locationManager.getLastKnownLocation(provider);


        Location myLocation= null;

        if(getLocation(locationManager)!=null) {

            myLocation = getLocation(locationManager);

            // Get latitude of the current location
            double latitude = myLocation.getLatitude();

            // Get longitude of the current location
            double longitude = myLocation.getLongitude();

            // Create a LatLng object for the current location
            LatLng latLng = new LatLng(latitude, longitude);

            // Show the current location in Google Map
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            // Zoom in the Google Map
            mMap.animateCamera(CameraUpdateFactory.zoomTo(14));

        }

    }


    public Location getLocation(LocationManager locationManager) {

        Location sendLocation = null;
        try {

            boolean gps_enabled = false;
            boolean network_enabled = false;


            int MY_PERMISSION_ACCESS_COURSE_LOCATION = 1000;

            // Enable MyLocation Layer of Google Map
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSION_ACCESS_COURSE_LOCATION);
            }

            if (Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //return 0 ;
            }



            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);


            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!gps_enabled && !network_enabled) {
                // no network provider is enabled
            }
            else {


                    if (sendLocation == null) {
                        sendLocation = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }

                }


                if (gps_enabled) {


                    if (sendLocation == null) {

                        if (locationManager != null) {

                            sendLocation = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        }
                    }
                }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return sendLocation;
    }

    public void addPeopleOnMap(LatLng latLng, String title, int type) {

        Marker mrkr = mMap.addMarker(new MarkerOptions().position(latLng).title(title)
                .icon(BitmapDescriptorFactory.defaultMarker())
                .anchor(0.0f, 1.0f)
                .icon(BitmapDescriptorFactory.defaultMarker(mrkrClrs[type])));

        mrkr.showInfoWindow();

        currentMarkers.add(mrkr);
    }

    public void clearMarkers() {
        System.out.println(" Clear markers called");

        if (currentMarkers != null) {
            if (currentMarkers.size() > 0)
                System.out.println(" Removing markers ");
            for (Marker marks : currentMarkers) {
                marks.remove();
            }
        }
    }


    public void resetCurrentMarkers() {
        this.currentMarkers.clear();
    }


    private Runnable updateServerWithCurrentLocation = new Runnable() {
        public void run() {
            if (isLocationServiceOn()) {

                if (islocationSet) {
                    //Inform server about current location
                    AsyncHttpClient client = new AsyncHttpClient();

                    try {

                        HashMap<String, String> paramMap = new HashMap<String, String>();
                        RequestParams params = new RequestParams(paramMap);
                        params.put("event_id", eventID);
                        params.put("user_id", userEmail);
                        params.put("latitude", currentLocation.latitude);
                        params.put("longitude", currentLocation.longitude);

                        System.out.println("Going to update server with current location");

                        //StringEntity entity = new StringEntity("{'data': [{'event_id':'"+eventID+"','user_id':'"+MainActivity.getUserID()+"','latitude': '"+currentLocation.latitude+"','longitude':'"+currentLocation.longitude+"'}]}");

                        client.post(context, url + "/updateCurrentLocation/", params,
                                new JsonHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                                        System.out.println("Server Update status" + response.toString());

                                        Log.d("Response", "" + response);
                                    }
                                }
                        );

                    } catch (Exception e) {

                    }

                }
            }
            Log.d(tag, "Updating current location thread executed");
            handler.postDelayed(this, 5000); // Thread running at the interval of 5 sec
        }
    };


    private Runnable getOtherUsersLocation = new Runnable() {

        public void run() {

            System.out.println("Check 1================");

            if (isLocationServiceOn()) {
                AsyncHttpClient client = new AsyncHttpClient();
                try {

                    System.out.println("Check 2================");

                    HashMap<String, String> paramMap = new HashMap<String, String>();
                    RequestParams params = new RequestParams(paramMap);
                    params.put("event_id", eventID);

                    client.get(context, url + "/getUserLocation/", params,
                            new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                    ArrayList<OtherPeople> others = new ArrayList<OtherPeople>();

                                    try {

                                        System.out.println("Check 3================");
                                        System.out.println("response iss::::::" + response.toString());
                                        if (response.has("users")) {

                                            Log.d("Response", "" + response.getString("users"));
                                            JSONArray array = response.getJSONArray("users");
                                            for (int i = 0; i < array.length(); i++) {
                                                if (array.getJSONObject(i).has("latitude") && array.getJSONObject(i).has("longitude") && array.getJSONObject(i).has("user_name")  && array.getJSONObject(i).has("user_id") ) {
                                                    OtherPeople obj = new OtherPeople();
                                                    if(!array.getJSONObject(i).getString("user_id").equals(userEmail) && !(array.getJSONObject(i).getString("latitude").equals("")) &&!(array.getJSONObject(i).getString("longitude").equals("")) ) {
                                                        obj.location = new LatLng(Double.parseDouble(array.getJSONObject(i).getString("latitude")), Double.parseDouble(array.getJSONObject(i).getString("longitude")));
                                                        //obj.location = new LatLng(array.getJSONObject(i).getDouble("latitude"), array.getJSONObject(i).getDouble("longitude"));
                                                        obj.name = array.getJSONObject(i).getString("user_name");
                                                        others.add(obj);

                                                    }


                                                    else
                                                        System.out.println("Same user details are neglected!!");
                                                }
                                            }
                                            addOtherPeople(others);

                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }

                            });
                } catch (Exception e) {

                }
            }
            Log.d(tag, "fetching other people location thread executed");
            handler.postDelayed(this, 5000); // Thread running at a time interval of 5 sec
        }
    };


    public void addOtherPeople(ArrayList<OtherPeople> others) {
        clearMarkers();
        resetCurrentMarkers();
        Log.d("CurrentMarker", "" + currentMarkers);
        int count = 1;
        int size = (mrkrClrs.length) - 1;
        for (OtherPeople o : others) {
            addPeopleOnMap(o.location, o.name, count % size);
            count++;
        }

    }

    public boolean isLocationServiceOn() {

        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
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
            Toast.makeText(context, "Please enable the Location Services!! ", Toast.LENGTH_LONG)
                    .show();
            return false;

        }
        return true;
    }

    public void setCurrentLocation(LatLng location) {
        islocationSet = true;
        currentLocation = location;
    }

    public void addCurrentLocationOnMap(LatLng latLng, String title) {

        if (current != null)
            current.remove();

        current = mMap.addMarker(new MarkerOptions().position(latLng).title(title)
                .icon(BitmapDescriptorFactory.defaultMarker())
                .snippet("Consider yourself located")
                .anchor(0.0f, 1.0f));

        current.showInfoWindow();

    }


    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
            setCurrentLocation(loc);

            //Toast.makeText(context, "Location changed!! ", Toast.LENGTH_LONG).show();

            Log.d("Current Loc string", new Gson().toJson(loc));

            // for simulation purpose -- start

            addCurrentLocationOnMap(loc, "You are here!");

            /*
            ArrayList<OtherPeople> others=new ArrayList<OtherPeople>();

            OtherPeople obj1 = new OtherPeople();
            obj1.location =new LatLng(37.3359136,-121.8839874);
            obj1.name="Bhuvan";

            OtherPeople obj2 = new OtherPeople();
            obj2.location =new LatLng(37.337136,-121.8845874);
            obj2.name="Teja";

            OtherPeople obj3 = new OtherPeople();
            obj3.location =new LatLng(37.3359136,-121.8879874);
            obj3.name="Guddanti";

            OtherPeople obj4 = new OtherPeople();
            obj4.location =new LatLng(37.3351136, -121.8899874);
            obj4.name="Anupam";

            others.add(obj1);
            others.add(obj2);
            others.add(obj3);
            others.add(obj4);

            addOtherPeople(others);

            */

            // for simulation purpose -- end

            if (mMap != null) {
               // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
            }
        }
    };

}

class OtherPeople {
    public LatLng location;
    public String name;
}
