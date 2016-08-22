package isis.vanderbilt.com.knock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class LoginActivity extends ActionBarActivity {
    public static final String PREFS_NAME = "KNOCK_LOGIN_PIN";

    private String currentEntry="";
    private Button one, two, three, four, five, six, seven, eight, nine, zero, back;
    private TextView mPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //If this is the first time the user opens the app, this page redirects to SetupActivity
        checkFirstTime();

        mPassword = (TextView)findViewById(R.id.password_field);

        //helper method to initialize the pinpad buttons
        initializeButtons();
        mPassword.setText("");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

    private void checkFirstTime(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if(settings.contains("password_pin")){
            //continue as usual as a password exists
        } else {
            //launch setupactivity
            Intent intent = new Intent(this, SetupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    public void initializeButtons(){
        one = (Button)findViewById(R.id.one_key);
        one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEntry+="1";
                updatePasswordField();
            }
        });
        zero = (Button)findViewById(R.id.zero_key);
        zero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEntry+="0";
                updatePasswordField();
            }
        });
        two = (Button)findViewById(R.id.two_key);
        two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEntry+="2";
                updatePasswordField();
            }
        });
        three = (Button)findViewById(R.id.three_key);
        three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEntry+="3";
                updatePasswordField();
            }
        });
        four = (Button)findViewById(R.id.four_key);
        four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEntry+="4";
                updatePasswordField();
            }
        });
        five = (Button)findViewById(R.id.five_key);
        five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEntry+="5";
                updatePasswordField();
            }
        });
        six = (Button)findViewById(R.id.six_key);
        six.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEntry += "6";
                updatePasswordField();
            }
        });
        seven = (Button)findViewById(R.id.seven_key);
        seven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEntry += "7";
                updatePasswordField();
            }
        });
        eight = (Button)findViewById(R.id.eight_key);
        eight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEntry += "8";
                updatePasswordField();
            }
        });
        nine = (Button)findViewById(R.id.nine_key);
        nine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEntry += "9";
                updatePasswordField();
            }
        });
        back=(Button)findViewById(R.id.back_key);
        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(currentEntry.length()>0){
                    currentEntry=currentEntry.substring(0,currentEntry.length()-1);
                    updatePasswordField();
                }
            }
        });
    }

    //called each time a pinpad button is pressed to see if the correct password has been entered
    //password field updated each time to indicate how many numbers have been entered
    private void updatePasswordField(){
        mPassword.setText(currentEntry);
        if (currentEntry.length() >= 4)
            checkPassword();
    }

    private void checkPassword(){
        //if currentEntry is equal to stored password, then open main screen
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String actualPassword = settings.getString("password_pin", null);
        if(actualPassword.equals(currentEntry)){
            //saves logged in state (so that notifications do not open Questions Activity without being logged in)
            SharedPreferences.Editor editor;
            editor = settings.edit();
            editor.putBoolean("logged_in", true);
            editor.commit();

            Intent intent = new Intent(this, QuestionsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            //passwords do not match, so clear the entry and indicate the password was wrong
            Animation fading = AnimationUtils.loadAnimation(this, R.anim.fade);
            mPassword.startAnimation(fading);
            Toast.makeText(this, "Incorrect Pin! Try again",Toast.LENGTH_LONG).show();
            currentEntry="";
            mPassword.setText(currentEntry);
        }
    }
}
