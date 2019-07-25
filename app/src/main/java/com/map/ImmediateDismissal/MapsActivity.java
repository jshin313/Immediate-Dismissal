package com.map.ImmediateDismissal;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.material.snackbar.Snackbar;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    SupportMapFragment mapFragment;
    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private double longitude;
    private double latitude;
    private GoogleApiClient googleApiClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private static final int POLYGON_STROKE_WIDTH_PX = 8;
    private static Boolean SnackBarShown = false;
    Snackbar snackbar;

    // The polygon that defines the boundaries and tells you when you exit them
    private final Point[] polygon = { new Point(42.353486, -71.105835),
            new Point(42.355092, -71.106543),
            new Point(42.360218, -71.096121),
            new Point( 42.364546, -71.104289),
            new Point( 42.365501, -71.102763),
            new Point(42.363359, -71.093570),
            new Point(42.364502, -71.092909),
            new Point(42.364377, -71.091079),
            new Point(42.363263, -71.091265),
            new Point(42.362793, -71.083611),
            new Point(42.360650, -71.082492),
            new Point(42.354794, -71.100255),
            new Point(42.353511, -71.105193)};

    // The polygon that tells you when you're close but not exactly outside of the boundaries
    private Point[] smallerPolygon;

    // Asks user for location permission
    private void makeLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Initializing googleApiClient
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (locationPermission != PackageManager.PERMISSION_GRANTED) {
                makeLocationPermissionRequest();
            } else {
                mMap.setMyLocationEnabled(true);
                getCurrentLocation();
            }
        } else {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        }



    }

    // Displays boundaries
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showPolygon() {

        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .clickable(false)
                .add(
                        new LatLng(42.353486, -71.105835),
                        new LatLng(42.355092, -71.106543),
                        new LatLng(42.360218, -71.096121),
                        new LatLng(42.364546, -71.104289),
                        new LatLng(42.365501, -71.102763),
                        new LatLng(42.363359, -71.093570),
                        new LatLng(42.364502, -71.092909),
                        new LatLng(42.364377, -71.091079),
                        new LatLng(42.363263, -71.091265),
                        new LatLng(42.362793, -71.083611),
                        new LatLng(42.360650, -71.082492),
                        new LatLng(42.354794, -71.100255),
                        new LatLng(42.353511, -71.105193)));


        polygon.setStrokeWidth(POLYGON_STROKE_WIDTH_PX);
        polygon.setStrokeColor(Color.RED);
        polygon.setFillColor(Color.argb(30, 200, 0, 0));

    }

    //Getting current location, moves map to the current location, and checks to make sure you're in the boundaries
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getCurrentLocation() {
        mMap.clear();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {
            //Getting longitude and latitude
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            //moving the map to location
            moveMap();
            checkBoundaries();
            showPolygon();
        }


    }

    // Tells you if a point is in a polygon
    // Credit for this goes to https://stackoverflow.com/a/8721483 and  http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
    public Boolean inPolygon(Point test) {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
            if ((polygon[i].y > test.y) != (polygon[j].y > test.y) &&
                    (test.x < (polygon[j].x - polygon[i].x) * (test.y - polygon[i].y) / (polygon[j].y-polygon[i].y) + polygon[i].x)) {
                result = !result;
            }
        }
        return result;
    }

    // Tells you if you're in the boundaries or not
    private void checkBoundaries()
    {
        Boolean inBoundaries = inPolygon(new Point(latitude, longitude));

        if (!inBoundaries)
        {
            if (SnackBarShown)
                snackbar.dismiss();
            snackbar = Snackbar.make(mapFragment.getView(), "You are outside the boundaries.", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
            SnackBarShown = true;
        }
        else
        {
            if (SnackBarShown)
                snackbar.dismiss();
            snackbar = Snackbar.make(mapFragment.getView(), "Congrats! You are now within bounds and safe from immediate dismissal.", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
            SnackBarShown = true;
        }

    }

    // Moves the map to whatever coordinates are currently stored in longitude and latitude
    private void moveMap() {
        /**
         * Creating the latlng object to store lat, long coordinates
         * adding marker to map
         * move the camera with animation
         */

        float zoom = 18;
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getCurrentLocation();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                    getCurrentLocation();

                } else {
                    makeLocationPermissionRequest();
                }
                return;
            }
        }
    }

    @Override
    public void onLocationChanged(Location location){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getCurrentLocation();
        }
        else{
            // Tell the user that they need a better phone
        }
    }


}