# Crop

使用系统自带裁剪，返回Uri。解决4.4以后改变Uri规则兼容问题

# Usage
   
   
###### 1、Using Crop Library in your application

Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:  

`allprojects {     
    repositories {    
        ...
        maven { url "https://jitpack.io" }
    }
} `   

Add the dependency

`dependencies {  
    compile 'com.github.crop:1.1'  
}`
   
###### 2、init CropParams  

裁剪  
mCropParams = CropParams.initCropParams();

不裁剪  
mCropParams = CropParams.initCropParams();  
mCropParams.crop = "false";


###### 3、implements CropHandler



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


###### 4、onActivityForResult

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


###### 5、clean croped file

`CropHelper.cleanAllCropCache(this);`
