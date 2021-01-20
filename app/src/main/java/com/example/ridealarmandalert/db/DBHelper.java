package com.example.ridealarmandalert.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;


import com.example.ridealarmandalert.models.AlarmModel;
import com.example.ridealarmandalert.utils.MyProgressDialog;

import java.util.ArrayList;
import java.util.HashMap;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "RideAlarmAndAlert.db";

    public static final String FOOD_CATEGORY_TABLE = "foodCategoryTbl";

    private Context context;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table alarmTbl " + "(id integer primary key, title text,PID integer,time integer)");


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS alarmTbl");


        onCreate(db);
    }

    public long insertAlarm(String title, long PID, long time, MyProgressDialog myProgressDialog) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", title);
        contentValues.put("PID", PID);
        contentValues.put("time", time);


        long id = db.insert("alarmTbl", null, contentValues);
        if (myProgressDialog != null)
            myProgressDialog.dismiss();
        return id;
    }

    //
//    public boolean insertItem(String itemName, String itemIngredients, String categoryId, String priceSmall, String priceMedium, String priceLarge, MyProgressDialog myProgressDialog) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("itemName", itemName);
//        contentValues.put("itemIngredients", itemIngredients);
//        contentValues.put("categoryId", categoryId);
//        contentValues.put("priceSmall", priceSmall);
//        contentValues.put("priceMedium", priceMedium);
//        contentValues.put("priceLarge", priceLarge);
//
//
//        db.insert("foodItemTbl", null, contentValues);
//        if (myProgressDialog != null)
//            myProgressDialog.dismiss();
//        return true;
//    }
//
//    public boolean insertItemToCart(String itemName, String priceSmall, String priceMedium, String priceLarge, String tableNo, String categoryId) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("itemName", itemName);
//        contentValues.put("priceSmall", priceSmall);
//        contentValues.put("priceMedium", priceMedium);
//        contentValues.put("priceLarge", priceLarge);
//        contentValues.put("categoryId", categoryId);
//        contentValues.put("tableNo", tableNo);
//
//
//        db.insert("foodItemCart", null, contentValues);
//        Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show();
//        return true;
//    }
//
//    public boolean insertItemToOrders(String desc, String totalBill, String tableNo) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("orderDescription", desc);
//        contentValues.put("totalBill", totalBill);
//        contentValues.put("tableNo", tableNo);
//
//
//        db.insert("foodOrders", null, contentValues);
//
//        return true;
//    }
//
//    public boolean insertItemToInvoice(String desc, String totalBill) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("orderDescription", desc);
//        contentValues.put("totalBill", totalBill);
//
//
//        db.insert("foodOrderInvoice", null, contentValues);
//
//        return true;
//    }
//
//
//    public int numberOfRows() {
//        SQLiteDatabase db = this.getReadableDatabase();
//        int numRows = (int) DatabaseUtils.queryNumEntries(db, FOOD_CATEGORY_TABLE);
//        return numRows;
//    }
//
//
//    public Cursor getAllCategoriesRaw() {
//        ArrayList<String> array_list = new ArrayList<String>();
//
//        //hp = new HashMap();
//        SQLiteDatabase db = this.getReadableDatabase();
//        return db.rawQuery("select * from foodCategoryTbl", null);
//    }
//
//
    public ArrayList<AlarmModel> getAllAlarm() {
        ArrayList<AlarmModel> array_list = new ArrayList<>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from alarmTbl", null);
        res.moveToFirst();

        while (!res.isAfterLast()) {

            array_list.add(new AlarmModel(res.getString(res.getColumnIndex("id")),
                    res.getString(res.getColumnIndex("title")),
                    Long.parseLong(res.getString(res.getColumnIndex("PID"))),
                    Long.parseLong(res.getString(res.getColumnIndex("time")))));

            res.moveToNext();
        }
        return array_list;
    }

    //
//
//    public String getSingleAlarm(String id) {
//
//        String imgP = "";
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor res = db.rawQuery("select catImg from alarmTbl where id=" + id, null);
//        res.moveToFirst();
//
//        while (res.isAfterLast() == false) {
//            imgP = res.getString(res.getColumnIndex("catImg"));
//            res.moveToNext();
//        }
//        return imgP;
//    }

    //
//    public ArrayList<FoodNameModel> getAllItems(String id) {
//        ArrayList<FoodNameModel> array_list = new ArrayList<>();
//
//        //hp = new HashMap();
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor res = db.rawQuery("select * from foodItemTbl where categoryId=" + id, null);
//        res.moveToFirst();
//
//        while (res.isAfterLast() == false) {
//            String catName = res.getString(res.getColumnIndex("itemName"));
//            array_list.add(new FoodNameModel(res.getString(res.getColumnIndex("id")),
//                    catName, res.getString(res.getColumnIndex("itemIngredients")),
//                    res.getString(res.getColumnIndex("categoryId")),
//                    res.getString(res.getColumnIndex("priceSmall")),
//                    res.getString(res.getColumnIndex("priceMedium")),
//                    res.getString(res.getColumnIndex("priceLarge"))
//
//            ));
//
//            res.moveToNext();
//        }
//        return array_list;
//    }
//
//    public ArrayList<FoodNameModel> getAllItems() {
//        ArrayList<FoodNameModel> array_list = new ArrayList<>();
//
//        //hp = new HashMap();
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor res = db.rawQuery("select * from foodItemTbl", null);
//        res.moveToFirst();
//
//        while (res.isAfterLast() == false) {
//            String catName = res.getString(res.getColumnIndex("itemName"));
//            array_list.add(new FoodNameModel(res.getString(res.getColumnIndex("id")),
//                    catName, res.getString(res.getColumnIndex("itemIngredients")),
//                    res.getString(res.getColumnIndex("categoryId")),
//                    res.getString(res.getColumnIndex("priceSmall")),
//                    res.getString(res.getColumnIndex("priceMedium")),
//                    res.getString(res.getColumnIndex("priceLarge"))
//
//            ));
//
//            res.moveToNext();
//        }
//        return array_list;
//    }
//
//    public ArrayList<OrdersModel> getAllOrders() {
//        ArrayList<OrdersModel> array_list = new ArrayList<>();
//
//        //hp = new HashMap();
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor res = db.rawQuery("select * from foodOrders", null);
//        res.moveToFirst();
//
//        while (res.isAfterLast() == false) {
//            array_list.add(new OrdersModel(res.getString(res.getColumnIndex("id")),
//                    res.getString(res.getColumnIndex("orderDescription"))
//
//                    , res.getString(res.getColumnIndex("totalBill")), res.getString(res.getColumnIndex("tableNo"))
//
//            ));
//
//            res.moveToNext();
//        }
//        return array_list;
//    }
//
//    public ArrayList<CartItemModel> getAllCartItems(String tableNo) {
//        ArrayList<CartItemModel> array_list = new ArrayList<>();
//
//        //hp = new HashMap();
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor res = db.rawQuery("select * from foodItemCart where tableNo=" + tableNo, null);
//        res.moveToFirst();
//
//        while (res.isAfterLast() == false) {
//            String catName = res.getString(res.getColumnIndex("itemName"));
//            array_list.add(new CartItemModel(res.getString(res.getColumnIndex("id")),
//                    catName,
//                    res.getString(res.getColumnIndex("tableNo")),
//                    res.getString(res.getColumnIndex("priceSmall")),
//                    res.getString(res.getColumnIndex("priceMedium")),
//                    res.getString(res.getColumnIndex("priceLarge")),
//                    res.getString(res.getColumnIndex("categoryId"))
//
//            ));
//
//            res.moveToNext();
//        }
//        return array_list;
//    }
//
//
//    public void deleteCartItem(String id) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete("foodItemCart", "id = ? ", new String[]{id});
//    }
//
//    public void deleteCatItem(String id) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete("foodCategoryTbl", "id = ? ", new String[]{id});
//
//        deleteFoodItemOnCatId(id);
//    }
//
    public void deleteAlarm(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("alarmTbl", "id = ? ", new String[]{id});


    }
//
//    public void deleteOrderById(String id) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete("foodOrders", "id = ? ", new String[]{id});
//
//
//    }
//
//    public void deleteFoodItem(String id) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete("foodItemTbl", "id = ? ", new String[]{id});
//
//
//    }
//
//    public void deleteAllCartItemOnTable(String tableNo) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete("foodItemCart", "tableNo = ? ", new String[]{tableNo});
//    }
}