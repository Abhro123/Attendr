package com.edubios.groveus.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;

//import pub.devrel.easypermissions.EasyPermissions;

import static com.edubios.groveus.app.AppController.TAG;

public class SProfileView extends AppCompatActivity {
    private TextView stname;
    //private TextView stlname;
    private TextView stdob;
    private TextView stclass;
    private TextView stsec;
    private TextView stgname;
    private TextView staddress;
    private TextView stphone;
    private JSONArray jsonarray;
    private SQLiteHandler db;
    private SessionManager session;
    public String u_id;
    private static final int SELECT_PICTURE = 0;
    private ImageView imageView;
    private ProgressDialog pDialog;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.tsprofile);
        Bundle bundle = getIntent().getExtras();

        //Extract the data…
        String u_id = bundle.getString("USER_ID");
        CookieManager cookieManager = new CookieManager(new PersistentCookieStore(getApplicationContext()),
                CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        CookieHandler.setDefault(cookieManager);

        stname = (TextView) findViewById(R.id.user_profile_name);
        //stlname=(TextView)findViewById(R.id.st_lname);
        stdob = (TextView) findViewById(R.id.stdob);
        stclass = (TextView) findViewById(R.id.stclass);
        stsec = (TextView) findViewById(R.id.stsec);
        stgname = (TextView) findViewById(R.id.stgurdian);
        stphone = (TextView) findViewById(R.id.stphone);
        staddress = (TextView) findViewById(R.id.staddress);
        imageView = (ImageView) findViewById(R.id.user_profile_photo);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selelctImage();

            }

        });
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        db = new SQLiteHandler(getApplicationContext());

        final HashMap<String, String> user = db.getUserDetails();


        isNetworkConnectionAvailable();
        // session manager
        session = new SessionManager(getApplicationContext());
        // u_id = user.get("u_id");


        String is_staff = user.get("is_staff");
        // Check if user is already logged in or not

        studentprofile(u_id);


    }

    private void selelctImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bitmap bitmap = getPath(data.getData());
            imageView.setImageBitmap(bitmap);
        }
    }

    private Bitmap getPath(Uri uri) {

        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String filePath = cursor.getString(column_index);
        // cursor.close();
        // Convert file path into bitmap image using below line.
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);

        return bitmap;
    }


    private void studentprofile(final String u_id) {

        String tag_string_req = "req_login";
        pDialog.setMessage("Fetching Details...");
        showDialog();

        String url = "https://attendanceproject.herokuapp.com/home/apid" + "/" + u_id + "/?format=json";
        StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response);
                hideDialog();
                //Toast.makeText(getBaseContext(), response.toString(), Toast.LENGTH_LONG).show();

                try {

                    JSONArray j = new JSONArray(response);
                    for (int i = 0; i < j.length(); i++) {
                        JSONObject jObj = j.getJSONObject(i);
                        String st_fname = jObj.getString("first_name");
                        String st_lname = jObj.getString("last_name");
                        String st_dob = jObj.getString("dob");
                        String st_class = jObj.getString("s_class");
                        String st_sec = jObj.getString("sec");
                        String st_phone = jObj.getString("phone");
                        String st_gurdian = jObj.getString("g_name");
                        String st_address = jObj.getString("address");
                        stname.setText(st_fname + " " + st_lname);
                        stclass.setText("Class: " + st_class);
                        stsec.setText("Section: " + st_sec);
                        stdob.setText("Date Of Birth: " + st_dob);
                        stphone.setText("Phone: " + st_phone);
                        stgname.setText("Gurdian's Name: " + st_gurdian);
                        staddress.setText("Address: " + st_address);
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
                hideDialog();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}


