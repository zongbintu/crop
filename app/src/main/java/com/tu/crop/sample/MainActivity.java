package com.tu.crop.sample;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.tu.crop.CropHandler;
import com.tu.crop.CropHelper;
import com.tu.crop.CropParams;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CropHandler {
    private CropParams mCropParams;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCropParams = new CropParams();
        mImageView = (ImageView) findViewById(R.id.imageview);
        findViewById(R.id.test).setOnClickListener(this);
        findViewById(R.id.gallery).setOnClickListener(this);
        findViewById(R.id.clean).setOnClickListener(this);
        findViewById(R.id.gallery_not_crop).setOnClickListener(this);
        findViewById(R.id.capture_not_crop).setOnClickListener(this);
    }

    @Override
    public void onPhotoCropped(Uri uri) {
        mImageView.setImageURI(uri);
//        mImageView.setImageBitmap(BitmapUtil.compressImage(uri.getPath(),800f,480f,100));
    }

    @Override
    public void onCropCancel() {
        Toast.makeText(this, "crop cancel", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCropFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public CropParams getCropParams() {
        return mCropParams;
    }

    @Override
    public Activity getContext() {
        return this;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.test:
                mCropParams = CropParams.initCropParams();
                startActivityForResult(CropHelper.buildCaptureIntent(mCropParams.uri), CropHelper.REQUEST_CAMERA);
                break;
            case R.id.gallery:
                mCropParams = CropParams.initCropParams();
                startActivityForResult(CropHelper.buildGalleryIntent(), CropHelper.REQUEST_GALLERY);
                break;
            case R.id.gallery_not_crop:
                mCropParams = CropParams.initCropParams();
                mCropParams.crop = "false";
                startActivityForResult(CropHelper.buildGalleryIntent(), CropHelper.REQUEST_GALLERY);
                break;
            case R.id.capture_not_crop:
                mCropParams = CropParams.initCropParams();
                mCropParams.crop = "false";
                startActivityForResult(CropHelper.buildCaptureIntent(mCropParams.uri), CropHelper.REQUEST_CAMERA);
                break;
            case R.id.clean:
                boolean flag = CropHelper.cleanAllCropCache(this);
                Toast.makeText(this, "delete:" + flag, Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CropHelper.REQUEST_CAMERA:
            case CropHelper.REQUEST_GALLERY:
            case CropHelper.REQUEST_CROP:
                CropHelper.handleResult(this, requestCode, resultCode, data);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        CropHelper.cleanAllCropCache(this);
        super.onDestroy();
    }
}
