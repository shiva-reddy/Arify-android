package com.example.virosample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.core.content.FileProvider;

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
    //keep track of cropping intent
    final int PIC_CROP = 3;
    File final_image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_target);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        this.imageView = (ImageView)this.findViewById(R.id.imageView);
        this.editText = (EditText) this.findViewById(R.id.imageTarget_ET);
        model_spinner = findViewById(R.id.model_spinner);
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
            final_image=file;
            findViewById(R.id.crop).setOnClickListener((v) -> {
                if ( (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                        (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                    performCrop(file);
                }
                else {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }

            });

            findViewById(R.id.place_model).setOnClickListener((v) -> {
                Log.d("getText",editText.getText().toString());
                String image_name = editText.getText().toString();
                UploadImageTargetRequest req = new UploadImageTargetRequest();
                req.current_scene = current_scene;
                req.image_name = image_name;
                req.final_image = final_image;
                new UploadImageTask().execute(req);
                finish();
            });
    }

    class UploadImageTargetRequest{
        String current_scene, image_name;
        File final_image;
    }


    class UploadImageTask extends AsyncTask<UploadImageTargetRequest, UploadImageTargetRequest, String> {

        @Override
        protected String doInBackground(UploadImageTargetRequest... uploadImageTargetRequests) {
            UploadImageTargetRequest req = uploadImageTargetRequests[0];
            ApiClient.build().uploadImageTargetToScene(req.current_scene, req.image_name, final_image);
            ApiClient.build().linkImageTargetToARObjectInScene(req.current_scene,model_spinner.getSelectedItem().toString(),req.image_name);
            return null;
        }
    }

    public File saveImageToInternalStorage(Bitmap image) {
        String filename = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime()) + ".png";
        try {
// Use the compress method on the Bitmap object to write image to
// the OutputStream
            File index = new File(Environment.getExternalStorageDirectory(), "Images/");
            if(!index.exists()){
                index.mkdir();
            }
            File file= new File (Environment.getExternalStorageDirectory(), "Images/" + filename);
            FileOutputStream fos = new FileOutputStream(file);
            Log.d("path", String.valueOf(getApplicationContext().getFilesDir()));
// Writing the bitmap to the output stream
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            Log.d("saveImageToInternalStorage()", filename);
            return file;
        } catch (Exception e) {
            Log.e("saveToInternalStorage()", e.getMessage());
            return null;
        }
    }
    public void setImageView(Bitmap photo) {
        this.imageView.setImageBitmap(photo);
        //this.imageView.setRotation(90); //= (-it.rotationDegrees).toFloat();
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap bitmap = null;
        //user is returning from capturing an image using the camera
        if(requestCode == PIC_CROP){
            //get the returned data
            //Bundle extras = data.getExtras();
            //get the cropped bitmap
            //Bitmap thePic = (Bitmap) extras.get("data");
            //display the returned cropped image
            //imageView.setImageBitmap(thePic);
            Uri uri = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                imageView.setImageBitmap(bitmap);
            }
            catch(Exception e){

            }
            final_image= saveImageToInternalStorage(bitmap);
        }
    }

    private void performCrop(File file){
        try {
            //call the standard crop action intent (the user device may not support it)
            //indicate image type and Uri
            Log.d("path",file.toURI().toString());


            Uri photoURI = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);
            Log.d("crop", photoURI.toString());
            this.grantUriPermission("com.android.camera",photoURI,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(photoURI, "image/*");

            //set crop properties

            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        }
        catch(Exception anfe){
            Log.e("crop",anfe.getMessage());
            //display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}

