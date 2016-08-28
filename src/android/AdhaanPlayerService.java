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

import android.os.ResultReceiver;
import android.os.Bundle;


/**
 * This class echoes a string called from JavaScript.
 */
public class AdhaanPlayerService extends Service {

    public static final int NOTIFICATION_ID = -324433954;


    MediaPlayer objPlayer;
    final Timer scheduler = new Timer();
    TimerTask keepAliveTask;
    ResultReceiver resultReceiver;


    /**
     * Allow clients to call on to the service.
     */
    @Override
    public IBinder onBind (Intent intent) {
        return null;
    }


     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {

      if (intent != null) {
        resultReceiver = intent.getParcelableExtra("receiver");
        Boolean playSound = intent.getBooleanExtra("play_sound", true);
        showNotification();
        if (playSound) {
          playAdhaan();
          Bundle bundle = new Bundle();
          resultReceiver.send(100, bundle);
        } else  {
           Bundle bundle = new Bundle();
          resultReceiver.send(201, bundle);
        }

      } else {
        sleepWell();
      }

      return START_STICKY;
     }


     /**
     * Put the service in a foreground state to prevent app from being killed
     * by the OS.
     */
    @Override
    public void onCreate () {
        super.onCreate();
        keepAwake();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        // notificationManager.cancel(NOTIFICATION_ID);

        if (objPlayer != null) {
          objPlayer.stop();
        }
        sleepWell();
    }


    private void showNotification() {
         Notification.Builder notificationBuilder = new Notification.Builder(this)
            .setContentTitle("Salaat Time")
            .setContentText("It's time to pray!")
            .setTicker("ticker")
            // .setOngoing(true)
            .setSmallIcon(getIconResId());

            Context context = getApplicationContext();
            String pkgName  = context.getPackageName();
            Intent intent   = context.getPackageManager().getLaunchIntentForPackage(pkgName);


            if (intent != null) {
                PendingIntent contentIntent = PendingIntent.getActivity(
                        context, NOTIFICATION_ID, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                notificationBuilder.setContentIntent(contentIntent);
            }

            NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);

            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void playAdhaan() {
        try {
            AssetFileDescriptor afd = getAssets().openFd("adhaan.mp3");
            objPlayer = new MediaPlayer();
            objPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    Bundle bundle = new Bundle();
                    resultReceiver.send(200, bundle);
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
        resId = res.getIdentifier("notification_icon", "drawable", pkgName);


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return res.getIdentifier("notification_icon", "drawable", pkgName);
        } else {
            return res.getIdentifier("icon", "drawable", pkgName);
        }

    }
}
