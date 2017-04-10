package com.tu.crop.sample;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * @author Tu enum@foxmail.com.
 */

public class CameraActivity extends Activity {
  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera);
    findViewById(R.id.button_ok).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Uri outPutUri = getIntent().getParcelableExtra(MediaStore.EXTRA_OUTPUT);
        // TODO: 17/4/10 add uri  ,bitman ===
        Intent data = new Intent();
        data.putExtra(MediaStore.EXTRA_OUTPUT,outPutUri);
        setResult(Activity.RESULT_OK, data);
        finish();
      }
    });
  }
}
