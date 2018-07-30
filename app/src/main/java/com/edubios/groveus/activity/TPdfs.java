package com.edubios.groveus.activity;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.edubios.groveus.R;
import com.edubios.groveus.helper.SQLiteHandler;
import com.edubios.groveus.helper.SessionManager;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class TPdfs extends ActionBarActivity implements Response.Listener<byte[]>, Response.ErrorListener, View.OnClickListener, AdapterView.OnItemSelectedListener, NavigationView.OnNavigationItemSelectedListener {
    Button downloadss,downloadcw;
    private DrawerLayout tDrawerLayout;
    private ActionBarDrawerToggle tToggle;
    private DatePickerDialog datePickerDialog1,datePickerDialog2;
    InputStreamVolleyRequest request;
    int count;
    private SQLiteHandler db;
    public String u_id,date,date1,ss,cls,sec;
    private SessionManager session;
    private EditText datepickss,datepickcw,stdid;
    public String filename;
    public String date2;
    private Spinner clss,secs;
    private ArrayList<String> data = new ArrayList<String>();
    private ProgressDialog pDialog;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tpdf);
        tDrawerLayout = (DrawerLayout)findViewById(R.id.tdrawerlayout);
        tToggle = new ActionBarDrawerToggle(this,tDrawerLayout,R.string.drawer_open,R.string.drawer_close);
        tDrawerLayout.addDrawerListener(tToggle);
        tToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigation = (NavigationView) findViewById(R.id.tnavigation);
        navigation.setNavigationItemSelectedListener(this);

        if (shouldAskPermissions()) {
            askPermissions();
        }
        downloadss =(Button)findViewById(R.id.tpdfgeneratess);
        downloadcw =(Button)findViewById(R.id.tpdfgeneratecw);
        datepickss=(EditText)findViewById(R.id.datePickerts);
        datepickcw=(EditText)findViewById(R.id.datePickertc);
        stdid=(EditText)findViewById(R.id.studentid);
        db = new SQLiteHandler(getApplicationContext());
        //TextView selectsingle=(TextView) findViewById(R.id.selectsingle);
        //TextView selectclass=(TextView) findViewById(R.id.selectclass);
        datepickss.setVisibility(View.VISIBLE);
        stdid.setVisibility(View.VISIBLE);
        downloadss.setVisibility(View.VISIBLE);
        //selectsingle.setVisibility(View.VISIBLE);

        final HashMap<String, String> user = db.getUserDetails();
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);


        //Set listeners of views
        setViewActions();

        //Create DatePickerDialog to show a calendar to user to select birthdate
        prepareDatePickerDialog();
        // session manager
        session = new SessionManager(getApplicationContext());
        u_id = user.get("u_id");




        downloadss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ss=stdid.getText().toString().trim();
                //Toast.makeText(getApplicationContext(),
                        //ss, Toast.LENGTH_LONG).show();

                //Change your url below
                pDialog.setMessage("Downloading PDF...");
                showDialog();

                String mUrl="https://attendanceproject.herokuapp.com/home/pdf/";
                try {
                    request = new InputStreamVolleyRequest(Request.Method.POST, mUrl, TPdfs.this, TPdfs.this, (HashMap<String, String>) getParams());
                } catch (AuthFailureError authFailureError) {
                    authFailureError.printStackTrace();
                }
                RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext(),
                        new HurlStack());
                mRequestQueue.add(request);
            }
        });



        isNetworkConnectionAvailable();

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        cls = clss.getSelectedItem().toString();
        sec = secs.getSelectedItem().toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        cls="";
        sec="";
    }


    private void setViewActions()
    {
        datepickss.setOnClickListener(this);
        // submit.setOnClickListener(this);
        //datepickcw.setOnClickListener(this);
    }

    private void prepareDatePickerDialog() {
        //Get current date
        Calendar calendar=Calendar.getInstance();

        //Create datePickerDialog with initial date which is current and decide what happens when a date is selected.
        datePickerDialog1 = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
            {
                monthOfYear+=1;
                String mt;
                if(monthOfYear<10)
                    mt="0"+monthOfYear;
                else
                    mt=String.valueOf(monthOfYear);
                //When a date is selected, it comes here.
                //Change birthdayEdittext's text and dismiss dialog.
                DateFormat df = new SimpleDateFormat("yy");
                //DateFormat dm = new SimpleDateFormat("00");
                String formattedDatey = df.format(Calendar.getInstance().getTime());
                //String formattedDatem = dm.format(Calendar.getInstance().getTime());
                datepickss.setTextColor(Color.GRAY);
                datepickss.setText("PDF for month and year: "+mt+"/"+formattedDatey);
                date1=mt+"/"+formattedDatey;
                date=dayOfMonth+"/"+mt+"/"+formattedDatey;
                date2=dayOfMonth+mt+formattedDatey;
                //datePickerDialog.dismiss();
            }
        },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));




    }
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.datePickerts:
                datePickerDialog1.show();
                break;

        }}

    @Override
    public void onResponse(byte[] response) {
        hideDialog();
        HashMap<String, Object> map = new HashMap<String, Object>();
        try {
            if (response!=null) {



                //Read file name from headers
                String content =request.responseHeaders.get("Content-Disposition").toString();
                StringTokenizer st = new StringTokenizer(content, "=");
                String[] arrTag = st.toArray();

                filename = u_id+date2+".pdf";
                filename = filename.replace(":", ".");
                Log.d("DEBUG::RESUME FILE NAME", filename);
                Toast.makeText(TPdfs.this, "PDF file downloaded at:"+filename , Toast.LENGTH_LONG).show();

                try{
                    long lenghtOfFile = response.length;

                    //covert reponse to input stream
                    InputStream input = new ByteArrayInputStream(response);
                    File path = Environment.getExternalStorageDirectory() ;
                    File file = new File(path+"/Download", filename);

                    map.put("resume_path", file.toString());
                    BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));
                    byte data[] = new byte[1024];

                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        output.write(data, 0, count);
                    }

                    output.flush();

                    output.close();
                    input.close();
                }catch(IOException e){
                    e.printStackTrace();

                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE");
            e.printStackTrace();
        }
        Intent i = new Intent(this, ViewPdf.class);
        //viewPdf(filename);
        //Create the bundle
        Bundle bundle = new Bundle();
//Add your data from getFactualResults method to bundle
        bundle.putString("FILE", filename);
        bundle.putString("UID", ss);
        bundle.putString("DATE", date1);
//Add the bundle to the intent
        i.putExtras(bundle);
        startActivity(i);
    }
    @Override
    public void onErrorResponse(VolleyError error) {
        // Log.e(TAG, "Login Err: " + error.getMessage());
        Toast.makeText(getApplicationContext(),
                error.getMessage(), Toast.LENGTH_LONG).show();
        hideDialog();
    }
    //@Override
    protected Map<String, String> getParams() throws AuthFailureError {
        // Posting parameters to login url
        Map<String, String> params = new HashMap<>();
        //params.put("c_name", cname);
        //params.put("c_subject", cmessage);
        params.put("uid", ss);
        params.put("date",date);
        //params.put("class_id",cls);
        //params.put("sec_id",sec);
        return params;
    }

    // Method for opening a pdf file
   /* private void viewPdf(String file) {

        File pdfFile = new File(Environment.getExternalStorageDirectory() +  "/" + file);
        Uri path = Uri.fromFile(pdfFile);

        // Setting the intent for pdf reader
        Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
        pdfIntent.setDataAndType(path, "application/pdf");
        pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        try {
            startActivity(pdfIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(TPdfs.this, "Can't read pdf file", Toast.LENGTH_SHORT).show();
        }
    }*/
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(tToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.slogout:
                session.setLogin(false);

                db.deleteUsers();

                // Launching the login activity
                Intent intent = new Intent(TPdfs.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;

            case R.id.tprofile:

                // Launching the login activity
                Intent i = new Intent(TPdfs.this, TProfileView.class);
                startActivity(i);
                return true;

            case R.id.thome:

                // Launching the login activity
                Intent ih = new Intent(TPdfs.this, TDashboard.class);
                startActivity(ih);
                return true;

            case R.id.tcontact:

                // Launching the login activity
                Intent ic = new Intent(TPdfs.this, TContactUs.class);
                startActivity(ic);
                return true;

            case R.id.single:

                // Launching the login activity
                Intent is = new Intent(TPdfs.this, TPdfs.class);
                startActivity(is);
                return true;

            case R.id.classwise:

                // Launching the login activity
                Intent icc = new Intent(TPdfs.this, TPdfc.class);
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
