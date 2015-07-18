// http://www.jayway.com/2012/10/04/how-to-make-the-height-of-a-gridview-wrap-its-content/
// László Urszuly
// Public Domain

package net.grappendorf.buyright;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class NonScrollingGridView extends GridView {

  public NonScrollingGridView(Context context) {
    super(context);
  }

  public NonScrollingGridView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public NonScrollingGridView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int heightSpec;

    if (getLayoutParams().height == LayoutParams.WRAP_CONTENT) {
      heightSpec = MeasureSpec.makeMeasureSpec(
          // The great Android "hackatlon", the love, the magic.
          // The two leftmost bits in the height measure spec have
          // a special meaning, hence we can't use them to describe height.
          Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
    } else {
      heightSpec = heightMeasureSpec;
    }

    super.onMeasure(widthMeasureSpec, heightSpec);
  }
}
