package cordova.plugin.isij;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;




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


    @Override
    public void onReceive(Context context, Intent intent) {

         Intent service_intent = new Intent(
                context, ISIJAdhaanSchedulerFG.class);



         try {
            // context.bindService(
            //         intent, connection, Context.BIND_AUTO_CREATE);


            context.startService(service_intent);
        } catch (Exception e) {

        }
    }


}
