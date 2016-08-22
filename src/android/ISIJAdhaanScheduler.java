package cordova.plugin.isij;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.widget.Toast;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.app.Activity;
import android.os.ResultReceiver;
import android.os.Bundle;
import android.os.Handler;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;

/**
 * This class echoes a string called from JavaScript.
 */
public class ISIJAdhaanScheduler extends CordovaPlugin {


    private Boolean isActive = false;
    private Boolean isAdhaanActive = false;


    private CallbackContext adhaanStartedCallbackContext;
    private CallbackContext adhaanStoppedCallbackContext;


    AdhaanSchedulerService schedulerService;

    // Used to (un)bind the service to with the activity
    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AdhaanSchedulerService.ForegroundBinder binder =
                    (AdhaanSchedulerService.ForegroundBinder) service;

            schedulerService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Nothing to do here
        }
    };



    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("start")) {
            this.start(callbackContext);
            return true;
        } else if (action.equals("stopCurrent")) {
            this.stopCurrentAlarm(callbackContext);
            return true;
        } else if (action.equals("schedule")) {
            Long timestamp = args.getLong(0);
            this.scheduleAlarm(timestamp,callbackContext);
            return true;
        } else if (action.equals("onAdhaanStarted")) {
            this.adhaanStartedCallbackContext = callbackContext;
            return true;
        } else if (action.equals("onAdhaanStopped")) {
            this.adhaanStoppedCallbackContext = callbackContext;
            return true;
        } else if (action.equals("getAdhaanStatus")) {
            callbackContext.success(this.isAdhaanActive ? "true" : "false");
            return true;
        }
        return false;
    }
    private void scheduleAlarm(Long timestamp, CallbackContext callbackContext) {
        // if (timestamp != null) {
        //     alarmManager.setExact(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent);
        //     Toast.makeText(cordova.getActivity(), "Alarm Set for "+timestamp, Toast.LENGTH_SHORT).show();
        //     callbackContext.success();
        // } else {
        //     callbackContext.error("Unable to schedule alarm.");
        // }
    }

    private void stopCurrentAlarm(CallbackContext callbackContext) {
        Activity context = cordova.getActivity();

       this.isActive = false;

       schedulerService.stopCurrentAlarm();
    }


    private void start(CallbackContext callbackContext) {
        this.isActive = true;

        SchedulerResultReceiver schedulerResultReceiver = new SchedulerResultReceiver(null);

         Intent service_intent = new Intent(
                cordova.getActivity(), AdhaanSchedulerService.class);

         service_intent.putExtra("receiver", schedulerResultReceiver);

         try {
            cordova.getActivity().bindService(
                    service_intent, connection, Context.BIND_AUTO_CREATE);
            cordova.getActivity().startService(service_intent);
        } catch (Exception e) {

        }

        callbackContext.success();
    }


    class SchedulerResultReceiver extends ResultReceiver
     {
      public SchedulerResultReceiver(Handler handler) {
       super(handler);
      }

      @Override
      protected void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 100:
                Toast.makeText(cordova.getActivity(), "Alarm Started", Toast.LENGTH_SHORT).show();
                isAdhaanActive = true;
                if (adhaanStartedCallbackContext != null) {
                    adhaanStartedCallbackContext.success();
                }
            break;
            case 200:
                Toast.makeText(cordova.getActivity(), "Alarm Stopped", Toast.LENGTH_SHORT).show();
                isAdhaanActive = false;
                if (adhaanStoppedCallbackContext != null) {
                    adhaanStoppedCallbackContext.success();
                }
            break;
        }

      }
     }
}
