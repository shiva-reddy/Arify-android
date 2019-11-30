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

public class AddSceneFullscreenDialog extends DialogFragment implements View.OnClickListener {

    private Callback callback;
    private EditText sceneName_ET;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {

            case R.id.fullscreen_dialog_close:
                dismiss();
                break;

            case R.id.fullscreen_dialog_action:
                callback.onActionClick(sceneName_ET.getText().toString());
                dismiss();
                break;

        }
    }

    static AddSceneFullscreenDialog newInstance() {
        return new AddSceneFullscreenDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_scene_dialog, container, false);
        ImageButton close = view.findViewById(R.id.fullscreen_dialog_close);
        TextView action = view.findViewById(R.id.fullscreen_dialog_action);
        sceneName_ET = view.findViewById(R.id.imageTarget_ET);

        close.setOnClickListener(this);
        action.setOnClickListener(this);

        return view;
    }

    public interface Callback {
        void onActionClick(String name);
    }
}
