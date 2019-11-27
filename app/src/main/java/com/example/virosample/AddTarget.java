package com.example.virosample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

public class AddTarget extends Activity {
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_target);
        this.imageView = (ImageView)this.findViewById(R.id.imageView);
        Intent intent= getIntent();
        Bitmap photo = intent.getParcelableExtra("image");
        this.setImageView(photo);
    }

    public void setImageView(Bitmap photo) {
        this.imageView.setImageBitmap(photo);
        this.imageView.setRotation(90); //= (-it.rotationDegrees).toFloat();
    }
}
