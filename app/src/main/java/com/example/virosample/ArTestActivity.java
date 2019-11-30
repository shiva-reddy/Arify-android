package com.example.virosample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArTestActivity extends AppCompatActivity {

    public static Map<ViroImageTarget, List<ViroArObject>> imageTargetVsArObjectListMap = new HashMap<>();

    private TextView loadingText;

    private Handler uiViewHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_test);
        findViewById(R.id.start).setOnClickListener((v) -> start());
        loadingText = findViewById(R.id.loading_text_id);
    }


    private void start() {
        new DownloadTask().execute();
    }

    class DownloadTask extends AsyncTask<String, String, Map<ViroImageTarget, List<ViroArObject>>>{

        Map<ViroImageTarget, List<ViroArObject>> result;

        @Override
        protected Map<ViroImageTarget, List<ViroArObject>> doInBackground(String... strings) {
            uiViewHandler.post(()-> loadingText.setVisibility(View.VISIBLE));
            result = ApiClient
                    .build()
                    .getImageTargetVsArObjectList("scene_1");
            return null;
        }

        @Override
        protected void onPreExecute() {
            Log.i("my_viro_log", "Pre execute");
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Map<ViroImageTarget, List<ViroArObject>> viroImageTargetListMap) {
            Log.i("my_viro_log", "Post execute");
            uiViewHandler.post(()-> loadingText.setVisibility(View.INVISIBLE));
            imageTargetVsArObjectListMap = result;
            Intent startAR = new Intent(ArTestActivity.this, ViroActivityAR.class);
            startActivity(startAR);
            super.onPostExecute(viroImageTargetListMap);
        }
    }

}
