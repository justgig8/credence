package credence.oorja.com.androidsdk;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by gaurav on 05/12/16.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MFMS";
    private static final AtomicInteger NOTIFICATION_ID_GENERATOR = new AtomicInteger(0);

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Map<String, String> data = remoteMessage.getData();

        String title;
        String body;
        String icon;
        String sound;
        String tag;
        if (data == null) {
            Log.d(TAG, "notification: " + notification);
            title = notification.getTitle();
            body = notification.getBody();
            icon = notification.getIcon();
            sound = notification.getSound();
            tag = notification.getTag();
            String notificationId = tag;
            sendNotification(notificationId, title, body, icon, sound, tag);
        } else {
            Log.d(TAG, "data: " + data);
            title = data.get("title");
            body = data.get("body");
            icon = data.get("icon");
            sound = data.get("sound");
            tag = data.get("tag");
            String notificationId = data.get("notificationId");
            String image = data.get("image");
            String targetUrl = data.get("targetUrl");
            String deepLink = data.get("deepLink");
            sendNotificationCustom(notificationId, title, body, icon, sound, tag, image, targetUrl, deepLink);
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String notificationId, String title, String messageBody, String icon, String sound, String tag) {
        Log.d(TAG, "showing general notification with tag: " + tag);
        Context context = this.getApplicationContext();

        Intent intent = new Intent(this, NotificationBroadcastReceiver.class);
        intent.setAction("notification_clicked");
        intent.putExtra("body", messageBody);
        intent.putExtra("notificationId", notificationId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent dismissIntent = new Intent(this, NotificationBroadcastReceiver.class);
        dismissIntent.setAction("notification_cancelled");
        dismissIntent.putExtra("notificationId", notificationId);
        PendingIntent pendingDismissIntent = PendingIntent.getBroadcast(context, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Log.d(TAG, "icon: " + icon);
        int iconId = -1;
        if (icon != null) {
            iconId = context.getResources().getIdentifier(icon, "drawable", context.getPackageName());
            Log.d(TAG, "iconId: " + iconId);
        }

        Log.d(TAG, "sound: " + sound);
        Uri soundId = null;
        if (sound != null) {
            soundId = Uri.parse("android.resource://" + context.getPackageName() + "/raw/" + sound);
            Log.d(TAG, "sound: " + soundId);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), iconId))
                /*.setStyle(new NotificationCompat.BigTextStyle().bigText("dfds"))*/
                .setContentText(messageBody)
                .setContentTitle(title != null ? title : getString(R.string.app_name))
                .setSound(sound != null ? soundId : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setDeleteIntent(pendingDismissIntent)
                .setAutoCancel(true);
        if (icon != null)
            notificationBuilder.setSmallIcon(iconId);
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID_GENERATOR.incrementAndGet(), notificationBuilder.build());
    }

    /**
     * Added by gaurav on 28/09/17.
     */
    private void sendNotificationCustom(String notificationId, String title, String messageBody, String icon, String sound, String tag, String image, String targetUrl, String deepLink) {
        Log.d(TAG, "showing custom notification with tag: " + tag + ", id: " + notificationId);
        Context context = this.getApplicationContext();

        Intent intent = new Intent(this, NotificationBroadcastReceiver.class);
        intent.setAction("notification_clicked");
        intent.putExtra("body", messageBody);
        intent.putExtra("notificationId", notificationId);
        if (targetUrl != null)
            intent.putExtra("targetUrl", targetUrl);
        if (deepLink != null)
            intent.putExtra("deepLink", deepLink);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent dismissIntent = new Intent(this, NotificationBroadcastReceiver.class);
        dismissIntent.setAction("notification_cancelled");
        dismissIntent.putExtra("notificationId", notificationId);
        PendingIntent pendingDismissIntent = PendingIntent.getBroadcast(context, 0, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Log.d(TAG, "icon: " + icon);
        int iconId = -1;
        if (icon != null) {
            iconId = context.getResources().getIdentifier(icon, "drawable", context.getPackageName());
            Log.d(TAG, "iconId: " + iconId);
        }

        Log.d(TAG, "sound: " + sound);
        Uri soundId = null;
        if (sound != null) {
            soundId = Uri.parse("android.resource://" + context.getPackageName() + "/raw/" + sound);
            Log.d(TAG, "sound: " + soundId);
        }


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), iconId))
                /*.setStyle(new NotificationCompat.BigTextStyle().bigText("dfds"))*/
                .setContentText(messageBody)
                .setContentTitle(title != null ? title : getString(R.string.app_name))
                .setSound(sound != null ? soundId : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setDeleteIntent(pendingDismissIntent)
                .setAutoCancel(true);
        if (iconId != 0 && iconId != -1)
            notificationBuilder.setSmallIcon(iconId);
        if (image != null) {
            Bitmap bitmap = null;
            try {
                bitmap = getBitmapFromURL(image);
                if (bitmap != null) {
                    NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle().bigPicture(bitmap);
                    if (messageBody != null)
                        style.setSummaryText(messageBody);
                    if (title != null)
                        style.setBigContentTitle(title);
                    notificationBuilder.setStyle(style);
                    notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID_GENERATOR.incrementAndGet(), notificationBuilder.build());

        PushSdkClient.publishEventToServer(context, notificationId, R.string.event_delivered);
    }

    private Bitmap getBitmapFromURL(String src) {
        try {
            Log.d(TAG, "getting bitmap for: " + src);
            Bitmap b = loadImageFromStorage(src);
            if (b != null) {
                Log.d(TAG, "picked up bitmap from local storage");
                return b;
            }
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            saveToInternalStorage(src, myBitmap);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String d = null;

    private void saveToInternalStorage(String imageUrl, Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        String hash = "i" + Math.abs(imageUrl.hashCode());
        // Create imageDir
        File mypath = new File(directory, hash);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            Log.d(TAG, "saved bitmap in local storage at path: " + mypath);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        d = directory.getAbsolutePath();
        Log.d(TAG, "dir path: " + d);
    }

    private Bitmap loadImageFromStorage(String imageUrl) {
        try {
            String hash = "i" + Math.abs(imageUrl.hashCode());
            File f = new File(d, hash);
            Log.d(TAG, "checking for bitmap in local storage at path: " + f.getAbsolutePath());
            if (!f.exists())
                return null;
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
