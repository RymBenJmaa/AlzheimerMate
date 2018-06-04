package com.sim.alzheimermate.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.sim.alzheimermate.Models.CircleTransform;
import com.sim.alzheimermate.Models.EndPoints;
import com.sim.alzheimermate.Models.VolleyMultipartRequest;
import com.sim.alzheimermate.R;
import com.sim.alzheimermate.Utils.AppSingleton;
import com.sim.alzheimermate.Utils.SharedData;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

/**
 * A simple {@link Fragment} subclass.
 */
public class Add_Med_Fragment extends Fragment {
    public static final int REQUEST_CODE_CAMERA = 0012;
    public static final int REQUEST_CODE_GALLERY = 0013;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final int PERMISSION_LOCATION_REQUEST_CODE = 100;
    EditText nameMed, heure, nbr_p;
    ImageView im;
    Button photo;
    String ba1;
    byte[] f;
    //  private static final String ARG_PARAM2 = "param2";
    private int test = 0;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String givenDateString;
    private String url;
    private String[] items = {"Camera", "Gallery"};
    private File imgfile;
    private String selectedimgname;
    private String tvPath;
    private boolean connected;

    public Add_Med_Fragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static Add_Med_Fragment newInstance(String param1, String param2) {
        Add_Med_Fragment fragment = new Add_Med_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            showPermissionAlert();
        }
    }

    private void showPermissionAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.permission_request_title);
        builder.setMessage(R.string.app_permission_notice);
        builder.create();
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_LOCATION_REQUEST_CODE);

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Oh Sorry!")
                        .setContentText("In order to proceed you have to grant the permission")
                        .setConfirmText("Yes, open permissions")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:" + getActivity().getPackageName()));
                                getActivity().finish();
                                startActivity(intent);
                            }
                        })
                        .show();
     }
        });
        builder.show();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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

            nameMed = view.findViewById(R.id.nom_med);
            heure = view.findViewById(R.id.heure);
            nbr_p = view.findViewById(R.id.nb_prises);
            photo = view.findViewById(R.id.med_img);
            im = view.findViewById(R.id.im_med);
            photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openImage();
                }
            });
            Button saveBtn = view.findViewById(R.id.save);
            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (nameMed.getText().toString().equals("") && !heure.getText().toString().equals("") && !nbr_p.getText().toString().equals("") && imgfile != null) {
                        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops... an error has occurred ")
                                .setContentText("Please enter the name of the drug")
                                .show();
                    } else if (!nameMed.getText().toString().equals("") && heure.getText().toString().equals("") && !nbr_p.getText().toString().equals("") && imgfile != null) {
                        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops... an error has occurred ")
                                .setContentText("Please select the number of hours")
                                .show();
                    } else if (!nameMed.getText().toString().equals("") && !heure.getText().toString().equals("") && nbr_p.getText().toString().equals("") && imgfile != null) {
                        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops... an error has occurred ")
                                .setContentText("Please enter the number of takes")
                                .show();
                    } else if (!nameMed.getText().toString().equals("") && !heure.getText().toString().equals("") && !nbr_p.getText().toString().equals("") && imgfile == null) {
                        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops... an error has occurred ")
                                .setContentText("Please select a picture")
                                .show();
                    } else if (!nameMed.getText().toString().equals("") && !heure.getText().toString().equals("") && !nbr_p.getText().toString().equals("") && imgfile != null) {
                        new Thread(new Runnable() {
                            public void run() {
                                if (imgfile != null) {
                                    Bitmap bm = BitmapFactory.decodeFile(imgfile.getAbsolutePath());
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    bm.compress(Bitmap.CompressFormat.JPEG, 50, baos); //bm is the bitmap object
                                    f = baos.toByteArray();
                                    uploadBitmap(f);
                                }
                            }
                        }).start();

                    } else {
                        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops... an error has occurred ")
                                .setContentText("Please fill all the fields")
                                .show();
                    }
                }
            });

            heure.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    // TODO Auto-generated method stub
                    Calendar mcurrentTime = Calendar.getInstance();
                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = mcurrentTime.get(Calendar.MINUTE);
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            if (String.valueOf(selectedMinute).length() == 1) {
                                String x = "0" + String.valueOf(selectedMinute);
                                if (String.valueOf(selectedHour).length() == 1) {
                                    String y = "0" + String.valueOf(selectedHour);
                                    heure.setText(y + ":" + x);
                                    givenDateString = y + ":" + x + ":00";
                                } else {
                                    heure.setText(selectedHour + ":" + x);
                                    givenDateString = selectedHour + ":" + x + ":00";
                                }

                            } else if (String.valueOf(selectedHour).length() == 1) {
                                String y = "0" + String.valueOf(selectedHour);
                                if (String.valueOf(selectedMinute).length() == 1) {
                                    String x = "0" + String.valueOf(selectedMinute);
                                    heure.setText(y + ":" + x);
                                    givenDateString = y + ":" + x + ":00";
                                } else {
                                    heure.setText(y + ":" + selectedMinute);
                                    givenDateString = y + ":" + selectedMinute + ":00";
                                }
                            } else {
                                heure.setText(selectedHour + ":" + selectedMinute);
                                givenDateString = selectedHour + ":" + selectedMinute + ":00";
                            }


                        }
                    }, hour, minute, true);//Yes 24 hour time
                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();

                    givenDateString = heure.getText().toString();

                }
            });
        }
    }

    @Override
    public void onResume() {

        super.onResume();

    }

    private void uploadBitmap(final byte[] b) {


        final String n = nameMed.getText().toString().trim();
        final String h = heure.getText().toString().trim();
        final String nb = nbr_p.getText().toString().trim();
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, EndPoints.UPLOAD_URL_Med,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {

                            JSONObject obj = new JSONObject(new String(response.data));
                            if (obj.getString("success").equals("true")) {
                                nameMed.setText("");
                                heure.setText("");
                                nbr_p.setText("");

                                Picasso.with(getContext()).load(R.drawable.pills).transform(new CircleTransform()).into(im);
                                new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Good job!")
                                        .setContentText("Drug  added with success!")
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {

            /*
            * If you want to add more parameters with the image
            * you can do it here
            * here we have only one parameter with the image
            * which is tags
            * */
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("nom", n);
                params.put("heure_prise", h);
                params.put("nbr_prises", nb);
                params.put("malade", SharedData.alzMail);


                return params;
            }

            /*
            * Here we are passing image by renaming it with a unique name
            * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("photo", new DataPart(imagename + ".png", b));
                return params;
            }
        };
        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(1000 * 60, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //adding the request to volley
        Volley.newRequestQueue(getContext()).add(volleyMultipartRequest);
    }

    private void openImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Options");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Camera")) {
                    EasyImage.openCamera(Add_Med_Fragment.this, REQUEST_CODE_CAMERA);
                } else if (items[i].equals("Gallery")) {
                    EasyImage.openGallery(Add_Med_Fragment.this, REQUEST_CODE_GALLERY);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EasyImage.handleActivityResult(requestCode, resultCode, data, getActivity(), new DefaultCallback() {
            @Override
            public void onImagesPicked(@NonNull List<File> imageFiles, EasyImage.ImageSource source, int type) {

                switch (type) {
                    case REQUEST_CODE_CAMERA:
                        imgfile = imageFiles.get(0);
                        tvPath = imageFiles.get(0).getAbsolutePath();
                        selectedimgname = imageFiles.get(0).getName();
                        Picasso.with(getContext()).load(imgfile).transform(new CircleTransform()).into(im);
                        break;
                    case REQUEST_CODE_GALLERY:
                        imgfile = imageFiles.get(0);
                        selectedimgname = imageFiles.get(0).getName();
                        tvPath = imageFiles.get(0).getAbsolutePath();
                        Picasso.with(getContext()).load(imgfile).transform(new CircleTransform()).into(im);
                        break;
                }
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_med, container, false);
    }

    public void makeJsonObjectRequest(String urlJsonObj) {
        String REQUEST_TAG = "com.androidtravelplanner.saveagnceeRequest";
        JSONObject params = new JSONObject();
        try {
            params.put("file", ba1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                urlJsonObj, params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getString("success").equals("true")) {

                        nameMed.setText("");
                        heure.setText("");
                        nbr_p.setText("");
                        Picasso.with(getContext()).load(R.drawable.pills).transform(new CircleTransform()).into(im);

                        new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Good job!")
                                .setContentText("Drug  added with success!")
                                .show();
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
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }


        };
        if (test == 0) {
            AppSingleton.getInstance(getContext()).addToRequestQueue(jsonObjReq, REQUEST_TAG);
            test = 1;
        }
    }


}
