package net.grappendorf.buyright;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.gc.materialdesign.views.Button;
import net.grappendorf.buyrightcommon.Product;
import net.grappendorf.buyrightcommon.ProductListener;

public class ProductEditDialog extends DialogFragment {

  private Product product;
  private EditText name;
  private Button save;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.product_edit, container);
    save = (Button) view.findViewById(R.id.save);
    save.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        save();
      }
    });
    view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dismiss();
      }
    });
    name = ((EditText) view.findViewById(R.id.name));
    name.setText(product.getName());
    name.setOnKeyListener(new View.OnKeyListener() {
      @Override
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
          validate();
        }
        return false;
      }
    });
    validate();
    return view;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Dialog dialog = super.onCreateDialog(savedInstanceState);
    if (getArguments() != null) {
      product = (Product) getArguments().get(Product.PRODUCT);
    }
    if (product == null) {
      product = new Product();
    }
    dialog.setTitle(product.isNew() ? R.string.add_product : R.string.edit_product);
    int dividerId = dialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
    View divider = dialog.findViewById(dividerId);
    divider.setBackgroundColor(getResources().getColor(R.color.dialog_background));
    return dialog;
  }

  public void save() {
    product.setName(name.getText().toString());
    ((ProductListener) getActivity()).onProductSave(product);
    dismiss();
  }

  private void validate() {
    boolean valid = !name.getText().toString().isEmpty();
    save.setEnabled(valid);
  }
}
