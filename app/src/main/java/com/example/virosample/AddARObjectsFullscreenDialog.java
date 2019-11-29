package com.example.virosample;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

public class AddARObjectsFullscreenDialog extends DialogFragment implements View.OnClickListener {

    private Callback callback;
    private EditText objectName_ET;
    private static String SCENE_NAME;
    private Spinner ar_model_spinner;
    private Context mContext;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public AddARObjectsFullscreenDialog(String SCENE_NAME){
        this.SCENE_NAME = SCENE_NAME;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {

            case R.id.add_objects_fullscreen_dialog_close:
                dismiss();
                break;

            case R.id.add_objects_fullscreen_dialog_action:
                callback.onActionClick(ar_model_spinner.getSelectedItem().toString());
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
        View view = inflater.inflate(R.layout.add_objects_dialog, container, false);
        ImageButton close = view.findViewById(R.id.add_objects_fullscreen_dialog_close);
        TextView action = view.findViewById(R.id.add_objects_fullscreen_dialog_action);
        ar_model_spinner = view.findViewById(R.id.ar_model_spinner);

        close.setOnClickListener(this);
        action.setOnClickListener(this);

        ApiClient.ListArObjectsResult listArObjectsResult = ApiClient.build().listArObjectsForScene(SCENE_NAME);
        List<String> modelSpinnerArray =  new ArrayList<String>();
        listArObjectsResult.results.forEach(object -> {
            modelSpinnerArray.add(object.name);
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, modelSpinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ar_model_spinner.setAdapter(adapter);

        return view;
    }

    public interface Callback {
        void onActionClick(String arObjectName);
    }
}
