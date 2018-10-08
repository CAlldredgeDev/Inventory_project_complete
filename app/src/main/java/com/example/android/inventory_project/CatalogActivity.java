package com.example.android.inventory_project;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventory_project.data.InventoryContract.InventoryEntry;
import com.example.android.inventory_project.data.InventoryDbHelper;
import com.example.android.inventory_project.data.InventoryProvider;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Tag for all log messages
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();
    private static final int ITEM_LOADER = 0;

    InventoryCursorAdapter mCursorAdapter;

    /**
     * Database helper that will provide handle to database
     */
    private InventoryDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with item data
        ListView itemListView = (ListView) findViewById(R.id.list_view_item);

        // Find and set empty view on the Listview, so that it only shows when the list
        // has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        itemListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of item data in the Cursor.
        // There is no item data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new InventoryCursorAdapter(this, null);
        itemListView.setAdapter(mCursorAdapter);

        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                // Need a new URI that appends the id to create the specific ID for the
                // item we clicked on.
                Uri currentItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                // This is how we add the Uri to the intent.
                intent.setData(currentItemUri);
                startActivity(intent);
            }
        });
        // Kick off the loader
        getLoaderManager().initLoader(ITEM_LOADER, null, this);
    }

    // Create the options menu that will house the navigation of our app.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void insertItem() {
        // Need a ContentValues object to store the column names
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, "Kraken V2");
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, 20);
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, 1);
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, "Razer");
        values.put(InventoryEntry.COLUMN_SUPPLIER_PHONE, R.string.generic_phone);

        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
    }

    // Sell an Item.
    public void sellItem(int itemId, int quantity) {
        // If quantity more than zero, reduce it
        if (quantity > 0) {
            quantity--;

            // Create new URI and ContentValues objects to store new values.
            Uri sellUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, itemId);
            ContentValues values = new ContentValues();
            values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);
            int rowsUpdated = getContentResolver().update(
                    sellUri,
                    values,
                    null,
                    null);
            if (rowsUpdated == 0) {
                // If no rows were updated, there was an error with the sale, and we need to show
                // a toast about it.
                Toast.makeText(this, R.string.sale_item_error, Toast.LENGTH_LONG).show();
            } else {
                // Otherwise, the sale was good and we can show a toast for that.
                Toast.makeText(this, R.string.sale_item_ok, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, R.string.sale_item_big_error, Toast.LENGTH_LONG).show();
        }
    }

    // Actions for the Options menu.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.insert:
                insertItem(); // Inserts the sample object into the database for testing.
                return true;
            case R.id.action_delete_all_entries:
                deleteAllItems();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        Log.v(LOG_TAG, rowsDeleted + getString(R.string.rows_deleted_from_db));
    }


    // Refresh the UI whenever the app is started.
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@Link #InventoryCursorAdapter} with this new cursor containing update item data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
