package com.golfscore.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.golfscore.app.data.JSONParser;
import com.golfscore.app.util.Utility;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Joel on 16/01/14.
 */
public class LoginActivity extends Activity {

    JSONObject json = null;
    String url = null;
    JSONParser parser = new JSONParser();
    String user, pass;

    // catchable error
    Boolean hasError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginButton = (Button)findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                // get user input
                EditText userText = (EditText) findViewById(R.id.username_text);
                EditText passText = (EditText) findViewById(R.id.password_text);

                // convert user input to String
                user = userText.getText().toString();
                pass = passText.getText().toString();

                // encode variables
                try {
                    user = URLEncoder.encode(user, "UTF-8");
                    pass = URLEncoder.encode(pass, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                // URL to execute PHP
                url = Utility.url + "login_sql.php?sql=member&fields=member_id~user_type&userColumn=member_id&userID=" + user + "&passwordColumn=password&userPassword=" + pass;
                // url = "http://10.0.172.124//login_sql.php?sql=member&fields=member_id~user_type&userColumn=member_id&userID=" + user + "&passwordColumn=password&userPassword=" + pass;

                // create a new thread
                Thread t = new Thread() {
                    public void run() {
                        json = parser.makeHttpRequest(url);

                        try {
                            int success = json.getInt("success");

                            // if user found in database
                            if (success == 1) {
                                JSONArray userInfo = json.getJSONArray("CisFitness");

                                JSONObject userDetails = userInfo.getJSONObject(0);

                                Utility.userId = userDetails.getString("member_id");

                                if (user.equals("member") && pass.equals("password")) {
                                    // User is a member. Go to member menu
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else if(user.equals("admin") && pass.equals("password")) {
                                    // User is an admin. Go to admin menu
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }

                                // launch the MainMenuActivity
                                // Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                // startActivity(intent);
                                // finish();
                            } else {
                                // if error occurred
                                hasError = true;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };

                // start the thread and later join it to the main thread
                try {
                    t.start();
                    t.join();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                // toast a message based if the was an error with log in credentials
                if (hasError) {
                    Toast.makeText(LoginActivity.this, "Login Failed.", Toast.LENGTH_LONG).show();
                    hasError = false;
                } else {
                    if (Utility.isAdmin) {
                        Toast.makeText(LoginActivity.this, "Logged in as Admin.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Login Success.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
}
