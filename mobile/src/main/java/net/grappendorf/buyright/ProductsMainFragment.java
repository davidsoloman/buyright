package net.grappendorf.buyright;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import net.grappendorf.buyrightcommon.Product;
import net.grappendorf.buyrightcommon.ProductListener;
import net.grappendorf.buyrightcommon.Products;

public class ProductsMainFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

  private GridView todoGrid;
  private GridView doneGrid;
  private ProductAdapter todoAdapter;
  private ProductAdapter doneAdapter;
  private ProductListener productListener;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    productListener = (ProductListener) activity;
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.products_main, container, false);

    todoGrid = (GridView) view.findViewById(R.id.todo_items);
    todoAdapter = new ProductAdapter(getActivity(), null);
    todoGrid.setAdapter(todoAdapter);
    todoGrid.setOnItemClickListener(new ProductClickListener());
    todoGrid.setFocusable(false);
    todoGrid.setEmptyView(view.findViewById(R.id.no_todo_items));
    registerForContextMenu(todoGrid);

    doneGrid = (GridView) view.findViewById(R.id.done_items);
    doneAdapter = new ProductAdapter(getActivity(), null);
    doneGrid.setAdapter(doneAdapter);
    doneGrid.setOnItemClickListener(new ProductClickListener());
    doneGrid.setFocusable(false);
    doneGrid.setEmptyView(view.findViewById(R.id.no_done_items));
    registerForContextMenu(doneGrid);

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();

    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    preferences.registerOnSharedPreferenceChangeListener(this);
    onSharedPreferenceChanged(preferences, BuyrightPreferences.DRAW_CARD_ANIMATIONS);

    reloadLists();
  }

  @Override
  public void onPause() {
    PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    super.onPause();
  }

  public void reloadLists() {
    Products products = productListener.onSupplyProducts();
    todoAdapter.changeCursor(products.findTodo());
    doneAdapter.changeCursor(products.findDone());
  }

  private class ProductClickListener implements AdapterView.OnItemClickListener {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      Product product = (Product) view.getTag();
      productListener.onProductSelect(product);
    }
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    long productId = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).id;
    switch (item.getItemId()) {
      case R.id.action_edit:
        productListener.onProductEdit(productId);
        return true;
      case R.id.action_delete:
        productListener.onProductDelete(productId);
        return true;
    }
    return super.onContextItemSelected(item);
  }

  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (BuyrightPreferences.DRAW_CARD_ANIMATIONS.equals(key) ||
        BuyrightPreferences.SINGLE_LINE_CARDS.equals(key)) {
      boolean drawAnimations = sharedPreferences.getBoolean(BuyrightPreferences.DRAW_CARD_ANIMATIONS, true);
      boolean singleLineCards = sharedPreferences.getBoolean(BuyrightPreferences.SINGLE_LINE_CARDS, true);
      todoAdapter.setDrawCardRipple(drawAnimations);
      todoAdapter.setSingleLineCards(singleLineCards);
      doneAdapter.setDrawCardRipple(drawAnimations);
      doneAdapter.setSingleLineCards(singleLineCards);
      todoGrid.setAdapter(todoAdapter);
      doneGrid.setAdapter(doneAdapter);
    }
  }
}
