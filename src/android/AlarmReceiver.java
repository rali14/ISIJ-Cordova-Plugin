package cordova.plugin.isij;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import android.widget.Toast;
import android.os.ResultReceiver;
import android.os.Bundle;
import android.os.Handler;


public class AlarmReceiver extends BroadcastReceiver {

    //    // Used to (un)bind the service to with the activity
    // private final ServiceConnection connection = new ServiceConnection() {

    //     @Override
    //     public void onServiceConnected(ComponentName name, IBinder binder) {
    //         // Nothing to do here
    //     }

    //     @Override
    //     public void onServiceDisconnected(ComponentName name) {
    //         // Nothing to do here
    //     }
    // };

    Context context;
    ResultReceiver schedulerServiceResultReceiver;



    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;


        Intent service_intent = new Intent(
                context, AdhaanPlayerService.class);

        this.schedulerServiceResultReceiver = intent.getParcelableExtra("receiver");

        ServiceResultReceiver serviceResultReceiver = new ServiceResultReceiver(null);
        service_intent.putExtra("receiver", serviceResultReceiver);
        service_intent.putExtra("play_sound", intent.getBooleanExtra("play_sound", true));

         try {
            // context.bindService(
            //         intent, connection, Context.BIND_AUTO_CREATE);


            context.startService(service_intent);
        } catch (Exception e) {

        }
    }

    class ServiceResultReceiver extends ResultReceiver
     {
      public ServiceResultReceiver(Handler handler) {
       super(handler);
      }

      @Override
      protected void onReceiveResult(int resultCode, Bundle resultData) {
        schedulerServiceResultReceiver.send(resultCode, resultData);
      }
     }
    }
