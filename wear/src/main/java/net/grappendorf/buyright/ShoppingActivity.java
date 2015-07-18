package net.grappendorf.buyright;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.*;
import net.grappendorf.buyrightcommon.Product;
import net.grappendorf.buyrightcommon.ProductListener;
import net.grappendorf.buyrightcommon.Products;
import net.grappendorf.buyrightcommon.WearableHelper;

public class ShoppingActivity extends Activity implements ProductListener {

  public GridViewPager pager;
  private DotsPageIndicator dots;
  private GoogleApiClient googleApiClient;
  private int currentItemIndex;
  private Products products;
  private String nodeId;
  private WearableDataListener wearableDataListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    googleApiClient = new GoogleApiClient.Builder(this)
        .addApi(Wearable.API)
        .addConnectionCallbacks(new GoogleApiConnectionListener())
        .addOnConnectionFailedListener(new GoogleApiConnectionFailedListener())
        .build();

    products = new ProductsList();

    setContentView(R.layout.items_activity);
    final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
    stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
      @Override
      public void onLayoutInflated(WatchViewStub stub) {
        pager = (GridViewPager) findViewById(R.id.pager);
        pager.setAdapter(new ProductAdapter(ShoppingActivity.this, products));
        dots = (DotsPageIndicator) findViewById(R.id.dots);
        dots.setPager(pager);
        // Workaround
        // The DotsPageIndicator switches to column 0 if the pager dataset changes
        // So we must remember the currently visible column
        dots.setOnPageChangeListener(new GridViewPager.OnPageChangeListener() {
          @Override
          public void onPageScrolled(int row, int column, float rowOffset, float columnOffset, int rowOffsetPixels, int columnOffsetPixels) {
          }

          @Override
          public void onPageSelected(int row, int column) {
            currentItemIndex = column;
          }

          @Override
          public void onPageScrollStateChanged(int state) {
          }
        });
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    googleApiClient.connect();
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (wearableDataListener != null) {
      Wearable.DataApi.removeListener(googleApiClient, wearableDataListener);
      wearableDataListener = null;
    }
    googleApiClient.disconnect();
  }

  class GoogleApiConnectionListener implements GoogleApiClient.ConnectionCallbacks {
    @Override
    public void onConnected(Bundle bundle) {
      wearableDataListener = new WearableDataListener();
      Wearable.DataApi.addListener(googleApiClient, wearableDataListener);
      Wearable.NodeApi.getLocalNode(googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
        @Override
        public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {
          nodeId = getLocalNodeResult.getNode().getId();
        }
      });
      loadProducts();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }
  }

  class GoogleApiConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
  }

  class WearableDataListener implements DataApi.DataListener {
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
      for (DataEvent event : dataEvents) {
        DataItem item = event.getDataItem();
        if (item.getUri().getHost().equals(nodeId)) {
          continue;
        }
        if (!item.getUri().getPath().startsWith(WearableHelper.CART_PRODUCTS)) {
          continue;
        }
        Product product;
        switch (event.getType()) {
          case DataEvent.TYPE_CHANGED:
            DataMap data = DataMapItem.fromDataItem(item).getDataMap();
            product = products.get(WearableHelper.getIdFromCartProductsUri(item.getUri()));
            if (product != null) {
              product.setName(data.getString(Product.NAME));
              product.setTodo(data.getBoolean(Product.TODO));
            } else {
              product = new Product();
              product.setId(WearableHelper.getIdFromCartProductsUri(item.getUri()));
              product.setName(data.getString(Product.NAME));
              product.setTodo(data.getBoolean(Product.TODO));
              products.create(product);
            }
            updateList();
            break;
          case DataEvent.TYPE_DELETED:
            product = products.get(WearableHelper.getIdFromCartProductsUri(item.getUri()));
            if (product != null) {
              products.delete(product);
              updateList();
            }
            break;
        }
      }
      dataEvents.release();
    }
  }

  private void loadProducts() {
    products.deleteAll();
    WearableHelper.processDataItems(googleApiClient, WearableHelper.CART_PRODUCTS,
        new WearableHelper.DataItemProcessor() {
          @Override
          public void process(DataItem item) {
            DataMap data = DataMapItem.fromDataItem(item).getDataMap();
            Product product = new Product(WearableHelper.getIdFromCartProductsUri(item.getUri()));
            product.setName(data.getString(Product.NAME));
            product.setTodo(data.getBoolean(Product.TODO));
            products.create(product);
          }

          @Override
          public void postProcess() {
            updateList();
          }
        });
  }

  @Override
  public Products onSupplyProducts() {
    return null;
  }

  @Override
  public void onProductSelect(Product product) {
    product.setTodo(!product.isTodo());
    updateList();
    if (!googleApiClient.isConnected()) {
      return;
    }
    PutDataMapRequest mapRequest = PutDataMapRequest.create(WearableHelper.CART_PRODUCTS + "/" + product.getId());
    DataMap data = mapRequest.getDataMap();
    data.putBoolean(Product.TODO, product.isTodo());
    WearableHelper.setTimestamp(data);
    PutDataRequest request = mapRequest.asPutDataRequest();
    Wearable.DataApi.putDataItem(googleApiClient, request);
  }

  @Override
  public void onProductDelete(long product) {
  }

  @Override
  public void onProductEdit(long product) {
  }

  @Override
  public void onProductSave(Product product) {
  }

  private void updateList() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        pager.getAdapter().notifyDataSetChanged();
        // Workaround: Go back to the last visible column
        dots.onPageSelected(0, currentItemIndex);
      }
    });
  }
}
