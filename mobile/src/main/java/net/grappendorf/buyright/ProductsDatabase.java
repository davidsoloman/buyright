package net.grappendorf.buyright;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import net.grappendorf.buyrightcommon.NotImplementedException;
import net.grappendorf.buyrightcommon.Product;
import net.grappendorf.buyrightcommon.Products;
import org.json.JSONArray;
import org.json.JSONObject;

public class ProductsDatabase implements Products {

  public static final String TABLE = "products";

  public Product get(long id) {
    Cursor cursor = where(Product.ID + " = ?", new String[]{String.valueOf(id)});
    return cursor.moveToNext() ? Product.createFromCursor(cursor) : null;
  }

  @Override
  public Product getAt(int index) {
    throw new NotImplementedException();
  }

  public Cursor all() {
    SQLiteDatabase db = DatabaseHelper.getDatabase();
    return db.query(TABLE, null, null, null, null, null, null);
  }

  public Cursor findTodo() {
    return where(Product.TODO + " == 1", null);
  }

  public Cursor findDone() {
    return where(Product.TODO + " == 0", null);
  }

  public Product findByName(String name) {
    Cursor cursor = where(Product.NAME + " like ?", new String[]{name});
    return cursor.moveToNext() ? Product.createFromCursor(cursor) : null;
  }

  public Cursor findDoneByName(String name) {
    return where(Product.TODO + "== 0 and " + Product.NAME + " like ? ", new String[]{name});
  }

  public int countTodo() {
    return count(Product.TODO + " == 1", null);
  }

  public int countAll() {
    SQLiteDatabase db = DatabaseHelper.getDatabase();
    return (int) DatabaseUtils.queryNumEntries(db, TABLE);
  }

  public void create(Product product) {
    ContentValues values = new ContentValues();
    if (product.getId() > 0) {
      values.put(Product.ID, product.getId());
    }
    values.put(Product.NAME, product.getName());
    values.put(Product.TODO, product.isTodo() ? 1 : 0);
    SQLiteDatabase db = DatabaseHelper.getDatabase();
    product.setId(db.insert(TABLE, null, values));
  }

  public void save(Product product) {
    ContentValues values = new ContentValues();
    values.put(Product.NAME, product.getName());
    values.put(Product.TODO, product.isTodo() ? 1 : 0);
    SQLiteDatabase db = DatabaseHelper.getDatabase();
    db.update(TABLE, values, Product.ID + " = ?", new String[]{String.valueOf(product.getId())});
  }

  public void delete(Product product) {
    delete(product.getId());
  }

  public void delete(long id) {
    SQLiteDatabase db = DatabaseHelper.getDatabase();
    db.delete(TABLE, Product.ID + " = ?", new String[]{String.valueOf(id)});
  }

  public void deleteAll() {
    SQLiteDatabase db = DatabaseHelper.getDatabase();
    db.delete(TABLE, null, null);
  }

  public void setAllDone() {
    ContentValues values = new ContentValues();
    values.put(Product.TODO, false);
    SQLiteDatabase db = DatabaseHelper.getDatabase();
    db.update(TABLE, values, Product.TODO + " = ?", new String[]{String.valueOf(1)});
  }

  public JSONArray allAsJson() {
    Cursor cursor = all();
    cursor.moveToFirst();
    JSONArray json = new JSONArray();
    while (!cursor.isAfterLast()) {
      JSONObject product = Product.createFromCursor(cursor).toJson();
      json.put(product);
      cursor.moveToNext();
    }
    cursor.close();
    return json;
  }

  public void updatePrimaryKeySequence() {
    SQLiteDatabase db = DatabaseHelper.getDatabase();
    db.execSQL("update sqlite_sequence set seq = (select max(" +
        Product.ID + ") from " + TABLE + ") where name = '" + TABLE + "'");
  }

  private Cursor where(String selection, String[] selectionArgs) {
    SQLiteDatabase db = DatabaseHelper.getDatabase();
    return db.query(TABLE, null, selection, selectionArgs, null, null, Product.NAME);
  }

  private int count(String selection, String[] selectionArgs) {
    SQLiteDatabase db = DatabaseHelper.getDatabase();
    return (int) DatabaseUtils.queryNumEntries(db, TABLE, selection, selectionArgs);
  }
}
