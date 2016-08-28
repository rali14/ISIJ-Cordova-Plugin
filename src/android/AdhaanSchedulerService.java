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
import android.os.Binder;
import android.util.Log;
import android.media.MediaPlayer;
import android.content.res.AssetFileDescriptor;
import android.app.AlarmManager;
import android.os.ResultReceiver;
import android.os.Bundle;
import android.widget.Toast;
import android.os.PowerManager;

import java.util.Date;
import java.text.SimpleDateFormat;
import android.text.format.DateUtils;
import java.util.Calendar;
import java.text.DateFormat;


/**
 * This class echoes a string called from JavaScript.
 */
public class AdhaanSchedulerService extends Service {

    public static final int NOTIFICATION_ID = -374433954;
    public static final int ALARM_TAG = -312333954;


    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private SalaatTimesProvider salaatTimesProvider;
    private Boolean soundEnabled;
    private ResultReceiver resultReceiver;


    final Timer scheduler = new Timer();
    TimerTask keepAliveTask;

    private ResultReceiver pluginResultReceiver;
    private PowerManager.WakeLock wakeLock;


    private Date upcomingAdhaanTime = null;

    // Binder given to clients
    private final IBinder mBinder = new ForegroundBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class ForegroundBinder extends Binder {
        AdhaanSchedulerService getService() {
            // Return this instance of ForegroundService
            // so clients can call public methods
            return AdhaanSchedulerService.this;
        }
    }

    /**
     * Allow clients to call on to the service.
     */
    @Override
    public IBinder onBind (Intent intent) {
       return mBinder;
    }


     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {


        pluginResultReceiver = intent.getParcelableExtra("receiver");

        this.soundEnabled = intent.getExtras().getBoolean("enable_sound");

        this.scheduleNextAlarm();

        //Notify Plugin Schedule Receiver that service has started
        Bundle bundle = new Bundle();
        pluginResultReceiver.send(300, bundle);

        return START_STICKY;
     }

     private void scheduleAlarm(Date date) {
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.putExtra("receiver", resultReceiver);
        alarmIntent.putExtra("play_sound", this.soundEnabled);
        pendingIntent = PendingIntent.getBroadcast(this, ALARM_TAG, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, date.getTime(), pendingIntent);
     }

     private void scheduleNextAlarm() {
        this.upcomingAdhaanTime = this.salaatTimesProvider.getUpcomingAdhaanTime(this.upcomingAdhaanTime);
        if (this.upcomingAdhaanTime != null) {
            this.scheduleAlarm(this.upcomingAdhaanTime);
        }
        //make notification
        Notification notification = makeNotification("Next Adhaan will be played "+
          this.formatSalaatTime(this.upcomingAdhaanTime));
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, notification);

     }

     /**
     * Put the service in a foreground state to prevent app from being killed
     * by the OS.
     */
    @Override
    public void onCreate () {
        super.onCreate();

         keepAwake();

         this.resultReceiver = new AlarmResultReceiver(null);

         this.alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

         //Load Salaat Times
        try {
          this.salaatTimesProvider = new SalaatTimesProvider(getAssets().open("times.json"));
        } catch (Exception e) {
            e.printStackTrace();
        }





    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (pendingIntent != null) {
            this.alarmManager.cancel(pendingIntent);
        }
        sleepWell();
    }


    private void keepAwake() {


        startForeground(NOTIFICATION_ID, makeNotification(""));


        PowerManager powerMgr = (PowerManager)
                getSystemService(POWER_SERVICE);

        wakeLock = powerMgr.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "ISIJAdhaanScheduler");

        wakeLock.acquire();

    }

    /**
     * Stop background mode.
     */
    private void sleepWell() {
      stopForeground(true);

        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    private void cancelCurrentAlarm() {
        // Intent i = new Intent(this, AdhaanPlayerService.class);

        // PendingIntent pi = PendingIntent.getBroadcast(this, ALARM_TAG, i,
        //         PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
          System.out.println("Alarm Cancelled");
            alarmManager.cancel(pendingIntent);
        }
    }


    protected void stopCurrentAlarm() {
        this.cancelCurrentAlarm();
       //Stop Service
        Intent intent = new Intent(
                this, AdhaanPlayerService.class);
        // context.unbindService(connection);
        this.stopService(intent);

    }

    protected void disableAdhaanSound() {
        if (soundEnabled) {
          System.out.println("Alarm Disabled");
            this.stopCurrentAlarm();
            this.soundEnabled = false;

            this.scheduleAlarm(this.upcomingAdhaanTime);
        }
    }

    protected void enableAdhaanSound() {
        if (!soundEnabled) {
          System.out.println("Alarm Enabled");
            this.stopCurrentAlarm();
            this.soundEnabled = true;

            this.scheduleAlarm(this.upcomingAdhaanTime);
        }
    }

    protected void skipUpcomingAdhaan() {
        this.stopCurrentAlarm();
        //Reschedule next
        scheduleNextAlarm();
    }

    protected Date getUpcomingAdhaanTime() {

        return this.upcomingAdhaanTime;
    }


     private String formatSalaatTime(Date time) {
        if (DateUtils.isToday(time.getTime())) {
            return "at "+DateUtils.formatSameDayTime(time.getTime(), (Calendar.getInstance()).getTimeInMillis(),
            DateFormat.SHORT, DateFormat.SHORT).toString();
        } else {
            return DateUtils.getRelativeDateTimeString(this.getApplicationContext(),
                time.getTime(), DateUtils.DAY_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0).toString();
        }

     }

     private Notification makeNotification(String text) {

         Notification.Builder notificationBuilder = new Notification.Builder(this)
            .setContentTitle("ISIJ of Toronto")
            .setOngoing(true)
            .setSmallIcon(getIconResId());


            if (!text.equals("")) {
              notificationBuilder.setStyle(new Notification.BigTextStyle().bigText(text))
              .setContentText(text);

            }

            Context context = getApplicationContext();
            String pkgName  = context.getPackageName();
            Intent intent   = context.getPackageManager().getLaunchIntentForPackage(pkgName);


            if (intent != null) {
                PendingIntent contentIntent = PendingIntent.getActivity(
                        context, NOTIFICATION_ID, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                notificationBuilder.setContentIntent(contentIntent);
            }


            return notificationBuilder.build();
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


    class AlarmResultReceiver extends ResultReceiver {
      public AlarmResultReceiver(Handler handler) {
       super(handler);
      }

      @Override
      protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == 100) {

          //Schedule next Alarm
          scheduleNextAlarm();
        } else if (resultCode == 200) {
            //Stop Service
          Intent intent = new Intent(AdhaanSchedulerService.this, AdhaanPlayerService.class);
          // context.unbindService(connection);
          AdhaanSchedulerService.this.stopService(intent);
        } else if (resultCode == 201) {

              //Stop Service
              Intent intent = new Intent(AdhaanSchedulerService.this, AdhaanPlayerService.class);
              // context.unbindService(connection);
              AdhaanSchedulerService.this.stopService(intent);

              //Schedule next Alarm
              scheduleNextAlarm();
        }

        pluginResultReceiver.send(resultCode, resultData);

      }
     }
}
