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
import android.widget.FrameLayout;
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


public class LoginFragment extends Fragment {

    public static final String CONNECTIONS = "prefs_user";
    private String url;
    private EditText mPasswordView;
    private EditText mEmailView;
    private int test = 0;
    private FrameLayout mLoginFormView;
    private Button signuplink, mEmailSignInButton;

    public LoginFragment() {

    }

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
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

        return inflater.inflate(R.layout.fragment_login, container, false);
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

            signuplink = view.findViewById(R.id.Signuplink);
            signuplink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.containerfrag, new SignUpFragment()).commit();
                }
            });
            mEmailView = view.findViewById(R.id.emailE);
            mLoginFormView = view.findViewById(R.id.login);
            mPasswordView = view.findViewById(R.id.passwordE);
            final SharedPreferences preferences = getContext().getSharedPreferences(CONNECTIONS, Context.MODE_PRIVATE);
            String userConnections = preferences.getString("Username", null);

            if (userConnections != null) {
                mEmailView.setText(userConnections);
                mPasswordView.requestFocus();

            }
            mEmailSignInButton = view.findViewById(R.id.email_sign_in_button1);
            mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mEmailView.setError(null);
                    mPasswordView.setError(null);
                    String email = mEmailView.getText().toString();
                    String password = mPasswordView.getText().toString();

                    url = SharedData.url + "login";// Simulate network access.
                    makeJsonObjectRequest(url, email, password);

                }
            });
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    public void makeJsonObjectRequest(String urlJsonObj, final String email, final String pwd) {

        String REQUEST_TAG = "com.androidAlzheimerMate.loginRequest";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", pwd);
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
                    if (response.getString("success").equals("true") && response.getJSONObject("data").getJSONObject("role").getString("role").equals("assistant")) {
                        System.out.println("login effectué dans la base de donnée");
                        SharedData.token = response.getJSONObject("data").getString("token");
                        System.out.println(response.getJSONObject("data").getString("token"));
                        System.out.println(response.getJSONObject("data").getJSONObject("role").getString("role"));
                        SharedPreferences.Editor editor = getContext().getSharedPreferences(CONNECTIONS, Context.MODE_PRIVATE).edit();

                        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                        editor.putString("Username", String.valueOf(mEmailView.getText()));
                        editor.putString("Date", currentDateTimeString);
                        editor.apply();

                        startActivity(new Intent(getContext(), ResponsableActivity.class));

                    } else if (response.getString("success").equals("false")) {
                        Toast.makeText(getContext(), "Invalid Email/password"
                                , Toast.LENGTH_LONG).show();
                        System.out.println(response.getString("success"));
                        View focusView = null;
                        focusView = mEmailView;
                    } else {
                        boolean cancel = false;
                        View focusView = null;
                        // Check for a valid email address.
                        if (pwd.equals("")) {
                            mPasswordView.setError(getString(R.string.error_field_required));
                            focusView = mPasswordView;
                            cancel = true;
                        } else if (!isPasswordValid(pwd)) {
                            mPasswordView.setError(getString(R.string.error_invalid_password));
                            focusView = mPasswordView;
                            cancel = true;
                        }
                        // Check for a valid email address.
                        if (email.equals("")) {
                            mEmailView.setError(getString(R.string.error_field_required));
                            focusView = mEmailView;
                            cancel = true;
                        } else if (!isEmailValid(email)) {
                            mEmailView.setError(getString(R.string.error_invalid_email));
                            focusView = mEmailView;
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
                Toast.makeText(getContext(), "Invalid Email/password"
                        , Toast.LENGTH_LONG).show();
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
