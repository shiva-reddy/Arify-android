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



        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);

        // Show context menu for groups
        if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.image_target_context_menu, menu);
            menu.setHeaderTitle("Options");

            // Show context menu for children
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.ar_objects_context_menu, menu);
            menu.setHeaderTitle("Options");
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableListView.ExpandableListContextMenuInfo menuPosition = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();

        int type = ExpandableListView.getPackedPositionType(menuPosition.packedPosition);
        int groupPosition = ExpandableListView.getPackedPositionGroup(menuPosition.packedPosition);
        int childPosition = ExpandableListView.getPackedPositionChild(menuPosition.packedPosition);

        if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            String imageTargetName = (String) expandableListView.getItemAtPosition(ExpandableListView.getPackedPositionGroup(menuPosition.packedPosition));
            switch (item.getItemId()) {

                case R.id.action_one:
                    this.addARObjects(SCENE_NAME, imageTargetName);
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {

            ViroArObject arObjectNames = imageTargetVsObjLocationMap.get(
                    imageTargetList.get(groupPosition)).get(
                    childPosition);
            this.editARObjects(arObjectNames);
        }

        return super.onContextItemSelected(item);
    }

    public void addARObjects(String SCENE_NAME, String imageTargetName){
        DialogFragment dialog = new AddARObjectsFullscreenDialog(SCENE_NAME);
        ((AddARObjectsFullscreenDialog) dialog).setCallback(new AddARObjectsFullscreenDialog.Callback() {
            @Override
            public void onActionClick(String arObjectName) {
                ApiClient.build().linkImageTargetToARObjectInScene(SCENE_NAME,arObjectName,imageTargetName);
                //Toast.makeText(mContext,name, Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show(getSupportFragmentManager(), "tag");
    }

    public void editARObjects(ViroArObject arObjectName){
        DialogFragment dialog = new EditARObjectsFullscreenDialog(arObjectName);
        ((EditARObjectsFullscreenDialog) dialog).setCallback(new EditARObjectsFullscreenDialog.Callback() {
            @Override
            public void onActionClick(ViroArObject newArObject) {

            }
        });
        dialog.show(getSupportFragmentManager(), "tag");
    }


}
