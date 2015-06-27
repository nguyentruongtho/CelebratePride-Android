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
    int width = source.getWidth();
    int height = source.getHeight();
    Bitmap result = Bitmap.createBitmap(width, height, source.getConfig());
    Bitmap overlay = Bitmap.createBitmap(width, height, source.getConfig());

    int[] pixels = new int[width * height];
    int delta = source.getHeight() / 6;
    int color1 = res.getColor(R.color.celebrate_color_overlay1);
    int color2 = res.getColor(R.color.celebrate_color_overlay2);
    int color3 = res.getColor(R.color.celebrate_color_overlay3);
    int color4 = res.getColor(R.color.celebrate_color_overlay4);
    int color5 = res.getColor(R.color.celebrate_color_overlay5);
    int color6 = res.getColor(R.color.celebrate_color_overlay6);
    for (int y = 0; y < height; ++y) {
      for (int x = 0; x < width; ++x) {
        if (y / delta < 1) {
          pixels[x + y * width] = color1;
        } else if (y / delta < 2) {
          pixels[x + y * width] = color2;
        } else if (y / delta < 3) {
          pixels[x + y * width] = color3;
        } else if (y / delta < 4) {
          pixels[x + y * width] = color4;
        } else if (y / delta < 5) {
          pixels[x + y * width] = color5;
        } else {
          pixels[x + y * width] = color6;
        }
      }
    }
    overlay.setPixels(pixels, 0, width, 0, 0, width, height);

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
