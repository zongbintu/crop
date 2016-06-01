package com.tu.crop;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public interface CropHandler {

  void onPhotoCropped(Uri uri);

  void onCropCancel();

  void onCropFailed(String message);

  CropParams getCropParams();

  Context getContext();

  /**
   * Call {@link Activity#startActivityForResult(Intent, int)}
   */
  void startActivityForResult(Intent intent, int requestCode);
}
