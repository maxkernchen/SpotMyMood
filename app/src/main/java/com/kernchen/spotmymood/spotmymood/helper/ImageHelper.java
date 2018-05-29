
package com.kernchen.spotmymood.spotmymood.helper;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;

import android.support.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * Static helper class written with the help of Android Developer guides at:
 * https://developer.android.com/topic/performance/graphics/load-bitmap#load-bitmap
 * https://android-developers.googleblog.com/2016/12/introducing-the-exifinterface-support-library.html
 *
 * This class scales down a bitmap to prevent out of memory errors. Current testing has a 5 - 8
 * megapixel image using 80-100mb of memory
 *
 * @author Max Kernchen
 * @version 1/1 5/28/2018
 */
public class ImageHelper {
    // max width of an image
    private static final int IMAGE_MAX_WIDTH = 1280;
    // max height of an image
    private static final int IMAGE_MAX_HEIGHT = 720;
    // log tag for logging
    private static final String logTag = "ImageHelper";


    /**
     * Compresses the BitMap if necessary
     * @param imageUri the uri of the image
     * @param contentResolver content resolve query data
     * @return a compressed or original bitmap
     */
    public static Bitmap compressBitMap(Uri imageUri, ContentResolver contentResolver)
    {
        try {
            // Load the image into InputStream.
            InputStream imageInputStream = contentResolver.openInputStream(imageUri);

            // we only need the dimensions so don't decode everything
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Rect outPadding = new Rect();
            BitmapFactory.decodeStream(imageInputStream, outPadding, options);

            // get width and height of current image
            int imgHeight = options.outHeight;
            int imgWidth  = options.outWidth;

            // calculate inSampleSize this is a factor as to which we will scale the bitmap down
            options.inSampleSize = calculateSampleSize(imgWidth, imgHeight);
            options.inJustDecodeBounds = false;
            imageInputStream.close();

            // Load the bitmap with the options which may contain a inSampleSize > 1
            // which will lead to a scaled down bitmap
            imageInputStream = contentResolver.openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(imageInputStream, outPadding, options);

            imageInputStream.close();


            return rotateBitmap(bitmap, getImageRotationAngle(imageUri));
        } catch (Exception e) {
            Log.e(logTag,e.toString());
            return null;
        }

    }

    /**
     * Helper method which calculates our samplesize for BitMap options
     * This is a factor as to which we scale down our image.
     *
     * Ex. inSampleSize == 4
     * 1920 X 1080  / inSampleSize == 480 x 270
     * @param imgWidth our target width
     * @param imgHeight our target height
     * @return the int value as to which to scale our image, must be a power of 2
     */
    private static int calculateSampleSize(int imgWidth, int imgHeight ) {
        int inSampleSize = 1;
        //compare half width and height to not scale down too much
        int halfWidth  = imgWidth / 2;
        int halfHeight = imgHeight / 2;
        // increase samplesize until we have met our image
        while (((halfWidth/inSampleSize) >= IMAGE_MAX_WIDTH) &&
                ((halfHeight/inSampleSize) >= IMAGE_MAX_HEIGHT)) {
            //sample size should be a power of two
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    /**
     * Helper method which uses Exif image to data to get the orientation of the image
     * Image must be portrait for emotion/face detection to work
     * @param imageUri the Uri of the image
     * @return the angle of rotation
     */
    private static int getImageRotationAngle(Uri imageUri) {
        int angle = 0;
        ExifInterface exif;
        // using Exif data get the orientation and return an angle which corresponds to it
        try {
            exif = new ExifInterface(imageUri.getPath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    angle = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    angle = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    angle = 90;
                    break;
                default:
                    break;
            }
        }catch(IOException ioe){
            Log.e(logTag,ioe.toString());
    }
        return angle;
    }

    /**
     * Rotate the bitmap by an angle using a Matrix
     * @param bitmap the bitmap to rotate
     * @param angle the current angle of the bitmap
     * @return a bitmap in portrait orientation
     */
    private static Bitmap rotateBitmap(Bitmap bitmap, int angle) {

        if (angle != 0) {
            Matrix matrix = new Matrix();
            //rotate the matrix by left matrix multiplication
            matrix.postRotate(angle);
            Log.d(logTag,"Image rotated by " + angle + " degrees");
            //create a new bitmap that is rotated into portrait orientation
            return Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } else {
            return bitmap;
        }
    }

}
