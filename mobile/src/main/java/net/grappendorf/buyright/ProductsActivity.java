package net.grappendorf.buyright;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.*;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.*;
import net.grappendorf.buyrightcommon.Product;
import net.grappendorf.buyrightcommon.ProductListener;
import net.grappendorf.buyrightcommon.Products;
import net.grappendorf.buyrightcommon.WearableHelper;
import org.json.JSONException;
import java.io.IOException;

public class ProductsActivity extends AppCompatActivity implements ProductListener {

  private static final int REQUEST_RESOLVE_ERROR = 1000;
  private static final int NOTIFICATION_ID = 1;

  private GoogleApiClient googleApiClient;
  private boolean resolvingError;
  private EditText searchText;
  Fragment currentFragment;
  private ProductsMainFragment mainFragment;
  private ProductsSearchFragment searchFragment;
  private String nodeId;
  private Products products;
  private WearableDataListener wearableDataListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    products = new ProductsDatabase();

    googleApiClient = new GoogleApiClient.Builder(this)
        .addApi(Wearable.API)
        .addConnectionCallbacks(new GoogleApiConnectionListener())
        .addOnConnectionFailedListener(new GoogleApiConnectionFailedListener())
        .build();

    setContentView(R.layout.products);
    searchText = ((EditText) findViewById(R.id.search_text));
    searchText.addTextChangedListener(new SearchTextWatcher());
    searchText.setOnEditorActionListener(new SearchActionListener());

    mainFragment = new ProductsMainFragment();
    searchFragment = new ProductsSearchFragment();
    activateMainFragment();
  }

  @Override
  protected void onStart() {
    super.onStart();
    DatabaseHelper.start(this);
    if (!resolvingError) {
      googleApiClient.connect();
    }
  }

  @Override
  protected void onStop() {
    DatabaseHelper.stop();
    if (!resolvingError) {
      if (wearableDataListener != null) {
        Wearable.DataApi.removeListener(googleApiClient, wearableDataListener);
        wearableDataListener = null;
      }
      googleApiClient.disconnect();
    }
    super.onStop();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.products_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_add:
        addProduct();
        return true;
      case R.id.action_clear_shopping_list:
        clearShoppingListAfterConfirmation();
        return true;
      case R.id.action_export:
        exportData();
        return true;
      case R.id.action_import:
        importData();
        return true;
      case R.id.action_settings:
        startActivity(new Intent(this, BuyrightPreferences.class));
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    getMenuInflater().inflate(R.menu.product_card_menu, menu);
  }

  class GoogleApiConnectionListener implements GoogleApiClient.ConnectionCallbacks {
    @Override
    public void onConnected(Bundle bundle) {
      resolvingError = false;
      wearableDataListener = new WearableDataListener();
      Wearable.DataApi.addListener(googleApiClient, wearableDataListener);
      Wearable.NodeApi.getLocalNode(googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
        @Override
        public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {
          nodeId = getLocalNodeResult.getNode().getId();
        }
      });
      wearableUpdateProducts();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }
  }

  class GoogleApiConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
    @Override
    public void onConnectionFailed(ConnectionResult result) {
      if (resolvingError) {
        return;
      }
      if (result.hasResolution()) {
        try {
          resolvingError = true;
          result.startResolutionForResult(ProductsActivity.this, REQUEST_RESOLVE_ERROR);
        } catch (IntentSender.SendIntentException e) {
          googleApiClient.connect();
        }
      } else {
        if (result.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
          UIHelper.toast(ProductsActivity.this, R.string.error_wear_app_not_installed, Toast.LENGTH_LONG);
        } else {
          UIHelper.toast(ProductsActivity.this, R.string.error_google_api_client_connect_failed, Toast.LENGTH_LONG);
        }
        resolvingError = false;
        if (wearableDataListener != null) {
          Wearable.DataApi.removeListener(googleApiClient, wearableDataListener);
        }
      }
    }
  }

  class WearableDataListener implements DataApi.DataListener {
    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
      for (DataEvent event : dataEventBuffer) {
        DataItem item = event.getDataItem();
        if (item.getUri().getHost().equals(nodeId)) {
          continue;
        }
        DataMap data = DataMapItem.fromDataItem(item).getDataMap();
        Product product = products.get(WearableHelper.getIdFromCartProductsUri(item.getUri()));
        if (product != null) {
          product.setTodo(data.getBoolean(Product.TODO));
          products.save(product);
        }
        activateMainFragment();
        notifyTodoChanged();
      }
      dataEventBuffer.release();
    }
  }

  private class SearchTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int before, int count) {
      String query = text.toString();
      boolean isSearch = query.length() > 0;
      if (isSearch) {
        activateSearchFragment(query);
      } else {
        activateMainFragment();
      }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
  }

  private class SearchActionListener implements TextView.OnEditorActionListener {
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
      if (event == null || event.getAction() != KeyEvent.ACTION_DOWN) {
        return false;
      }
      String name = v.getText().toString();
      if (TextUtils.isEmpty(name)) {
        return true;
      }
      Product product = products.findByName(name);
      if (product != null) {
        onProductSelect(product);
      } else {
        product = new Product();
        product.setName(Product.capitalizeName(name));
        product.setTodo(true);
        createProduct(product);
      }
      return true;
    }
  }

  @Override
  public Products onSupplyProducts() {
    return products;
  }

  @Override
  public void onProductSelect(Product product) {
    if (product.isNew()) {
      product.setTodo(true);
      createProduct(product);
    } else {
      toggleProductTodo(product);
    }
    searchText.setText("");
  }

  @Override
  public void onProductEdit(long productId) {
    ProductEditDialog dialog = new ProductEditDialog();
    Bundle bundle = new Bundle();
    bundle.putSerializable(Product.PRODUCT, products.get(productId));
    dialog.setArguments(bundle);
    dialog.show(getSupportFragmentManager(), "product_edit");
  }

  @Override
  public void onProductSave(Product product) {
    if (product.isNew()) {
      products.create(product);
    } else {
      products.save(product);
      wearableUpdateProduct(product);
    }
    searchText.setText("");
    activateMainFragment();
  }

  @Override
  public void onProductDelete(final long productId) {
    Product product = products.get(productId);
    UIHelper.showDialog(this, getString(R.string.delete_product), getString(R.string.delete_confirm, product.getName()),
        new UIHelper.DialogListener() {
          @Override
          public void onAccept() {
            products.delete(productId);
            activateMainFragment();
            notifyTodoChanged();
            wearableDeleteProduct(productId);
          }
        });
  }

  private void activateMainFragment() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (currentFragment != mainFragment) {
          FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
          transaction.replace(R.id.products_fragment, mainFragment);
          transaction.commit();
          currentFragment = mainFragment;
        } else {
          mainFragment.reloadLists();
        }
      }
    });
  }

  private void activateSearchFragment(String query) {
    if (currentFragment != searchFragment) {
      Bundle args = new Bundle();
      args.putString("query", query);
      searchFragment.setArguments(args);
      FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
      transaction.replace(R.id.products_fragment, searchFragment);
      transaction.commit();
      currentFragment = searchFragment;
    } else {
      searchFragment.search(query);
    }
  }


  private void createProduct(Product product) {
    products.create(product);
    searchText.setText("");
    activateMainFragment();
    notifyTodoChanged();
    wearableUpdateProduct(product);
  }

  private void toggleProductTodo(Product product) {
    product.setTodo(!product.isTodo());
    products.save(product);
    activateMainFragment();
    notifyTodoChanged();
    wearableUpdateProduct(product);
  }

  private void addProduct() {
    ProductEditDialog dialog = new ProductEditDialog();
    dialog.show(getSupportFragmentManager(), "product_edit");
  }

  private void clearShoppingList() {
    products.setAllDone();
    activateMainFragment();
    notifyTodoChanged();
    wearableDeleteAllProducts();
  }

  private void clearShoppingListAfterConfirmation() {
    UIHelper.showDialog(this, getString(R.string.clear_shopping_list),
        getString(R.string.clear_shopping_list_confirm),
        new UIHelper.DialogListener() {
          @Override
          public void onAccept() {
            clearShoppingList();
          }
        });
  }

  private void notifyTodoChanged() {
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ProductsActivity.this);

    int numTodos = products.countTodo();
    if (numTodos > 0) {
      NotificationCompat.Builder builder = new NotificationCompat.Builder(ProductsActivity.this);
      builder.setSmallIcon(R.mipmap.logo);
      builder.setContentTitle(getResources().getString(R.string.app_name));
      builder.setContentText(getResources().getString(R.string.num_items_todo,
          getResources().getQuantityString(R.plurals.items, numTodos, (int) numTodos)));
      builder.setContentIntent(PendingIntent.getActivity(ProductsActivity.this, 0,
          new Intent(ProductsActivity.this, ProductsActivity.class), 0));
      builder.setLocalOnly(true);
      notificationManager.notify(NOTIFICATION_ID, builder.build());
    } else {
      notificationManager.cancelAll();
    }

    wearableNotifyTodoChanged(numTodos);
  }

  private void wearableUpdateProduct(Product product) {
    if (!googleApiClient.isConnected()) {
      return;
    }
    if (product.isTodo()) {
      PutDataMapRequest mapRequest = PutDataMapRequest.create(WearableHelper.CART_PRODUCTS + "/" + product.getId());
      DataMap data = mapRequest.getDataMap();
      data.putString(Product.NAME, product.getName());
      data.putBoolean(Product.TODO, product.isTodo());
      WearableHelper.setTimestamp(data);
      PutDataRequest request = mapRequest.asPutDataRequest();
      Wearable.DataApi.putDataItem(googleApiClient, request);
      if (products.countTodo() == 1) {
        wearableDeleteAllProductsExcept(product);
      }
    } else {
      Uri uri = new Uri.Builder().scheme(PutDataRequest.WEAR_URI_SCHEME)
          .authority(nodeId).path(WearableHelper.CART_PRODUCTS + "/" + product.getId()).build();
      Wearable.DataApi.deleteDataItems(googleApiClient, uri);
    }
  }

  private void wearableDeleteProduct(long productId) {
    if (!googleApiClient.isConnected() || nodeId == null) {
      return;
    }
    Uri uri = new Uri.Builder().scheme(PutDataRequest.WEAR_URI_SCHEME)
        .authority(nodeId).path(WearableHelper.CART_PRODUCTS + "/" + productId).build();
    Wearable.DataApi.deleteDataItems(googleApiClient, uri);
  }

  private void wearableDeleteAllProducts() {
    WearableHelper.processDataItems(googleApiClient, WearableHelper.CART_PRODUCTS,
        new WearableHelper.DataItemProcessor() {
          @Override
          public void process(DataItem item) {
            Wearable.DataApi.deleteDataItems(googleApiClient, item.getUri());
          }
        });
  }

  private void wearableDeleteAllProductsExcept(final Product except) {
    WearableHelper.processDataItems(googleApiClient, WearableHelper.CART_PRODUCTS,
        new WearableHelper.DataItemProcessor() {
          @Override
          public void process(DataItem item) {
            if (WearableHelper.getIdFromCartProductsUri(item.getUri()) != except.getId()) {
              Wearable.DataApi.deleteDataItems(googleApiClient, item.getUri());
            }
          }
        });
  }

  private void wearableUpdateProducts() {
    WearableHelper.processDataItems(googleApiClient, WearableHelper.CART_PRODUCTS,
        new WearableHelper.DataItemProcessor() {
          @Override
          public void process(DataItem item) {
            DataMap data = DataMapItem.fromDataItem(item).getDataMap();
            boolean todo = data.getBoolean(Product.TODO);
            Product product = products.get(WearableHelper.getIdFromCartProductsUri(item.getUri()));
            if (product != null && product.isTodo() != todo) {
              product.setTodo(todo);
              products.save(product);
            }
          }

          @Override
          public void postProcess() {
            activateMainFragment();
          }
        });
  }

  private void wearableUpdateCart() {
    wearableDeleteAllProducts();
    Cursor cursor = products.findTodo();
    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      wearableUpdateProduct(Product.createFromCursor(cursor));
      cursor.moveToNext();
    }
  }

  private void wearableNotifyTodoChanged(int numTodos) {
    if (googleApiClient.isConnected()) {
      PutDataMapRequest mapRequest = PutDataMapRequest.create(WearableHelper.NOTIFICATION);
      DataMap data = mapRequest.getDataMap();
      data.putInt(WearableHelper.COLUMN_NUM_TODOS, numTodos);
      WearableHelper.setTimestamp(data);
      PutDataRequest putDataRequest = mapRequest.asPutDataRequest();
      Wearable.DataApi.putDataItem(googleApiClient, putDataRequest);
    }
  }

  private void importData() {
    UIHelper.showDialog(this, getString(R.string.import_data),
        getString(R.string.import_data_confirm),
        new UIHelper.DialogListener() {
          @Override
          public void onAccept() {
            try {
              DatabaseHelper.instance().importFromFile(ProductsActivity.this, "buyright.json");
              activateMainFragment();
              wearableUpdateCart();
              UIHelper.toast(ProductsActivity.this, R.string.import_data_successful, Toast.LENGTH_SHORT);
            } catch (IOException | JSONException e) {
              UIHelper.toast(ProductsActivity.this, e.getMessage(), Toast.LENGTH_LONG);
            }
          }
        });
  }

  private void exportData() {
    UIHelper.showDialog(this, getString(R.string.export_data),
        getString(R.string.export_data_confirm),
        new UIHelper.DialogListener() {
          @Override
          public void onAccept() {
            try {
              DatabaseHelper.instance().exportToFile(ProductsActivity.this, "buyright.json");
              UIHelper.toast(ProductsActivity.this, R.string.export_data_successful, Toast.LENGTH_SHORT);
            } catch (IOException | JSONException e) {
              UIHelper.toast(ProductsActivity.this, e.getMessage(), Toast.LENGTH_LONG);
            }
          }
        });
  }
}
