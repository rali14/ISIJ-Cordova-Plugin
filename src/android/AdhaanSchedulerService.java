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


/**
 * This class echoes a string called from JavaScript.
 */
public class AdhaanSchedulerService extends Service {

    public static final int NOTIFICATION_ID = -374433954;


    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private SalaatTimesProvider salaatTimesProvider;


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

        this.scheduleNextAlarm();

        //Notify Plugin Schedule Receiver that service has started
        Bundle bundle = new Bundle();
        pluginResultReceiver.send(300, bundle);

        return START_STICKY;
     }

     private void scheduleAlarm(Date date) {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, date.getTime(), pendingIntent);
     }

     private void scheduleNextAlarm() {
        this.upcomingAdhaanTime = this.salaatTimesProvider.getUpcomingAdhaanTime(this.upcomingAdhaanTime);
        this.scheduleAlarm(this.upcomingAdhaanTime);

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
        this.setupAlarmManager();

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


    private void setupAlarmManager() {
        AlarmResultReceiver resultReceiver = new AlarmResultReceiver(null);

        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.putExtra("receiver", resultReceiver);

        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

        this.alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    }

    protected void stopCurrentAlarm() {
        this.alarmManager.cancel(pendingIntent);
       //Stop Service
        Intent intent = new Intent(
                this, AdhaanPlayerService.class);
        // context.unbindService(connection);
        this.stopService(intent);



        //Notify Plugin Receiver
        Bundle bundle = new Bundle();
        pluginResultReceiver.send(200, bundle);

        //Schedule next Alarm
        scheduleNextAlarm();
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

      return DateUtils.getRelativeDateTimeString(this.getApplicationContext(), time.getTime(),
        DateUtils.HOUR_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0).toString();
     }

     private Notification makeNotification(String text) {

         Notification.Builder notificationBuilder = new Notification.Builder(this)
            .setContentTitle("ISIJ Adhaan Player")
            .setOngoing(true)
            .setSmallIcon(getIconResId());

            if (!text.equals("")) {
              notificationBuilder.setContentText(text);
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
        resId = res.getIdentifier("icon", "drawable", pkgName);

        return resId;
    }


    class AlarmResultReceiver extends ResultReceiver {
      public AlarmResultReceiver(Handler handler) {
       super(handler);
      }

      @Override
      protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == 200) {
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
