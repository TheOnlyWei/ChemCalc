package com.wei.ChemCalc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;
import android.content.ContentProviderOperation;

/*
 *  content provider is implemented as a subclass of ContentProvider class and 
 *  must implement a standard set of APIs that enable other applications to 
 *  perform transactions.
 *
 *
 *  Every time you change something you must delete the app or update version number.
 */
/*

Last Modified by Wei Shi 12/07/2015

*/


public class PeriodicTable extends ContentProvider {

    private List<Element> elements;

    //specify the query string in the form of a URI
    static final String PROVIDER_NAME = "com.wei.ChemCalc.PeriodicTable";
    static final String URL = "content://" + PROVIDER_NAME + "/elements";
    static final Uri CONTENT_URI = Uri.parse(URL);
    //content_URI tells the contentresolver what data to fetch.
    static final String ATOMIC_NUMBER = "atomicNumber";
    static final String SYMBOL = "symbol";
    static final String NAME = "name";
    static final String MOLAR_MASS = "molarMass";

    static final String NULL = "null";
    static final Uri URI_NULL = Uri.parse(NULL);

    private static HashMap<String, String> STUDENTS_PROJECTION_MAP;

    static final int ELEMENTS = 1;
    static final int ELEMENT_ID = 2;

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "elements", ELEMENTS);
        uriMatcher.addURI(PROVIDER_NAME, "elements/#", ELEMENT_ID);
    }

    /**
     * Database specific constant declarations
     */
    private SQLiteDatabase db;
    static final String DATABASE_NAME = "PeriodicTable";
    static final String ELEMENTS_TABLE_NAME = "elements";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_DB_TABLE =
            " CREATE TABLE " + ELEMENTS_TABLE_NAME +
                    " ( atomicNumber INTEGER PRIMARY KEY ASC, " +
                    " symbol TEXT NOT NULL, " +
                    " name TEXT NOT NULL, " +
                    " molarMass TEXT NOT NULL);";

    /**
     * Helper class that actually creates and manages
     * the provider's underlying data repository.
     */
    private class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);

        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(CREATE_DB_TABLE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + ELEMENTS_TABLE_NAME);

            //This method is called when the provider is started.
            onCreate(db);

        }

    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();

        Cursor c = query(CONTENT_URI, null, null, null, PeriodicTable.ATOMIC_NUMBER);
        if(!c.moveToFirst()){
            parse();

        }
        c.close();
        return (db == null)? false:true;
    }

    public void parse(){
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        db = dbHelper.getWritableDatabase();
        if(db != null){
            try {
                XmlPullParserHandler parser = new XmlPullParserHandler();
                InputStream is = context.getAssets().open("elements.xml");
                elements = parser.parse(is);

            } catch (IOException e) {e.printStackTrace();}

            try{
                ArrayList<ContentProviderOperation> operations = new ArrayList<>();
                //ContentValues[] values = new ContentValues[elements.size()];
                db.beginTransaction();
                for(int i=0; i<elements.size(); i++){
                    ContentValues v = new ContentValues();
                    Element current = elements.get(i);
                    v.put(ATOMIC_NUMBER, current.getAtomicNumber());
                    v.put(SYMBOL, current.getSymbol());
                    v.put(NAME, current.getName());
                    v.put(MOLAR_MASS, current.getMolarMass());
                    //System.out.println("@@@@@@@@@@@@@" + v.get(NAME));
                    operations.add(ContentProviderOperation.newInsert(CONTENT_URI).withValues(v).build());

                }
                db.setTransactionSuccessful();
                applyBatch(operations);

            }catch (final OperationApplicationException e1){
            }
            finally {
                db.endTransaction();
            }
        }


    }
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /**
         * Add a new student record
         */
        long rowID = db.insert(	ELEMENTS_TABLE_NAME, "", values);
        /**
         * If record is added successfully
         */
        if (rowID > 0)
        {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            //This notifies CursorAdapter that changes has been made to the database.
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        else Toast.makeText(getContext(), "ROW ID: " + rowID, Toast.LENGTH_SHORT).show();

        return URI_NULL;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(ELEMENTS_TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case ELEMENTS:
                qb.setProjectionMap(STUDENTS_PROJECTION_MAP);
                break;
            case ELEMENT_ID:
                qb.appendWhere( ATOMIC_NUMBER + "=" + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (sortOrder == null || sortOrder.equals("")){
            /**
             * By default sort on student names
             */
            sortOrder = NAME;
        }
        Cursor c = qb.query(db,	projection,	selection, selectionArgs,
                null, null, sortOrder);
        /**
         * register to watch a content URI for changes
         * This means it updates what the cursor points to if the database is changed in the background.
         */
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)){
            case ELEMENTS:
                count = db.delete(ELEMENTS_TABLE_NAME, selection, selectionArgs);
                break;
            case ELEMENT_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete( ELEMENTS_TABLE_NAME, ATOMIC_NUMBER +  " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" +
                                selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)){
            case ELEMENTS:
                count = db.update(ELEMENTS_TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            case ELEMENT_ID:
                count = db.update(ELEMENTS_TABLE_NAME, values, ATOMIC_NUMBER +
                        " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" +
                                selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
/*
The MIME types returned by ContentProvider.getType have two distinct parts:

type/subType
The type portion indicates the well known type that is returned for a given URI by the ContentProvider, as the query methods can only return Cursors the type should always be:

vnd.android.cursor.dir // for when you expect the Cursor to contain 0..x items
// or
vnd.android.cursor.item // for when you expect the Cursor to contain 1 item
The subType portion can be either a well known subtype or something unique to your application.

So when using a ContentProvider you can customize the second subType portion of the MIME type, but not the first portion. e.g a valid MIME type for your apps ContentProvider could be:

vnd.android.cursor.dir/vnd.myexample.whatever
The MIME type returned from a ContentProvider can be used by an Intent to determine which activity to launch to handle the data retrieved from a given URI.


 */
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            /**
             * Get all student records
             */
            case ELEMENTS:
                return "vnd.android.cursor.dir/vnd.example.elements";
            /**
             * Get a particular student
             */
            case ELEMENT_ID:
                return "vnd.android.cursor.item/vnd.example.elements";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
}
