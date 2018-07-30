package com.edubios.groveus;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import com.edubios.groveus.activity.SharedPreference;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static android.content.ContentValues.TAG;



public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

    public static final String TOKEN_BROADCAST = "myfcmtokenbroadcast";

    @Override
    public void onTokenRefresh() {

        String token = FirebaseInstanceId.getInstance().getToken();

        registerToken(token);
        getApplicationContext().sendBroadcast(new Intent(TOKEN_BROADCAST));
        storeToken(token);
    }

    private void registerToken(String token) {

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("Token",token)
                .build();

        Log.d(TAG, "registerToken: "+token);

        Request request = new Request.Builder()
                .url("http://10.0.2.2/gcm/gcm.php")
                .post(body)
                .build();

        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void storeToken(String token) {
        //saving the token on shared preferencec
        SharedPreference.getInstance(getApplicationContext()).storeToken(token);
    }

}
