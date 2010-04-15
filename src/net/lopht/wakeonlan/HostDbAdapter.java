/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/* 
 * This class was created using the NotesDbAdapter class from the Notepad tutorial
 * at http://d.android.com
 */
package net.lopht.wakeonlan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple hosts database access helper class. Defines the basic CRUD operations
 * and gives the ability to list all notes as well as retrieve or modify a 
 * specific host.
 */
public class HostDbAdapter {

    public static final String KEY_HOSTNAME = "hostname";
    public static final String KEY_MAC = "mac";
    public static final String KEY_IP = "ip";
    public static final String KEY_PORT = "port";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "HostDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE = "create table hosts ("
    	+ KEY_ROWID + " integer primary key autoincrement, "
    	+ KEY_HOSTNAME + " text not null, "
    	+ KEY_MAC + "text not null, "
		+ KEY_IP + " text not null, "
		+ KEY_PORT + " text not null);";

    private static final String DATABASE_NAME = "wakeonlan_data";
    private static final String DATABASE_TABLE = "hosts";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS hosts");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public HostDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the hosts database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public HostDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new host using the hostname, IP,  and MAC provided. If the host is
     * successfully created return the new rowId for that host, otherwise return
     * a -1 to indicate failure.
     * 
     * @param hostname the hostname of the host
     * @param mac the MAC address of the host
     * @param ip the IP address of the host
     * @param port the port to send the packet to
     * @return rowId or -1 if failed
     */
    public long createHost(String hostname, String mac, String ip, String port) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_HOSTNAME, hostname);
        initialValues.put(KEY_MAC, mac);
        initialValues.put(KEY_IP, ip);
        initialValues.put(KEY_PORT, port);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the host with the given rowId
     * 
     * @param rowId id of host to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteHost(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all hosts in the database
     * 
     * @return Cursor over all hosts
     */
    public Cursor fetchAllHosts() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_HOSTNAME,
                KEY_MAC, KEY_IP, KEY_PORT}, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the host that matches the given rowId
     * 
     * @param rowId id of host to retrieve
     * @return Cursor positioned to matching host, if found
     * @throws SQLException if host could not be found/retrieved
     */
    public Cursor fetchHost(long rowId) throws SQLException {

    	Cursor mCursor = mDb.query(true, DATABASE_TABLE,
			new String[] {KEY_ROWID, KEY_HOSTNAME, KEY_MAC, KEY_IP, KEY_PORT},
			KEY_ROWID + "=" + rowId,
			null, null, null, null, null);
    	
    	if (mCursor != null) {
    		mCursor.moveToFirst();
    	}

    	return mCursor;
    }

    /**
     * Update the host using the details provided. The host to be updated is
     * specified using the rowId, and it is altered to use the hostname and ip
     * values passed in
     * 
     * @param rowId id of host to update
     * @param hostname value to set host hostname to
     * @param mac value to set host MAC to
     * @param ip value to set host IP to
     * @param port value to set host port to
     * @return true if the host was successfully updated, false otherwise
     */
    public boolean updateHost(long rowId, String hostname, String mac, String ip, String port) {
        ContentValues args = new ContentValues();
        args.put(KEY_HOSTNAME, hostname);
        args.put(KEY_MAC, mac);
        args.put(KEY_IP, ip);
        args.put(KEY_PORT, port);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
