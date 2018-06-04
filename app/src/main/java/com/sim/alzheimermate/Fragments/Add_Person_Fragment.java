package com.sim.alzheimermate.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

/**
 * A simple {@link Fragment} subclass.
 */
public class Add_Person_Fragment extends Fragment {
    //  private String mParam2;
    public static final int REQUEST_CODE_CAMERA = 0012;
    public static final int REQUEST_CODE_GALLERY = 0013;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    EditText lastname, lien, firstname, num, email;
    Button photo;
    ImageView im;
    String ba1;
    Bitmap bitmap1;
    byte[] f;
    //private static final String ARG_PARAM2 = "param2";
    private int test = 0;
    private String url;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String[] items = {"Camera", "Gallery"};
    private File imgfile;
    private String selectedimgname;
    private String tvPath;
    private Target loadtarget;
    private boolean connected;

    public Add_Person_Fragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static Add_Person_Fragment newInstance(String param1, String param2) {
        Add_Person_Fragment fragment = new Add_Person_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);

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

        //checking the permission
        //if the permission is not given we will open setting to add permission
        //else app will not open
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getActivity().getPackageName()));
            getActivity().finish();
            startActivity(intent);
            return;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add__person, container, false);
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

            lastname = view.findViewById(R.id.nom_person);
            lien = view.findViewById(R.id.lien);
            firstname = view.findViewById(R.id.prenom_per);
            photo = view.findViewById(R.id.picture);
            num = view.findViewById(R.id.num_tel);
            email = view.findViewById(R.id.email_per);
            im = view.findViewById(R.id.im_test);
            // EditText nbr_p = view.findViewById(R.id.nb_prises);
            photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    openImage();
                }
            });
            Button saveBtn = view.findViewById(R.id.savebtn);
            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (imgfile == null && !lastname.getText().toString().equals("") && !firstname.getText().toString().equals("") && !num.getText().toString().equals("") && !email.getText().toString().equals("")&& !lien.getText().toString().equals("")) {
                        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops... an error has occurred ")
                                .setContentText("Please select a picture")
                                .show();
                    } else if (imgfile != null && lastname.getText().toString().equals("") && !firstname.getText().toString().equals("") && !num.getText().toString().equals("") && !email.getText().toString().equals("")&& !lien.getText().toString().equals("")) {
                        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops... an error has occurred ")
                                .setContentText("Please insert a last name")
                                .show();
                    }else   if (imgfile != null && !lastname.getText().toString().equals("") && firstname.getText().toString().equals("") && !num.getText().toString().equals("") && !email.getText().toString().equals("")&& !lien.getText().toString().equals("")) {
                        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops... an error has occurred ")
                                .setContentText("Please insert a first name")
                                .show();
                    }else   if (imgfile != null && !lastname.getText().toString().equals("") && !firstname.getText().toString().equals("") && num.getText().toString().equals("") && !email.getText().toString().equals("")&& !lien.getText().toString().equals("")) {
                        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops... an error has occurred ")
                                .setContentText("Please insert a correct phone number")
                                .show();
                    }else   if (imgfile != null && !lastname.getText().toString().equals("") && !firstname.getText().toString().equals("") && !num.getText().toString().equals("") && (email.getText().toString().equals("") || !email.getText().toString().contains("@"))&& !lien.getText().toString().equals("")) {
                        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops... an error has occurred ")
                                .setContentText("Please insert a correct email address")
                                .show();

                    }
                    else if(imgfile != null && !lastname.getText().toString().equals("") && !firstname.getText().toString().equals("") && !num.getText().toString().equals("") && !email.getText().toString().equals("")&& lien.getText().toString().equals("")){
                        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops... an error has occurred ")
                                .setContentText("Please insert a kinship")
                                .show();
                    }else if(imgfile != null && !lastname.getText().toString().equals("") && !firstname.getText().toString().equals("") && !num.getText().toString().equals("") && !email.getText().toString().equals("")&& !lien.getText().toString().equals("")) {
                        new Thread(new Runnable() {
                            public void run() {

                                    Bitmap bm = BitmapFactory.decodeFile(imgfile.getAbsolutePath());
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    bm.compress(Bitmap.CompressFormat.JPEG, 50, baos); //bm is the bitmap object
                                    f = baos.toByteArray();
                                    uploadBitmap(f);
                                }
                           // }
                        }).start();
                    }
                    else {
                        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops... an error has occurred ")
                                .setContentText("Please fill all the fields")
                                .show();
                    }
                    }



            });
        }
    }

    private void uploadBitmap(final byte[] b) {


        final String ln = lastname.getText().toString().trim();
        final String fn = firstname.getText().toString().trim();
        final String liens = lien.getText().toString().trim();
        final String emails = email.getText().toString().trim();
        final String numtels = num.getText().toString().trim();


        //our custom volley request
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, EndPoints.UPLOAD_URL_Per,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {

                            JSONObject obj = new JSONObject(new String(response.data));
                            if (obj.getString("success").equals("true")) {
                                lastname.setText("");
                                firstname.setText("");
                                lien.setText("");
                                email.setText("");
                                num.setText("");
                                Picasso.with(getContext()).load(R.drawable.imgholder).transform(new CircleTransform()).into(im);

                                new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Good job!")
                                        .setContentText("Person added with success!")
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

                params.put("nom", ln);
                params.put("prenom", fn);
                params.put("lien", liens);
                params.put("email", emails);
                params.put("num_tel", numtels);
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

    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void openImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Options");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Camera")) {
                    EasyImage.openCamera(Add_Person_Fragment.this, REQUEST_CODE_CAMERA);
                } else if (items[i].equals("Gallery")) {
                    EasyImage.openGallery(Add_Person_Fragment.this, REQUEST_CODE_GALLERY);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public void loadBitmap(String url) {

        if (loadtarget == null) loadtarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                // do something with the Bitmap
                handleLoadedBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }

        };

        Picasso.with(getContext()).load(url).transform(new CircleTransform()).into(loadtarget);
    }

    public void handleLoadedBitmap(Bitmap b) {
        // do something here
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
                        Snackbar.make(getView(), "person added with success", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        lastname.setText("");
                        firstname.setText("");
                        lien.setText("");
                        email.setText("");
                        num.setText("");
                        Picasso.with(getContext()).load(R.drawable.imgholder).transform(new CircleTransform()).into(im);
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
