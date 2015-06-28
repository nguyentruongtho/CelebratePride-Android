package net.thoj.celebratepride;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import com.squareup.picasso.Transformation;

public class CelebratePrideTransformation implements Transformation {
  private final int[] colors;

  public CelebratePrideTransformation(Context context) {
    Resources res = context.getResources();

    colors = new int[] {
        res.getColor(R.color.celebrate_color_overlay1),
        res.getColor(R.color.celebrate_color_overlay2),
        res.getColor(R.color.celebrate_color_overlay3),
        res.getColor(R.color.celebrate_color_overlay4),
        res.getColor(R.color.celebrate_color_overlay5),
        res.getColor(R.color.celebrate_color_overlay6)
    };
  }

  @Override public Bitmap transform(Bitmap source) {
    int width = source.getWidth();
    int height = source.getHeight();
    Bitmap result = Bitmap.createBitmap(width, height, source.getConfig());
    Bitmap overlay = Bitmap.createBitmap(1, colors.length, source.getConfig());

    Canvas canvas = new Canvas(result);
    canvas.drawBitmap(source, new Matrix(), null);

    for (int i = 0; i < colors.length; ++i) {
      overlay.setPixel(0, i, colors[i]);
    }

    canvas.drawBitmap(overlay, new Rect(0, 0, 1, colors.length - 1),
        new Rect(0, 0, width - 1, height - 1), null);

    if (source != result) {
      source.recycle();
    }
    if (overlay != result) {
      overlay.recycle();
    }
    return result;
  }

  @Override public String key() {
    return "celebrate_pride";
  }
}
