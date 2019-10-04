package com.kernchen.spotmymood.spotmymood.helper;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.kernchen.spotmymood.R;
import com.kernchen.spotmymood.spotmymood.EmotionDetectActivity;

public class FailedImageView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_failed_image_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
      //  Bitmap temp = getIntent().getParcelableExtra("FAILED_IMAGE");
     //   ImageView imageView = findViewById(R.id.failedImageView);
     //   imageView.setImageBitmap(temp);

      //  setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
