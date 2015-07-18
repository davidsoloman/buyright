package net.grappendorf.buyrightcommon;

import android.database.Cursor;

public interface Products {

  Product get(long id);

  Product getAt(int index);

  Cursor all();

  Cursor findTodo();

  Cursor findDone();

  Product findByName(String name);

  Cursor findDoneByName(String name);

  int countAll();

  int countTodo();

  void create(Product product);

  void save(Product product);

  void delete(Product product);

  void delete(long id);

  void deleteAll();

  void setAllDone();
}
