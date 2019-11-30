package com.example.virosample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.math.BigDecimal;

public class EditARObjectsFullscreenDialog extends DialogFragment implements View.OnClickListener {

    private Callback callback;
    private EditText objectName_ET;
    private static String imageTargetName;
    private EditText scaleX;
    private EditText scaleY;
    private EditText scaleZ;
    private EditText rotX;
    private EditText rotY;
    private EditText offsetX;
    private EditText offsetY;
    private EditText offsetZ;

    ViroArObject selectArObject;


    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public EditARObjectsFullscreenDialog(ViroArObject arObject){
        this.imageTargetName = arObject.objectName;
        this.selectArObject = arObject;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {

            case R.id.add_objects_fullscreen_dialog_close:
                dismiss();
                break;

            case R.id.add_objects_fullscreen_dialog_action:
                ViroArObject newArObject = this.updateArObject();
                callback.onActionClick(newArObject);
                dismiss();
                break;

        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_objects_dialog, container, false);
        ImageButton close = view.findViewById(R.id.add_objects_fullscreen_dialog_close);
        TextView action = view.findViewById(R.id.add_objects_fullscreen_dialog_action);

        scaleX = view.findViewById(R.id.scaleX_editText);
        scaleY = view.findViewById(R.id.scaleY_editText);
        scaleZ = view.findViewById(R.id.scaleZ_editText);
        rotX = view.findViewById(R.id.roationX_editText);
        rotY = view.findViewById(R.id.rotationY_editText);
        offsetX = view.findViewById(R.id.offsetX_editText);
        offsetY = view.findViewById(R.id.offsetY_editText);
        offsetZ = view.findViewById(R.id.offsetZ_editText);

        scaleX.setText(selectArObject.scaleX != null ? selectArObject.scaleX+"" : "");
        scaleY.setText(selectArObject.scaleY!= null ? selectArObject.scaleY+"" : "");
        scaleZ.setText(selectArObject.scaleZ!= null ? selectArObject.scaleZ+"" : "");
        rotX.setText(selectArObject.rotX != null ? selectArObject.rotX+"" : "");
        rotY.setText(selectArObject.rotZ!= null ? selectArObject.rotZ+"" : "");
        offsetX.setText(selectArObject.XOffset != null ? selectArObject.XOffset+"" : "");
        offsetY.setText(selectArObject.YOffset != null ? selectArObject.YOffset+"" : "");
        offsetZ.setText(selectArObject.ZOffset != null ? selectArObject.ZOffset+"" : "");


        close.setOnClickListener(this);
        action.setOnClickListener(this);

        return view;
    }

    public ViroArObject updateArObject(){
        selectArObject.scaleX = round(new Float(scaleX.getText().toString()),2);
        selectArObject.scaleY = round(new Float(scaleY.getText().toString()),2);
        selectArObject.scaleZ = round(new Float(scaleZ.getText().toString()),2);
        selectArObject.rotX = round(new Float(rotX.getText().toString()),2);
        selectArObject.rotZ = round(new Float(rotY.getText().toString()),2);
        selectArObject.XOffset = round(new Float(offsetX.getText().toString()),2);
        selectArObject.YOffset = round(new Float(offsetY.getText().toString()),2);
        selectArObject.ZOffset = round(new Float(offsetZ.getText().toString()),2);

        return  selectArObject;
    }

    public static float round(float number, int decimalPlace) {
        BigDecimal bd = new BigDecimal(number);
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public interface Callback {
        void onActionClick(ViroArObject newArObject);
    }
}
