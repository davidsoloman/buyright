package net.grappendorf.buyrightcommon;

import android.database.Cursor;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;

public class Product implements Serializable {

  public static final String PRODUCT = "product";

  public static final String ID = "_id";
  public static final String NAME = "name";
  public static final String TODO = "todo";

  public static final String[] ATTRIBUTES = {ID, NAME, TODO};

  public Product() {
    this.id = 0;
  }

  public Product(long id) {
    this.id = id;
  }

  public Product(long id, String name, boolean todo) {
    this(id);
    this.name = name;
    this.todo = todo;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public boolean isTodo() {
    return todo;
  }

  public void setTodo(boolean todo) {
    this.todo = todo;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isNew() {
    return id == 0;
  }

  @Override
  public String toString() {
    return "Product{" +
        ID + "=" + id +
        ", " + NAME + "='" + name + '\'' +
        ", " + TODO + "=" + todo +
        '}';
  }

  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    try {
      json.put(ID, getId());
      json.put(NAME, getName());
      json.put(TODO, isTodo());
    } catch (JSONException ignored) {
    }
    return json;
  }

  public static Product createFromCursor(Cursor cursor) {
    Product product = new Product(cursor.getLong(cursor.getColumnIndex(ID)));
    product.setName(cursor.getString(cursor.getColumnIndex(NAME)));
    product.setTodo(cursor.getInt(cursor.getColumnIndex(TODO)) != 0);
    return product;
  }

  public static Product createFromJson(JSONObject json) {
    Product product = new Product(JSONHelper.getLong(json, ID, 0));
    product.setName(JSONHelper.getString(json, NAME, null));
    product.setTodo(JSONHelper.getBoolean(json, TODO, false));
    return product;
  }

  public static String capitalizeName(String name) {
    return name.substring(0, 1).toUpperCase() + name.substring(1);
  }

  private long id;
  private String name;
  private boolean todo;
}
