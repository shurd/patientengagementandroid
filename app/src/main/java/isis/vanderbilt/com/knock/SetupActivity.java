package isis.vanderbilt.com.knock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class SetupActivity extends ActionBarActivity {
    public static final String PREFS_NAME = "KNOCK_LOGIN_PIN";

    private String currentEntry="", first="";
    private Button one, two, three, four, five, six, seven, eight, nine, zero, back;
    private TextView mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mPassword = (TextView)findViewById(R.id.password_field_first);

        initializeButtons();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setup, menu);
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

    public void initializeButtons(){
        one = (Button)findViewById(R.id.one_key_first);
        one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEntry+="1";
                updatePasswordField();
            }
        });
        zero = (Button)findViewById(R.id.zero_key_first);
        zero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEntry+="0";
                updatePasswordField();
            }
        });
        two = (Button)findViewById(R.id.two_key_first);
        two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEntry+="2";
                updatePasswordField();
            }
        });
        three = (Button)findViewById(R.id.three_key_first);
        three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEntry+="3";
                updatePasswordField();
            }
        });
        four = (Button)findViewById(R.id.four_key_first);
        four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEntry+="4";
                updatePasswordField();
            }
        });
        five = (Button)findViewById(R.id.five_key_first);
        five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEntry+="5";
                updatePasswordField();
            }
        });
        six = (Button)findViewById(R.id.six_key_first);
        six.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEntry += "6";
                updatePasswordField();
            }
        });
        seven = (Button)findViewById(R.id.seven_key_first);
        seven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEntry += "7";
                updatePasswordField();
            }
        });
        eight = (Button)findViewById(R.id.eight_key_first);
        eight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEntry += "8";
                updatePasswordField();
            }
        });
        nine = (Button)findViewById(R.id.nine_key_first);
        nine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEntry += "9";
                updatePasswordField();
            }
        });
        back=(Button)findViewById(R.id.back_key_first);
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

    private void updatePasswordField(){
        if(currentEntry.length()>4){
            mPassword.setText("");
        }
        mPassword.setText(currentEntry);
        if (currentEntry.length() == 4){
            if(first.equals("")){
                first = currentEntry;
                currentEntry="";
                mPassword.setText(currentEntry);
            } else {
                if(first.equals(currentEntry)){
                    //save pin in shared preferences
                    savePin();
                } else {
                    first = "";
                    currentEntry="";
                    mPassword.setText(currentEntry);
                    Toast.makeText(this, "Pins do not match! Try again.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    private void savePin(){
        //save pin
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = settings.edit();
        editor.putString("password_pin", first);
        editor.commit();

        //TODO: have them login after this (also need to set logged_in boolean as true in preferences
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
