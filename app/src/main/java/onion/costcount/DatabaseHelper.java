package onion.costcount;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.prefs.PreferencesFactory;

public class DatabaseHelper extends SQLiteOpenHelper {



    public static final String DATABASE_NAME = "prices2.db";
    public static final String TABLE_NAME = "costcount";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "ITEM";
    SharedPreferences sharedPreferences;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = " CREATE TABLE " + TABLE_NAME + "(" + COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_2 + " FLOAT )";
        db.execSQL(sql);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    public ArrayList<CostItem> getAllData()  {
        ArrayList<CostItem> arrayList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase dbl = this.getReadableDatabase();


        Cursor cursor = dbl.rawQuery(query, null);
        while(cursor.moveToNext()) {


            int id = cursor.getInt(0);
            float price = cursor.getFloat(1);
            CostItem homework = new CostItem();
            homework.setPrice(price);
            homework.setId(id);
            arrayList.add(homework);
        }
        return arrayList;
    }

    public void deleteItem(int id, float price){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE " + COL_1 + " = '" + id + "'" + " AND " + COL_2 + " = '" + price + "'";
        db.execSQL(query);
    }

    public void clearAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = " DELETE FROM " + TABLE_NAME;
        db.execSQL(query);
    }


    public boolean addData(float price) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, price);
        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1) {


            return false;
        } else {
            return true;
        }

    }


    public float getCost() {
        float cost = 0;
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase dbl = this.getReadableDatabase();


        Cursor cursor = dbl.rawQuery(query, null);
        while(cursor.moveToNext()) {
            float price = cursor.getFloat(1);
            cost += price;
        }
        return cost;
    }

}
