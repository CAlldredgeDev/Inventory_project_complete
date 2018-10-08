package com.example.android.inventory_project.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.android.inventory_project.data.InventoryContract.InventoryEntry;

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = InventoryDbHelper.class.getSimpleName();
    public static final String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " +
            InventoryEntry.TABLE_NAME + " (" +
            InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            InventoryEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
            InventoryEntry.COLUMN_PRODUCT_PRICE + " INTEGER, " +
            InventoryEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER DEFAULT 0, " +
            InventoryEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, " +
            InventoryEntry.COLUMN_SUPPLIER_PHONE + " INTEGER);";
    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + InventoryEntry.TABLE_NAME;
    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "inventory.db";
    /**
     * Database version.  If the database schema is changed,
     * then this value must increment
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link InventoryDbHelper}.
     *
     * @param context of the app
     */
    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Method for the first time creation of the database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    /**
     * Method to be called when the database is upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
    }
}
