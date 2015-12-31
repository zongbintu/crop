package com.tu.crop;

import android.graphics.Bitmap;
import android.net.Uri;

public class CropParams {

	public static final String CROP_TYPE = "image/*";
	public static final String OUTPUT_FORMAT = Bitmap.CompressFormat.JPEG
			.toString();

	public static final int DEFAULT_ASPECT = 1;
	public static final int DEFAULT_OUTPUT = 300;

	public Uri uri;
	public Uri cropResult;

	public String type;
	public String outputFormat;
	public String crop;

	public boolean scale;
	public boolean returnData;
	public boolean noFaceDetection;
	public boolean scaleUpIfNeeded;

	public int aspectX;
	public int aspectY;

	public int outputX;
	public int outputY;

	public CropParams() {
		uri = CropHelper.buildUri();
		type = CROP_TYPE;
		outputFormat = OUTPUT_FORMAT;
		crop = "true";
		scale = true;
		returnData = false;
		noFaceDetection = true;
		scaleUpIfNeeded = true;
		aspectX = DEFAULT_ASPECT;
		aspectY = DEFAULT_ASPECT;
		outputX = DEFAULT_OUTPUT;
		outputY = DEFAULT_OUTPUT;
	}

	public static CropParams initCropParams(){
		return new CropParams();
	}
}
