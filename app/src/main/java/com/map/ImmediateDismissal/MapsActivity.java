package com.map.ImmediateDismissal;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.material.snackbar.Snackbar;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener,
        View.OnClickListener {

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
    private final Point[] points = { new Point(42.353486, -71.105835),
                        new Point(42.355092, -71.106543),
                        new Point(42.360218, -71.096121),
                        new Point(42.363322, -71.101242),
                        new Point(42.363896, -71.100635),
                        new Point(42.363359, -71.093570),
                        new Point(42.364502, -71.092909),
                        new Point(42.364377, -71.091079),
                        new Point(42.363263, -71.091265),
                        new Point(42.362793, -71.083611),
                        new Point(42.360650, -71.082492),
                        new Point(42.354794, -71.100255),
                        new Point(42.353511, -71.105193)};

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
                showPolygon();
                moveMap();
            }
        } else {
            mMap.setMyLocationEnabled(true);
            showPolygon();
            moveMap();
        }



    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showPolygon() {

        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .clickable(false)
                .add(
                        new LatLng(42.353486, -71.105835),
                        new LatLng(42.355092, -71.106543),
                        new LatLng(42.360218, -71.096121),
                        new LatLng(42.363322, -71.101242),
                        new LatLng(42.363896, -71.100635),
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

    //Getting current location
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
        }


    }

    // Credit for this goes to https://stackoverflow.com/a/8721483 and  http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
    public Boolean inPolygon(Point test) {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = points.length - 1; i < points.length; j = i++) {
            if ((points[i].y > test.y) != (points[j].y > test.y) &&
                    (test.x < (points[j].x - points[i].x) * (test.y - points[i].y) / (points[j].y-points[i].y) + points[i].x)) {
                result = !result;
            }
        }
        return result;
    }

    private void checkBoundaries()
    {
        Boolean inBoundaries = inPolygon(new Point(latitude, longitude));

        if (!inBoundaries)
        {
            snackbar = Snackbar.make(mapFragment.getView(), "You are outside the boundaries", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
            SnackBarShown = true;
        }
        else
        {
            if (SnackBarShown) {
                snackbar.dismiss();
                SnackBarShown = false;
            }
        }

    }

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

    @Override
    public void onClick(View view) {
        Log.v(TAG, "view click event");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getCurrentLocation();
        showPolygon();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        // mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        Toast.makeText(MapsActivity.this, "onMarkerDragStart", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        Toast.makeText(MapsActivity.this, "onMarkerDrag", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        // getting the Co-ordinates
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;

        //move to current position
        moveMap();
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


    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(MapsActivity.this, "onMarkerClick", Toast.LENGTH_SHORT).show();
        return true;
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
                    getCurrentLocation();
                    mMap.setMyLocationEnabled(true);
                    showPolygon();


                } else {
                }
                return;
            }
        }
    }


}

