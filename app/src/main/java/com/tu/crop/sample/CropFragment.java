package com.tu.crop.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.tu.crop.CropHandler;
import com.tu.crop.CropHelper;
import com.tu.crop.CropParams;

public class CropFragment extends Fragment implements View.OnClickListener, CropHandler {
  private CropParams mCropParams;
  private ImageView mImageView;
  private Button containerButton;

  public CropFragment() {
    // Required empty public constructor
  }

  public static CropFragment newInstance() {
    CropFragment fragment = new CropFragment();
    return fragment;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mCropParams = new CropParams();
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.crop_layout, container, false);
    mImageView = (ImageView) rootView.findViewById(R.id.imageview);
    rootView.findViewById(R.id.camera).setOnClickListener(this);
    rootView.findViewById(R.id.gallery).setOnClickListener(this);
    rootView.findViewById(R.id.clean).setOnClickListener(this);
    rootView.findViewById(R.id.gallery_not_crop).setOnClickListener(this);
    rootView.findViewById(R.id.capture_not_crop).setOnClickListener(this);

    containerButton = (Button) rootView.findViewById(R.id.change_container);
    containerButton.setText(R.string.to_activity);
    containerButton.setOnClickListener(this);
    return rootView;
  }

  @Override public void onPhotoCropped(Uri uri) {
    mImageView.setImageURI(uri);
  }

  @Override public void onCropCancel() {
    Toast.makeText(getContext(), "crop cancel", Toast.LENGTH_SHORT).show();
  }

  @Override public void onCropFailed(String message) {
    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
  }

  @Override public CropParams getCropParams() {
    return mCropParams;
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.camera:
        mCropParams = CropParams.initCropParams();
        startActivityForResult(CropHelper.buildCaptureIntent(mCropParams.uri),
            CropHelper.REQUEST_CAMERA);
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
        startActivityForResult(CropHelper.buildCaptureIntent(mCropParams.uri),
            CropHelper.REQUEST_CAMERA);
        break;
      case R.id.clean:
        boolean flag = CropHelper.cleanAllCropCache(getContext());
        Toast.makeText(getContext(), "delete:" + flag, Toast.LENGTH_SHORT).show();
        break;
      case R.id.change_container:
        startActivity(new Intent(getActivity(),MainActivity.class));
        break;
    }
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case CropHelper.REQUEST_CAMERA:
      case CropHelper.REQUEST_GALLERY:
      case CropHelper.REQUEST_CROP:
        CropHelper.handleResult(this, requestCode, resultCode, data);
        break;
    }
  }

  @Override public void onDestroy() {
    CropHelper.cleanAllCropCache(getActivity().getApplicationContext());
    super.onDestroy();
  }
}
