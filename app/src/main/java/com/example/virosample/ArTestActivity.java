package com.example.virosample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ArTestActivity extends AppCompatActivity {

    public static Map<ViroImageTarget, List<ViroArObject>> imageTargetVsArObjectListMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_test);
        findViewById(R.id.start).setOnClickListener((v) -> start());
    }


    private void start() {
//        if(chosenScene == null){
//            return;
//        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        imageTargetVsArObjectListMap = ApiClient
                .build()
                .getImageTargetVsArObjectList("scene_1");

        Intent startAR = new Intent(this, ViroActivityAR.class);
        startActivity(startAR);
    }

}
