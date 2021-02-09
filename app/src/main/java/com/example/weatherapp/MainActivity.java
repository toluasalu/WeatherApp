package com.example.weatherapp;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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

import java.text.MessageFormat;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    TextView tempTextView;
    TextView locationTextView;
    TextView  weatherTextView;
    ImageView backgroundImage;
  private static  final String TAG = MainActivity.class.getSimpleName();
  private  static final int PERMISSION_REQUEST_INTERNET = 0;
  private static final int PERMISSION_REQUEST_LOCATION = 1;
  private View mLayout;
  private RequestQueue requestQueue;
  private double mLatitude;
  private double mLongitude;
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
        backgroundImage = findViewById(R.id.imageView);
        requestQueue = Volley.newRequestQueue(mLayout.getContext().getApplicationContext());

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


    }

    @Override
    protected void onStart() {
        super.onStart();
        checkLocationPermission();
        checkInternetPermission();

    }

    private void checkLocationPermission() {
        //Check if the Location Permission has been granted
        if(ActivityCompat.checkSelfPermission(mLayout.getContext().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            //Location Permission is already available
            Snackbar.make(mLayout,
                    R.string.location_permission_available,
                    Snackbar.LENGTH_SHORT).show();
            getLastLocation();
        }  else {
            //Permission is missing and must be requested
            Snackbar.make(mLayout,
                    R.string.requesting_location_permission,
                    Snackbar.LENGTH_SHORT).show();
               requestLocationPermission();

        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with cda button to request the missing permission.
            Snackbar.make(mLayout, R.string.location_permission_necessity,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions((Activity) mLayout.getContext(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_LOCATION);
                }
            }).show();

        } else {
            Snackbar.make(mLayout, R.string.location_permission_denied, Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions((Activity) mLayout.getContext(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
       mFusedLocationClient.getLastLocation().addOnCompleteListener(this,new OnCompleteListener<Location>() {
           @Override
           public void onComplete(@NonNull Task<Location> task) {
               if (task.isSuccessful() && task.getResult() != null) {
                   mLastLocation = task.getResult();
                   mLatitude = mLastLocation.getLatitude();
                   mLongitude = mLastLocation.getLongitude();
                   Snackbar.make(mLayout,
                           R.string.location_detected,
                           Snackbar.LENGTH_LONG).show();

               } else {
                   Log.w(TAG, "getLastLocation:exception", task.getException());
                   Snackbar.make(mLayout,
                           R.string.no_location_detected,
                           Snackbar.LENGTH_LONG).show();
               }
           }
       });

    }

    private void checkInternetPermission() {
        // Check if the Internet permission has been granted
        if (ActivityCompat.checkSelfPermission(mLayout.getContext().getApplicationContext(), Manifest.permission.INTERNET)
                == PackageManager.PERMISSION_GRANTED) {
            // Internet Permission is already available, go ahead and make network request
            Snackbar.make(mLayout,
                    R.string.camera_permission_available,
                    Snackbar.LENGTH_SHORT).show();
            if(mLongitude > 0 && mLatitude > 0){
                makeNetworkRequest(mLatitude, mLongitude);
            }  else {
                makeNetworkRequest();
            }

        } else {
            // Permission is missing and must be requested.
            Snackbar.make(mLayout,
                    R.string.requesting_internet_permission,
                    Snackbar.LENGTH_SHORT).show();
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
                    new String[]{Manifest.permission.INTERNET}, PERMISSION_REQUEST_INTERNET);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_INTERNET) {
            //Request for internet permission
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted.
                Snackbar.make(mLayout, R.string.internet_permission_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();
                if(mLongitude > 0 && mLatitude > 0){
                    makeNetworkRequest(mLatitude, mLongitude);
                }  else {
                    makeNetworkRequest();
                }
            } else {
                // Permission request was denied.
                Snackbar.make(mLayout, R.string.internet_permission_denied,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }

        }
         else if(requestCode == PERMISSION_REQUEST_LOCATION){
             //Request for Location permission
            if(grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // Permission has been granted
                Snackbar.make(mLayout, R.string.location_permission_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();
                     getLastLocation();
            } else {
                // Permission request was denied.
                Snackbar.make(mLayout, R.string.location_permission_denied,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void makeNetworkRequest() {


        String url = "http://api.openweathermap.org/data/2.5/weather?lat=8.9943827&lon=7.5861802&appid=016a17805cf72b8426b9651f731700f5";



        // Request a string response from the provided URL.
        JsonRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                           JSONObject main =  response.getJSONObject("main");
                           JSONArray weather = response.getJSONArray("weather");
                           JSONObject weatherResponseObject = weather.getJSONObject(0);
                           String weatherInfo = weatherResponseObject.getString("main").toUpperCase();
                           String location = response.getString("name");
                           String temp = main.getString("temp");
                           tempTextView.setText(temp);
                           locationTextView.setText(location);
                           weatherTextView.setText(weatherInfo);
                           setBackgroundImage(weatherInfo);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    private void setBackgroundImage(String weatherInformation) {
                        switch (weatherInformation){
                            case "CLOUDS": backgroundImage.setImageResource(R.drawable.dark_stormy_clouds);
                                   break;

                            case "RAIN":  backgroundImage.setImageResource(R.drawable.outdoor_child_playing_joy_cheerful);
                                 break;

                            case "CLEAR": backgroundImage.setImageResource(R.drawable.blue_sky_with_puffy_white_clouds);
                                  break;

                            case "DRIZZLE": backgroundImage.setImageResource(R.drawable._0424679);
                                  break;
                            
                            case "SNOW": backgroundImage.setImageResource(R.drawable._d_snowy_landscape_with_trees);
                                  break;


                            default:
                                backgroundImage.setImageResource(R.drawable.white_cloud_blue_sky);
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


    }

    private void makeNetworkRequest(double latitude, double longitude) {


        String restUrl = "http://api.openweathermap.org/data/2.5/weather?lat={0}&lon={1}&appid=016a17805cf72b8426b9651f731700f5";
        String url = createUrlwithLocationParams(restUrl, latitude,longitude);


        // Request a string response from the provided URL.
        JsonRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject main =  response.getJSONObject("main");
                            JSONArray weather = response.getJSONArray("weather");
                            JSONObject weatherResponseObject = weather.getJSONObject(0);
                            String weatherInfo = weatherResponseObject.getString("main").toUpperCase();
                            String location = response.getString("name");
                            String temp = main.getString("temp");
                            tempTextView.setText(temp);
                            locationTextView.setText(location);
                            weatherTextView.setText(weatherInfo);
                            setBackgroundImage(weatherInfo);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    private void setBackgroundImage(String weatherInformation) {
                        switch (weatherInformation){
                            case "CLOUDS": backgroundImage.setImageResource(R.drawable.dark_stormy_clouds);
                                break;

                            case "RAIN":  backgroundImage.setImageResource(R.drawable.outdoor_child_playing_joy_cheerful);
                                break;

                            case "CLEAR": backgroundImage.setImageResource(R.drawable.blue_sky_with_puffy_white_clouds);
                                break;

                            case "DRIZZLE": backgroundImage.setImageResource(R.drawable._0424679);
                                break;

                            case "SNOW": backgroundImage.setImageResource(R.drawable._d_snowy_landscape_with_trees);
                                break;


                            default:
                                backgroundImage.setImageResource(R.drawable.white_cloud_blue_sky);
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


    }




    private String createUrlwithLocationParams(String url, Object ... params) {
       return new MessageFormat(url).format(params);
    }
}