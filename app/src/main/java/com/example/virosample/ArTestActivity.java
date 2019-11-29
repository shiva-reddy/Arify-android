package com.example.virosample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;

import com.amazonaws.util.IOUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class ArTestActivity extends AppCompatActivity {

    public static Map<ImageTarget, ArObject> imageTargetVsObjLocationMap = new HashMap<>();

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

        imageTargetVsObjLocationMap = ApiClient
                .build()
                .listLinksForScene("scene_1")
                .results
                .stream()
                .collect(Collectors.toMap(
                        link -> {
                            ImageTarget imageTarget = new ImageTarget();
                            imageTarget.btm = getImageAssetUri(link.image_target.link);
                            imageTarget.name = link.image_target.name;
                            return imageTarget;
                        },
                        link ->{
                            ArObject arObject = new ArObject();
                            arObject.objectWebLink = link.ar_object.link;
                            arObject.objectName = link.ar_object.name;
                            arObject.mtlWebLink = link.ar_object.mtl_link;
                            arObject.type = link.ar_object.objType();
                            arObject.scaleX = getOrDefault(link.ar_object.scale_x, 0.1f);
                            arObject.scaleY = getOrDefault(link.ar_object.scale_y, 0.1f);
                            arObject.scaleZ = getOrDefault(link.ar_object.scale_z, 0.1f);
                            arObject.rotX = getOrDefault(link.ar_object.rot_x, 0.0f);
                            arObject.rotZ = getOrDefault(link.ar_object.rot_z,0.0f);
                            return arObject;
                        }
                ));

        Intent startAR = new Intent(this, ViroActivityAR.class);
        startActivity(startAR);
    }

    private static Float getOrDefault(Float val, Float def){
        return val == null? def : val;
    }


    public static Bitmap getImageAssetUri(String link) {
        try {
            Log.i("my_viro_log", link);
            URL aUrl = new URL(link);
            return BitmapFactory.decodeStream((InputStream) aUrl.getContent());
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

}
