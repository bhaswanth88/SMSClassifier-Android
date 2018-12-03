package com.verndatech.intellisms;

import android.util.Log;


import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {

    private static final String BASE_URL = "http://sms-nbclassifier.verndatech.com:8080/sms/classify";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    static Gson gson = new Gson();
    public static void classifySMS(SMSObject smsObject, Callback callback) {

        RequestBody body = RequestBody.create(JSON,     gson.toJson(smsObject));

        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(body)
                .build();
        try {
           client.newCall(request).enqueue(callback);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Exception occured.",e.getMessage(),e);
        }
    }

    static OkHttpClient client = new OkHttpClient();


}

