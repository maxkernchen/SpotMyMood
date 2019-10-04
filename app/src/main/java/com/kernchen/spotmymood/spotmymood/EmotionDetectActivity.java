
package com.kernchen.spotmymood.spotmymood;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.kernchen.spotmymood.R;
import com.kernchen.spotmymood.spotmymood.helper.CameraHelperActivity;
/*
old API
import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.Order;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.emotion.rest.EmotionServiceException;
*/

import com.kernchen.spotmymood.spotmymood.helper.FailedImageView;
import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;
import com.kernchen.spotmymood.spotmymood.helper.ImageHelper;
import com.microsoft.projectoxford.face.rest.ClientException;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Main class which holds the first screen and also detects and recognizes emotions using the
 * Microsoft Emotion API.
 * Class written with the help of the sample application at
 * https://github.com/Microsoft/Cognitive-Emotion-Android
 *
 * @author Max Kernchen
 * @version 1.2 -  6/12/2019
 *
 */
public class EmotionDetectActivity extends AppCompatActivity {

    // request code for taking a picture, currently no gallery pictures are supported
    private static final int REQUEST_TAKE_PICTURE = 0;
    // Compressing Value for bitmap of image taken
    private static final int COMPRESSION_BIT_MAP = 50;
    // Button which is displayed on main page to selected an image
    private Button selectImageButton;
    // The URI of the image selected to detect.
    private Uri imageUri;
    // The image selected to detect as a bitmap.
    private Bitmap imageBitMap;
    // client we use to send a web service request with the bytecode of the image
    private FaceServiceClient faceClient;
    //dialog for showing progress of analyzing image
    private ProgressDialog loadingDialog;
    // tag used for logging this activity
    private static final String logTag = "EmotionDetectActivity";

    private Byte[] failedImage;

    /**
     * On the start of the application set the layout and create a new emotion client with the key
     * passed in.
     * @param savedInstanceState - Bundles for passing data, not used in this case
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);
        faceClient = new FaceServiceRestClient(getString(R.string.api_end_point),
                getString(R.string.face_subscription_key));
        selectImageButton = (Button) findViewById(R.id.buttonSelectImage);
        // show a warning that these images are sent to microsoft web services and may be stored
        Toast.makeText(this, getString(R.string.consent_warning),
                Toast.LENGTH_LONG).show();

    }

    /**
     * Helper method which calls the inner class detect emotion
     */
    private void startDetection() {
        //disable button and create progress dialog
        selectImageButton.setEnabled(false);
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loadingDialog.setMessage(getString(R.string.loading_message));
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();

        // call inner class detectEmotion
        try {
            new DetectEmotion().execute();
        } catch (Exception e) {
           //
            // Log.d(logTag,e.getMessage());
        }
    }

    /**
     * Called when take picture button is clicked
      * @param view the view where this event came from
     */
    public void takePictureClick(View view) {
        //go to camera helper activity to take photo and send back the image Uri
      Intent toCameraHelper  = new Intent(EmotionDetectActivity.this,
              CameraHelperActivity.class);
      startActivityForResult(toCameraHelper, REQUEST_TAKE_PICTURE);

    }

    /**
     * On the finish of the Camera Helper Activity, get the URI from the data
     * @param requestCode code for what we should do, not used in this case
     * @param resultCode result code for the previous activity, checked to make sure it wasn't
     *                   canceled
     * @param data - data from the previous activity in this case an imageUri
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

                if (resultCode == RESULT_OK) {
                    //set imageUri field for use later
                    imageUri = data.getData();
                    //go to bitmap helper to get a bitmap that is scaled down
                    imageBitMap = ImageHelper.compressBitMap(
                            imageUri, getContentResolver());
                    Log.d(logTag,imageUri.getPath());
                    Log.d(logTag,String.valueOf(imageBitMap.getByteCount()));
                    // if we get a bitmap back then go ahead and try to detect a face and emotion
                    if (imageBitMap != null) {

                        startDetection();
                    }else{
                        Toast.makeText(this, getString(R.string.image_processing_error)
                                , Toast.LENGTH_LONG).show();
                        //Log.e(logTag,"The bit map was null coming from ImageHelper activity");
                    }
                    // delete the temporary file
                    //deletePictures();
                }
    }

    /**
     * Send the bitmap as a byte array to the emotion web service
     * @return a list of results of emotion
     * @throws IOException - exception thrown if byte array could not be created
     */
    private Face[] getResults() throws ClientException,
            IOException {

        // create byte array to send to emotion client
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        imageBitMap.compress(Bitmap.CompressFormat.JPEG, this.COMPRESSION_BIT_MAP, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        //for debugging bad images
      //  failedImage = output.toByteArray();

        // only get emotion response
        FaceServiceClient.FaceAttributeType[] faceAttributeTypes =
                new FaceServiceClient.FaceAttributeType[1];
        faceAttributeTypes[0] = FaceServiceClient.FaceAttributeType.Emotion;

        return faceClient.detect(inputStream,true,false, faceAttributeTypes);
    }

    /**
     * Helper method which deletes the temporary picture we stored.
     * Unfortunately we cannot delete any pictures stored automatically from using the camera
     * activity
     */
    private boolean deletePictures(){
        File file = new File(imageUri.getPath());
        return file.delete();
    }

    /**
     * Helper method which checks to see if there is an internet connection
     * @return true for a connection is available false for no connections
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }




    /**
     * Helper method to navigate to the ResultsActivity and send the scores and emotions as a bundle
     */
    private void toResults(Emotion emotionResults){
        Intent toResults = new Intent(EmotionDetectActivity.this,
                EmotionResultActivity.class);
        //add a bundle of the ArrayLists for scores and emotions
        this.orderedEmotionsToMap(emotionResults);

        this.startActivity(toResults);
    }

    /**
     *
     * @param emotion
     * @return
     */
    private SortedMap<String,Double> orderedEmotionsToMap (Emotion emotion){
        SortedMap<String,Double> emotionsOrdered = new TreeMap<String,Double>();
        Field [] fields = emotion.getClass().getDeclaredFields();

        return null;
    }

    /**
     * Inner class which represents an Async task to process the request to the emotion client
     */
    private class DetectEmotion extends AsyncTask<String, String, Face[]> {
        // store the exception for use onPostExecute
        Exception exception;

        /**
         * get the results in a background thread
         * @param args - not used here
         * @return - the results back from the web service call
         */
        @Override
        protected Face[] doInBackground(String... args) {
           // try and get the results, store the errors
                try {
                    Face[] temp = getResults();
                    return temp;
                } catch (Exception e) {
                    //Log.e(logTag,e.toString());
                    exception = e;
                }

            return null;
        }

        /**
         * Once the background thread has finished confirm we got a result or display appropriate
         * error messages
         * @param result
         */
        @Override
        protected void onPostExecute(Face[] result) {
            super.onPostExecute(result);
            loadingDialog.dismiss();
            selectImageButton.setEnabled(true);
            if (exception != null) {

                //check to see if we have an internet connection
                if (!isNetworkAvailable()) {
                    //show Dialog for no internet connection
                    final AlertDialog finished = new AlertDialog.Builder(
                            EmotionDetectActivity.this).create();

                    finished.setTitle("No Internet");
                    finished.setMessage(getString(R.string.no_internet));


                        finished.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        finished.dismiss();
                                    }
                                });

                    finished.show();

                }

                // Log.e(logTag,exception.toString());
            } else {
                //if we got no results most likely a face is not in the picture
                if (result.length == 0) {

                    final AlertDialog finished = new AlertDialog.Builder
                            (EmotionDetectActivity.this).create();

                    finished.setTitle("No Mood");
                    finished.setMessage(getString(R.string.no_faces));
                    finished.setButton(AlertDialog.BUTTON_POSITIVE, "ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finished.dismiss();
                                }
                            });

                    //finished.show();
                    Intent toFailedImage  = new Intent(EmotionDetectActivity.this,
                            FailedImageView.class);
                   // toFailedImage.putExtra("FAILED_IMAGE",imageBitMap);
                    startActivity(toFailedImage);
                    //a successful result
                } else if (result.length > 0) {
                    // get a ranked list of results
                    Log.d(logTag,"found Results!");
                    //emotionResults = result.get(0).scores.ToRankedList(Order.DESCENDING);
                    Emotion emotionResults = result[0].faceAttributes.emotion;
                    ArrayList<Double> emotionsOrdered = new ArrayList<Double>();
                    // add all eight emotions detected by Face API


                    toResults(emotionResults);

                }
            }

        }

    }

}
