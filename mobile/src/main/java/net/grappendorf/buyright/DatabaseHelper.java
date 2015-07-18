package net.grappendorf.buyright;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import net.grappendorf.buyrightcommon.Buyright;
import net.grappendorf.buyrightcommon.JSONHelper;
import net.grappendorf.buyrightcommon.Product;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;

public class DatabaseHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "buyright.db";

  private static final int DATABASE_VERSION = 1;

  private static final String VERSION_PROPERTY = "version";

  private static DatabaseHelper instance;

  private static SQLiteDatabase database;

  public DatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  public static void start(Context context) {
    if (instance == null) {
      instance = new DatabaseHelper(context.getApplicationContext());
      database = instance.getWritableDatabase();
    }
  }

  public static void stop() {
    instance.close();
    instance = null;
  }

  public static synchronized DatabaseHelper instance() {
    return instance;
  }

  public static SQLiteDatabase getDatabase() {
    return database;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    Log.d(Buyright.TAG, "DatabaseHelper.onCreate");
    db.execSQL("create table " + ProductsDatabase.TABLE + " (" +
        Product.ID + " integer primary key autoincrement, " +
        Product.NAME + " text, " +
        Product.TODO + " integer default 0);");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.d(Buyright.TAG, "DatabaseHelper.onUpgrade");
  }

  public void exportToFile(Context context, String filename) throws IOException, JSONException {
    FileOutputStream out = null;
    JSONObject data = new JSONObject();
    data.put(VERSION_PROPERTY, DATABASE_VERSION);
    data.put(ProductsDatabase.TABLE, new ProductsDatabase().allAsJson());
    try {
      out = context.openFileOutput(filename, Context.MODE_PRIVATE);
      out.write(data.toString().getBytes());
      out.close();
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }

  public void importFromFile(Context context, String filename) throws IOException, JSONException {
    JSONObject json = JSONHelper.readFromFile(context, filename);

    int jsonVersion = JSONHelper.getInt(json, VERSION_PROPERTY, 0);
    if (jsonVersion != DATABASE_VERSION) {
      throw new IOException("JSON file version " + jsonVersion +
          " is incompatible with database version " + DATABASE_VERSION);
    }

    ProductsDatabase products = new ProductsDatabase();
    products.deleteAll();
    JSONArray jsonProducts = JSONHelper.getArray(json, ProductsDatabase.TABLE, new JSONArray());
    for (int i = 0; i < jsonProducts.length(); ++i) {
      Product product = Product.createFromJson(jsonProducts.getJSONObject(i));
      products.create(product);
    }
    products.updatePrimaryKeySequence();
  }
}
