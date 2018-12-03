package com.verndatech.intellisms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    public static final String SMS_BUNDLE = "pdus";

    public void onReceive(Context context, Intent intent) {
        Log.d("receveid sms", "have to process it");

        Bundle intentExtras = intent.getExtras();

        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String smsMessageStr = "";
            String address=null;
            long timestamp=0L;
            for (int i = 0; i < sms.length; ++i) {
                String format = intentExtras.getString("format");
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i], format);

                String smsBody = smsMessage.getMessageBody().toString();
                address = smsMessage.getOriginatingAddress();

//                smsMessageStr += "SMS From: " + address + "\n";
                smsMessageStr += smsBody + "\n";
                timestamp= smsMessage.getTimestampMillis();
            }

            Log.d("receveid sms", smsMessageStr);
            final SMSObject smsObject= new SMSObject();
            smsObject.setSmsSender(address);
            smsObject.setSmsText(smsMessageStr);
            smsObject.setReceivedTime(timestamp);
            HttpUtil.classifySMS(smsObject, new Callback() {
               @Override
               public void onFailure(Call call, IOException e) {

               }

               @Override
               public void onResponse(Call call, Response response) throws IOException {

                   smsObject.setSmsClass(response.body().string());
                   MainActivity inst = MainActivity.instance();
                   inst.updateInbox(smsObject);
               }
           });

        }
    }
}