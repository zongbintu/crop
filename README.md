# CropSimple

使用系统自带裁剪，返回Uri。解决4.4以后改变Uri规则兼容问题

# Usage

1. new CropParams()

2.implements CropHandler



    @Override
    public void onPhotoCropped(Uri uri) {
        //croped
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


3.onActivityForResult

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case CropHelper.REQUEST_CAMERA:
            case CropHelper.REQUEST_GALLERY:
            case CropHelper.REQUEST_CROP:
                CropHelper.handleResult(this, requestCode, resultCode, data);
                break;
        }
    }


4.clean croped file

CropHelper.clearCachedCropFile(uri);
