package net.grappendorf.buyright;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;
import net.grappendorf.buyrightcommon.WearableHelper;

public class NotificationService extends WearableListenerService {

  private static final int NOTIFICATION_ID = 1;

  @Override
  public void onDataChanged(DataEventBuffer dataEvents) {
    for (DataEvent dataEvent : dataEvents) {
      if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
        if (WearableHelper.NOTIFICATION.equals(dataEvent.getDataItem().getUri().getPath())) {
          DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
          int numTodos = dataMapItem.getDataMap().getInt(WearableHelper.COLUMN_NUM_TODOS);
          sendNotification(numTodos);
        }
      }
    }
  }

  private void sendNotification(int numTodos) {
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

    if (numTodos > 0) {
      NotificationCompat.Builder builder = new android.support.v4.app.NotificationCompat.Builder(this);

      builder.setSmallIcon(R.mipmap.logo);
      builder.setContentTitle(getString(R.string.app_name));
      if (numTodos > 0) {
        builder.setContentText(getResources().getString(R.string.num_items_todo,
            getResources().getQuantityString(R.plurals.items, numTodos, (int) numTodos)));
      } else {
        builder.setContentText(getResources().getString(R.string.nothing_to_do));
      }

      Intent viewIntent = new Intent(this, ShoppingActivity.class);
      PendingIntent pendingViewIntent = PendingIntent.getActivity(this, 0, viewIntent, 0);
      builder.setContentIntent(pendingViewIntent);

      notificationManager.notify(NOTIFICATION_ID, builder.build());
    } else {
      notificationManager.cancelAll();
    }
  }
}
