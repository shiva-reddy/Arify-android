package com.example.virosample;

import com.google.gson.Gson;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiClient {

    OkHttpClient client = new OkHttpClient();
    Gson gson = new Gson();

    private static final String BASE_URL ="https://arifyheroku.herokuapp.com";


    public static Map<String, String> getImageTargetVsObjectsforScene(String sceneName) {
        Map<String, String> imgVsObj = new HashMap<>();
        ListLinkResult listLinkResult = (new ApiClient()).listLinksForScene(sceneName);
        listLinkResult.results.forEach(result ->{
            imgVsObj.put(result.image_target.link, result.ar_object.link);
        });
        return imgVsObj;
    }


    public String run(String endPoint) {
        try {
            Request request = new Request.Builder()
                    .url(BASE_URL + endPoint)
                    .build();
            System.out.println(request.url());
            try (Response response = client.newCall(request).execute()) {
                return response.body().string();
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) throws IOException, JSONException {
        ApiClient apiClient = new ApiClient();
        apiClient.listLinksForScene("scene_1");
    }


    public ScenesListResult listScenes(){
        String resp = run("/scenes/");
        ScenesListResult listResult = gson.fromJson(resp, ScenesListResult.class);
        for(SceneResult result : listResult.results){
            System.out.println(result.name);
        }
        return listResult;
    }

    public ListLinkResult listLinksForScene(String sceneName) {
        String resp = run("/scenes/" + sceneName + "/list_links");
        ListLinkResult listLinkResult = gson.fromJson(resp, ListLinkResult.class);
        return listLinkResult;
    }

    private static class SceneResult {
        String name;
    }

    private static class ImageTarget{
        String name;
        String link;
    }

    private static class ArObject{
        String name;
        String link;
    }

    private static class LinkResult{
        ImageTarget image_target;
        ArObject ar_object;
    }

    private static class ListLinkResult{
        List<LinkResult> results;
    }

    private static class ScenesListResult{
        List<SceneResult> results;
    }

}
