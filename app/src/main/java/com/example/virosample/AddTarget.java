package com.example.virosample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import android.os.StrictMode;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;

public class AddTarget extends Activity {
    private ImageView imageView;
    private EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_target);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        this.imageView = (ImageView)this.findViewById(R.id.imageView);
        this.editText = (EditText) this.findViewById(R.id.editText);
        Intent intent= getIntent();
        String current_scene = intent.getStringExtra("scene");
        Log.d("onCreate",current_scene);
        String imageTargetName= "testimage";//editText.getText().toString();
        ApiClient apiClient=new ApiClient();
        try {
            ApiClient.ListArObjectsResult listArObjectsResult = apiClient.listArObjectsForScene(current_scene);
            Log.d("list", listArObjectsResult.results.get(0).name);
            Bitmap photo = intent.getParcelableExtra("image");
            this.setImageView(photo);
            File file = saveImageToInternalStorage(photo);
            apiClient.uploadImageTargetToScene(current_scene, imageTargetName, file);
        }
        catch (Exception e) {
            Log.e("onCreate()", ""+e.getMessage());
        }
    }
    public File saveImageToInternalStorage(Bitmap image) {
        String filename= "desiredFilename.png";
        try {
// Use the compress method on the Bitmap object to write image to
// the OutputStream
            FileOutputStream fos = getBaseContext().openFileOutput(filename, Context.MODE_PRIVATE);

// Writing the bitmap to the output stream
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            File file = new File( getBaseContext().getFilesDir(), filename);
            Log.d("saveImageToInternalStorage()", filename);
            return file;
        } catch (Exception e) {
            Log.e("saveToInternalStorage()", e.getMessage());
            return null;
        }
    }
    public void setImageView(Bitmap photo) {
        this.imageView.setImageBitmap(photo);
        this.imageView.setRotation(90); //= (-it.rotationDegrees).toFloat();
    }
}
