package com.example.virosample;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {

    OkHttpClient client = new OkHttpClient().newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
    Gson gson = new Gson();

    private static final String BASE_URL ="https://arifyheroku.herokuapp.com";

    public static ApiClient build(){
        return new ApiClient();
    }

    /**
     * Lists all scenes
     * @return
     */
    public ScenesListResult listScenes(){
        String resp = get("/scenes/");
        ScenesListResult listResult = gson.fromJson(resp, ScenesListResult.class);
        for(SceneResult result : listResult.results){
            System.out.println(result.name);
        }
        return listResult;
    }

    /**
     * Upload image target to a scene
     * @param scene
     * @param imageTargetName
     * @param imageTargetFile
     */
    public void uploadImageTargetToScene(String scene, String imageTargetName, File imageTargetFile){
        MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image_name", imageTargetName)
                .addFormDataPart("file", imageTargetFile.getName(), RequestBody.create(MEDIA_TYPE_PNG, imageTargetFile))
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "/" + scene + "/upload_image")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * To link an image target to an ar object in a scene
     * @param scene
     * @param arObjectName
     * @param imageTargetName
     */
    public void linkImageTargetToARObjectInScene(String scene, String arObjectName, String imageTargetName){
        Request request = new Request.Builder()
                .url(BASE_URL + "/" + scene + "/link_image_with_ar_object")
                .post(new FormBody.Builder()
                        .add("image_name", imageTargetName)
                        .add("ar_object_name", arObjectName)
                        .build())
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
    }

    /**
     * All linked image targets and corresponding associated ar objects
     * @param sceneName
     * @return
     */
    public ListLinkResult listLinksForScene(String sceneName) {
        String resp = get("/scenes/" + sceneName + "/list_links");
        ListLinkResult listLinkResult = gson.fromJson(resp, ListLinkResult.class);
        return listLinkResult;
    }

    /**
     * All Ar Objects in a scene
     * @param sceneName
     * @return
     */
    public ListArObjectsResult listArObjectsForScene(String sceneName) {
        String resp = get("/scenes/" + sceneName + "/list_ar_objects");
        return gson.fromJson(resp, ListArObjectsResult.class);
    }

    /**
     * All Image targets in a scene
     * @param sceneName
     * @return
     */
    public ListImageTargetsResult listImageTargetsResult(String sceneName){
        String resp = get("/scenes/" + sceneName + "/list_image_targets");
        return gson.fromJson(resp, ListImageTargetsResult.class);
    }

    public static void main(String[] args) throws IOException, JSONException {
        ApiClient apiClient = new ApiClient();
        apiClient.listLinksForScene("scene_1");
    }

    public String get(String endPoint) {
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


    public static class SceneResult {
        String name;
    }

    public static class ImageTarget{
        String name;
        String link;
    }

    public static class ArObject{
        String name;
        String link;
    }

    public static class LinkResult{
        ImageTarget image_target;
        ArObject ar_object;
    }

    public static class ListArObjectsResult{
        List<ArObject> results;
    }

    public static class ListImageTargetsResult{
        List<ImageTarget> results;
    }

    public static class ListLinkResult{
        List<LinkResult> results;
    }

    public static class ScenesListResult{
        List<SceneResult> results;
    }

}
