package com.verndatech.intellisms;

import android.content.Context;
import android.database.SQLException;
import android.util.Log;

import org.dizitart.no2.FindOptions;
import org.dizitart.no2.IndexOptions;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.SortOrder;
import org.dizitart.no2.filters.Filters;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;

import java.util.List;

public class DBManager {


    private Context context;

    private Nitrite database;
    ObjectRepository<SMSObject> repository;

    public DBManager(Context c) {
        context = c;
    }

    public void open() throws SQLException {
        database = Nitrite.builder()
                .compressed()
                .filePath(context.getFilesDir().getPath() + "/smsclassifiedv2.db")
                .openOrCreate("user", "password");
        repository = database.getRepository(SMSObject.class);
        repository.dropAllIndices();
        repository.createIndex("receivedTime", IndexOptions.indexOptions(IndexType.NonUnique));
    }

    public void close() {
        database.close();
    }

    public void insert(SMSObject object) {
        repository.insert(object);
    }

    public List<SMSObject> fetch(int offset, int size, String category) {
        Cursor<SMSObject> cursor = repository.find(ObjectFilters.eq("smsClass",category),FindOptions.sort("receivedTime", SortOrder.Descending).thenLimit(offset, size));
       List<SMSObject> list=cursor.toList();
        Log.d("sms classified fetch for"+category,"total found;;"+list.size());
        return list;
    }

}