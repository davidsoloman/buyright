package net.grappendorf.buyright;

import android.content.Context;
import android.view.View;
import android.widget.Toast;
import com.gc.materialdesign.widgets.Dialog;

public class UIHelper {
  public static abstract class DialogListener {
    public abstract void onAccept();

    public void onCancel() {
    }
  }

  public static void showDialog(Context context, String title, String message, final DialogListener listener) {
    Dialog dialog = new Dialog(context, title, message);
    dialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        listener.onAccept();
      }
    });
    dialog.addCancelButton(context.getString(R.string.cancel));
    dialog.setOnCancelButtonClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        listener.onCancel();
      }
    });
    dialog.show();
  }

  public static void toast(Context context, int messageResId, int duration) {
    Toast toast = Toast.makeText(context, messageResId, duration);
    View view = toast.getView();
    view.setBackgroundResource(R.drawable.toast);
    toast.setView(view);
    toast.show();
  }

  public static void toast(Context context, String message, int duration) {
    Toast toast = Toast.makeText(context, message, duration);
    View view = toast.getView();
    view.setBackgroundResource(R.drawable.toast);
    toast.setView(view);
    toast.show();
  }
}
