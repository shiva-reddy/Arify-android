package com.example.virosample;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.StrictMode;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SceneListActivity extends AppCompatActivity {

    ListView sceneListView;
    List<String> sceneList =new ArrayList<>();
    SceneListAdapter sceneListAdapter;
    SceneListActivity mContext =this;
    private Handler uiViewHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_page);
        new ScenesListLoader().execute();
    }

    public void loadScenesView(ApiClient.ScenesListResult listResult){
        setContentView(R.layout.activity_scene_list);
        sceneListView = findViewById(R.id.scene_list_view);
        sceneList = listResult.results
                .stream()
                .map(link -> link.name
                ).collect(Collectors.toList());

        ArrayList<SceneList> sceneListArrayList = new ArrayList<>();
        sceneList.forEach(sceneName ->{
            sceneListArrayList.add(new SceneList(sceneName));
        });
        sceneListAdapter = new SceneListAdapter(this, sceneListArrayList);
        sceneListView.setAdapter(sceneListAdapter);
        registerForContextMenu(sceneListView);
        sceneListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            SceneList sceneListEle = (SceneList) parent.getItemAtPosition(position );

            Intent arSceneIntent = new Intent(this,ViroActivityAR.class);
            //arSceneIntent.putExtra("SceneName", sceneListEle.getmSceneName());
            //startActivity(arSceneIntent);
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            DialogFragment dialog = AddSceneFullscreenDialog.newInstance();
            ((AddSceneFullscreenDialog) dialog).setCallback(new AddSceneFullscreenDialog.Callback() {
                @Override
                public void onActionClick(String name) {
                    Toast.makeText(mContext,name, Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show(getSupportFragmentManager(), "tag");
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, view, menuInfo);
        ListView lv = (ListView) view;
        AdapterView.AdapterContextMenuInfo menuPosition = (AdapterView.AdapterContextMenuInfo) menuInfo;
        SceneList sceneList = (SceneList)  lv.getItemAtPosition(menuPosition.position);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.scene_context_menu, menu);
        menu.setHeaderTitle("Options");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        SceneList sceneListEle = (SceneList) sceneListView.getItemAtPosition(info.position);

        switch (item.getItemId()) {

            case R.id.action_one:
                this.openImageTargetListActivity(sceneListEle.getmSceneName());
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void openImageTargetListActivity(String sceneName){
        Intent imageTargetActivityIntent = new Intent(this,ImageTargetListActivity.class);
        imageTargetActivityIntent.putExtra("SCENE_NAME", sceneName);
        startActivity(imageTargetActivityIntent);
    }

    class ScenesListLoader extends AsyncTask<String, String, ApiClient.ScenesListResult> {

        @Override
        protected ApiClient.ScenesListResult doInBackground(String... strings) {
            return ApiClient.build().listScenes();
        }

        @Override
        protected void onPostExecute(ApiClient.ScenesListResult listResult) {
            Log.i("my_viro_log", "Post execute");
            loadScenesView(listResult);
            super.onPostExecute(listResult);
        }
    }


}
