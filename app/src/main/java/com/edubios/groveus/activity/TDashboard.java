package com.edubios.groveus.activity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.edubios.groveus.R;
import com.edubios.groveus.app.AppConfig;
import com.edubios.groveus.app.AppController;
import com.edubios.groveus.helper.SQLiteHandler;
import com.edubios.groveus.helper.SessionManager;
import com.itextpdf.text.pdf.AcroFields;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TDashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,View.OnClickListener, MaterialSpinner.OnItemSelectedListener {

    private DrawerLayout tDrawerLayout;
    private ActionBarDrawerToggle tToggle;
    private SQLiteHandler db;
    private SessionManager session;
    public String u_id;
    public TextView listname;
    public TextView listintime;
    public TextView listouttime;
    private ArrayList<String> data = new ArrayList<String>();
    private ListView lv;
    private JSONArray data1;
    private JSONArray data2;
    private Switch mySwitch = null;
    private static final String TAG = LoginActivity.class.getSimpleName();
    public String clas, secs, date, date1, date2;
    private String[] status = new String[10];
    private EditText datepicktd, inputSearch;
    private MaterialSpinner s1, s2;
    private DatePickerDialog datePickerDialog3;
    private Button getattend;
    private ProgressDialog pDialog;
    private int i;
    private ImageView tool;

    // Listview Adapter
    ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tdashboard);

        tDrawerLayout = (DrawerLayout) findViewById(R.id.tdrawerlayout);
        tToggle = new ActionBarDrawerToggle(this, tDrawerLayout, R.string.drawer_open, R.string.drawer_close);
        tDrawerLayout.addDrawerListener(tToggle);
        tToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigation = (NavigationView) findViewById(R.id.tnavigation);
        navigation.setNavigationItemSelectedListener(this);
        inputSearch = (EditText) findViewById(R.id.inputSearch);
        listname = (TextView) findViewById(R.id.list_item_text);
        listintime = (TextView) findViewById(R.id.intimelist);
        listouttime = (TextView) findViewById(R.id.outtimelist);
        datepicktd = (EditText) findViewById(R.id.datePickertd);
        getattend = (Button) findViewById(R.id.getattendance);
        mySwitch = (Switch) findViewById(R.id.switch1);
        tool=(ImageView) findViewById(R.id.tooltip);
        // mySwitch.setOnCheckedChangeListener(TDashboard.this);

        CookieManager cookieManager = new CookieManager(new PersistentCookieStore(getApplicationContext()),
                CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        CookieHandler.setDefault(cookieManager);

        db = new SQLiteHandler(getApplicationContext());

        final HashMap<String, String> user = db.getUserDetails();

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);


        // session manager
        session = new SessionManager(getApplicationContext());
        u_id = user.get("u_id");


            String[] arraySpinner1 = new String[]{
                    "Select Class", "X", "XI"
            };
            s1 = (MaterialSpinner) findViewById(R.id.spinner1);
            ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_dropdown_item, arraySpinner1) {
                @Override
                public boolean isEnabled(int position) {
                    if (position == 0) {
                        // Disable the first item from Spinner
                        // First item will be use for hint
                        return false;
                    } else {
                        return true;
                    }
                }

                @Override
                public View getDropDownView(int position, View convertView,
                                            ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    TextView tv = (TextView) view;
                    if (position == 0) {
                        // Set the hint text color gray
                        tv.setTextColor(Color.GRAY);
                    } else {
                        tv.setTextColor(Color.BLACK);
                    }
                    return view;
                }
            };

            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            s1.setAdapter(adapter1);


            String[] arraySpinner2 = new String[]{
                    "Select Section", "A", "B"
            };
            s2 = (MaterialSpinner) findViewById(R.id.spinner2);
            ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_dropdown_item, arraySpinner2) {
                @Override
                public boolean isEnabled(int position) {
                    if (position == 0) {
                        // Disable the first item from Spinner
                        // First item will be use for hint
                        return false;
                    } else {
                        return true;
                    }
                }

                @Override
                public View getDropDownView(int position, View convertView,
                                            ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    TextView tv = (TextView) view;
                    if (position == 0) {
                        // Set the hint text color gray
                        tv.setTextColor(Color.BLACK);
                    } else {
                        tv.setTextColor(Color.GRAY);
                    }
                    return view;
                }
            };


            adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            s2.setAdapter(adapter2);


            s1.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
                @Override
                public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                    clas = item;

                }

            });
            s2.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
                @Override
                public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                    secs = item;

                }

            });


            lv = (ListView) findViewById(R.id.listview);
            //generateListContent();
            //lv.setAdapter(new MyListAdaper(this, R.layout.list_item, data));
            //Doing the same with this method as we did with getName()

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Toast.makeText(TDashboard.this, "Details for student  " + getid(position), Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(TDashboard.this, SProfileView.class);
                    Bundle bundle = new Bundle();
                    //Add your data from getFactualResults method to bundle
                    bundle.putString("USER_ID", getid(position));
                    //Add the bundle to the intent
                    i.putExtras(bundle);

                    //Fire the second activity
                    startActivity(i);


                }
            });

            setViewActions();

            prepareDatePickerDialog();

            inputSearch.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                    // When user changed the Text
                    TDashboard.this.adapter.getFilter().filter(cs);
                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                              int arg3) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void afterTextChanged(Editable arg0) {
                    // TODO Auto-generated method stub
                }
            });

            isNetworkConnectionAvailable();


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


    private void setViewActions() {
        datepicktd.setOnClickListener(this);
        // submit.setOnClickListener(this);
        //datepickcw.setOnClickListener(this);
    }

    private void prepareDatePickerDialog() {
        //Get current date
        Calendar calendar = Calendar.getInstance();

        //Create datePickerDialog with initial date which is current and decide what happens when a date is selected.
        datePickerDialog3 = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                monthOfYear += 1;
                String mt, dt;
                if (monthOfYear < 10)
                    mt = "0" + monthOfYear;

                else
                    mt = String.valueOf(monthOfYear);
                if (dayOfMonth < 10)
                    dt = "0" + dayOfMonth;

                else
                    dt = String.valueOf(dayOfMonth);
                //When a date is selected, it comes here.
                //Change birthdayEdittext's text and dismiss dialog.
                DateFormat df = new SimpleDateFormat("yy");
                //DateFormat dm = new SimpleDateFormat("00");
                String formattedDatey = df.format(Calendar.getInstance().getTime());
                //String formattedDatem = dm.format(Calendar.getInstance().getTime());
                datepicktd.setTextColor(Color.GRAY);
                datepicktd.setText(dt + "/" + mt + "/" + formattedDatey);

                date1 = mt + "/" + formattedDatey;
                date = dt + "/" + mt + "/" + formattedDatey;
                date2 = dt + mt + formattedDatey;
                //datePickerDialog.dismiss();
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));


    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.datePickertd:
                datePickerDialog3.show();
                break;

        }
    }


    public void onclick2(View view) {

        getattendance(clas, secs, date);
    }


    public void getattendance(final String clas, final String secs, final String date) {

        String tag_string_req = "req_login";
        pDialog.setMessage("Fetching Attendance...");
        showDialog();
        data.clear();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_CREATEPWD, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                inputSearch.setVisibility(View.VISIBLE);
                tool.setVisibility(View.VISIBLE);

                hideDialog();
                Log.d(TAG, "Attend Response: " + response);
                try {


                    JSONObject jObj = new JSONObject(response);
                    data2 = jObj.getJSONArray("name");
                    data1 = jObj.getJSONArray("data");
                    if(data1.length()==0) {
                        inputSearch.setVisibility(View.GONE);
                        tool.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "No attendance for " + date + ". Holiday or Wrong Date Input", Toast.LENGTH_LONG).show();
                    }//String msg="";
                    //String u_id = jObj.getString("uid");

                    for (i = 0; i < data1.length(); i++) {
                        try {
                            //Getting json object
                            //JSONObject json = data1.getJSONObject(i);
                            JSONObject json1 = data1.getJSONObject(i);

                            String s_id = json1.getString("st_id");


                            status[i] = json1.getString("status");
                            Log.d(TAG, "Array Response: " + status[i]);

                            //Toast.makeText(getApplicationContext(), status, Toast.LENGTH_LONG).show();
                            for (int j = 0; j < data2.length(); j++) {
                                JSONObject json2 = data2.getJSONObject(j);
                                String ss_id = json2.getString("st_id");
                                if (ss_id.equals(s_id)) {
                                    data.add(json2.getString("first_name") + " " + json2.getString("last_name"));
                                }
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    adapter = new MyListAdaper(TDashboard.this, R.layout.list_item, data);
                    lv.setAdapter(adapter);

                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
                //Log.e(TAG, "Login Err: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        "Fill all the details", Toast.LENGTH_LONG).show();

            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                //String clas="X";
                //String secs="A";
                // String date="03/05/18";
                params.put("h_class", clas);
                params.put("h_sec", secs);
                params.put("h_date", date);
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    private String getname(int position) {
        String id = "";

        try {
            //Getting json object
            //JSONObject json = data1.getJSONObject(i);
            JSONObject json1 = data1.getJSONObject(position);

            String s_id = json1.getString("st_id");
            //Toast.makeText(getApplicationContext(), status, Toast.LENGTH_LONG).show();
            for (int j = 0; j < data2.length(); j++) {
                JSONObject json2 = data2.getJSONObject(j);
                String ss_id = json2.getString("st_id");
                if (ss_id.equals(s_id)) {
                    id = json2.getString("first_name");
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Returning the name
        return id;
    }

    private String getid(int position) {
        String id = "";
        try {
            //Getting object of given index
            JSONObject json = data1.getJSONObject(position);

            //Fetching name from that object
            id = json.getString("st_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Returning the name
        return id;
    }

    @Override
    public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {

    }


    private class MyListAdaper extends ArrayAdapter<String> {
        private int layout;
        private List<String> mObjects;

        private MyListAdaper(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            mObjects = objects;
            layout = resource;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder mainViewholder = null;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);
                ViewHolder viewHolder = new ViewHolder();
                //viewHolder.thumbnail = (ImageView) convertView.findViewById(R.id.list_item_thumbnail);
                viewHolder.title = (TextView) convertView.findViewById(R.id.list_item_text);
                viewHolder.listintime = (TextView) convertView.findViewById(R.id.intimelist);
                viewHolder.listouttime = (TextView) convertView.findViewById(R.id.outtimelist);
                viewHolder.mySwitch = (Switch) convertView.findViewById(R.id.switch1);

                convertView.setTag(viewHolder);
            }
            mainViewholder = (ViewHolder) convertView.getTag();
            Log.d(TAG, "position Response: " + position);

            if (position != i) {
                //final ViewHolder finalMainViewholder = mainViewholder;
                if (status[position].equals("0"))
                    mainViewholder.mySwitch.setChecked(false);
                else if (status[position].equals("1")) {
                    mainViewholder.mySwitch.setChecked(true);
                }
            }

            mainViewholder.mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //Toast.makeText(getContext(), "Changing Attendance for" + getid(position), Toast.LENGTH_SHORT).show();


                    if (isChecked) {
                        // do something when check is selected
                        giveattendance("1", date);
                    } else {
                        //do something when unchecked
                        giveattendance("0", date);
                    }
                    //Toast.makeText(getContext(), "Status" + status[position], Toast.LENGTH_SHORT).show();

                }

                public void giveattendance(final String status, final String date) {

                    String tag_string_req = "req_login";
                    pDialog.setMessage("Attendance Changed for " + getname(position));
                    showDialog();

                    StringRequest strReq = new StringRequest(Request.Method.POST,
                            AppConfig.URL_REGISTER, new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            hideDialog();
                            //  Toast.makeText(getApplicationContext(), SharedPreference.getInstance(getApplicationContext()).getToken(), Toast.LENGTH_LONG).show();

                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //Log.e(TAG, "Login Err: " + error.getMessage());
                            Toast.makeText(getApplicationContext(),
                                    error.getMessage(), Toast.LENGTH_LONG).show();

                        }
                    }) {

                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            // Posting parameters to login url
                            Map<String, String> params = new HashMap<>();
                            //String h_class = "X";
                            String notif = "2";
                            //String h_date = "26/03/18";
                            params.put("st_id", getid(position));
                            params.put("date", date);
                            params.put("status", status);
                            params.put("notif_s", notif);
                            return params;
                        }
                    };

                    // Adding request to request queue
                    AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
                }
            });

            SpannableString content = new SpannableString("\u2022 " + getItem(position));
            content.setSpan(new UnderlineSpan(), 2, content.length(), 0);

            mainViewholder.title.setText(
                    content);
            String intime = "", outtime = "";

            try {
                //Getting json object
                //JSONObject json = data1.getJSONObject(i);
                JSONObject json1 = data1.getJSONObject(position);

                String s_id = json1.getString("st_id");
                //Toast.makeText(getApplicationContext(), status, Toast.LENGTH_LONG).show();
                for (int j = 0; j < data2.length(); j++) {
                    JSONObject json2 = data2.getJSONObject(j);
                    String ss_id = json2.getString("st_id");
                    if (ss_id.equals(s_id)) {
                        intime = json1.getString("in_time");
                        outtime = json1.getString("out_time");
                        mainViewholder.listintime.setText("IN: " + intime);
                        mainViewholder.listouttime.setText("OUT: " + outtime);
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }


            return convertView;
        }
    }

    public class ViewHolder {

        //ImageView thumbnail;
        TextView title, listintime, listouttime;
        Switch mySwitch;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (tToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (this.tDrawerLayout.isDrawerOpen(GravityCompat.START))
        {
            this.tDrawerLayout.closeDrawer(GravityCompat.START);
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
            this.tDrawerLayout.closeDrawer(GravityCompat.START);
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.slogout:
                session.setLogin(false);

                db.deleteUsers();

                // Launching the login activity
                Intent intent = new Intent(TDashboard.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;

            case R.id.tprofile:

                // Launching the login activity
                Intent i = new Intent(TDashboard.this, TProfileView.class);
                startActivity(i);
                return true;
            case R.id.thome:

                // Launching the login activity
                Intent ih = new Intent(TDashboard.this, TDashboard.class);
                startActivity(ih);
                return true;

            case R.id.tcontact:

                // Launching the login activity
                Intent ic = new Intent(TDashboard.this, TContactUs.class);
                startActivity(ic);
                return true;

            case R.id.single:

                // Launching the login activity
                Intent is = new Intent(TDashboard.this, TPdfs.class);
                startActivity(is);
                return true;

            case R.id.classwise:

                // Launching the login activity
                Intent icc = new Intent(TDashboard.this, TPdfc.class);
                startActivity(icc);
                return true;

            default:
                tDrawerLayout = (DrawerLayout) findViewById(R.id.tdrawerlayout);
                tDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
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
