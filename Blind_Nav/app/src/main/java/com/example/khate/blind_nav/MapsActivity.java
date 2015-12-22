package com.example.khate.blind_nav;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.StrictMode;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        OnConnectionFailedListener {

    private GoogleMap mMap;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Context context;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = MapsActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;
    Marker marker;
    Location myCurrentLocation;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    Circle shape;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        promptSpeechInput();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {

        Toast.makeText(this, "On Connected", Toast.LENGTH_SHORT).show();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {

            Toast.makeText(this, mLastLocation.getLatitude() + " " + mLastLocation.getLongitude(),Toast.LENGTH_SHORT).show();
        }
        myCurrentLocation = mLastLocation;
        handleNewLocation(mLastLocation);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        handleNewCurrentLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /// === Initial Current location
    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("I am here!");
        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

    }

    /// === Updated Current location
    private void handleNewCurrentLocation(Location location) {
        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(currentLatitude + ", " + currentLongitude)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

    }

    public void onClick1(String destination)throws IOException {

        Geocoder gc = new Geocoder(this);

        List<Address> list1 = gc.getFromLocationName(destination, 1);
        //Toast.makeText(this, list.get(0).toString(), Toast.LENGTH_SHORT).show();
        Address add1 = list1.get(0);

        String locality1 = add1.getLocality();

        //Toast.makeText(this, locality, Toast.LENGTH_SHORT).show();

        double lat1 = add1.getLatitude();
        double lng1 = add1.getLongitude();

        gotoLocation(myCurrentLocation.getLatitude(), myCurrentLocation.getLongitude(), 6);
        gotoLocation(lat1, lng1, 6);

        setMarker("Current Location", myCurrentLocation.getLatitude(), myCurrentLocation.getLongitude());
        setMarkerCircle(add1.getLocality(), lat1, lng1);

        drawPath(new LatLng(myCurrentLocation.getLatitude(), myCurrentLocation.getLongitude()), new LatLng(lat1, lng1));
        testFunction(new LatLng(myCurrentLocation.getLatitude(), myCurrentLocation.getLongitude()), new LatLng(lat1, lng1));

    }


    //// === This function will geather the Instructions and the point where to give instruction
    private void testFunction(LatLng source, LatLng destination){
        String text = "";
        try {

            int SDK_INT = android.os.Build.VERSION.SDK_INT;
            if (SDK_INT > 8) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                        .permitAll().build();
                StrictMode.setThreadPolicy(policy);
                //your codes here


                GMapV2Direction md = new GMapV2Direction();
                mMap = ((SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map)).getMap();
                Document doc = md.getDocument(source, destination,
                        GMapV2Direction.MODE_DRIVING);

                ArrayList<String> directionPoint = md.getTurns(doc);
                ArrayList<LatLng> directionPoint1 = md.getTurnsPoint(doc);

                for (int i = 0; i < directionPoint.size(); i++) {
                    text += directionPoint.get(i) + "\n" + directionPoint1.get(i) + "\n\n";         /// === Instruction's list
                    setMark(directionPoint1.get(i));        /// === Instruction's Points
                }

            }

        }

        catch (Exception e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

        }

    }

    private void gotoLocation(double lat, double lng, float zoom){
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mMap.moveCamera(update);
    }

    //// === Normal / Current location
    private void setMarker(String locality, double lat, double lng) {
        /*if (marker != null) {
            marker.remove();
        }*/
        MarkerOptions options = new MarkerOptions()
                .position(new LatLng (lat, lng))
                .title(locality);
        //.flat(true);
        marker = mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15));
    }

    //// === Instruction's mark
    private void setMark(LatLng ll) {
        /*if (marker != null) {
            marker.remove();
        }*/
        MarkerOptions options = new MarkerOptions()
                .position(ll)
                .title(ll.latitude + ", " + ll.longitude);
        //.flat(true);
        marker = mMap.addMarker(options);

        CircleOptions op = new CircleOptions()
                .center(ll)
                .radius(40)
                .fillColor(0x330000FF)
                .strokeColor(Color.BLUE)
                .strokeWidth(3);

        mMap.addCircle(op);
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15));
    }

    //// === Destination Mark
    private void setMarkerCircle(String locality, double lat, double lng) {
        /*if (marker != null) {
            marker.remove();
        }*/
        MarkerOptions options = new MarkerOptions()
                .position(new LatLng (lat, lng))
                .title(locality);
        //.flat(true);
        marker = mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15));


        CircleOptions op = new CircleOptions()
                .center(new LatLng(lat,lng))
                .radius(150)
                .fillColor(0x330000FF)
                .strokeColor(Color.BLUE)
                .strokeWidth(3);

        mMap.addCircle(op);
    }

    private void drawPath(LatLng source, LatLng destination) {
        try {

            int SDK_INT = android.os.Build.VERSION.SDK_INT;
            if (SDK_INT > 8) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                        .permitAll().build();
                StrictMode.setThreadPolicy(policy);
                //your codes here


                GMapV2Direction md = new GMapV2Direction();
                mMap = ((SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map)).getMap();
                Document doc = md.getDocument(source, destination,
                        GMapV2Direction.MODE_DRIVING);

                ArrayList<LatLng> directionPoint = md.getDirection(doc);
                PolylineOptions rectLine = new PolylineOptions().width(3).color(
                        Color.RED);

                for (int i = 0; i < directionPoint.size(); i++) {
                    rectLine.add(directionPoint.get(i));
                }
                Polyline polylin = mMap.addPolyline(rectLine);
            }
        }

        catch (Exception e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

        }

    }


    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //txtSpeechInput.setText(result.get(0));
                    try {
                        onClick1(result.get(0));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                break;
            }

        }
    }
}
