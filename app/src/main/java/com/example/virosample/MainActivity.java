package com.example.virosample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.ar_test_button).setOnClickListener((v) -> {
            Intent intent = new Intent(this, ArTestActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.camera_test).setOnClickListener((v) -> {
            Intent intent = new Intent(this, CameraTestActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.ui_test).setOnClickListener((v) -> {
            Intent intent = new Intent(this, SceneListActivity.class);
            startActivity(intent);
        });

    }
}
