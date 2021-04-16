package br.com.kamarugosan.songlist.ui.activity.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;

public class MainBroadcastReceiver extends BroadcastReceiver {
    /**
     * Action defining the song list should be loaded.
     */
    public static String ACTION_LOAD_LIST = "br.com.kamarugo.songlist.main.LOAD_LIST";

    private final MainActivity activity;

    public MainBroadcastReceiver(MainActivity activity) {
        this.activity = activity;
    }

    @NonNull
    public static IntentFilter getIntentFilter() {
        return new IntentFilter(ACTION_LOAD_LIST);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (ACTION_LOAD_LIST.equals(intent.getAction())) {
                activity.loadList();
            }
        }
    }
}
