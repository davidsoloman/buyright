package net.grappendorf.buyright;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.balysv.materialripple.MaterialRippleLayout;
import net.grappendorf.buyrightcommon.Product;

class ProductAdapter extends CursorAdapter {

  private boolean drawCardRipple;
  private boolean singleLineCards;

  public ProductAdapter(Context context, Cursor cursor) {
    super(context, cursor, 0);
  }

  public void setDrawCardRipple(boolean drawCardRipple) {
    this.drawCardRipple = drawCardRipple;
  }

  public void setSingleLineCards(boolean singleLineCards) {
    this.singleLineCards = singleLineCards;
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
    return LayoutInflater.from(context).inflate(R.layout.product_card, parent, false);
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor) {
    CardView card = (CardView) view;
    Product product = Product.createFromCursor(cursor);
    TextView name = ((TextView) view.findViewById(R.id.name));
    name.setText(product.getName());
    name.setTypeface(null, product.getId() != 0 ? Typeface.NORMAL : Typeface.ITALIC);
    name.setSingleLine(singleLineCards);
    card.setCardBackgroundColor(context.getResources().getColor(product.isTodo() ? R.color.product_todo_card : R.color.product_done_card));
    MaterialRippleLayout ripple = (MaterialRippleLayout) card.findViewById(R.id.ripple);
    ripple.setRippleDuration(drawCardRipple ? 250 : 0);
    ripple.setTag(product);
  }
}
