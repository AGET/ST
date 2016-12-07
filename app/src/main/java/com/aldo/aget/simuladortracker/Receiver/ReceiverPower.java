package com.aldo.aget.simuladortracker.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.aldo.aget.simuladortracker.MainActivity;

public class ReceiverPower extends BroadcastReceiver {
    public ReceiverPower() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Intent intentForMain = new Intent(context, MainActivity.class);
        intentForMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intentForMain);
    }
}
