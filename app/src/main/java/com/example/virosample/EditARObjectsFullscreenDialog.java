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

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public EditARObjectsFullscreenDialog(String imageTargetName){
        this.imageTargetName = imageTargetName;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {

            case R.id.edit_objects_fullscreen_dialog_close:
                dismiss();
                break;

            case R.id.edit_objects_fullscreen_dialog_action:
                callback.onActionClick(objectName_ET.getText().toString());
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
        ImageButton close = view.findViewById(R.id.edit_objects_fullscreen_dialog_close);
        TextView action = view.findViewById(R.id.edit_objects_fullscreen_dialog_action);

        close.setOnClickListener(this);
        action.setOnClickListener(this);

        return view;
    }

    public interface Callback {
        void onActionClick(String name);
    }
}
