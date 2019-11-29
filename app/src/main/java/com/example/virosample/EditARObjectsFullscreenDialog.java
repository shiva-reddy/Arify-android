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
        selectArObject.scaleX = new Float(scaleX.getText().toString());
        selectArObject.scaleY = new Float(scaleY.getText().toString());
        selectArObject.scaleZ = new Float(scaleZ.getText().toString());
        selectArObject.rotX = new Float(rotX.getText().toString());
        selectArObject.rotZ = new Float(rotY.getText().toString());
        selectArObject.XOffset = new Float(offsetX.getText().toString());
        selectArObject.YOffset = new Float(offsetY.getText().toString());
        selectArObject.ZOffset = new Float(offsetZ.getText().toString());

        return  selectArObject;
    }

    public interface Callback {
        void onActionClick(ViroArObject newArObject);
    }
}
