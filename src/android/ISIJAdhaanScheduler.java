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
    private Boolean isBind = false;
    private Boolean isAdhaanActive = false;


    private CallbackContext adhaanStartedCallbackContext;
    private CallbackContext adhaanStoppedCallbackContext;
    private CallbackContext schedulerServiceStartedCallbackContext;


    AdhaanSchedulerService schedulerService;

    // Used to (un)bind the service to with the activity
    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AdhaanSchedulerService.ForegroundBinder binder =
                    (AdhaanSchedulerService.ForegroundBinder) service;

            schedulerService = binder.getService();

            schedulerServiceStartedCallbackContext.success();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Nothing to do here
        }
    };



    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("startService")) {
            this.startService(callbackContext);
            return true;
        } else if (action.equals("stopCurrentAdhaan")) {
            this.stopCurrentAlarm(callbackContext);
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
        } else if (action.equals("getServiceStatus")) {
            callbackContext.success(this.isActive ? "true" : "false");
            return true;
        } else if (action.equals("skipUpcomingAdhaan")) {
            this.skipUpcomingAdhaan(callbackContext);
            return true;
        } else if (action.equals("getUpcomingAdhaanTime")) {
            this.getUpcomingAdhaanTime(callbackContext);
            return true;
        }
        return false;
    }

    private void getUpcomingAdhaanTime(CallbackContext callbackContext) {

        if (!isBind) return;

        long time = schedulerService.getUpcomingAdhaanTime().getTime();
        callbackContext.success(Long.toString(time));
    }

    private void skipUpcomingAdhaan(CallbackContext callbackContext) {

        if (!isBind) return;

       schedulerService.skipUpcomingAdhaan();

       callbackContext.success();
    }

    private void stopCurrentAlarm(CallbackContext callbackContext) {

        if (!isBind) return;

        Activity context = cordova.getActivity();

       this.isActive = false;

       schedulerService.stopCurrentAlarm();
    }


    private void startService(CallbackContext callbackContext) {

        if (isBind) return;

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

        isBind = true;

        schedulerServiceStartedCallbackContext = callbackContext;


    }

    private void stopService(CallbackContext callbackContext) {

        if (!isBind) return;

        this.isActive = true;

        SchedulerResultReceiver schedulerResultReceiver = new SchedulerResultReceiver(null);

         Intent service_intent = new Intent(
                cordova.getActivity(), AdhaanSchedulerService.class);


         try {
            cordova.getActivity().unbindService(connection);
            cordova.getActivity().stopService(service_intent);
        } catch (Exception e) {

        }

        isBind = false;

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
                isAdhaanActive = true;
                if (adhaanStartedCallbackContext != null) {
                    adhaanStartedCallbackContext.success();
                }
            break;
            case 200:
                isAdhaanActive = false;
                if (adhaanStoppedCallbackContext != null) {
                    adhaanStoppedCallbackContext.success();
                }
            break;
            case 300:

            break;
        }

      }
     }
}
