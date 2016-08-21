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
/**
 * This class echoes a string called from JavaScript.
 */
public class ISIJAdhaanScheduler extends CordovaPlugin {

    private AlarmManager alarmManager;

    private Boolean isActive = false;

    private PendingIntent pendingIntent;


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
        }


        return false;
    }

    private void scheduleAlarm(Long timestamp, CallbackContext callbackContext) {
        if (timestamp != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent);
            Toast.makeText(cordova.getActivity(), "Alarm Set for "+timestamp, Toast.LENGTH_SHORT).show();
            callbackContext.success();
        } else {
            callbackContext.error("Unable to schedule alarm.");
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


        MyResultReceiver resultReceiver = new MyResultReceiver(null);



        Intent alarmIntent = new Intent(cordova.getActivity(), AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(cordova.getActivity(), 0, alarmIntent, 0);
        alarmIntent.putExtra("receiver", resultReceiver);
        this.alarmManager = (AlarmManager) cordova.getActivity().getSystemService(Context.ALARM_SERVICE);


        callbackContext.success();
    }

     class MyResultReceiver extends ResultReceiver
 {
  public MyResultReceiver(Handler handler) {
   super(handler);
  }

  @Override
  protected void onReceiveResult(int resultCode, Bundle resultData) {
    Toast.makeText(cordova.getActivity(), "Alarm Received", Toast.LENGTH_SHORT).show();
  }
 }
}
