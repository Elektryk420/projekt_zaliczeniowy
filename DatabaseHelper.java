package com.example.projekt_zaliczeniowy_sklep;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ShopDatabase";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_ORDERS = "Orders";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_NAME = "Name";
    private static final String COLUMN_PRICE = "Price";
    private static final String COLUMN_USER_NAME = "UserName";
    private static final String COLUMN_ORDER_DATE = "OrderDate";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tworzenie tabeli zamówień
        String CREATE_ORDERS_TABLE = "CREATE TABLE " + TABLE_ORDERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT, "
                + COLUMN_PRICE + " REAL, "
                + COLUMN_USER_NAME + " TEXT, "
                + COLUMN_ORDER_DATE + " TEXT" + ")";
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Usunięcie starej tabeli, jeśli istnieje
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        onCreate(db);
    }

    // Dodawanie produktu do bazy danych
    public void addProductToOrder(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, product.getName());
        values.put(COLUMN_PRICE, product.getPrice());

        long id = db.insert(TABLE_ORDERS, null, values);
        if (id == -1) {
            Log.e("DatabaseHelper", "Wystąpił błąd podczas dodawania produktu do zamówienia.");
        } else {
            Log.i("DatabaseHelper", "Produkt dodany do zamówienia: ID = " + id);
        }
        db.close();
    }
}
