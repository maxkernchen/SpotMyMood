package com.kernchen.spotmymood.spotmymood;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.kernchen.spotmymood.R;

import java.util.ArrayList;

/**
 * Class which displays the results of the detecting emotions
 * a graph with the top 4 emotions is shown and a web view with the playlist is also shown
 * Graph created with MPAndroidChart: https://github.com/PhilJay/MPAndroidChart
 *
 * @author Max Kernchen
 * @version 1.1 5/28/2018
 *
 */
public class EmotionResultActivity extends AppCompatActivity {
    // scores for each emotion ordered from greatest to lowest
    private ArrayList<String> scores;
    //each emotion order from greatest score to lowest
    private ArrayList<String> emotions;
    // the WebView which will hold our playlists
    private WebView playListView;
    // the index where the top emotion is
    private final static int TOP_EMOTION = 0;
    //the url of the top emotion playlist
    private String topEmotionPlaylistURL;

    /**
     * On Creation of the activity, get the scores/emotions and
     * set their results to a the graph and web view
     * @param savedInstanceState - Bundle not used here
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // get the scores and emotions as String ArrayLists
        // they should already be in order from greatest to lowest
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            scores   = (ArrayList<String>) extras.get("scores");
            emotions = (ArrayList<String>) extras.get("emotions");
            // create a new Horizontal Bar Chart
            HorizontalBarChart chart = (HorizontalBarChart) findViewById(R.id.chart);
            // get the chart data from the helper method getDataSet()
            BarData data = new BarData(getDataSet());
            data.setBarWidth(.25f);
            //set the data
            chart.setData(data);
            // make graph animated
            chart.animateXY(2000, 2000);

            // further format the graph make lines/labels invisible that we don't want to see
            chart.getXAxis().setDrawAxisLine(false);
            chart.getXAxis().setDrawGridLines(false);
            chart.getXAxis().setDrawLabels(false);
            chart.getXAxis().setTextColor(Color.WHITE);
            chart.getXAxis().setDrawLimitLinesBehindData(false);
            chart.getAxisLeft().setDrawAxisLine(false);
            chart.getAxisLeft().setDrawGridLines(false);
            chart.getAxisLeft().setDrawLabels(false);
            chart.getAxisLeft().setDrawTopYLabelEntry(false);
            chart.getAxisLeft().setDrawZeroLine(false);

            chart.getAxisRight().setDrawLabels(false);
            chart.getLegend().setTextColor(Color.WHITE);

            chart.setDrawGridBackground(false);
            chart.setPinchZoom(false);
            chart.setDescription(null);

            // refresh the chart
            chart.invalidate();
            // set the Title TextView for the top emotion found
            TextView playListTitle = (TextView) findViewById(R.id.playlist_title);
            playListTitle.setText(getString(R.string.result_title_playlist,
                    emotions.get(TOP_EMOTION)));
            // set the WebView for the top emotion's playlist
            playListView = (WebView) findViewById(R.id.web_playlist);
            this.setPlayListWebView(emotions.get(TOP_EMOTION));
        }
        //display errors, although they are not likely to happen at this point
        else{
            Toast.makeText(this,"Cannot display graph results",Toast.LENGTH_LONG).show();
            Log.d("EmotionResultActivity",getString(R.string.null_result_bundles));
        }

    }

    /**
     * Helper method which gets the chart data set. The data set is the top 4 emotions their scores
     *
     * @return a data set of the scores and emotions
     */
    private ArrayList<IBarDataSet> getDataSet() {
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();

        ArrayList<BarEntry> emotion0 = new ArrayList<>();
        //add .1 to make each score show on the graph
        BarEntry score0 = new BarEntry(3.000f, Float.parseFloat(scores.get(0))+.01f);
        //make a set for each score and emotion
        emotion0.add(score0);
        BarDataSet emotion0Set = new BarDataSet(emotion0, emotions.get(0));
        emotion0Set.setColor(Color.GREEN);
        emotion0Set.setDrawValues(false);
        dataSets.add(emotion0Set);

        ArrayList<BarEntry> emotion1 = new ArrayList<>();
        BarEntry score1 = new BarEntry(2.000f, Float.parseFloat(scores.get(1))+.01f);
        emotion1.add(score1);
        BarDataSet emotion1Set = new BarDataSet(emotion1, emotions.get(1));
        emotion1Set.setColor(Color.BLUE);
        emotion1Set.setDrawValues(false);
        dataSets.add(emotion1Set);

        ArrayList<BarEntry> emotion2 = new ArrayList<>();
        BarEntry score2 = new BarEntry(1.000f, Float.parseFloat(scores.get(2))+.01f);
        emotion2.add(score2);
        BarDataSet emotion2Set = new BarDataSet(emotion2, emotions.get(2));
        emotion2Set.setColor(Color.CYAN);
        emotion2Set.setDrawValues(false);
        dataSets.add(emotion2Set);

        ArrayList<BarEntry> emotion3 = new ArrayList<>();
        BarEntry score3 = new BarEntry(0.000f, Float.parseFloat(scores.get(3))+.01f);
        emotion3.add(score3);
        BarDataSet emotion3Set = new BarDataSet(emotion3, emotions.get(3));
        emotion3Set.setColor(Color.MAGENTA);
        emotion3Set.setDrawValues(false);
        dataSets.add(emotion3Set);

        return dataSets;
    }

    /**
     * create a web view to hold the playlists, this need to be customized to handle the links
     * within the playlist
     * @param emotion the top emotion used to get the correct playlist url
     */
    private void setPlayListWebView(String emotion){
        // javascript is required for the playlist to display
        playListView.getSettings().setJavaScriptEnabled(true);
        playListView.setWebViewClient(new WebViewClient(){
            // create a new WebView client with Overriden methods.

            //the loading dialog to be displayed with page is loading.
            ProgressDialog loadingDialog = new ProgressDialog(EmotionResultActivity.this);

            /**
             * Dismiss the loading dialog when finished loading page
             * @param view the WebView referenced, not used in this case
             * @param url - the url referenced not used in this case
             */
            @Override
            public void onPageFinished(WebView view, String url) {
                loadingDialog.dismiss();
            }

            /**
             * start the loading dialog when page starts loading
             * @param view - not used in this case
             * @param url - not used in this case
             * @param favicon - not used in this case
             */
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                loadingDialog.setCancelable(false);
                loadingDialog.setMessage("Loading...");
                loadingDialog.show();
            }

            /**
             * Spotifiy has market:// links which go to the play store.
             * These show up in the webview, but really should go to the top emotion.
             * The below method handles them differently and sends the user to the playlist in full
             * @param view not used in this case
             * @param url - String url of the current url being loaded
             * @return if we have overridden default URL loading
             */
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && url.startsWith("market://")) {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(topEmotionPlaylistURL)));
                    return true;
                } else {
                    return false;
                }
            }


        });
        // switch on the emotion and load the url for each emotions playlist
        switch(emotion){
            case "HAPPINESS":playListView.loadUrl(getString(R.string.happy_playlist_url));
                             topEmotionPlaylistURL = getString(R.string.happy_playlist_url);
                             break;
            case "ANGER":    playListView.loadUrl(getString(R.string.angry_playlist_url));
                             topEmotionPlaylistURL = getString(R.string.angry_playlist_url);
                             break;
            case "NEUTRAL":  playListView.loadUrl(getString(R.string.netural_playlist_url));
                             topEmotionPlaylistURL = getString(R.string.netural_playlist_url);
                             break;
            case "SURPRISE": playListView.loadUrl(getString(R.string.suprise_playlist_url));
                             topEmotionPlaylistURL = getString(R.string.suprise_playlist_url);
                             break;
            case "SADNESS": playListView.loadUrl(getString(R.string.sad_playlist_url));
                            topEmotionPlaylistURL = getString(R.string.sad_playlist_url);
                            break;

        }

    }




}
