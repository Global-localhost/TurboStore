package com.phonemetra.turbo.store.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.phonemetra.turbo.store.net.WifiStateChangeService;

public class WifiStateChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        intent.setComponent(new ComponentName(context, WifiStateChangeService.class));
        context.startService(intent);
    }
}
