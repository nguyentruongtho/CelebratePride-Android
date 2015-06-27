package net.thoj.celebratepride;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ShareActionProvider;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.Intent.ACTION_PICK;
import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.MIME_TYPE;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = MainActivity.class.getSimpleName();

  private static final int REQUEST_TAKE_PICTURE = 1;
  private static final int REQUEST_GALLERY_PICTURE = 2;
  private static final String FIRST_LAUNCH_PREF = TAG + ".firstLaunch";

  @InjectView(R.id.photo) ImageButton photo;
  @OnClick(R.id.photo) void sharePhoto() {
    if (lastUri != null) {
      Picasso.with(this)
          .load(lastUri)
          .transform(new CelebratePrideTransformation(this))
          .into(shareBitmapTarget);
    } else {
      Picasso.with(this)
          .load(R.drawable.sample)
          .into(shareBitmapTarget);
    }
  }

  @OnClick(R.id.take_picture) void handleTakePicture() {
    Intent takePictureIntent = new Intent(ACTION_IMAGE_CAPTURE);
    if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
      startActivityForResult(takePictureIntent, REQUEST_TAKE_PICTURE);
    }
  }

  @OnClick(R.id.pick_photo) void handlePickPhoto() {
    Intent pickGalleryPictureIntent = new Intent(ACTION_PICK, EXTERNAL_CONTENT_URI);
    startActivityForResult(pickGalleryPictureIntent, REQUEST_GALLERY_PICTURE);
  }

  @OnClick(R.id.import_facebook) void handleImportFacebook() {
    // TODO don't want to add heavy facebook library and require internet permission for now
  }

  @OnClick({ R.id.t7m_1, R.id.t7m_2 }) void thongAssThoBayMau() {
    openLink("https://www.facebook.com/ThoBayMau");
  }

  @OnClick({ R.id.source_1, R.id.source_2 }) void openSourceCodePage() {
    openLink("https://bit.ly/celebrate-pride");
  }

  private void openLink(String link) {
    try {
      Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
      startActivity(myIntent);
    } catch (ActivityNotFoundException e) {
      Toast.makeText(this, "No application can handle this request."
          + " Please install a webbrowser",  Toast.LENGTH_LONG).show();
      e.printStackTrace();
    }
  }

  private ShareActionProvider shareActionProvider;
  private Target shareBitmapTarget;
  private Uri lastUri;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ButterKnife.inject(this);
    shareBitmapTarget = new ShareTarget();
    shareActionProvider = new ShareActionProvider(this);
  }

  @Override protected void onDestroy() {
    shareBitmapTarget = null;
    shareActionProvider = null;
    super.onDestroy();
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);

      if (resultCode == Activity.RESULT_OK && data != null) {
        switch (requestCode) {
          case REQUEST_GALLERY_PICTURE:
            handleGalleryPicture(data);
            break;
          case REQUEST_TAKE_PICTURE:
            handleCameraPicture(data);
            break;
        }
      }
  }

  private void handleCameraPicture(Intent data) {
    try {
      Bundle extras = data.getExtras();
      Bitmap imageBitmap = (Bitmap) extras.get("data");

      handleBitmapPicture(imageBitmap);
    } catch (Exception ex) {
      Toast.makeText(this, R.string.error_get_image_from_camera, Toast.LENGTH_LONG).show();
    }
  }

  private void handleBitmapPicture(Bitmap imageBitmap) {
    File imageFileFolder = new File(this.getCacheDir(), "CelebratePrideTransformation");
    if (!imageFileFolder.exists()) {
      imageFileFolder.mkdir();
    }

    FileOutputStream out = null;
    File imageFile = new File(imageFileFolder, "celebrate-pride-" + System.currentTimeMillis() + ".jpg");
    try {
      out = new FileOutputStream(imageFile);
      imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
      out.flush();
    } catch (IOException e) {
      Log.e(TAG, "Failed to convert image to JPEG" + e.getMessage());
      return;
    } finally {
      try {
        if (out != null) {
          out.close();
        }
      } catch (IOException ignored) {
      }
    }

    displayPhoto(imageFile);
  }

  private void handleGalleryPicture(Intent data) {
    Uri selectedImage = data.getData();
    String[] filePathColumn = { MIME_TYPE, DATA };

    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
    cursor.moveToFirst();

    int typeColumnIndex = cursor.getColumnIndex(filePathColumn[0]);
    int dataColumnIndex = cursor.getColumnIndex(filePathColumn[1]);
    String dataType = cursor.getString(typeColumnIndex);
    String dataPath = cursor.getString(dataColumnIndex);
    cursor.close();

    if (dataPath == null) {
      Toast.makeText(this, R.string.error_from_online_gallery, Toast.LENGTH_LONG).show();
      return;
    }

    if (dataPath.startsWith("http")) {
      displayPhoto(dataPath);
      Toast.makeText(this, R.string.error_from_online_gallery, Toast.LENGTH_LONG).show();
    } else {
      File imageFile = new File(dataPath);

      if (imageFile.exists()) {
        displayPhoto(imageFile);
      }
    }
  }

  private void displayPhoto(final Uri imageUri) {
    Picasso.with(this)
        .load(imageUri)
        .transform(new CelebratePrideTransformation(this))
        .placeholder(R.drawable.sample)
        .error(R.drawable.t7m)
        .into(photo, new Callback() {
          @Override public void onSuccess() {
            lastUri = imageUri;

            checkFirstLaunch();
          }

          @Override public void onError() {
            Toast.makeText(MainActivity.this, R.string.error_load_image, Toast.LENGTH_SHORT).show();
          }
        });
  }

  private void checkFirstLaunch() {
    SharedPreferences settings = getSharedPreferences(TAG, MODE_PRIVATE);
    if (settings.getBoolean(FIRST_LAUNCH_PREF, true)) {
      Toast.makeText(this, R.string.guide_how_to_share, Toast.LENGTH_LONG).show();
      settings.edit().putBoolean(FIRST_LAUNCH_PREF, false).apply();
    }
  }

  private void displayPhoto(File savedImageFile) {
    displayPhoto(Uri.fromFile(savedImageFile));
  }

  private void displayPhoto(String dataPath) {
    displayPhoto(Uri.parse(dataPath));
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.about_me) {
      openLink("http://twitter.com/thontu");
      return true;
    }

    return super.onOptionsItemSelected(item);
  }


  public class ShareTarget implements Target {
    @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
      if (shareActionProvider != null) {
        String pathOfBmp = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null, null);
        Uri bmpUri = Uri.parse(pathOfBmp);
        final Intent sendIntent = new Intent(android.content.Intent.ACTION_SEND);
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        sendIntent.setType("image/*");
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_title));
        sendIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.share_title));
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_title));
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_to)));
        shareActionProvider.setShareIntent(sendIntent);
      }
    }

    @Override public void onBitmapFailed(Drawable errorDrawable) {
      Toast.makeText(MainActivity.this, R.string.error_load_image, Toast.LENGTH_LONG).show();
    }

    @Override public void onPrepareLoad(Drawable placeHolderDrawable) {
    }
  };
}
