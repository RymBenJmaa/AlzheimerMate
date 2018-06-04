package com.sim.alzheimermate.Services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;
import com.sim.alzheimermate.Activities.Main2Activity;
import com.sim.alzheimermate.Models.VolleySingleton;
import com.sim.alzheimermate.R;
import com.sim.alzheimermate.Utils.SharedData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PositionService extends Service {
    LocationListener locationListener;
    private LatLng home;
    private Location lastLocation;
    private String url;
    private LatLng ltng, l;
    private LocationManager locationManager;
    private boolean nrawaaa7;

    public PositionService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("location service");
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        // Or, use GPS location data:
        // String locationProvider = LocationManager.GPS_PROVIDER;
        l = getActualPosition();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
        getHomeRequest(SharedData.url + "getHome?email=" + SharedData.alzMail);
        url = SharedData.url + "position";

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("MyService", "MyPositionService is Started");
        l = getActualPosition();

        if (l != null) {
            savePosition(url, Double.toString(l.latitude), Double.toString(l.longitude));
            nrawaaa7 = NeedToGoHomeDistance(l, home);
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                savePosition(url, Double.toString(lastLocation.getLatitude()), Double.toString(lastLocation.getLongitude()));
            }

        }
        if (nrawaaa7) {
            createNotification();
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public LatLng getActualPosition() {
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                ltng = new LatLng(location.getLatitude(), location.getLongitude());

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            //MapFragment.showPermissionAlert();
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            return ltng;
        } else
            return null;

    }

    public void savePosition(String urlJsonObj, final String la, final String lo) {


        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("latitude", la);
            jsonBody.put("longitude", lo);
            jsonBody.put("email", SharedData.alzMail);
        } catch (JSONException e) {
            System.out.println(e);
            e.printStackTrace();
        }
        final String requestBody = jsonBody.toString();
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                urlJsonObj, (JSONObject) null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    System.out.println(response);
                    if (response.getString("success").equals("true")) {
                        System.out.println("Service added " + la + " " + lo + " position added with success");

                    } else {
                        System.out.println(response.getString("success"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    System.out.println(e);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        }) {


            @Override
            public byte[] getBody() {

                return requestBody.getBytes();
            }


        };

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjReq);
    }

    public void getHomeRequest(String urlJsonObj) {
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                urlJsonObj, (JSONObject) null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                JSONArray js = null;

                try {
                    js = response.getJSONArray("data");

                    home = new LatLng(js.getDouble(0), js.getDouble(1));

                    System.out.println(home);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("TAG", "Error: " + error.getMessage());
                System.out.println(error.getMessage());
            }
        });
        // Adding String request to request queue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjReq);

    }

    public boolean NeedToGoHomeDistance(LatLng current, LatLng home) {
        float[] distance = new float[2];


        Location.distanceBetween(current.latitude, current.longitude,
                home.latitude, home.longitude, distance);

        if (distance[0] > 500.0) {
            System.out.println("Current Position fel test " + current.latitude + " " + current.longitude + ">500.0");
            System.out.println("Home Position fel test " + home.latitude + home.longitude + " " + ">500.0");
            return true;
        } else {
            System.out.println("Current Position fel test " + current.latitude + " " + current.longitude + "<500.0");
            System.out.println("Home Position fel test " + home.latitude + home.longitude + " " + "<500.0");
            return false;
        }
    }

    public void createNotification() {
        // Prepare intent which is triggered if the
        // notification is selected
        Intent intent = new Intent(this, Main2Activity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
        //Bitmap icon = BitmapFactory.decodeResource(getApplication().getResources(), R.drawable.home_notif);
        // Build notification
        // Actions are just fake
        Notification.Builder noti = new Notification.Builder(this)
                .setContentTitle("You are far from home")
                .setContentText("Do you want to go home?")
                //.setLargeIcon(icon)
                .setSmallIcon(R.drawable.home_notif)
                .setContentIntent(pIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .addAction(R.drawable.home, "Take me home", pIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.build().flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, noti.build());

    }
}

