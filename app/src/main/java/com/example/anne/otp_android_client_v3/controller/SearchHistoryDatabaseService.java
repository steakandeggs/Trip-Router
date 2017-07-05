package com.example.anne.otp_android_client_v3.controller;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.example.anne.otp_android_client_v3.model.Contract;
import com.example.anne.otp_android_client_v3.model.SearchHistoryDbHelper;
import com.example.anne.otp_android_client_v3.view.MainActivity;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Anne on 7/5/2017.
 */

public class SearchHistoryDatabaseService {

    private static SearchHistoryDbHelper searchHistoryDbHelper = null;

    private SearchHistoryDatabaseService() {}

    static long addToSearchHistory(MainActivity activity, String fromName, String toName,
                                           LatLng fromCoords, LatLng toCoords,
                                           String modes, long timeStamp) {

        if (searchHistoryDbHelper == null)
            searchHistoryDbHelper = new SearchHistoryDbHelper(activity);

        // Gets the data repository in write mode
        SQLiteDatabase db = searchHistoryDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(Contract.SearchHistoryTable.COLUMN_NAME_FROM_NAME, fromName);
        values.put(Contract.SearchHistoryTable.COLUMN_NAME_TO_NAME, toName);
        values.put(Contract.SearchHistoryTable.COLUMN_NAME_FROM_COORDINATES,
                Double.toString(fromCoords.latitude) + "," + Double.toString(fromCoords.longitude));
        values.put(Contract.SearchHistoryTable.COLUMN_NAME_TO_COORDINATES,
                Double.toString(toCoords.latitude) + "," + Double.toString(toCoords.longitude));
        values.put(Contract.SearchHistoryTable.COLUMN_NAME_MODES, modes);
        values.put(Contract.SearchHistoryTable.COLUMN_NAME_TIMESTAMP, timeStamp);

        // Insert the new row, returning the primary key value of the new row
        return db.insert(Contract.SearchHistoryTable.TABLE_NAME, null, values);

    }

}
