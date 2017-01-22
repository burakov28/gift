package com.ivan_pc.gift;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotifierReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = NotifierReceiver.class.getSimpleName();

    public NotifierReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "catch receiver");
        context.startService(new Intent(context, NotifierService.class));
    }
}
