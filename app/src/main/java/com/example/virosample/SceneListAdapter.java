package com.example.virosample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;

import java.util.ArrayList;


public class SceneListAdapter extends ArrayAdapter<SceneList> {
    private Context mContext;
    private ArrayList<SceneList> sceneList = new ArrayList<>();

    public SceneListAdapter(@NonNull Context context, ArrayList<SceneList> list) {
        super(context, 0,list);
        mContext = context;
        sceneList = list;
    }

    @Override
    public int getCount() {
        return sceneList.size();
    }

    @Override
    public SceneList getItem(int position) {
        return sceneList.get(position);
    }


    static class ViewHolder {
        private TextView sceneName;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        ViewHolder viewHolder = new ViewHolder();
        if(listItem == null){
            listItem = LayoutInflater.from(mContext).inflate(R.layout.scene_item,parent,false);
            viewHolder.sceneName = listItem.findViewById(R.id.sceneName_txt);
            listItem.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) listItem.getTag();
        }

        SceneList currentScreen = sceneList.get(position);

        viewHolder.sceneName.setText(currentScreen.getmSceneName());


        return listItem;
    }



}
