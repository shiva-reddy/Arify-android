package com.example.virosample;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.util.IOUtils;
import com.viro.core.AsyncObject3DListener;
import com.viro.core.Object3D;
import com.viro.core.Vector;
import com.viro.core.ViroView;
import com.viro.core.ViroViewARCore;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static com.example.virosample.ApiClient.getImageTargetVsObjectsforScene;

public class MainActivity extends Activity {

    public static Map<Bitmap, File> imageTargetVsObjLocationMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.start).setOnClickListener((v)-> start());
    }

    private void start() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        clearModelsDirectory();
        imageTargetVsObjLocationMap = getImageTargetVsObjectsforScene("scene_1")
                .entrySet().stream()
                .collect(Collectors.toMap(e -> getImageAssetUri(e.getKey()), e -> getArObjectAssetUri(e.getValue())));
        Intent startAR = new Intent(MainActivity.this, ViroActivityAR.class);
        startActivity(startAR);
    }

    private void clearModelsDirectory(){
        File index = new File(Environment.getExternalStorageDirectory(), "Models/");
        String[] entries = index.list();
        for(String s: entries){
            File currentFile = new File(index.getPath(),s);
            currentFile.delete();
        }
    }

    private Bitmap getImageAssetUri(String link) {
        try {
            Log.i("my_viro_log", link);
            URL aUrl = new URL(link);
            return BitmapFactory.decodeStream((InputStream) aUrl.getContent());
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private File getArObjectAssetUri(String objectLink){
        Random random = new Random();
        File file = new File(Environment.getExternalStorageDirectory(), "Models/ar_object" +  random.nextInt(99) + ".obj");
        Log.i("my_viro_log", ""+Uri.fromFile(file));
        downloadFile(objectLink, file);
        return file;
    }

    void downloadFile(String _url, File _file) {
        try {
            URL u = new URL(_url);
            DataInputStream stream = new DataInputStream(u.openStream());
            byte[] buffer = IOUtils.toByteArray(stream);
            FileOutputStream fos = new FileOutputStream(_file);
            fos.write(buffer);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

}
