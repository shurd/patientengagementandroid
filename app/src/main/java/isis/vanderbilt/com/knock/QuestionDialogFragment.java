package isis.vanderbilt.com.knock;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import java.util.Set;

/**
 * Created by Sam on 3/18/2016.
 */
public class QuestionDialogFragment extends DialogFragment {
    public static final String PREFS_NAME = "KNOCK_LOGIN_PIN";

    private EditText mResponse;
    private TextView mQuestionText;
    private String mQuestion="", mReply="", mSid="";
    private Boolean mDone = false;
    QuestionsActivity mPrevActivity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View dialog = inflater.inflate(R.layout.question_dialog,null);

        //initialize the question text and the edittext whre they input answer
        mQuestionText = (TextView)dialog.findViewById(R.id.question_text);
        mQuestionText.setText(mQuestion);

        mResponse = (EditText)dialog.findViewById(R.id.reply_edit_text);
        mResponse.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mReply = s.toString();
            }
        });

        builder.setView(dialog)
                .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        QuestionDialogFragment.this.getDialog().cancel();
                    }
                });

        final AlertDialog actualDialog = builder.create();
        actualDialog.show();

        //overriding onClick command of Submit Button so that it does not close the dialog immediately
        actualDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mDone){
                    //remove the question from the listview
                    removeQuestion();

                    //refresh the list
                    mPrevActivity.setupList();

                    //dismiss the dialog
                    actualDialog.dismiss();
                }else {
                    //send response (in background), typically just one answer, so setting mDone
                    //to true here, but could also continue multiple times before setting mDone to true
                    //TODO: check to make sure a response was given first
                    SendResponse myResponse = new SendResponse(mReply, mSid);
                    myResponse.execute("");
                    mDone = true;
                }
            }
        });

        return actualDialog;
    }

    private void removeQuestion(){
        //after the question has been answered, remove it from the saved sid list
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        Set<String> sidList = new HashSet<>(prefs.getStringSet("sidList", new HashSet<String>()));
        sidList.remove(mSid);

        SharedPreferences.Editor editor;
        editor = prefs.edit();
        editor.putStringSet("sidList",sidList);

        editor.commit();
    }

    public void setQuestion(String str){
        mQuestion = str;
    }

    public void changeQuestionText(String data){
        mQuestionText.setText(data+"123");
    }

    public void setSid(String str){
        mSid = str;
    }

    public void setPrevActivity(QuestionsActivity act){
        mPrevActivity = act;
    }

    private class SendResponse extends AsyncTask<String, String, String> {
        private String reply, sid;
        public SendResponse(String data, String sidData) {
            super();
            reply = data;
            sid = sidData;
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
                jsonParam.put("sid", sid);
                //TODO: change this to the expected name
                jsonParam.put("digits",reply);
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
            changeQuestionText(result);
        }
    }

}
