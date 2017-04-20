package com.example.android.drinkwaterfree;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

import static android.media.CamcorderProfile.get;


public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    GoogleMap m_map;
    boolean mapReady = false;
    private static final int PERMISSION_REQUEST_CODE_LOCATION = 1; //NEW

    ArrayList<String[]> coordinates = new ArrayList<>();

    private final String LOG_TAG = AppCompatActivity.class.getSimpleName();
    private TextView txtOutput;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private static final int MY_PERMISSIONS_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //shows the map
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        configureLocationUpdates();

        try {
            LoadLocations();

        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),
                    "Problems: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }



    }

    @Override
    public void onMapReady(GoogleMap map) {
        mapReady = true;
        m_map = map;
        LatLng amsterdamNL = new LatLng(52.3702, 4.8952);
        CameraPosition target = CameraPosition.builder().target(amsterdamNL).zoom(14).build();
        m_map.moveCamera(CameraUpdateFactory.newCameraPosition(target));

//        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
//                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
//
//        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
//            @Override
//            public void onPlaceSelected(Place place) {
//                // TODO: Get info about the selected place.
//                Log.i("PlaceAutoComplete", "Place: " + place.getName());
//            }
//
//            @Override
//            public void onError(Status status) {
//                // TODO: Handle the error.
//                Log.i("PlaceAutoComplete", "An error occurred: " + status);
//            }
//        });
        }

    @Override
    protected void onStart() {
        super.onStart();
        //Connect the Client
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        //Disconnect the Client
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private void configureLocationUpdates() {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(3*60000); //Update location every 3 minutes
    }

    private void requestLocationUpdate() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //This check necessary for API 23 or higher (Android 6.0)
        int permissionCheck = ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this, new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }

        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    requestLocationUpdate();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    txtOutput.setText("No permission to get the location");
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "GoogleApiClient connection has been suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "GoogleApiClient connection has failed");
    }

    Marker marker;
    @Override
    public void onLocationChanged(Location location) {
        Log.v("CURRENT LOCATION", location.toString());
        LatLng currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
        //Bundle currentAddress = location.getExtras();
        //Log.v("CURRENT ADDRESS", currentAddress.toString());

        // Add a marker to display current location of device and reposition the map
        // First check if there is a marker already and remove it if it is there
        if (marker != null) {
            marker.remove();
        }

        // If there is no marker or marker was removed, put a new marker to display current locaton
        marker = m_map.addMarker(new MarkerOptions()
                .position(currentLoc)
                .title("Here are you!")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        //Reposition the map to display current location
        CameraPosition CURRENT = CameraPosition.builder().target(currentLoc).zoom(14).build();
        flyTo(CURRENT);

        //Adds Markers to the map from where_water1.txt
        addMarkersToMap();

    }

    private void flyTo(CameraPosition target) {
        m_map.animateCamera(CameraUpdateFactory.newCameraPosition(target), 2000, null);
    }


    public void LoadLocations() throws IOException {
        String str="";
        InputStream is = this.getResources().openRawResource(R.raw.where_water1);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        if (is!=null) {

            while ((str = reader.readLine()) != null) {
                String[] coord = str.split(",");
                coordinates.add(coord);
                  }
        }
        is.close();
    }

    public void addMarkersToMap() {
        for (int j = 1; j<coordinates.size(); j ++){
            String fountainaddress = coordinates.get(j)[2] +", "+coordinates.get(j)[3];
            double fountainlat = Double.parseDouble(coordinates.get(j)[0].trim());
            double fountainlng = Double.parseDouble(coordinates.get(j)[1].trim());
            LatLng fountainCoordinates = new LatLng(fountainlat, fountainlng);

            m_map.addMarker(new MarkerOptions()
                    .position(fountainCoordinates)
                    .title(fountainaddress)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }
    }



}



