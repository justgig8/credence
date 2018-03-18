package credence.oorja.com.androidsdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by gaurav on 01/11/17.
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "NBR";

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        Log.d(TAG, "intent: " + intent + ", extras: " + bundle);
        if (bundle == null)
            return;
        Log.d(TAG, "extras: " + Arrays.toString(bundle.keySet().toArray()));
        String notificationId = bundle.getString("notificationId");
        String action = intent.getAction();
        Log.d(TAG, "action received: " + action + ", for notificationId: " + notificationId);
        switch (action) {
            case "notification_cancelled":
                PushSdkClient.publishEventToServer(context, notificationId, R.string.event_dismissed);
                break;
            case "notification_clicked":
                PushSdkClient.publishEventToServer(context, notificationId, R.string.event_tapped);

                String deepLink = bundle.getString("deepLink");
                String targetUrl = bundle.getString("targetUrl");
                String url = deepLink != null ? deepLink : targetUrl;
                if (url != null) {
                    Log.d(TAG, "url: " + url);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.setData(Uri.parse(url));
                    context.startActivity(i);
                }
                break;
            default:
                break;
        }

//        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }
}
