package com.example.android.inventory_project.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import static com.example.android.inventory_project.data.InventoryContract.CONTENT_AUTHORITY;
import static com.example.android.inventory_project.data.InventoryContract.InventoryEntry;
import static com.example.android.inventory_project.data.InventoryContract.PATH_INVENTORY;

public class InventoryProvider extends ContentProvider {

    // Tag for all log messages
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    // URI matcher code for the content URI for the items table
    private static final int ITEMS = 100;

    // URI matcher code for the content URI for a single item in the items table
    private static final int ITEM_ID = 101;

    // UriMatcher object to match a content URI to a corresponding code.
    // The input passed into the constructor represents the code to return for the root URI.
    // It's common to use NO_MATCH as the input for this case.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_INVENTORY, ITEMS);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_INVENTORY + "/#", ITEM_ID);
    }

    private InventoryDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        // Ensure global scope, so it can be referenced from other ContentProvider methods.
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    // Perform the query for the given URI.
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Grab a readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specfic code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // For the ITEMS code, query the item table directly with the given
                // projection, selection, selection args, and sort order. The cursor
                // could contain multiple rows of the items table.
                cursor = database.query(InventoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case ITEM_ID:
                // For the ITEM_ID code, extract out the ID from the URI.
                // For an example URI such as 'content://com.example.android.inventory/item/2',
                // the selection will be '_id=?' and the selection argument will be a
                // String array containing the actual ID of 2 in this case.

                // For every '?' in the selection, we need to have an element in the selection
                // arguments that will fill in the '?'. Since we have 1 question mark in the
                // selection, we have 1 string in the selection args' String array.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the item table where the _id equals 2 to return a
                // Cursor containing that row of the table.
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // Set notification URI on the Cursor,
        // So we know what content URI the Cursor was created for,
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    // CRUD Methods
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        String name = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Item requires a name");
        }

        Integer price = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_PRICE);
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Item requires a valid price");
        }

        Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Item requires a valid quantity");
        }

        String supplierName = values.getAsString(InventoryEntry.COLUMN_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException("Item requires a valid supplier name");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the item
        long id = database.insert(InventoryEntry.TABLE_NAME, null, values);

        // Check to ensure that the entry was good.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the item content URI
        // uri: content:// com.example.android.inventory/inventory
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateInventory(uri, values, selection, selectionArgs);
            case ITEM_ID:
                // For the ITEM_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will bee "_id=?" amd selection
                // arguments will be a String arrray containing the actual ID.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateInventory(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    // Update items in the db with the given content values. Apply the changes to the rows
    // specified in the selection and selection args (which could be 0 or 1 or more items).
    // Return the number of rows that were updated.
    private int updateInventory(Uri uri, ContentValues values, String selection, String[] selectionsArgs) {

        // If the {@Link InventoryEntry#COLUMN_PRODUCT_NAME} key is present,
        // check that the name values is not null.
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Item requires a name");
            }
        }
        // If {@Link InventoryEntry#COLUMN_SUPPLIER_NAME} key is present,
        // check that the gender values is valid.
        if (values.containsKey(InventoryEntry.COLUMN_SUPPLIER_NAME)) {
            String supplierName = values.getAsString(InventoryEntry.COLUMN_SUPPLIER_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("Item requires a supplier name");
            }
        }
        // Values for {@Link InventoryEntry#COLUMN_PRODUCT_PRICE},
        // {@Link InventoryEntry#COLUMN_PRODUCT_QUANTITY}, and
        // {@Link InventoryEntry#COLUMN_SUPPLIER_PHONE}, can be blank but will default to 0 if blank.
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_PRICE)) {
            Integer price = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_PRICE);
            if (price == null || price < 0) {
                price = 0;
                values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, price);
            }
        }
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity == null || quantity < 0) {
                quantity = 0;
                values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);
            }

        }
        if (values.containsKey(InventoryEntry.COLUMN_SUPPLIER_PHONE)) {
            Integer supplierPhone = values.getAsInteger(InventoryEntry.COLUMN_SUPPLIER_PHONE);
            if (supplierPhone == null || supplierPhone < 0) {
                supplierPhone = 0;
                values.put(InventoryEntry.COLUMN_SUPPLIER_PHONE, supplierPhone);
            }
        }

        // If there are no values to update, then we don't update the db.
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, grab writable db to update the data.
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the db and get the number of rows affected.
        int rowsupdated = database.update(InventoryEntry.TABLE_NAME, values, selection, selectionsArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the given
        // URI has changed.
        if (rowsupdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsupdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // Grab writable db
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            // Delete all rows that match the selection and selection args
            // For case ITEMS
            case ITEMS:
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEM_ID:
                // Delete a single row given the ID in the URI
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed.
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    // Returns the MIME type of data for the content URI
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + "with match " + match);
        }
    }
}
