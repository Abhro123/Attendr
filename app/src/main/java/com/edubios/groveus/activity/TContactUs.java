package com.edubios.groveus.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.edubios.groveus.R;
import com.edubios.groveus.app.AppController;
import com.edubios.groveus.helper.SQLiteHandler;
import com.edubios.groveus.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TContactUs extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    EditText name;
    EditText message;
    Button submit;
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    public String u_id;
    private SessionManager session;
    private DrawerLayout tDrawerLayout;
    private ActionBarDrawerToggle tToggle;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tcontactscreen);
        tDrawerLayout = (DrawerLayout)findViewById(R.id.tdrawerlayout);
        tToggle = new ActionBarDrawerToggle(this,tDrawerLayout,R.string.drawer_open,R.string.drawer_close);
        tDrawerLayout.addDrawerListener(tToggle);
        tToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigation = (NavigationView) findViewById(R.id.tnavigation);
        navigation.setNavigationItemSelectedListener(this);

        name=(EditText)findViewById(R.id.tcontactname);
        message=(EditText)findViewById(R.id.tcontactmessage);
        submit=(Button)findViewById(R.id.tcontactbutton);
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        db = new SQLiteHandler(getApplicationContext());

        final HashMap<String, String> user = db.getUserDetails();

        isNetworkConnectionAvailable();

        // session manager
        session = new SessionManager(getApplicationContext());
        u_id = user.get("u_id");


        submit.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String cname = name.getText().toString().trim();
                String cmessage = message.getText().toString().trim();

                // Check for empty data in the form
                if (!cname.isEmpty() && !cmessage.isEmpty()) {
                    // login user
                    sendMessage(cname, cmessage,u_id);
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                            "Enter Name And Message", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });
    }
    public void sendMessage(final String cname,final String cmessage, final String u_id){
        String tag_string_req = "req_login";

        pDialog.setMessage("Sending Message ...");
        showDialog();
        String url= "https://attendanceproject.herokuapp.com/home/contact/";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {

            public static final String TAG = "success";

            @Override
            public void onResponse(String response) {
                //Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                Log.d(TAG, "Message Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    String msg = "";
                    msg+= jObj.getString("msg");
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
               // Log.e(TAG, "Login Err: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("c_name", cname);
                params.put("c_subject", cmessage);
                params.put("uid", u_id);
                //params.put("token", SharedPreference.getInstance(getApplicationContext()).getToken());
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(tToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void checkNetworkConnection(){
        AlertDialog.Builder builder =new AlertDialog.Builder(this);
        builder.setTitle("No internet Connection");
        builder.setMessage("Please turn on internet connection to continue");
        builder.setNegativeButton("close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public boolean isNetworkConnectionAvailable(){
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        if(isConnected) {
            Log.d("Network", "Connected");
            return true;
        }
        else{
            checkNetworkConnection();
            Log.d("Network","Not Connected");
            return false;
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.slogout:
                session.setLogin(false);

                db.deleteUsers();

                // Launching the login activity
                Intent intent = new Intent(TContactUs.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;

            case R.id.tprofile:

                // Launching the login activity
                Intent i = new Intent(TContactUs.this, TProfileView.class);
                startActivity(i);
                return true;

            case R.id.thome:

                // Launching the login activity
                Intent ih = new Intent(TContactUs.this, TDashboard.class);
                startActivity(ih);
                return true;

            case R.id.tcontact:

                // Launching the login activity
                Intent ic = new Intent(TContactUs.this, TContactUs.class);
                startActivity(ic);
                return true;

            case R.id.single:

                // Launching the login activity
                Intent is = new Intent(TContactUs.this, TPdfs.class);
                startActivity(is);
                return true;

            case R.id.classwise:

                // Launching the login activity
                Intent icc = new Intent(TContactUs.this, TPdfc.class);
                startActivity(icc);
                return true;

            default:
                tDrawerLayout =(DrawerLayout)findViewById(R.id.tdrawerlayout);
                tDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
        }

    }
    @Override
    public void onBackPressed() {
        if (this.tDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.tDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
