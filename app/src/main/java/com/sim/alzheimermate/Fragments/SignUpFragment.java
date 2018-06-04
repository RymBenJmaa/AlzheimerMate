package com.sim.alzheimermate.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sim.alzheimermate.Activities.ResponsableActivity;
import com.sim.alzheimermate.R;
import com.sim.alzheimermate.Utils.AppSingleton;
import com.sim.alzheimermate.Utils.SharedData;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class SignUpFragment extends Fragment {
    private String url;
    private  EditText username,email,password,alzmail;
    private Button signup;
    private int test = 0;
    public static final String CONNECTIONS = "prefs_user";

    public static SignUpFragment newInstance() {
        SignUpFragment fragment = new SignUpFragment();
        Bundle args = new Bundle();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        boolean connected = checkInternetConnection(getContext());
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

            signup = view.findViewById(R.id.email_sign_up_button1);
            email =view.findViewById(R.id.emailS);
            password = view.findViewById(R.id.passwordS);
            username = view.findViewById(R.id.UsernameS);
            alzmail =  view.findViewById(R.id.alzmailS);
            signup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    email.setError(null);
                    password.setError(null);


                    url = SharedData.url + "register";
                    if(!email.getText().toString().contains("@")||email.getText().toString().equals("") ) {
                        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops... an error has occurred ")
                                .setContentText("Please enter a valid email address")
                                .show();
                        email.requestFocus();
                    }else if (!alzmail.getText().toString().contains("@")||alzmail.getText().toString().equals("") ){
                        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops... an error has occurred ")
                                .setContentText("Please enter a valid email address")
                                .show();
                        alzmail.requestFocus();
                    }else if(password.getText().toString().length()<6 || password.getText().toString().equals("") ) {
                        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops... an error has occurred ")
                                .setContentText("Please enter a valid password with more than 5 characters")
                                .show();
                        password.requestFocus();
                    }else if (username.getText().toString().equals("")) {
                        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops... an error has occurred ")
                                .setContentText("Please enter a valid username")
                                .show();
                        username.requestFocus();
                    }else {
                        makeJsonObjectRequest(url, username.getText().toString(),email.getText().toString(), password.getText().toString(),alzmail.getText().toString());
                    }
                }
            });
        }
    }
    public void makeJsonObjectRequest(String urlJsonObj, final String username, final String mail, final String pwd, final String emailAlz) {

        String REQUEST_TAG = "com.androidAlzheimerMate.loginRequest";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", username);
            jsonBody.put("email", mail);
            jsonBody.put("password", pwd);
            jsonBody.put("password_confirmation", pwd);
            jsonBody.put("alzmail", emailAlz);
            jsonBody.put("role", "assistant");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String requestBody = jsonBody.toString();
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                urlJsonObj, (JSONObject) null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    //System.out.println(response);
                    if (response.getString("success").equals("true") && !response.getString("token").equals("")) {
                        System.out.println("SignUp effectué dans la base de donnée");
                        SharedData.token = response.getString("token");
                        System.out.println(response.getString("token"));
                        SharedPreferences.Editor editor = getContext().getSharedPreferences(CONNECTIONS, Context.MODE_PRIVATE).edit();

                        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                        editor.putString("Username", String.valueOf(email.getText()));
                        editor.putString("Date", currentDateTimeString);
                        editor.apply();

                        startActivity(new Intent(getContext(), ResponsableActivity.class));

                          /*  }
                        }, 1000);*/
                    } else if (response.getString("success").equals("false")) {
                        Toast.makeText(getContext(), "Invalid Email/password"
                                , Toast.LENGTH_LONG).show();
                        System.out.println(response.getString("success"));
                        System.out.println(response.getString("error"));
                    } else {
                        boolean cancel = false;
                        View focusView = null;
                        // Check for a valid email address.
                        if (pwd.equals("")) {
                            password.setError(getString(R.string.error_field_required));
                            focusView = password;
                            cancel = true;
                        } else if (pwd.length()<6) {
                            password.setError(getString(R.string.error_invalid_password));
                            focusView = password;
                            cancel = true;
                        }
                        // Check for a valid email address.
                        if (mail.equals("")) {
                            email.setError(getString(R.string.error_field_required));
                            focusView = email;
                            cancel = true;
                        } else if (!(mail.contains("@"))) {
                            email.setError(getString(R.string.error_invalid_email));
                            focusView = email;
                            cancel = true;
                        }else if (!(emailAlz.contains("@"))) {
                            alzmail.setError(getString(R.string.error_invalid_email));
                            focusView = alzmail;
                            cancel = true;
                        }else  if (emailAlz.equals("")) {
                            alzmail.setError(getString(R.string.error_field_required));
                            focusView = alzmail;
                            cancel = true;
                        }

                        if (cancel) {
                            // There was an error; don't attempt login and focus the first
                            // form field with an error.
                            focusView.requestFocus();
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println( error);
            }
        }) {


            @Override
            public byte[] getBody() {

                return requestBody.getBytes();
            }


        };

        AppSingleton.getInstance(getContext()).addToRequestQueue(jsonObjReq, REQUEST_TAG);
        test = 1;


    }
}

