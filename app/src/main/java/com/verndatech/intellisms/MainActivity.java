package com.verndatech.intellisms;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;
    ArrayList<String> smsMessagesList = new ArrayList<>();
    ListView messages;
    //    ArrayAdapter arrayAdapter;
    SimpleAdapter arrayAdapter;
    List<HashMap<String, String>> aList = new ArrayList<HashMap<String, String>>();
    private static final String FRAGMENT_TAG_DATA_PROVIDER = "data provider";
    private static final String FRAGMENT_LIST_VIEW = "list view";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_personal:
                    loadMessages("p");
                    return true;
                case R.id.navigation_finance:
                    loadMessages("f");
                    return true;
                case R.id.navigation_misc:
                    loadMessages("m");
                    return true;
                case R.id.navigation_spam:
                    loadMessages("s");
                    return true;
            }
            return false;
        }
    };

    private void loadMessages(final String smsClass) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (arrayAdapter != null) {
                    final List<SMSObject> smses = manager.fetch(0, 9, smsClass);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            aList.clear();
                            if (smses != null) {
                                for (SMSObject sms : smses) {
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("listview_image", Integer.toString(R.drawable.profile_pc2));
                                    map.put("listview_title", sms.getSmsSender());
                                    map.put("listview_discription", sms.getSmsText());
                                    aList.add(map);
                                }

                            }
                            arrayAdapter.notifyDataSetChanged();
                        }
                    });

                }
            }
        });

    }

    static DBManager manager;
    static BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (manager == null) {
            manager = new DBManager(this);
            manager.open();
        }
        setTitle("IntelliSMS");
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        messages = (ListView) findViewById(R.id.messages);
        String[] from = {"listview_image", "listview_title", "listview_discription"};
        int[] to = {R.id.listview_image, R.id.listview_item_title, R.id.listview_item_short_description};

        arrayAdapter = new SimpleAdapter(getBaseContext(), aList, R.layout.listview_activity, from, to);

        messages.setAdapter(arrayAdapter);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            getPermissionToReadSMS();
        } else {
            // here select item 1 and load item 1 results
            //refreshSmsInbox();
            navigation.setSelectedItemId(R.id.navigation_personal);
        }
    }


    public void getPermissionToReadSMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.RECEIVE_SMS)) {
                Toast.makeText(this, "Please allow permission!", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    READ_SMS_PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_SMS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read SMS permission granted", Toast.LENGTH_SHORT).show();
//                AsyncTask.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        refreshSmsInbox();
//                    }
//                });
            } else {
                Toast.makeText(this, "Read SMS permission denied", Toast.LENGTH_SHORT).show();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void refreshSmsInbox() {
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        int indexDateSent = smsInboxCursor.getColumnIndex("date_sent");


        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        aList.clear();;
        do {

            final SMSObject obj = new SMSObject();
            obj.setSmsText(smsInboxCursor.getString(indexBody));
            obj.setSmsSender(smsInboxCursor.getString(indexAddress));
            obj.setReceivedTime(smsInboxCursor.getLong(indexDateSent));
            obj.setSmsId(UUID.randomUUID().toString());
            HttpUtil.classifySMS(obj, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    obj.setSmsClass(response.message());
                    manager.insert(obj);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("listview_image", Integer.toString(R.drawable.profile_pc2));
                    map.put("listview_title", obj.getSmsSender());
                    map.put("listview_discription", obj.getSmsText());
                    aList.add(map);
                    arrayAdapter.notifyDataSetChanged();

                }
            });
//            obj.setSmsClass(smsClass);
            Log.d("sms added to db", obj.getSmsText() + "::" + obj.getSmsClass());
        } while (smsInboxCursor.moveToNext());
    }

    static MainActivity inst;

    public static MainActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    public void updateInbox(final SMSObject smsObject) {
        manager.insert(smsObject);
        Log.d("sms added to db", smsObject.getSmsText() + "::" + smsObject.getSmsClass());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (navigation != null) {
                    switch (navigation.getSelectedItemId()) {
                        case R.id.navigation_personal:
                            if (smsObject.getSmsClass().equalsIgnoreCase("p")) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put("listview_image", Integer.toString(R.drawable.profile_pc2));
                                map.put("listview_title", smsObject.getSmsSender());
                                map.put("listview_discription", smsObject.getSmsText());
                                aList.add(0, map);
                            }
                            break;
                        case R.id.navigation_finance:
                            if (smsObject.getSmsClass().equalsIgnoreCase("f")) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put("listview_image", Integer.toString(R.drawable.profile_pc2));
                                map.put("listview_title", smsObject.getSmsSender());
                                map.put("listview_discription", smsObject.getSmsText());
                                aList.add(0, map);
                            }
                            break;
                        case R.id.navigation_misc:
                            if (smsObject.getSmsClass().equalsIgnoreCase("m")) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put("listview_image", Integer.toString(R.drawable.profile_pc2));
                                map.put("listview_title", smsObject.getSmsSender());
                                map.put("listview_discription", smsObject.getSmsText());
                                aList.add(0, map);
                            }
                            break;
                        case R.id.navigation_spam:
                            if (smsObject.getSmsClass().equalsIgnoreCase("s")) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put("listview_image", Integer.toString(R.drawable.profile_pc2));
                                map.put("listview_title", smsObject.getSmsSender());
                                map.put("listview_discription", smsObject.getSmsText());
                                aList.add(0, map);
                            }
                            break;
                    }
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        });

    }
}
