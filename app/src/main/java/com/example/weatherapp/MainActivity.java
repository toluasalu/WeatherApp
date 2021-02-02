package com.example.weatherapp;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    TextView tempTextView;
    TextView locationTextView;
    TextView  weatherTextView;
  private static  final String TAG = MainActivity.class.getSimpleName();
  private  static final int PERMISSION_REQUEST_INTERNET = 0;
  private View mLayout;
  private RequestQueue requestQueue;
  double mLatitude;
  double mLongitude;
    /**
     * Provides the entry point to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayout = findViewById(R.id.main_layout);
        tempTextView = findViewById(R.id.tempTextView);
        locationTextView = findViewById(R.id.locationTextView);
        weatherTextView = findViewById(R.id.weathertextView);
        requestQueue = Volley.newRequestQueue(mLayout.getContext().getApplicationContext());
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


    }


    @Override
    protected void onStart() {
        super.onStart();
        checkInternetPermission();
        if(!checkLocationPermissions()){
            requestLocationPermissions();
        }

    }


    private boolean checkLocationPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });

        }
        else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest();
        }
    }

    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();
                            mLatitude = mLastLocation.getLatitude();
                            mLongitude = mLastLocation.getLongitude();



                        } else {
                            Log.w(TAG, "getLastLocation:exception", task.getException());
                            showSnackbar(getString(R.string.no_location_detected));
                        }
                    }
                });
    }


    private void showSnackbar(final String text) {
        View container = findViewById(R.id.main_layout);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }



    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void checkInternetPermission() {
        // Check if the Internet permission has been granted
        if (ActivityCompat.checkSelfPermission(mLayout.getContext().getApplicationContext(), Manifest.permission.INTERNET)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available, start camera preview
            Snackbar.make(mLayout,
                    R.string.camera_permission_available,
                    Snackbar.LENGTH_SHORT).show();
            makeNetworkRequest();
        } else {
            // Permission is missing and must be requested.
            requestInternetPermission();
        }
    }

    private void requestInternetPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mLayout.getContext(),
                Manifest.permission.INTERNET)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with cda button to request the missing permission.
            Snackbar.make(mLayout, R.string.internet_access_required,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions((Activity) mLayout.getContext(),
                            new String[]{Manifest.permission.INTERNET},
                            PERMISSION_REQUEST_INTERNET);
                }
            }).show();

        } else {
            Snackbar.make(mLayout, R.string.internet_unavailable, Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions((Activity) mLayout.getContext(),
                    new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_INTERNET);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST_INTERNET){
            //Request for internet permission
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted.
                Snackbar.make(mLayout, R.string.internet_permission_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();
                makeNetworkRequest();
            } else {
                // Permission request was denied.
                Snackbar.make(mLayout, R.string.internet_permission_denied,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }

        }
    }

    private void makeNetworkRequest() {


        String url;
        url = String.format("api.openweathermap.org/data/2.5/weather?lat=%f"
                + "&lon=%d" + "&appid=016a17805cf72b8426b9651f731700f5", mLatitude, mLongitude);

        // Request a string response from the provided URL.
        JsonRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                           JSONObject main =  response.getJSONObject("main");
                           JSONArray weather = response.getJSONArray("weather");
                           JSONObject weatherResponseObject = weather.getJSONObject(0);
                           String weatherInfo = weatherResponseObject.getString("description").toUpperCase();
                           String location = response.getString("name");
                           String temp = main.getString("temp");
                           tempTextView.setText(temp);
                           locationTextView.setText(location);
                           weatherTextView.setText(weatherInfo);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                Snackbar.make(mLayout,
                        R.string.process_failed,
                        Snackbar.LENGTH_SHORT).show();

            }
        });

        jsonRequest.setRetryPolicy(new DefaultRetryPolicy
                (20 * 1000, 1, 1.0f));
        /* Add your Requests to the RequestQueue to execute */
        requestQueue.add(jsonRequest);

        // Add the request to the RequestQueue.
        requestQueue.add(jsonRequest);
    }
}