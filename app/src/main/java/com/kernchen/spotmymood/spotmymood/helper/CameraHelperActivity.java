
package com.kernchen.spotmymood.spotmymood.helper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.kernchen.spotmymood.R;
import java.io.File;
import java.io.IOException;

/**
 * Class which takes a picture and sends back to Uri to the calling activity
 * @author Max Kernchen
 * @version 1.1 5/28/2018
 */
public class CameraHelperActivity extends AppCompatActivity {

    // only one type of request currently, to take a photo
    private static final int REQUEST_TAKE_PHOTO = 0;
    //tag used for logging
    private static final String logTag = "CameraHelperActivity";
    // The URI of photo taken with the camera
    private Uri uriPhotoTaken;


    /**
     * Set the layout and call takePhoto
     * @param savedInstanceState - bundles are not used in this case
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture);
        this.takePhoto();
    }


    /**
     * Once take photo finishes send the Uri to the calling class
     * @param requestCode request code not used in this case
     * @param resultCode - check to make sure the activity was not cancelled
     * @param data - data from the Intent not used in this case
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Intent toRecognize = new Intent();
            toRecognize.setData(uriPhotoTaken);
            setResult(RESULT_OK, toRecognize);
        }
        //finish this activity even if it was cancelled
        finish();
    }

    /**
     * Take a picture using built in Android Camera Intents
     * and save a temporary photo file
     */
    public void takePhoto() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //make sure we have the camera activity
        if(cameraIntent.resolveActivity(getPackageManager()) != null) {
            // save the photo taken to a temporary file.
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try {
                File tempPicFile = File.createTempFile("IMG_", ".jpg", storageDir);

                //get the uri from the temp file
                uriPhotoTaken = Uri.fromFile(tempPicFile);
                //put the URI as an extra
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriPhotoTaken);

                startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);
            } catch (IOException ioe) {
                Log.e(logTag, ioe.toString());
            }
        }
    }


}
