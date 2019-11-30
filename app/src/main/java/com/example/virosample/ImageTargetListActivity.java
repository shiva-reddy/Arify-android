package com.example.virosample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImageTargetListActivity extends AppCompatActivity {

    private ExpandableListView expandableListView;

    private ExpandableListViewAdapter expandableListViewAdapter;

    private String SCENE_NAME;

    Map<ViroImageTarget, List<ViroArObject>> imageTargetVsObjMap = null;
    List<ViroImageTarget> mImageTargetList;

    private Context mContext = this;

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String sceneName = getIntent().getStringExtra("SCENE_NAME");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_page);
        new ImageTargetVsObjsListMapLoader().execute(sceneName);

    }

    class ImageTargetVsObjsListMapLoader extends AsyncTask<String, String, Map<ViroImageTarget, List<ViroArObject>>> {

        @Override
        protected Map<ViroImageTarget, List<ViroArObject>> doInBackground(String... strings) {
            return ApiClient.build().getImageTargetVsArObjectList(strings[0]);
        }

        @Override
        protected void onPostExecute(Map<ViroImageTarget, List<ViroArObject>> result) {
            Log.i("my_viro_log", "Post execute");
            loadView(result);
            super.onPostExecute(result);
        }
    }

    public void loadView(Map<ViroImageTarget, List<ViroArObject>> imageTargetVsObjLocationMap){
        setContentView(R.layout.activity_image_target_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        expandableListView = findViewById(R.id.image_target_list_view);
        SCENE_NAME = getIntent().getStringExtra("SCENE_NAME");
        imageTargetVsObjMap = imageTargetVsObjLocationMap;

        List<ViroImageTarget> imageTargetList = new ArrayList<ViroImageTarget>(imageTargetVsObjLocationMap.keySet());
        mImageTargetList = imageTargetList;
        // initializing the views
        initViews();

        // initializing the objects
        initObjects(imageTargetList, imageTargetVsObjLocationMap);

        FloatingActionButton fab = findViewById(R.id.imageTarget_fab);
        fab.setOnClickListener(view -> {
            this.openCameraActivity();
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
    private void initObjects(List<ViroImageTarget> imageTargetList, Map<ViroImageTarget, List<ViroArObject>> imageTargetVsObjLocationMap) {

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

            ViroArObject arObjectNames = imageTargetVsObjMap.get(
                    mImageTargetList.get(groupPosition)).get(
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
                ApiClient.build().updateArObject(SCENE_NAME,arObjectName.objectName,newArObject.scaleX,newArObject.scaleY,newArObject.scaleZ,newArObject.XOffset,newArObject.YOffset,newArObject.ZOffset,newArObject.rotX,newArObject.rotZ);
            }
        });
        dialog.show(getSupportFragmentManager(), "tag");
    }

    public void openCameraActivity(){
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        }
        else
        {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            Intent myIntent = new Intent(this, AddTarget.class);
            myIntent.putExtra("image", RotateBitmap(photo, (float) 90));
            myIntent.putExtra("scene",SCENE_NAME);
            startActivity(myIntent);
        }
    }
    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


}
