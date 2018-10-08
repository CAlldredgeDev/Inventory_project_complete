package com.example.android.inventory_project.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Store Inventory app.
 */

public final class InventoryContract {

    // Content authority for use with content provider
    public final static String CONTENT_AUTHORITY = "com.example.android.inventory_project";
    // Base_Content_Uri
    public final static Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    // PATH_TABLENAME is going to store the path for the table we want
    public final static String PATH_INVENTORY = "inventory";

    // Empty constructor to prevent accidental instantiation of the contract class.
    private InventoryContract() {
    }

    public static final class InventoryEntry implements BaseColumns {

        public final static Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        // The MIME type of the {@Link #CONTENT_URI} for a list of items.
        public final static String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        // The MIME type of the {@Link #CONTENT_URI} for a single item.
        public final static String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;


        public final static String TABLE_NAME = "inventory";

        /**
         * Unique ID number for product in the DB.
         * <p>
         * Type:
         * INTEGER
         * AUTO INCREMENT
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of product in DB.
         * <p>
         * Type:
         * TEXT
         * NON NULL
         */
        public final static String COLUMN_PRODUCT_NAME = "name";

        /**
         * Base Price of item
         * <p>
         * Type:
         * INTEGER
         */
        public final static String COLUMN_PRODUCT_PRICE = "price";

        /**
         * How many of item in stock
         * <p>
         * Type:
         * INTEGER
         * DEFAULT 0
         */
        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";

        /**
         * Name of Supplier of Item
         * <p>
         * Type:
         * TEXT
         * NON NULL
         */
        public final static String COLUMN_SUPPLIER_NAME = "suppName";

        /**
         * Phone Contact for product supplier
         * <p>
         * Type:
         * INTEGER
         */
        public final static String COLUMN_SUPPLIER_PHONE = "suppPhone";
    }
}
