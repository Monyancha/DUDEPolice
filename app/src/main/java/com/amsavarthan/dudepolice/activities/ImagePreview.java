package com.amsavarthan.dudepolice.activities;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.WindowManager;

import com.amsavarthan.dudepolice.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class ImagePreview extends AppCompatActivity {


    String intent_URI,intent_URL;

    private void initializeActivity() {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/bold.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initializeActivity();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        intent_URI=getIntent().getStringExtra("uri");
        intent_URL=getIntent().getStringExtra("url");

        PhotoView photoView = findViewById(R.id.photo_view);

        if(!TextUtils.isEmpty(intent_URI)) {
            photoView.setImageURI(Uri.parse(intent_URI));
        }else {

            Glide.with(this)
                    .setDefaultRequestOptions(new RequestOptions().placeholder(getResources().getDrawable(R.drawable.placeholder)))
                    .load(intent_URL)
                    .into(photoView);


        }

    }
}