package com.example.android.inventory_project;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventory_project.data.InventoryContract.InventoryEntry;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    // Member Variables go here
    public static final String LOG_TAG = EditorActivity.class.getSimpleName();
    private static final int EXISTING_ITEM_LOADER = 0;
    // Uri stuff
    private Uri mCurrentItemUri;
    // Edit Fields
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneEditText;


    // Status flags
    // Boolean flag that keeps track of whether the item has been updated (true) or not (false)
    private boolean mItemHasUpdate = false;

    // Helper Method for listening for changes from the user
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasUpdate = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Get the intent that was used to launch the activity,
        // in order to figure out if we're creating a new item or editing an existing one.
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        // If the intent DOES NOT contain an item URI, then we know that we are
        // creating a new item.
        if (mCurrentItemUri == null) {
            // This is a new item, so change the app bar to say 'Add Item'
            setTitle(R.string.add_new_item);
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing itme, so change app bar to say "Edit Item"
            setTitle(R.string.edit_item);

            // Init a loader to read the item data from the db
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        // Find all relevant view that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_item_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_item_quantity);
        mSupplierNameEditText = (EditText) findViewById(R.id.edit_supplier_name);
        mSupplierPhoneEditText = (EditText) findViewById(R.id.edit_supplier_phone);
        Button mDecreaseItemCount = (Button) findViewById(R.id.button_decrease_quantity);
        // Other UI items
        Button mIncreaseItemCount = (Button) findViewById(R.id.button_increase_quantity);

        // Setup an InputFilter to only allow numeric entry into certain fields.
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
                                       int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isDigit(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };

        mPriceEditText.setFilters(new InputFilter[]{filter});
        mQuantityEditText.setFilters(new InputFilter[]{filter});
        mSupplierPhoneEditText.setFilters(new InputFilter[]{filter});
        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let up know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);


        // Button to decrease Item Count in UI
        mDecreaseItemCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentQuantity = mQuantityEditText.getText().toString();
                int updateQuantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());

                if (Integer.valueOf(currentQuantity) != 0) {
                    mQuantityEditText.setText(String.valueOf(updateQuantity - 1));
                }
                if (Integer.valueOf(currentQuantity) == 0 || TextUtils.isEmpty(currentQuantity)) {
                    Toast.makeText(EditorActivity.this, R.string.cannot_be_zero, Toast.LENGTH_SHORT).show();
                    mQuantityEditText.setText(R.string.zero);
                }
            }
        });
        // Button to increase Item Count in UI
        mIncreaseItemCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentQuantity = mQuantityEditText.getText().toString();
                int updateQuantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());

                mQuantityEditText.setText(String.valueOf(updateQuantity + 1));
                if (TextUtils.isEmpty(currentQuantity)) {
                    mQuantityEditText.setText(R.string.zero);
                }
            }
        });
    }


    // CRUD Functions for DB.
    private void saveItem() {
        // Read from input fields
        // Use trim to eliminate extra white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString().trim();

        // Check if this is supposed to be a new item
        // and check if all the fields in the editor are blank
        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierPhoneString)) {
            // Sine no fields have changed, we can return early without creating a new item.
            // No need to create ContentValues and no need to do any ConentProvider ops.
            return;
        }
        // Create a ContentValues object where column names are the keys,
        // and item attributes from editor are the values.
        ContentValues values = new ContentValues();
        String productName = getString(R.string.unknown_model);
        if (!TextUtils.isEmpty(nameString)) {
            productName = nameString;
        }
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productName);

        String supplierName = getString(R.string.unknown_manu);
        if (!TextUtils.isEmpty(supplierNameString)) {
            supplierName = supplierNameString;
        }
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, supplierName);

        // If the phone number is not provided by the user, don't parse the string, Default to 0.
        long supplierPhone = 0;
        if (!TextUtils.isEmpty(supplierPhoneString) && supplierPhoneString.length() <= 15) {
            supplierPhone = Long.parseLong(supplierPhoneString);
        } else {
            Toast.makeText(this, R.string.phone_number_error,
                    Toast.LENGTH_SHORT).show();
        }
        values.put(InventoryEntry.COLUMN_SUPPLIER_PHONE, supplierPhone);
        // If the price is not provided, default to 0 without parsing.
        int itemPrice = 0;
        if (!TextUtils.isEmpty(priceString)) {
            itemPrice = Integer.parseInt(priceString);
        }
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, itemPrice);

        int itemQuantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            itemQuantity = Integer.parseInt(quantityString);
        }

        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, itemQuantity);
        // Determine if this is a new or existing item by checking if mCurrentItemUri is null or not
        if (mCurrentItemUri == null) {
            // This is a NEW item, so insert a new item into the provider,
            // returning the content URI for the new item.
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was good.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, R.string.issue_with_save, Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was good and we can display a toast.
                Toast.makeText(this, R.string.save_ok, Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an existing item, so update the item with content URI: mCurrentItemUri
            // and pass in the new ContentValues. Pass is null for the selection and selection args
            // because mCurrentItemUri will already id the correct row in the db that we want.
            int rowsaffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            // Show a toast message depending on whether or not the update was good.
            if (rowsaffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, R.string.error_update, Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was good and we can display a toast.
                Toast.makeText(this, R.string.update_ok, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_item_question);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // User clicked "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // User clicked "Cancel", so dismiss the dialog and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {

        // Only perform the delete if this is an existing item.
        if (mCurrentItemUri != null) {
            // Call the ContentResolver to delete the item at the given Uri.
            // Pass in null for the selection and selection args since mCurrentItemUri
            // content URI already id's the item we want.
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
            // Show a toast message depending on whether or not the delete was good.
            if (rowsDeleted == 0) {
                Toast.makeText(this, R.string.error_delete, Toast.LENGTH_SHORT).show();
                Log.e(LOG_TAG, getString(R.string.error_delete_message) + mCurrentItemUri);
            } else {
                // Otherwise, the delete was good and we can display a toast.
                Toast.makeText(this, R.string.delete_ok, Toast.LENGTH_SHORT).show();
            }
            // Close the activity
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item, hide the "Delete" menu option
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save item to db
                saveItem();
                // Exit activity
                finish();
                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;

            // Respond to a click on "Re-Order" menu option
            // Pass in the contents of mSupplierPhoneNumber as string for later parsing to Uri.
            case R.id.action_order:
                startDialActivity(mSupplierPhoneEditText.getText().toString());
                return true;

            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item is unchanged, continue with navigating up to parent activity
                // which is the {@Link CatalogActivity}.
                if (!mItemHasUpdate) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, close the current activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show dialog that there are unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasUpdate) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all item attributes, define a projection that contains
        // all columns from the item table
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryEntry.COLUMN_SUPPLIER_PHONE
        };
        // This loader will execute the ContentProvider's query method on a background thread

        return new CursorLoader(this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the colums of item attributes that we are interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_PHONE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            Long supplierPhoneNumber = cursor.getLong(supplierPhoneColumnIndex);

            // Update the views on the screen with the values from the db.
            mNameEditText.setText(name);
            mPriceEditText.setText(Integer.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            mSupplierNameEditText.setText(supplierName);
            mSupplierPhoneEditText.setText(Long.toString(supplierPhoneNumber));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText(R.string.empty);
        mPriceEditText.setText(R.string.empty);
        mQuantityEditText.setText(R.string.empty);
        mSupplierNameEditText.setText(R.string.empty);
        mSupplierPhoneEditText.setText(R.string.empty);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.discard_and_quit);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep Editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void startDialActivity(String phone) {
        Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
        phoneIntent.setData(Uri.parse("tel: " + phone));
        startActivity(phoneIntent);
    }
}
