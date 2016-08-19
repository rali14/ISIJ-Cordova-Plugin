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

/**
 * This class echoes a string called from JavaScript.
 */
public class ISIJAdhaanScheduler extends CordovaPlugin {

    private AlarmManager alarmManager;

    private Boolean isActive = false;

    private PendingIntent pendingIntent;


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        } else if (action.equals("start")) {
            this.start(callbackContext);
            return true;
        } else if (action.equals("stopCurrent")) {
            this.stopCurrentAlarm(callbackContext);
            return true;
        }


        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void stopCurrentAlarm(CallbackContext callbackContext) {
        Activity context = cordova.getActivity();

       this.isActive = false;
       this.alarmManager.cancel(pendingIntent);
       //Stop Service
        Intent intent = new Intent(
                context, ISIJAdhaanSchedulerFG.class);
        // context.unbindService(connection);
        context.stopService(intent);
        callbackContext.success();

         Toast.makeText(cordova.getActivity(), "Stopped", Toast.LENGTH_SHORT).show();

    }

    private void start(CallbackContext callbackContext) {
        // Intent mServiceIntent = new Intent(cordova.getActivity(),  ISIJAdhaanSchedulerFG.class);
        // // mServiceIntent.setData(Uri.parse(dataUrl));
        // cordova.getActivity().startService(mServiceIntent);
        this.isActive = true;


        Intent alarmIntent = new Intent(cordova.getActivity(), AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(cordova.getActivity(), 0, alarmIntent, 0);

        this.alarmManager = (AlarmManager) cordova.getActivity().getSystemService(Context.ALARM_SERVICE);
        long interval = System.currentTimeMillis() + 10000;

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, interval, pendingIntent);
        Toast.makeText(cordova.getActivity(), "Alarm Set", Toast.LENGTH_SHORT).show();

         callbackContext.success("Success");
    }
}
