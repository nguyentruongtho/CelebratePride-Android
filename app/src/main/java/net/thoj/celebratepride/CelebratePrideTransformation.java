package net.thoj.celebratepride;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import com.squareup.picasso.Transformation;

public class CelebratePrideTransformation implements Transformation {
  private final Resources res;

  public CelebratePrideTransformation(Context context) {
    this.res = context.getResources();
  }

  @Override public Bitmap transform(Bitmap source) {
    Bitmap result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());
    Bitmap overlay = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());
    int delta = source.getHeight() / 6;
    for (int y=0; y<source.getHeight(); ++y) {
      for (int x=0; x<source.getWidth(); ++x) {
        if (y/delta < 1) {
          overlay.setPixel(x, y, res.getColor(R.color.celebrate_color_overlay1));
        } else if (y/delta < 2) {
          overlay.setPixel(x, y, res.getColor(R.color.celebrate_color_overlay2));
        } else if (y/delta < 3) {
          overlay.setPixel(x, y, res.getColor(R.color.celebrate_color_overlay3));
        } else if (y/delta < 4) {
          overlay.setPixel(x, y, res.getColor(R.color.celebrate_color_overlay4));
        } else if (y/delta < 5) {
          overlay.setPixel(x, y, res.getColor(R.color.celebrate_color_overlay5));
        } else {
          overlay.setPixel(x, y, res.getColor(R.color.celebrate_color_overlay6));
        }
      }
    }

    Canvas canvas = new Canvas(result);
    canvas.drawBitmap(source, new Matrix(), null);
    canvas.drawBitmap(overlay, 0, 0, null);
    if (result != source) {
      source.recycle();
    }
    return result;
  }

  @Override public String key() {
    return "celebrate_pride";
  }
}
