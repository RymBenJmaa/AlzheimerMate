package com.sim.alzheimermate.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;
import com.sim.alzheimermate.Fragments.Add_Home_Fragment;
import com.sim.alzheimermate.Fragments.Add_Med_Fragment;
import com.sim.alzheimermate.Fragments.Add_Person_Fragment;
import com.sim.alzheimermate.R;
import com.sim.alzheimermate.Utils.AppSingleton;
import com.sim.alzheimermate.Utils.SharedData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ResponsableActivity extends AppCompatActivity {

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connected = checkInternetConnection(getApplicationContext());
        if (!connected) {
            new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
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
                            finish();
                        }
                    })
                    .show();
        } else {
            setContentView(R.layout.activity_responsable);
            BottomBar bottomBar = findViewById(R.id.bottomBar2);
            bottomBar.setDefaultTab(R.id.tab_add_meds);
            bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
                @Override
                public void onTabSelected(@IdRes int tabId) {
                    switch (tabId) {
                        case R.id.tab_add_proches:
                            getSupportFragmentManager().beginTransaction().replace(R.id.container_responsable, new Add_Person_Fragment()).commit();
                            break;
                        case R.id.tab_add_meds:
                            getSupportFragmentManager().beginTransaction().replace(R.id.container_responsable, new Add_Med_Fragment()).commit();
                            break;
                        case R.id.tab_add_home:
                            getSupportFragmentManager().beginTransaction().replace(R.id.container_responsable, new Add_Home_Fragment()).commit();
                            break;
                    }
                }
            });

            bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
                @Override
                public void onTabReSelected(@IdRes int tabId) {

                }
            });

            FloatingActionButton fab = findViewById(R.id.fab1);
            fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.primaryDarkColor)));
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = SharedData.url + "disconnect?token=" + SharedData.token;// Simulate network access.
                    makeJsonObjectRequest(url);
                    SharedData.token = null;
                    startActivity(new Intent(getBaseContext(), Main2Activity.class));

                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_responsable, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            String url = SharedData.url + "logout?token=" + SharedData.token;// Simulate network access.
            makeJsonObjectRequest(url);
            SharedData.token = null;
            startActivity(new Intent(getBaseContext(), Main2Activity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void makeJsonObjectRequest(String urlJsonObj) {
        String REQUEST_TAG = "com.androidAlzheimerMate.logoutRequest";
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                urlJsonObj, (JSONObject) null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getString("success").equals("true")) {
                        System.out.println("Logout réalisé avec succés ");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        // Adding request to request queue
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjReq, REQUEST_TAG);
    }

    //we need the outState to save the position
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //tabLayout.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, Main2Activity.class));
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {

            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}