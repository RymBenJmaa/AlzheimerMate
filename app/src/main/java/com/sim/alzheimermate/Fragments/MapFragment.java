package com.sim.alzheimermate.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sim.alzheimermate.Activities.Main2Activity;
import com.sim.alzheimermate.Models.DirectionObject;
import com.sim.alzheimermate.Models.GsonRequest;
import com.sim.alzheimermate.Models.Helper;
import com.sim.alzheimermate.Models.LegsObject;
import com.sim.alzheimermate.Models.PolylineObject;
import com.sim.alzheimermate.Models.RouteObject;
import com.sim.alzheimermate.Models.StepsObject;
import com.sim.alzheimermate.Models.VolleySingleton;
import com.sim.alzheimermate.R;
import com.sim.alzheimermate.Utils.AppSingleton;
import com.sim.alzheimermate.Utils.SharedData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;


public class MapFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, OnMyLocationButtonClickListener,
        OnMyLocationClickListener {
    private static final String TAG = MapFragment.class.getSimpleName();
    private static final int PERMISSION_LOCATION_REQUEST_CODE = 100;
    public static String lat, lon;
    MapView mMapView;
    private LatLng home;
    private GoogleApiClient client;
    private Marker currentLocationMarker;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private List<LatLng> latLngList;
    private MarkerOptions yourLocationMarker;
    private String url;
    private int test = 0;
    private GoogleMap googleMap;
    private boolean connected;

    public static boolean checkInternetConnection(Context context) {
        try {
            ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable() && conMgr.getActiveNetworkInfo().isConnected())
                return true;
            else
                return false;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        FloatingActionButton fab = rootView.findViewById(R.id.fab_home);

        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.primaryDarkColor)));
        latLngList = new ArrayList<>();

        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        final LocationManager manager = (LocationManager) getActivity().getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            Toast.makeText(getContext(), "Please enable location", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
        connected = checkInternetConnection(getContext());
        if (!connected) {
            new SweetAlertDialog(getContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                    .setTitleText("Oops...")
                    .setContentText("You must be connected to retrieve data")
                    .setCustomImage(R.drawable.no_service)
                    .setConfirmText("Turn On!")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {

                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            sDialog.dismissWithAnimation();

                        }
                    })
                    .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                            getActivity().finish();
                        }
                    })
                    .show();
        } else {


            // needed to get the map to display immediately

            try {
                MapsInitializer.initialize(getActivity().getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
            getHomeRequest(SharedData.url + "getHome?email=" + SharedData.alzMail);
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap mMap) {
                    googleMap = mMap;
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
                        // For showing a move to my location button
                        googleMap.setMyLocationEnabled(true);
                        buildGoogleApiClient();
                        mMap.setMyLocationEnabled(true);

                        mMap.animateCamera(CameraUpdateFactory.zoomBy(6));
                        googleMap.clear();
                        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
                        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(client, builder.build());
                        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                            @Override
                            public void onResult(@NonNull LocationSettingsResult result) {
                                final Status status = result.getStatus();
                                switch (status.getStatusCode()) {
                                    case LocationSettingsStatusCodes.SUCCESS:
                                        Log.d(TAG, "Connection method has been called");
                                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
                                            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(client);
                                            assignLocationValues(mLastLocation);
                                            setDefaultMarkerOption(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                                            url = SharedData.url + "position";
                                            lat = Double.toString(mLastLocation.getLatitude());
                                            lon = Double.toString(mLastLocation.getLongitude());
                                            googleMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).title("Marker Title").snippet("Marker Description"));

                                            // For zooming automatically to the location of the marker
                                            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).zoom(12).build();
                                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                        } else {
                                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION_REQUEST_CODE);
                                        }
                                        break;
                                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                        break;
                                }
                                makeJsonObjectRequest(url, lat, lon);

                            }
                        });
                    } else {
                        showPermissionAlert();

                    }
                }
            });
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(home == (null)){
                        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops... an error has occurred ")
                                .setContentText("No home address filled yet")
                                .show();
                    }else {
                        if (latLngList.size() > 0) {
                            refreshMap(googleMap);
                            latLngList.clear();
                        }

                        latLngList.add(home);
                        Log.d(TAG, "Marker number " + latLngList.size());
                        googleMap.addMarker(yourLocationMarker);
                        googleMap.addMarker(new MarkerOptions().position(home));
                        LatLng defaultLocation = yourLocationMarker.getPosition();
                        LatLng destinationLocation = home;
                        //use Google Direction API to get the route between these Locations
                        String directionApiPath = Helper.getUrl(String.valueOf(defaultLocation.latitude), String.valueOf(defaultLocation.longitude),
                                String.valueOf(destinationLocation.latitude), String.valueOf(destinationLocation.longitude));
                        Log.d(TAG, "Path " + directionApiPath);
                        getDirectionFromDirectionApiServer(directionApiPath);
                    }
                }
            });
        }
        return rootView;
    }

    protected synchronized void buildGoogleApiClient() {
        client = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        client.connect();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(client, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.d(TAG, "Connection method has been called");
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(client);
                            assignLocationValues(mLastLocation);
                            setDefaultMarkerOption(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                            url = SharedData.url + "position";
                            lat = Double.toString(mLastLocation.getLatitude());
                            lon = Double.toString(mLastLocation.getLongitude());

                        } else {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION_REQUEST_CODE);
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
                makeJsonObjectRequest(url, lat, lon);

            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_LOCATION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    // permission was denied, show alert to explain permission
                    showPermissionAlert();
                } else {
                    //permission is granted now start a background service
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


                        Fragment frg = getActivity().getSupportFragmentManager().findFragmentById(R.id.mapfrag);
                        final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                        ft.detach(frg);
                        ft.attach(frg);
                        ft.commit();
                    }
                }
            }
        }
    }

    private void assignLocationValues(Location currentLocation) {
        if (currentLocation != null) {
            double latitudeValue = currentLocation.getLatitude();
            double longitudeValue = currentLocation.getLongitude();
            Log.d(TAG, "Latitude: " + latitudeValue + " Longitude: " + longitudeValue);
            markStartingLocationOnMap(googleMap, new LatLng(latitudeValue, longitudeValue));
            addCameraToMap(new LatLng(latitudeValue, longitudeValue));
        }
    }

    private void addCameraToMap(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(16)
                .build();
        if (googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            System.out.println("mMap null");
        }
    }

    public void showPermissionAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.permission_request_title);
        builder.setMessage(R.string.app_permission_notice);
        builder.create();
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION_REQUEST_CODE);

                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getContext(), R.string.permission_refused, Toast.LENGTH_LONG).show();
            }
        });

        builder.show();
    }

    private void markStartingLocationOnMap(GoogleMap mapObject, LatLng location) {

        if (mapObject != null && location != null) {

            mapObject.addMarker(new MarkerOptions().position(location).title("Current location"));
            mapObject.moveCamera(CameraUpdateFactory.newLatLng(location));
        }
    }

    private void refreshMap(GoogleMap mapInstance) {
        mapInstance.clear();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showPermissionAlert();
            return;
        }
        googleMap.setMyLocationEnabled(true);
        buildGoogleApiClient();

        googleMap.animateCamera(CameraUpdateFactory.zoomBy(6));
        googleMap.clear();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(client, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.d(TAG, "Connection method has been called");
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(client);
                            assignLocationValues(mLastLocation);
                            setDefaultMarkerOption(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));


                            googleMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).title("Marker Title").snippet("Marker Description"));

                            // For zooming automatically to the location of the marker
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).zoom(12).build();
                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        } else {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION_REQUEST_CODE);
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    private void setDefaultMarkerOption(LatLng location) {
        if (yourLocationMarker == null) {
            yourLocationMarker = new MarkerOptions();
        }
        yourLocationMarker.position(location);
    }

    private void getDirectionFromDirectionApiServer(String url) {
        GsonRequest<DirectionObject> serverRequest = new GsonRequest<DirectionObject>(
                Request.Method.GET,
                url,
                DirectionObject.class,
                createRequestSuccessListener(),
                createRequestErrorListener());
        serverRequest.setRetryPolicy(new DefaultRetryPolicy(
                Helper.MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(getContext()).addToRequestQueue(serverRequest);
    }

    private Response.Listener<DirectionObject> createRequestSuccessListener() {
        return new Response.Listener<DirectionObject>() {
            @Override
            public void onResponse(DirectionObject response) {
                try {
                    Log.d("JSON Response", response.toString());
                    if (response.getStatus().equals("OK")) {
                        List<LatLng> mDirections = getDirectionPolylines(response.getRoutes());
                        drawRouteOnMap(googleMap, mDirections);
                    } else {
                        Toast.makeText(getContext(), R.string.server_error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ;
        };
    }

    private List<LatLng> getDirectionPolylines(List<RouteObject> routes) {
        List<LatLng> directionList = new ArrayList<LatLng>();
        for (RouteObject route : routes) {
            List<LegsObject> legs = route.getLegs();
            for (LegsObject leg : legs) {
                List<StepsObject> steps = leg.getSteps();
                for (StepsObject step : steps) {
                    PolylineObject polyline = step.getPolyline();
                    String points = polyline.getPoints();
                    List<LatLng> singlePolyline = decodePoly(points);
                    for (LatLng direction : singlePolyline) {
                        directionList.add(direction);
                    }
                }
            }
        }
        return directionList;
    }

    private Response.ErrorListener createRequestErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        };
    }

    private void drawRouteOnMap(GoogleMap map, List<LatLng> positions) {
        PolylineOptions options = new PolylineOptions().width(10).color(R.color.colorPrimary).geodesic(true);
        options.addAll(positions);
        Polyline polyline = map.addPolyline(options);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(positions.get(1).latitude, positions.get(1).longitude))
                .zoom(17)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /**
     * Method to decode polyline points
     * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    @Override
    public void onLocationChanged(Location location) {
       Location lastLocation = location;
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
            yourLocationMarker.visible(false);
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(340.0F));
        currentLocationMarker = googleMap.addMarker(markerOptions);

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomBy(8));

        if (client != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, (com.google.android.gms.location.LocationListener) this);
        }
    }

    public void getHomeRequest(String urlJsonObj) {

        String REQUEST_TAG = "com.androidAlzheimerMate.gethome";
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
        AppSingleton.getInstance(getContext()).addToRequestQueue(jsonObjReq, REQUEST_TAG);

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public void makeJsonObjectRequest(String urlJsonObj, final String la, final String lo) {

        String REQUEST_TAG = "com.androidAlzheimerMate.positionnnRequest";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("latitude", la);
            jsonBody.put("longitude", lo);
            jsonBody.put("email", SharedData.alzMail);
        } catch (JSONException e) {
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
                        System.out.println("position ajoutée dans la base de donnée");

                    }
                } catch (JSONException e) {
                    System.out.println(e);
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {


            @Override
            public byte[] getBody() {

                return requestBody.getBytes();
            }


        };

        if (test == 0) {
            AppSingleton.getInstance(getContext()).addToRequestQueue(jsonObjReq, REQUEST_TAG);
            test = 1;
        }


    }

    @Override
    public void onResume() {

        connected = checkInternetConnection(getContext());
        super.onResume();
        if (connected) {
            if (mMapView == null) {
                startActivity(new Intent(getContext(), Main2Activity.class));
            } else {
                mMapView.onResume();
                if (googleMap != null) {
                    googleMap.clear();
                    startActivity(new Intent(getContext(), Main2Activity.class));
                }
            }

        }
    }

    @Override
    public void onPause() {
        connected = checkInternetConnection(getContext());
        super.onPause();
        if (connected) {
            mMapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        connected = checkInternetConnection(getContext());
        super.onDestroy();
        if (connected) {
            mMapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        connected = checkInternetConnection(getContext());
        if (connected) {
            mMapView.onLowMemory();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}
