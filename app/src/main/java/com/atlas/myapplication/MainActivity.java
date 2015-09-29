package com.atlas.myapplication;

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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CropHandler {
    private CropParams mCropParams;
    private ImageView mImageView;
    private List<Uri> cropUris = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCropParams = new CropParams();
        mImageView = (ImageView) findViewById(R.id.imageview);
        findViewById(R.id.test).setOnClickListener(this);
        findViewById(R.id.gallery).setOnClickListener(this);
        findViewById(R.id.clean).setOnClickListener(this);
    }

    @Override
    public void onPhotoCropped(Uri uri) {
        mImageView.setImageURI(uri);
        cropUris.add(uri);
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
                Intent i = CropHelper.buildCaptureIntent(mCropParams.uri);
                startActivityForResult(i, CropHelper.REQUEST_CAMERA);
                break;
            case R.id.gallery:
                startActivityForResult(CropHelper.buildGalleryIntent(), CropHelper.REQUEST_GALLERY);
                break;
            case R.id.clean:
                for (Uri uri : cropUris) {
                    CropHelper.clearCachedCropFile(uri);
                    Toast.makeText(this, uri.getPath() + " is delete", Toast.LENGTH_LONG).show();
                }
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
        for (Uri uri : cropUris)
            CropHelper.clearCachedCropFile(uri);
        super.onDestroy();
    }
}
