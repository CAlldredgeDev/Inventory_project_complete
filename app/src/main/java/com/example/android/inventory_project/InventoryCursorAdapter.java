package com.example.android.inventory_project;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import static com.example.android.inventory_project.data.InventoryContract.InventoryEntry;


public class InventoryCursorAdapter extends CursorAdapter {
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }


    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        Button saleButton = (Button) view.findViewById(R.id.main_action_sale);

        // Find the columns of the item attribs that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
        final int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);

        // Read in the data from the Cursor for the current item
        String itemName = cursor.getString(nameColumnIndex);
        String itemPrice = cursor.getString(priceColumnIndex);
        String itemQuantity = cursor.getString(quantityColumnIndex);

        // Populate views with data we want to show
        nameTextView.setText(itemName);
        priceTextView.setText(itemPrice);
        quantityTextView.setText(itemQuantity);

        final int id = cursor.getInt(cursor.getColumnIndex(InventoryEntry._ID));
        final int quantity = Integer.parseInt(itemQuantity);

        saleButton.findViewById(R.id.main_action_sale).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CatalogActivity mainActivity = (CatalogActivity) context;
                mainActivity.sellItem(id, quantity);
            }
        });
    }

}
