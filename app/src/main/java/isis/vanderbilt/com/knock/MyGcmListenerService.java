package isis.vanderbilt.com.knock;

/**
 * Created by Sam on 3/23/2016.
 */
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;

import java.util.HashSet;
import java.util.Set;

public class MyGcmListenerService extends GcmListenerService {
    public static final String PREFS_NAME = "KNOCK_LOGIN_PIN";

    //private static final String TAG = "MyGcmListenerService";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String title = data.getString("title");
        String message = data.getString("message");
        String sid = data.getString("sid");

        //save the sid of the question to answer
        saveSidToDevice(sid);

        //send the notification with desired message and text
        sendNotification(title, message);
    }

    private void saveSidToDevice(String sid){
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        //saves the received sid to the set containing all sids (questions to be answered)
        Set<String> sidList = new HashSet<>(prefs.getStringSet("sidList", new HashSet<String>()));
        sidList.add(sid);
        SharedPreferences.Editor editor;
        editor = prefs.edit();
        editor.putStringSet("sidList",sidList);

        editor.commit();
    }

    //TODO: change notification id so that old ones not cleared
    private void sendNotification(String title, String message) {
        //create pending intent so notification opens QuestionsActivity when clicked
        Intent mIntent = new Intent(this, QuestionsActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent mPending = PendingIntent.getActivity(this,0,mIntent,PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.ic_launcher);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(message);
        mBuilder.setContentIntent(mPending);
        mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setPriority(Notification.PRIORITY_HIGH);

        NotificationManager mNotify = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotify.notify(0,mBuilder.build());
    }
}