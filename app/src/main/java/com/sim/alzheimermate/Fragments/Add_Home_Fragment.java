package com.sim.alzheimermate.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sim.alzheimermate.R;
import com.sim.alzheimermate.Utils.AppSingleton;
import com.sim.alzheimermate.Utils.SharedData;
import com.sim.alzheimermate.controllers.PlaceAutocompleteAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class Add_Home_Fragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, GoogleMap.OnMarkerClickListener {
    private static final int PERMISSION_LOCATION_REQUEST_CODE = 100;
    private static final String TAG = Add_Home_Fragment.class.getSimpleName();
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));
    public static String lat, lon;
    private final long DELAY = 1000;
    MapView mMapView;
    private List<LatLng> latLngList;
    private GoogleMap googleMap;
    private GoogleApiClient client;
    private double latitudeValue = 0.0;
    private double longitudeValue = 0.0;
    private MarkerOptions yourLocationMarker;
    private Location mLastLocation;
    private Marker currentLocationMarker;
    private Location lastLocation;
    private LocationRequest mLocationRequest;
    private AutoCompleteTextView mSearchText;
    private Timer timer = new Timer();
    private int test = 0;
    private String url;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private boolean connected;

    public Add_Home_Fragment() {
    }

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    private void geoLocate() {
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(getActivity());
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage());
        }

        if (list.size() > 0) {
            final Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            Toast.makeText(getContext(), address.getFeatureName(), Toast.LENGTH_LONG).show();
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));
            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {

                    new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Are you sure?")
                            .setContentText("This is the address of the home")
                            .setConfirmText("Yes,it is!")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    // sDialog.dismissWithAnimation();
                                    new Thread(new Runnable() {
                                        public void run() {
                                            url = SharedData.url + "insertHome";
                                            lat = Double.toString(address.getLatitude());
                                            lon = Double.toString(address.getLongitude());
                                            System.out.println(address.toString() + " geolocate");
                                            makeJsonObjectRequest(url, lat, lon);
                                        }

                                    }).start();
                                    sDialog.setConfirmClickListener(null)

                                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);

                                    sDialog.dismissWithAnimation();

                                }

                            })
                            .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                }
                            })
                            .show();
                    return true;
                }
            });
            // System.out.println(address.getLatitude()+","+address.getLongitude());
        }
    }

    private void moveCamera(final LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!title.equals("My Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            googleMap.addMarker(options);

        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add__home, container, false);
        mMapView = rootView.findViewById(R.id.mapViewAdmin);
        mMapView.onCreate(savedInstanceState);
        mSearchText = rootView.findViewById(R.id.input_search1);
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
                            sDialog.dismissWithAnimation();
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
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
            latLngList = new ArrayList<>();

            // needed to get the map to display immediately

            try {
                MapsInitializer.initialize(getActivity().getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
            mGoogleApiClient = new GoogleApiClient
                    .Builder(getContext())
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .enableAutoManage(getActivity(), this)
                    .build();

            mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(getContext(), mGoogleApiClient,
                    LAT_LNG_BOUNDS, null);

            mSearchText.setAdapter(mPlaceAutocompleteAdapter);
            mSearchText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                }

                @Override
                public void onTextChanged(final CharSequence s, int start, int before,
                                          int count) {
                    if (timer != null)
                        timer.cancel();
                }

                @Override
                public void afterTextChanged(final Editable s) {
                    //avoid triggering event when text is too short
                    if (s.length() >= 3) {

                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                // TODO: do what you need here (refresh list)
                                // you will probably need to use
                                // runOnUiThread(Runnable action) for some specific
                                // actions
                                System.out.println("typing");
                                //execute our method for searching
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        geoLocate();
                                    }
                                });

                            }

                        }, DELAY);
                    }
                }
            });

            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap mMap) {
                    googleMap = mMap;
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
                        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                            @Override
                            public void onMapClick(LatLng latLng) {
                                // Creating a marker
                                MarkerOptions markerOptions = new MarkerOptions();

                                // Setting the position for the marker
                                markerOptions.position(latLng);

                                // Setting the title for the marker.
                                // This will be displayed on taping the marker
                                markerOptions.title(latLng.latitude + " : " + latLng.longitude);

                                // Clears the previously touched position
                                googleMap.clear();

                                // Animating to the touched position
                                googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                                // Placing a marker on the touched position
                                googleMap.addMarker(markerOptions);
                                System.out.println(latLng.latitude + " , " + latLng.longitude);

                            }
                        });
                    } else {
                        showPermissionAlert();
                        //  refreshMap(googleMap);
                    }

                }
            });
        }
        return rootView;
    }

    private void markStartingLocationOnMap(GoogleMap mapObject, final LatLng location) {

        if (mapObject != null && location != null) {

            mapObject.addMarker(new MarkerOptions().position(location).title("Current location"));
            mapObject.moveCamera(CameraUpdateFactory.newLatLng(location));

        }

    }

    private void refreshMap(GoogleMap mapInstance) {
        mapInstance.clear();

    }

    private void showPermissionAlert() {
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

    @Override
    public void onPause() {
        super.onPause();

        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onLocationChanged(final Location location) {
        lastLocation = location;
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
                        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(client);
                        assignLocationValues(mLastLocation);
                        setDefaultMarkerOption(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                    }
                }
            }
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    private void addCameraToMap(final LatLng latLng) {
        final CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(16)
                .build();
        if (googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {


                @Override
                public boolean onMarkerClick(Marker marker) {

                    new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Are you sure?")
                            .setContentText("Is this the home address?")
                            .setConfirmText("Yes,it is!")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    //sDialog.dismissWithAnimation();
                                    new Thread(new Runnable() {
                                        public void run() {
                                            url = SharedData.url + "insertHome";
                                            lat = Double.toString(latLng.latitude);
                                            lon = Double.toString(latLng.longitude);
                                            System.out.println(latLng.toString() + " addCameraToMap");
                                            makeJsonObjectRequest(url, lat, lon);
                                        }

                                    }).start();
                                    sDialog.setConfirmClickListener(null)
                                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);


                                    sDialog.dismissWithAnimation();

                                }

                            })
                            .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                }
                            })

                            .show();
                    return true;
                }
            });
        } else {
            System.out.println("mMap null");
        }
    }

    private void setDefaultMarkerOption(LatLng location) {
        if (yourLocationMarker == null) {
            yourLocationMarker = new MarkerOptions();
        }
        yourLocationMarker.position(location);

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

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
                        System.out.println("Connection method has been called");
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(client);
                            assignLocationValues(mLastLocation);
                            setDefaultMarkerOption(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

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

    private void assignLocationValues(Location currentLocation) {
        if (currentLocation != null) {
            latitudeValue = currentLocation.getLatitude();
            longitudeValue = currentLocation.getLongitude();
            Log.d(TAG, "Latitude: " + latitudeValue + " Longitude: " + longitudeValue);
            markStartingLocationOnMap(googleMap, new LatLng(latitudeValue, longitudeValue));
            addCameraToMap(new LatLng(latitudeValue, longitudeValue));
        }
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
    public void onConnectionSuspended(int i) {

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
    public void onMapReady(GoogleMap mMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // For showing a move to my location button
            googleMap.setMyLocationEnabled(true);
            buildGoogleApiClient();
            googleMap.setMyLocationEnabled(true);
            googleMap.animateCamera(CameraUpdateFactory.zoomBy(6));
            mMap.clear();

        }
    }

    public void makeJsonObjectRequest(String urlJsonObj, final String la, final String lo) {
        String REQUEST_TAG = "com.androidAlzheimerMate.HomeeRequest";
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
                        System.out.println("adresse ajoutée dans la base de donnée");
                        Toast.makeText(getContext(), "Address added with success", Toast.LENGTH_LONG).show();
                    }else
                    {
                        System.out.println(response.getString("error").toString() );

                    }
                } catch (JSONException e) {
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
        AppSingleton.getInstance(getContext()).addToRequestQueue(jsonObjReq, REQUEST_TAG);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        System.out.println("marker clicked");
        return false;
    }
}
