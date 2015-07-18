package net.grappendorf.buyrightcommon;

import android.net.Uri;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.*;

public class WearableHelper {

  public static final String CART_PRODUCTS = "/cart/products";
  public static final String NOTIFICATION = "/notification";

  public static final String COLUMN_TIMESTAMP = "_timestamp";
  public static final String COLUMN_NUM_TODOS = "numTodos";

  public static long getIdFromCartProductsUri(Uri uri) {
    String path = uri.getPath();
    if (path.startsWith(CART_PRODUCTS)) {
      return Long.parseLong(path.substring(path.lastIndexOf('/') + 1));
    } else {
      return 0;
    }
  }

  public static void setTimestamp(DataMap data) {
    data.putLong(COLUMN_TIMESTAMP, System.currentTimeMillis());
  }

  public abstract static class DataItemProcessor {
    public abstract void process(DataItem item);

    public void postProcess() {
    }
  }

  public static void processDataItems(GoogleApiClient apiClient, String pathPrefix, final DataItemProcessor processor) {
    Uri uri = new Uri.Builder().scheme(PutDataRequest.WEAR_URI_SCHEME).path(pathPrefix).build();
    Wearable.DataApi.getDataItems(apiClient, uri, DataApi.FILTER_PREFIX)
        .setResultCallback(new ResultCallback<DataItemBuffer>() {
          @Override
          public void onResult(DataItemBuffer result) {
            if (result.getStatus().isSuccess()) {
              for (DataItem item : result) {
                processor.process(item);
              }
              result.release();
              processor.postProcess();
            }
          }
        });
  }
}
