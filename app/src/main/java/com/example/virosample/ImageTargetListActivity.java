package com.example.virosample;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.StrictMode;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImageTargetListActivity extends AppCompatActivity {

    public static Map<ImageTarget, ArrayList<ArObject>> imageTargetVsObjLocationMap = new HashMap<>();
    private String SCENE_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_target_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        SCENE_NAME = getIntent().getStringExtra("SCENE_NAME");

       /* StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        imageTargetVsObjLocationMap = ApiClient
                .build()
                .listLinksForScene(SCENE_NAME)
                .results
                .stream()
                .collect(Collectors.toMap(
                        link -> {
                            ImageTarget imageTarget = new ImageTarget();
                            imageTarget.name = link.image_target.name;
                            return imageTarget;
                        },
                        link ->{
                            ArObject arObject = new ArObject();
                            arObject.objectName = link.ar_object.name;
                            return arObject;
                        }
                ));*/
       /*ArrayList<ArObject> arObjects = new ArrayList<>();
       arObjects.add(new ArObject("Object1"));
        arObjects.add(new ArObject("Object2"));

        imageTargetVsObjLocationMap.put(,arObjects);*/

    }

}
