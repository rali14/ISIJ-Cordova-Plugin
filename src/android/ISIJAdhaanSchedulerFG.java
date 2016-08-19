package cordova.plugin.isij;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.media.MediaPlayer;
import android.content.res.AssetFileDescriptor;

import android.app.AlarmManager;
import android.app.IntentService;
import android.widget.Toast;


/**
 * This class echoes a string called from JavaScript.
 */
public class ISIJAdhaanSchedulerFG extends Service {



    MediaPlayer objPlayer;
    final Timer scheduler = new Timer();
    TimerTask keepAliveTask;


    /**
     * Allow clients to call on to the service.
     */
    @Override
    public IBinder onBind (Intent intent) {
        return null;
    }



     /**
     * Put the service in a foreground state to prevent app from being killed
     * by the OS.
     */
    @Override
    public void onCreate () {
        super.onCreate();

        keepAwake();
        showNotification();
        playAdhaan();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        objPlayer.stop();
        sleepWell();
    }


    private void showNotification() {
         Notification.Builder notificationBuilder = new Notification.Builder(this)
            .setContentTitle("Adhaan")
            .setContentText("It's time to pray!")
            .setTicker("ticker")
            // .setOngoing(true)
            .setSmallIcon(getIconResId());

            NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);

            notificationManager.notify(0, notificationBuilder.build());
    }

    private void playAdhaan() {
        try {
            AssetFileDescriptor afd = getAssets().openFd("adhaan.mp3");
            objPlayer = new MediaPlayer();
            objPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    sleepWell();
                }
            });
            objPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            objPlayer.prepare();
            objPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void keepAwake() {
        final Handler handler = new Handler();
        keepAliveTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Nothing to do here
                        // Log.d("BackgroundMode", "" + new Date().getTime());
                    }
                });
            }
        };

        scheduler.schedule(keepAliveTask, 0, 1000);
    }

    /**
     * Stop background mode.
     */
    private void sleepWell() {
        keepAliveTask.cancel();
        Toast.makeText(this, "Task Stopped", Toast.LENGTH_SHORT).show();
    }

    /**
     * Retrieves the resource ID of the app icon.
     *
     * @return
     *      The resource ID of the app icon
     */
    private int getIconResId() {
        Context context = getApplicationContext();
        Resources res   = context.getResources();
        String pkgName  = context.getPackageName();

        int resId;
        resId = res.getIdentifier("icon", "drawable", pkgName);

        return resId;
    }
}