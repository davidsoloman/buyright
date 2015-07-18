package net.grappendorf.buyright;

import android.app.Activity;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.GridView;
import net.grappendorf.buyrightcommon.Product;
import net.grappendorf.buyrightcommon.ProductListener;
import net.grappendorf.buyrightcommon.Products;

public class ProductsSearchFragment extends Fragment {

  private Activity activity;
  private ProductAdapter productsAdapter;
  private ProductListener productListener;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.activity = activity;
    productListener = (ProductListener) activity;
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.products_search, container, false);

    GridView productsGridView = (GridView) view.findViewById(R.id.products);
    productsAdapter = new ProductAdapter(activity, null);
    productsAdapter.setFilterQueryProvider(new FilterQueryProvider() {
      @Override
      public Cursor runQuery(CharSequence constraint) {
        Products products = productListener.onSupplyProducts();
        String text = constraint.toString();
        Cursor doneProducts = products.findDoneByName("%" + constraint + "%");
        if (text.length() > 0 && products.findByName(text) == null) {
          MatrixCursor newProduct = new MatrixCursor(Product.ATTRIBUTES);
          newProduct.addRow(new Object[]{0, Product.capitalizeName(text), 0});
          return new MergeCursor(new Cursor[]{newProduct, doneProducts});
        } else {
          return doneProducts;
        }
      }
    });
    productsGridView.setAdapter(productsAdapter);
    productsGridView.setOnItemClickListener(new ProductClickListener());
    productsGridView.setFocusable(false);
    filterProducts(getArguments().getString("query"));
    registerForContextMenu(productsGridView);

    return view;
  }

  public void search(String text) {
    filterProducts(text);
  }

  private void filterProducts(String text) {
    productsAdapter.getFilter().filter(text != null ? text : "");
  }

  private class ProductClickListener implements AdapterView.OnItemClickListener {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      Product product = (Product) view.getTag();
      productListener.onProductSelect(product);
    }
  }
}
