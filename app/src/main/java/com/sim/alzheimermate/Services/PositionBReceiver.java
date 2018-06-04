package com.sim.alzheimermate.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class PositionBReceiver extends BroadcastReceiver {

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
    public void onReceive(Context context, Intent intent) {
        System.out.println("brodcast active");
        if (checkInternetConnection(context)) {
            Intent background = new Intent(context, PositionService.class);
            context.startService(background);
        }
    }
}
