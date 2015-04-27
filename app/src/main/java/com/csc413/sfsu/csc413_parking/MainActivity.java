package com.csc413.sfsu.csc413_parking;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.RelativeLayout;

import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;

/**
 * Author: Luis Estrada + UI Team (Jonathan Raxa & Ishwari)
 *  Class: CSC413
 */
public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap theMap;
    private LocationManager locMan;
    private Marker userMarker;
    private static final int GPS_ERRORDIALOG_REQUEST = 9001;

    private TextView mLocationView;

    private GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;

    MenuItem item; // action bar icon

    /**
     * Where activity is initialized
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(servicesOK()){
            setContentView(R.layout.activity_main);

            if (initMap()){
                Toast.makeText(this,"Ready to park!", Toast.LENGTH_SHORT).show();
                mLocationView = new TextView(this);

                theMap.setMyLocationEnabled(true);
                theMap.setIndoorEnabled(false);
                theMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                theMap.getUiSettings().setZoomControlsEnabled(true);

                updatePlaces();

            } else{
                Toast.makeText(this,"Map Unavailable!", Toast.LENGTH_SHORT).show();
            }

        } else {
            setContentView(R.layout.activity_main);
        }
    }

    /**
     * Prints message if connected to location services
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this,"Connected to location services", Toast.LENGTH_SHORT).show();
    }

    /**
     * Implementing the location listener
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {
        // Log.i(TAG, "GoogleApiClient connection has been suspend");
        Toast.makeText(this,"Connected to location services", Toast.LENGTH_SHORT).show();
        LocationRequest request = LocationRequest.create();

        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(60000); //every 5 seconds
        request.setFastestInterval(1000);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //  Log.i(TAG, "GoogleApiClient connection has failed");
    }

    /**
     * Prints location coordinates
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        // mLocationView.setText("Location received: " + location.toString());
        String msg = "Location: " + location.getLatitude() + "," + location.getLongitude();
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    /**
     * Checks connection to Google Play Services
     * @return true if successfully connected, false if not
     */
    public boolean servicesOK(){
        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if(isAvailable == ConnectionResult.SUCCESS){
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)){
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, GPS_ERRORDIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "Can't connect to Google Play services", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * Initializes the map object
     * @return true if map is initialized, false if not
     */
     private boolean initMap() {
        if (theMap == null) {
            SupportMapFragment mapFrag =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            theMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
            //theMap = mapFrag.getMap();

        }
        if (theMap != null) {
            theMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                /**
                 *
                 * @param marker
                 * @return
                 */
                @Override
                public View getInfoContents(Marker marker) {
                    View v = getLayoutInflater().inflate(R.layout.info_window, null);
                    TextView tvLocality = (TextView)v.findViewById(R.id.tv_locality);
                    TextView tvLat = (TextView)v.findViewById(R.id.tv_lat);
                    TextView tvLng = (TextView)v.findViewById(R.id.tv_lng);
                    TextView tvSnippet = (TextView)v.findViewById(R.id.tv_snippet);

                    // gets latitude and longitude
                    LatLng ll = marker.getPosition();

                    tvLocality.setText(marker.getTitle());
                    tvLat.setText("Latitude: "+ll.latitude);
                    tvLng.setText("Longitude: "+ll.longitude);
                    tvSnippet.setText(marker.getSnippet());

                    return v;
                }
            });
        }
        return(theMap != null);
    }

    /**
     * Moves camera to specified location
     * @param lat - latitude
     * @param lng - longitude
     * @param zoom - zoom level
     */
    private void gotoLocation(double lat, double lng, float zoom) {

        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll,zoom);
        theMap.moveCamera(update);
    }

    /**
     * Finds coordinates, sets marker & moves camera to marker
     */
    private void updatePlaces(){
        //update location
        locMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location lastLoc = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        double lat = lastLoc.getLatitude();
        double lng = lastLoc.getLongitude();

        LatLng lastLatLng = new LatLng(lat, lng);

        if(userMarker!=null) userMarker.remove();

        userMarker = theMap.addMarker(new MarkerOptions()

                .position(lastLatLng)
                .title("Parking Location")
                .snippet("You are here"));

        userMarker.setDraggable(true);

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(lastLatLng, 18);
        theMap.moveCamera(update);
        theMap.animateCamera(CameraUpdateFactory.newLatLng(lastLatLng), 3000, null);
    }

    /**
     * Initialize the contents of the Activity's standard options menu (only called once)
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater mif = getMenuInflater();
        mif.inflate(R.menu.menu_main, menu);
        item = menu.findItem(R.id.parked_icon); // get parked icon
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Prints message for some checkable items on action bar
     * @param item - icon selected on action bar
     * @param msg - message to be printed in toast
     */
    private void checkableToast(MenuItem item, int msg) {
        if (item.isChecked()) {
            item.setChecked(false);
        } else {
            item.setChecked(true);
            Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }
    // overloaded

    /**
     * Called when user clicks on icon in action bar
     * @param item - item selected on action bar
     * @return true if option selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //RelativeLayout main_view = (RelativeLayout) findViewById(R.id.derp);

        // handle action bar item clicks
        switch(item.getItemId()) {

            // search
            case R.id.search_icon:
                Toast.makeText(getBaseContext(), "Search for a location", Toast.LENGTH_SHORT).show();
                return true;

            // layers
            case R.id.layers_icon:
                Toast.makeText(getBaseContext(), "Layers", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.layer_1:
                checkableToast(item, R.string.layer_1);
                return true;
            case R.id.layer_2:
                checkableToast(item, R.string.layer_2);
                return true;
            case R.id.layer_3:
                checkableToast(item, R.string.layer_3);
                return true;
            case R.id.layer_4:
                checkableToast(item, R.string.layer_4);
                return true;

            // parked
            case R.id.parked_icon:
                if(item.isChecked()) {
                    item.setChecked(false);
                    //item.setIcon(R.drawable.ic_car_2); // change to uncolored car
                    userMarker.setIcon(BitmapDescriptorFactory.defaultMarker());
                    Toast.makeText(getBaseContext(), "No longer parked", Toast.LENGTH_SHORT).show();
                } else {
                    updatePlaces();
                    item.setChecked(true);
                    //item.setIcon(R.drawable.ic_car_checked); // change to colored car
                    userMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car_position));
                    Toast.makeText(getBaseContext(), "Parked", Toast.LENGTH_SHORT).show();
                }
                return true;

            // action overflow
            case R.id.favorite:
                if(item.isChecked()) { // if checked & user clicks on it
                    item.setChecked(false);
                    Toast.makeText(getBaseContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                } else {
                    item.setChecked(true);
                    Toast.makeText(getBaseContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
                }
                return true;

            // settings
            case R.id.settings:
                Toast.makeText(getBaseContext(), "Settings", Toast.LENGTH_SHORT).show();

            default:
                return super.onOptionsItemSelected(item);
        } // end switch
    } // end onOptionsItemSelected
} // end MainActivity