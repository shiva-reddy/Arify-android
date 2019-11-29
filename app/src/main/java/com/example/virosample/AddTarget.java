package com.example.virosample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import android.os.StrictMode;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;


import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddTarget extends Activity {
    private ImageView imageView;
    private EditText editText;
    private Spinner model_spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_target);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        this.imageView = (ImageView)this.findViewById(R.id.imageView);
        this.editText = (EditText) this.findViewById(R.id.editText);
        this.model_spinner = (Spinner) this.findViewById(R.id.model_spinner);
        Intent intent= getIntent();
        String current_scene = intent.getStringExtra("scene");
        Log.d("onCreate",current_scene);
        String imageTargetName= "testimage";//editText.getText().toString();
        ApiClient apiClient=new ApiClient();
        try {
            ApiClient.ListArObjectsResult listArObjectsResult = apiClient.listArObjectsForScene(current_scene);
            List<String> spinnerArray =  new ArrayList<String>();
            for (int i=0; i<listArObjectsResult.results.size();i++) {
                spinnerArray.add(listArObjectsResult.results.get(i).name);
                Log.d("list", listArObjectsResult.results.get(i).name);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this, android.R.layout.simple_spinner_item, spinnerArray);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            model_spinner.setAdapter(adapter);
        }
        catch (Exception e) {
            Log.e("onCreate()", ""+e.getMessage());
        }


            Bitmap photo = intent.getParcelableExtra("image");
            this.setImageView(photo);
            File file = saveImageToInternalStorage(photo);
            findViewById(R.id.place_model).setOnClickListener((v) -> {
                Log.d("getText",editText.getText().toString());
                String image_name = editText.getText().toString();
                apiClient.uploadImageTargetToScene(current_scene, image_name, file);
                apiClient.linkImageTargetToARObjectInScene(current_scene,model_spinner.getSelectedItem().toString(),image_name);
            });

    }
    public File saveImageToInternalStorage(Bitmap image) {
        String filename = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime()) + ".png";
        try {
// Use the compress method on the Bitmap object to write image to
// the OutputStream
            FileOutputStream fos = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
            //Log.d("path",getApplicationContext().getPackageCodePath());
            //Log.d("path",getApplicationContext().getPackageName());
            //Log.d("path",getApplicationContext().getPackageResourcePath());
            Log.d("path", String.valueOf(getApplicationContext().getFilesDir()));
// Writing the bitmap to the output stream
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            File file = new File(getApplicationContext().getFilesDir(), filename);
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
