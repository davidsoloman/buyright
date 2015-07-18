package net.grappendorf.buyright;

import android.app.Activity;
import android.support.wearable.view.GridPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import net.grappendorf.buyrightcommon.Product;
import net.grappendorf.buyrightcommon.ProductListener;
import net.grappendorf.buyrightcommon.Products;

public class ProductAdapter extends GridPagerAdapter {

  private final Activity activity;
  private Products products;
  ProductListener productListener;

  ProductAdapter(Activity context, Products products) {
    this.activity = context;
    this.products = products;
    productListener = (ProductListener) activity;
  }

  @Override
  public int getRowCount() {
    return 1;
  }

  @Override
  public int getColumnCount(int row) {
    return Math.max(products.countAll(), 1);
  }

  @Override
  public Object instantiateItem(ViewGroup viewGroup, final int row, final int col) {
    View view;
    if (products.countAll() > 0) {
      view = LayoutInflater.from(activity).inflate(R.layout.product_item, viewGroup, false);
      final Product product = products.getAt(col);
      view.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (productListener != null) {
            productListener.onProductSelect(product);
          }
        }
      });
      ((TextView) view.findViewById(R.id.name)).setText(product.getName());
      view.setBackgroundColor(activity.getResources().getColor(product.isTodo() ? R.color.product_todo_card : R.color.product_done_card));
    } else {
      view = LayoutInflater.from(activity).inflate(R.layout.nothing_to_do, viewGroup, false);
    }
    viewGroup.addView(view);
    return view;
  }

  @Override
  public void destroyItem(ViewGroup viewGroup, int row, int col, Object o) {
    viewGroup.removeView((View) o);
  }

  @Override
  public boolean isViewFromObject(View view, Object o) {
    return view.equals(o);
  }
}
