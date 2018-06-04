package com.sim.alzheimermate.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.ybq.android.spinkit.style.WanderingCubes;
import com.sim.alzheimermate.Activities.Main2Activity;
import com.sim.alzheimermate.Models.Medicament;
import com.sim.alzheimermate.R;
import com.sim.alzheimermate.Utils.AppSingleton;
import com.sim.alzheimermate.Utils.SharedData;
import com.sim.alzheimermate.controllers.MedAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MedFragment extends Fragment {

    ProgressBar progressBar;
    List<Medicament> medss;
    String url1;
    WaveSwipeRefreshLayout mWaveSwipeRefreshLayout;
    private OnFragmentInteractionListener mListener;
    private GridView gridView;
    private MedAdapter gridAdapter;
    private Boolean connected;

    public MedFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static MedFragment newInstance() {
        MedFragment fragment = new MedFragment();
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
            url1 = SharedData.url + "getmed?malade=" + SharedData.alzMail;
            makeJsonObjectRequest(url1);
            if (medss == null) {
                medss = new ArrayList<>();
            }
            mWaveSwipeRefreshLayout = view.findViewById(R.id.main_swipe_med);
            mWaveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);
            mWaveSwipeRefreshLayout.setWaveColor(Color.argb(255, 86, 0, 39));

            mWaveSwipeRefreshLayout.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    // Do work to refresh the list here.(red: 0.34, green: 0.00, blue: 0.15, alpha: 1.0)
                    new Task().execute();
                }
            });
            gridView = view.findViewById(R.id.gridView_med);
            progressBar = view.findViewById(R.id.spin_kit_dp1);
            WanderingCubes doubleBounce = new WanderingCubes();
            progressBar.setIndeterminateDrawable(doubleBounce);
            gridView.setVisibility(View.INVISIBLE);


        }

    }

    public void makeJsonObjectRequest(String urlJsonObj) {

        String REQUEST_TAG = "com.androidAlzheimerMate.medRequest";
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                urlJsonObj, (JSONObject) null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                JSONArray js = null;
                JSONArray js11 = null;
                try {
                    js = response.getJSONArray("data");
                    js11 = response.getJSONArray("photos");
                    medss = new ArrayList<>();
                    for (int i = 0; i < js.length(); i++) {
                        // JSONObject item = js.getJSONObject(i);
                        Medicament med = new Medicament();
                        med.setNom(js.getJSONObject(i).getString("nom"));
                        med.setNbPrises(js.getJSONObject(i).getInt("nbr_prises"));
                        med.setHeures_prises(js.getJSONObject(i).getString("heure_prise"));

                        med.setImage_med(js11.getString(i));

                        medss.add(med);
                        System.out.println("Liste médicaments" + med.toString());

                    }

                    gridAdapter = new MedAdapter(getContext(), R.layout.item, medss);

                    if (medss.isEmpty()) {
                        new SweetAlertDialog((getContext()), SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Oops...")
                                .setContentText("No drugs to show yet!")
                                .setConfirmText("OK!")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                        startActivity(new Intent(getContext(), Main2Activity.class));

                                    }
                                })
                                .show();
                    }

                    gridView.setAdapter(gridAdapter);
                    gridView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);


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
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.items_list, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private class Task extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... voids) {
            return new String[0];
        }

        @Override
        protected void onPostExecute(String[] result) {
           /* url1 = SharedData.url + "getper?malade=" + SharedData.alzMail;
            makeJsonObjectRequest(url1);*/
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    url1 = SharedData.url + "getmed?malade=" + SharedData.alzMail;
                    makeJsonObjectRequest(url1);
                    if (medss == null) {
                        medss = new ArrayList<>();
                    }
                    mWaveSwipeRefreshLayout.setRefreshing(false);
                }
            }, 1500);
            super.onPostExecute(result);
        }
    }
}
