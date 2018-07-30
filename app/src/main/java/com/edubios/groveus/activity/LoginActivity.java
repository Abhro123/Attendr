/**
 * Author: Suhrid Ranjan Das
 */
package com.edubios.groveus.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.Map;

import com.edubios.groveus.R;
import com.edubios.groveus.app.AppConfig;
import com.edubios.groveus.app.AppController;
import com.edubios.groveus.helper.SQLiteHandler;
import com.edubios.groveus.helper.SessionManager;


public class LoginActivity extends Activity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(23)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //FirebaseInstanceIDService.onTokenRefresh();
        if (shouldAskPermissions()) {
            askPermissions();
        }
        //Cookie manager for session
        CookieManager cookieManager = new CookieManager(new PersistentCookieStore(getApplicationContext()),
                CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        CookieHandler.setDefault(cookieManager);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        Button btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        Button btnLinkToForgotPassword= (Button) findViewById(R.id.btnLinkToForgotPassword);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        HashMap<String, String> user = db.getUserDetails();

        String is_staff = user.get("is_staff");
        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            if(is_staff.equals("false")) {
                Intent intent = new Intent(LoginActivity.this,
                        SDashboard.class);
                startActivity(intent);
                finish();
            }
            else{
                Intent intent = new Intent(LoginActivity.this,
                        TDashboard.class);
                startActivity(intent);
                finish();
            }
        }

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                // Check for empty data in the form
                if (!email.isEmpty() && !password.isEmpty()) {
                    // login user
                    checkLogin(email, password);
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                            "Please enter the credentials!", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

        // Link to Register Screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        AdminView.class);
                startActivity(i);
               // finish();
            }
        });

        // Link to Forgot Password
        btnLinkToForgotPassword.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        SetpwdActivity.class);
                startActivity(i);
                //finish();
            }
        });


    }

    /**
     * function to verify login details in mysql db
     * */
    private void checkLogin(final String email, final String password) {
        // Tag used to cancel the request


        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in ...");
        showDialog();

        if (SharedPreference.getInstance(getApplicationContext()).getToken() != "") {

            StringRequest strReq = new StringRequest(Method.POST,
                    AppConfig.URL_LOGIN, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                   // Toast.makeText(getApplicationContext(), SharedPreference.getInstance(getApplicationContext()).getToken(), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Login Response: " + response);
                    Log.d(TAG, "TOKEN Response: " + SharedPreference.getInstance(getApplicationContext()).getToken());
                    hideDialog();

                    try {
                        JSONObject jObj = new JSONObject(response);
                        String msg="";
                        String u_id = jObj.getString("uid");


                        //Toast.makeText(getBaseContext(), u_id, Toast.LENGTH_LONG).show();
                        // Check for error node in json
                        if (u_id.equals(email))
                        {
                            Toast.makeText(getApplicationContext(), "Successfully Logged In!", Toast.LENGTH_SHORT).show();
                            session.setLogin(true);
                            String is_staff = jObj.getString("staff_value");

                            db.addUser(u_id,is_staff);

                            // Launch main activity
                            if(is_staff.equals("false")) {
                                Intent intent = new Intent(LoginActivity.this,
                                        SDashboard.class);
                                Toast.makeText(getApplicationContext(), "Successfully Logged In!", Toast.LENGTH_LONG).show();
                                startActivity(intent);
                                finish();
                            }
                            else{
                                Intent intent = new Intent(LoginActivity.this,
                                        TDashboard.class);

                                startActivity(intent);
                                finish();
                            }

                        } else {

                            // Error in login. Get the error message
                            String errorMsg = jObj.getString("msg");
                            Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        // JSON error
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Login Err: " + error.getMessage());
                    Toast.makeText(getApplicationContext(),
                            "Wrong Credentials!", Toast.LENGTH_LONG).show();
                    hideDialog();
                }
            }) {

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    // Posting parameters to login url
                    Map<String, String> params = new HashMap<>();
                    params.put("uid", email);
                    params.put("password", password);
                    params.put("token", SharedPreference.getInstance(getApplicationContext()).getToken());
                    return params;
                }
            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
        else
        {
            Toast.makeText(this, "Click Login Again", Toast.LENGTH_LONG).show();
            hideDialog();
        }
    }
    @Override
    public void onBackPressed() {

                        Intent a = new Intent(Intent.ACTION_MAIN);
                        a.addCategory(Intent.CATEGORY_HOME);
                        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(a);
    }


    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
