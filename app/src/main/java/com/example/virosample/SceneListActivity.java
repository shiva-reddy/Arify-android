package com.example.virosample;


import android.content.Intent;
import android.os.StrictMode;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SceneListActivity extends AppCompatActivity {

    ListView sceneListView;
    List<String> sceneList =new ArrayList<>();
    SceneListAdapter sceneListAdapter;
    SceneListActivity mContext =this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_list);
        //findViewById(R.id.start).setOnClickListener((v)-> start());
        //final Spinner spinner = (Spinner) findViewById(R.id.spinner);
        //spinner.setOnItemSelectedListener(this);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        sceneListView = findViewById(R.id.scene_list_view);

        sceneList = ApiClient.build()
                .listScenes()
                .results
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


}
