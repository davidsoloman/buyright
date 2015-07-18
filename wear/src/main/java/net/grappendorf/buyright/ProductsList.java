package net.grappendorf.buyright;

import android.database.Cursor;
import net.grappendorf.buyrightcommon.NotImplementedException;
import net.grappendorf.buyrightcommon.Product;
import net.grappendorf.buyrightcommon.Products;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import java.util.LinkedList;
import java.util.List;

public class ProductsList implements Products {

  private List<Product> products = new LinkedList<>();

  @Override
  public Product get(final long id) {
    return CollectionUtils.find(products, new Predicate<Product>() {
      public boolean evaluate(Product product) {
        return product.getId() == id;
      }
    });
  }

  @Override
  public Product getAt(int index) {
    return products.get(index);
  }

  @Override
  public Cursor all() {
    throw new NotImplementedException();
  }

  @Override
  public Cursor findTodo() {
    throw new NotImplementedException();
  }

  @Override
  public Cursor findDone() {
    throw new NotImplementedException();
  }

  @Override
  public Product findByName(String name) {
    throw new NotImplementedException();
  }

  @Override
  public Cursor findDoneByName(String name) {
    throw new NotImplementedException();
  }

  @Override
  public int countAll() {
    return products.size();
  }

  @Override
  public int countTodo() {
    throw new NotImplementedException();
  }

  @Override
  public void create(Product product) {
    products.add(product);
  }

  @Override
  public void save(Product product) {
    throw new NotImplementedException();
  }

  @Override
  public void delete(Product product) {
    products.remove(product);
  }

  @Override
  public void delete(long id) {
    throw new NotImplementedException();
  }

  @Override
  public void deleteAll() {
    products.clear();
  }

  @Override
  public void setAllDone() {
    throw new NotImplementedException();
  }
}
