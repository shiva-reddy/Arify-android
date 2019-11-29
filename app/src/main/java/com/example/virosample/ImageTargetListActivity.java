package com.example.virosample;

import android.content.Context;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.os.StrictMode;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImageTargetListActivity extends AppCompatActivity {

    private ExpandableListView expandableListView;

    private ExpandableListViewAdapter expandableListViewAdapter;

    public static Map<ViroImageTarget, List<ViroArObject>> imageTargetVsObjLocationMap = new HashMap<>();
    public static List<ViroImageTarget> imageTargetList;

    private String SCENE_NAME;

    private Context mContext = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_target_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        expandableListView = findViewById(R.id.image_target_list_view);

        SCENE_NAME = getIntent().getStringExtra("SCENE_NAME");

        imageTargetVsObjLocationMap = ApiClient.build().getImageTargetVsArObjectList(SCENE_NAME);
        imageTargetList = new ArrayList<ViroImageTarget>(imageTargetVsObjLocationMap.keySet());

        // initializing the views
        initViews();

        // initializing the objects
        initObjects();

        FloatingActionButton fab = findViewById(R.id.imageTarget_fab);
        fab.setOnClickListener(view -> {
            //TODO: Call Krunals component here
        });

        registerForContextMenu(expandableListView);

    }
    /**
     * method to initialize the views
     */
    private void initViews() {

        expandableListView = findViewById(R.id.image_target_list_view);

    }

    /**
     * method to initialize the objects
     */
    private void initObjects() {

        // initializing the adapter object
        expandableListViewAdapter = new ExpandableListViewAdapter(this, imageTargetList, imageTargetVsObjLocationMap);

        // setting list adapter
        expandableListView.setAdapter(expandableListViewAdapter);

    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, view, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.image_target_context_menu, menu);
        menu.setHeaderTitle("Options");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableListView.ExpandableListContextMenuInfo menuPosition = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
        String imageTargetName = (String) expandableListView.getItemAtPosition(ExpandableListView.getPackedPositionGroup(menuPosition.packedPosition));

        switch (item.getItemId()) {

            case R.id.action_one:
                this.addARObjects(imageTargetName);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void addARObjects(String imageTargetName){
        DialogFragment dialog = new AddARObjectsFullscreenDialog(imageTargetName);
        ((AddARObjectsFullscreenDialog) dialog).setCallback(new AddARObjectsFullscreenDialog.Callback() {
            @Override
            public void onActionClick(String name) {
                Toast.makeText(mContext,name, Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show(getSupportFragmentManager(), "tag");
    }


}
