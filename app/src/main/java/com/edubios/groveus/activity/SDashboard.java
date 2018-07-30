package com.edubios.groveus.activity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.edubios.groveus.app.AppController.TAG;

public class SDashboard extends AppCompatActivity implements  NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener {
    public String date;
    private EditText dateet;
    private SQLiteHandler db;
    private SessionManager session;
    private TextView fname;
    private TextView s_in;
    private TextView s_out;
    private DatePickerDialog datePickerDialog;
    private DrawerLayout sDrawerLayout;
    private ActionBarDrawerToggle sToggle;
    //An ArrayList for Spinner Items
    private ArrayList<String> students;
    private int status1;
    public String u_id;
    private ProgressDialog pDialog;
    //JSON Array
    private JSONArray data;
    public Spinner spinner;
    private Button Present;
    private Button Absent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.sdashboard);
        sDrawerLayout = (DrawerLayout)findViewById(R.id.sdrawerlayout);
        sToggle = new ActionBarDrawerToggle(this,sDrawerLayout,R.string.drawer_open,R.string.drawer_close);
        sDrawerLayout.addDrawerListener(sToggle);
        sToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigation = (NavigationView) findViewById(R.id.snavigation);
        navigation.setNavigationItemSelectedListener(this);

        CookieManager cookieManager = new CookieManager(new PersistentCookieStore(getApplicationContext()),
                CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        CookieHandler.setDefault(cookieManager);
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        Present=(Button)findViewById(R.id.attendance);
        //Absent=(Button)findViewById(R.id.absent);

        fname=(TextView)findViewById(R.id.welcome);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Ordinary.ttf");
        fname.setTypeface(typeface);
        s_in=(TextView)findViewById(R.id.intime);
        s_out=(TextView)findViewById(R.id.outtime);
        //sec1=(TextView)findViewById(R.id.section);
        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());
        isNetworkConnectionAvailable();
        final HashMap<String, String> user = db.getUserDetails();




        // session manager
        session = new SessionManager(getApplicationContext());
        u_id = user.get("u_id");

//Initializing the ArrayList
        students = new ArrayList<String>();

        //Initializing Spinner
        spinner = (Spinner) findViewById(R.id.datespin);

        //Adding an Item Selected Listener to our Spinner
        //As we have implemented the class Spinner.OnItemSelectedListener to this class iteself we are passing this to setOnItemSelectedListener
        spinner.setOnItemSelectedListener(this);
        studentattendance(u_id);
        studentalldetails(u_id);


    }


    private void studentattendance(final String u_id)
    {




        String tag_string_req = "req_login";
        String url="https://attendanceproject.herokuapp.com/home/apia/"+u_id+"?format=json";
        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Loginnnnnnnnnnnnnnn Response: " + response);
                //Toast.makeText(getBaseContext(), response.toString(),Toast.LENGTH_LONG).show();
                try {
                    JSONObject jObj = new JSONObject(response);
                    data = jObj.getJSONArray("data");

                    for (int i = 0; i < data.length(); i++) {
                        try {
                            //Getting json object
                            JSONObject json = data.getJSONObject(i);

                            //Adding the name of the student to array list
                            students.add(json.getString("date"));


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    ArrayAdapter<String> adapter1 =  new ArrayAdapter<String>(SDashboard.this, R.layout.spinner_item, students) {

                        @Override
                        public View getDropDownView(int position, View convertView,
                                                    ViewGroup parent) {
                            View view = super.getDropDownView(position, convertView, parent);
                            TextView tv = (TextView) view;

                                tv.setTextColor(Color.BLACK);

                            return view;
                        }

                    };
                    adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter1);


                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();

            }
        });


        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

    }


    private void studentalldetails(final String u_id) {

        String tag_string_req = "req_login";
        String url = "https://attendanceproject.herokuapp.com/home/apid" + "/" + u_id + "/?format=json";
        StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response);
                //Toast.makeText(getBaseContext(), response.toString(),Toast.LENGTH_LONG).show();

                try {

                    JSONArray j = new JSONArray(response);
                    for (int i = 0; i < j.length(); i++) {
                        JSONObject jObj = j.getJSONObject(i);
                        String f_name = jObj.getString("first_name");
                        String l_name = jObj.getString("last_name");
                        //String s_class = jObj.getString("s_class");
                        //String sec = jObj.getString("sec");
                        fname.setText("WELCOME " + f_name + " " + l_name);

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
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();

            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(sToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {

        if (this.sDrawerLayout.isDrawerOpen(GravityCompat.START))
        {
            this.sDrawerLayout.closeDrawer(GravityCompat.START);
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.dialog_warning)
                    .setTitle("Closing ATTENDR")
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent a = new Intent(Intent.ACTION_MAIN);
                            a.addCategory(Intent.CATEGORY_HOME);
                            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(a);

                        }

                    })
                    .setNegativeButton("No", null)
                    .show();
        } else
        {
            this.sDrawerLayout.closeDrawer(GravityCompat.START);
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.dialog_warning)
                    .setTitle("Closing ATTENDR")
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent a = new Intent(Intent.ACTION_MAIN);
                            a.addCategory(Intent.CATEGORY_HOME);
                            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(a);

                        }

                    })
                    .setNegativeButton("No", null)
                    .show();
        }

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
    public boolean onNavigationItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.slogout:
                session.setLogin(false);

                db.deleteUsers();
                // Launching the login activity
                Intent intent = new Intent(SDashboard.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;

            case R.id.sprofile:
                // Launching the login activity
                Intent i = new Intent(SDashboard.this, SnProfileView.class);
                startActivity(i);
                return true;
            case R.id.shome:

                // Launching the login activity
                Intent ih = new Intent(SDashboard.this, SDashboard.class);
                startActivity(ih);
                return true;


            case R.id.scontact:
                // Launching the login activity
                Intent ic = new Intent(SDashboard.this, SContactUs.class);
                startActivity(ic);
                return true;

            case R.id.pdf:
                // Launching the login activity
                Intent ip = new Intent(SDashboard.this, SPdf.class);
                startActivity(ip);
                return true;
            default:
                return false;
        }
    }

    //Doing the same with this method as we did with getName()
    private String getdate(int position)
    {
        String date="";
        try {
            //Getting object of given index
            JSONObject json = data.getJSONObject(position);

            //Fetching name from that object
            date = json.getString("date");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Returning the name
        return date;
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, final View view, int i, long l) {
        //final String date= getdate(i));



        final String date=getdate(i);
        String tag_string_req = "req_login";
        pDialog.setMessage("Fetching Attendance ...");
        showDialog();
        String url="https://attendanceproject.herokuapp.com/home/apia/";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>()
        {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "attnd Response: " + response);
                hideDialog();
                //Toast.makeText(getBaseContext(), response.toString(),Toast.LENGTH_LONG).show();
                try {
                    JSONObject jObj = new JSONObject(response);
                    JSONArray data = jObj.getJSONArray("data");

                    for (int i = 0; i < data.length(); i++) {
                        try {
                            //Getting json object
                            JSONObject json = data.getJSONObject(i);

                            String intime = json.getString("in_time");
                            String outtime = json.getString("out_time");
                            String status2 = json.getString("status");
                            s_in.setText("In Time: " + " " + intime);
                            s_out.setText("Out Time: " + " " + outtime);
                            if(status2.equals("0"))
                            {
                                Present.setVisibility(View.VISIBLE);
                                Present.setText("ABSENT");
                                Present.setBackgroundColor(getResources().getColor(R.color.red));
                                Present.setBackground(getResources().getDrawable(R.drawable.abuttonbordershadow));
                                //Toast.makeText(getApplicationContext(),"You are Absent", Toast.LENGTH_SHORT).show();
                            }
                            else if(status2.equals("1"))
                            {
                                Present.setVisibility(View.VISIBLE);
                                Present.setText("PRESENT");
                                Present.setBackgroundColor(getResources().getColor(R.color.green));
                                Present.setBackground(getResources().getDrawable(R.drawable.pbuttonbordershadow));
                                //Toast.makeText(getApplicationContext(),"You are Present", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Present.setVisibility(View.GONE);
                            }
                            //Toast.makeText(getApplicationContext(),
                                   // status2, Toast.LENGTH_LONG).show();

                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

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
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        "Attendance Could not be fetched", Toast.LENGTH_LONG).show();
                hideDialog();

            }
        }){
            protected Map<String, String> getParams() throws AuthFailureError {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("st_id",u_id);
                params.put("date", date);
                return params;
            }
        };


        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

    }


    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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
