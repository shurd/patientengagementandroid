package isis.vanderbilt.com.knock;

import android.app.Activity;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class QuestionsActivity extends FragmentActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final int QR_REQUEST = 111;

    private static final String TAG = "MainActivity";

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private boolean isReceiverRegistered;

    public static String myappid="", myurl="";

    public static final String PREFS_NAME = "KNOCK_LOGIN_PIN";

    private Button mRegister, mLogout;
    private ListView mList;
    private String mQuestion, mSid;
    private TextView mEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLoggedIn();

        setContentView(R.layout.activity_questions);
        mEmpty = (TextView)findViewById(R.id.empty_listview);

        mList = (ListView)findViewById(R.id.listview);

        //initialize list
        setupList();

        //Intialize Buttons
        mRegister = (Button)findViewById(R.id.register_button);
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQRActivity();
            }
        });
        mLogout = (Button)findViewById(R.id.logout_button);
        mLogout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                logoutOfActivity();
            }
        });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences.getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    //save unique app id to shared preferences: used to add phone's push notifications capabilities
                    myappid = sharedPreferences.getString("myappid","");
                } else {
                    //error message
                }
            }
        };

        // Registering BroadcastReceiver
        registerReceiver();

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

    }

    public void startDialog(String result){
        //Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        mQuestion = result;
        QuestionDialogFragment dialogFragment = new QuestionDialogFragment();
        dialogFragment.setSid(mSid);
        dialogFragment.setQuestion(mQuestion);
        //needed to be able to update list
        dialogFragment.setPrevActivity(this);

        dialogFragment.show(getSupportFragmentManager(), "Question");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_questions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == QR_REQUEST) {
            String result;
            if (resultCode == RESULT_OK) {
                result = data.getStringExtra(QRActivity.EXTRA_QR_RESULT);
            } else {
                result = "Error";
            }
            //TODO: make this post to register the device, possibly add url parameter to existing nested post class
            myurl = result+"/"+myappid;
            GetQuestion grab = new GetQuestion(mSid, myurl);
            grab.execute("");
        }
    }


    public void startQRActivity(){
        Intent qrScanIntent = new Intent(this, QRActivity.class);
        startActivityForResult(qrScanIntent, QR_REQUEST);
    }

    public void logoutOfActivity(){
        //save in preferences that the user is logged out
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = settings.edit();
        editor.putBoolean("logged_in", false);
        editor.commit();

        //return to the LoginActivity screen
        Intent logoutIntent = new Intent(this, LoginActivity.class);
        logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(logoutIntent, QR_REQUEST);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
        super.onPause();
    }

    //register to be able to get push notifications
    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    //method called at the beginning of onCreate to make sure activity does not open if not logged in
    private void checkLoggedIn(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Boolean loggedIn = settings.getBoolean("logged_in", false);
        if(!loggedIn){
            logoutOfActivity();
        }
    }

    //TODO: potentially remove this later
    public void makeResultToast(boolean pass, String result){
        if(pass){
            Toast.makeText(this, "Success", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed Registration", Toast.LENGTH_LONG).show();
        }
        Log.d("Registration", result);
    }

    public void setupList(){
        String[] values = new String[getListFromPrefs()];
        if(values.length==0){
            //if empty then display something to say no questions
            mEmpty.setText("No Questions");
        } else {
            //name the listview parts
            for(int i=1;i<=values.length;i++){
                values[i-1]="Question "+i;
            }
        }

        QuestionArrayAdapter adapter = new QuestionArrayAdapter(this, values);

        mList.setAdapter(adapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                //grab the question with the correct SID
                int counter = 0;
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

                Set<String> sidList = new HashSet<>(prefs.getStringSet("sidList", new HashSet<String>()));
                Iterator iter = sidList.iterator();

                //control statement iterates to correct question in set
                while(counter<=position){
                    if(iter.hasNext())
                        mSid = iter.next().toString();
                    counter++;
                }

                //when clicked, a call is made to knock sever to get question and once text is gathered, a dialogue starts
                //TODO: pass in knockpatientengagement url
                GetQuestion grab = new GetQuestion(mSid, "");
                grab.execute("");
                //when it returns it calls startDialog
            }

        });
    }

    private int getListFromPrefs(){
        //get size of sid set to know how many questions to add to the listview
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> sidList = new HashSet<>(prefs.getStringSet("sidList", new HashSet<String>()));
        return sidList.size();
    }

    //Class controlling listview
    public class QuestionArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final String[] values;

        public QuestionArrayAdapter(Context context, String[] values) {
            super(context, -1, values);
            this.context = context;
            this.values = values;
        }

        //setup how each row in listview looks
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.question_list_format, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.question_item);

            textView.setText(values[position]);

            return rowView;
        }
    }

    private class GetQuestion extends AsyncTask<String, String, String> {
        private String taskSid, mUrl;
        public GetQuestion(String sid, String Url){
            super();
            taskSid = sid;
            mUrl = Url;
        }
        @Override
        protected String doInBackground(String... data) {
            StringBuilder sb = new StringBuilder();

            //TODO change this to the proper http://www.knockpatientengagement.com/app
            String http = "http://posttestserver.com/post.php";

            HttpURLConnection urlConnection=null;
            try {
                URL url = new URL(http);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type","application/json");

                urlConnection.connect();

                //Create JSONObject here
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("sid", taskSid);
                OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
                out.write(jsonParam.toString());
                out.close();

                int HttpResult =urlConnection.getResponseCode();
                if(HttpResult ==HttpURLConnection.HTTP_OK){
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                }
            } catch (MalformedURLException e) {

                e.printStackTrace();
            }
            catch (IOException e) {

                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }finally{
                if(urlConnection!=null)
                    urlConnection.disconnect();
            }
            return sb.toString();
        }

        protected void onPostExecute(String result) {
            if(mUrl.equals(""))
                startDialog(result);
            else
            //TODO: see what good and bad results are
                makeResultToast(result!=null, result);
        }
    }
}
