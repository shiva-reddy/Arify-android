package com.example.virosample;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.gson.Gson;
import com.viro.core.Object3D;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    private static final String BASE_URL ="https://arify-backend-1.herokuapp.com";

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
        return listResult;
    }

    /**
     * Upload image target to a scene
     * @param sceneName
     */
    public void createScene(String sceneName){
        if(listScenes().results.stream().anyMatch(scene -> scene.name.equals(sceneName))){
            return;
        }

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("scene_name", sceneName)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "/scenes/create_new")
                .post(requestBody)
                .build();


        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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


        Request request = new Request.Builder().addHeader("accept","*/*").addHeader("accept-encoding"
                ,"gzip, deflate")
                .url( BASE_URL + "/scenes/" + scene + "/upload_image")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void updateArObject(String scene, String arObjectName,
                               Float scaleX, Float scaleY, Float scaleZ,
                               Float posX, Float posY, Float posZ,
                               Float rotX, Float rotZ){

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("ar_object_name", arObjectName)
                .addFormDataPart("scale_x", scaleX.toString())
                .addFormDataPart("scale_y", scaleY.toString())
                .addFormDataPart("scale_z", scaleZ.toString())
                .addFormDataPart("pos_offset_x", posX.toString())
                .addFormDataPart("pos_offset_y", posY.toString())
                .addFormDataPart("pos_offset_z", posZ.toString())
                .addFormDataPart("rot_x", rotX.toString())
                .addFormDataPart("rot_z", rotZ.toString())
                .build();


        Request request = new Request.Builder()
                .url( BASE_URL + "/scenes/" + scene + "/update_ar_object")
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
                .url(BASE_URL + "/scenes/" + scene + "/link_image_with_ar_object")
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

    public Map<ViroImageTarget, List<ViroArObject>> getImageTargetVsArObjectList(String sceneName){
        Map<ImageTarget, List<LinkResult>> collectorsResult = listLinksForScene(sceneName)
                .results
                .stream()
                .collect(Collectors.groupingBy(ApiClient.LinkResult::imageTarget));

        return collectorsResult
                .entrySet().stream().collect(
                        Collectors.toMap(e->{
                            ViroImageTarget viroImageTarget = new ViroImageTarget();
                            viroImageTarget.btm = getImageAssetUri(e.getKey().link);
                            viroImageTarget.name = e.getKey().name;
                            return viroImageTarget;
                        }, e -> {
                            List<ViroArObject> viroArObjects = new ArrayList<>();
                            e.getValue().forEach(linkResult -> {
                                viroArObjects.add(toArObject(linkResult.ar_object));
                            });
                            return viroArObjects;
                        })
                );
    }

    private ViroArObject toArObject(ApiClient.ArObject ar_object) {
        ViroArObject viroArObject = new ViroArObject();
        viroArObject.objectWebLink = ar_object.link;
        viroArObject.objectName = ar_object.name;
        viroArObject.mtlWebLink = ar_object.mtl_link;
        viroArObject.type = ar_object.objType();
        viroArObject.scaleX = getOrDefault(ar_object.scale_x, 0.1f);
        viroArObject.scaleY = getOrDefault(ar_object.scale_y, 0.1f);
        viroArObject.scaleZ = getOrDefault(ar_object.scale_z, 0.1f);
        viroArObject.XOffset = getOrDefault(ar_object.pos_offset_x, 0.0f);
        viroArObject.YOffset = getOrDefault(ar_object.pos_offset_y, 0.0f);
        viroArObject.ZOffset = getOrDefault(ar_object.pos_offset_z, 0.0f);
        viroArObject.rotX = getOrDefault(ar_object.rot_x, 0.0f);
        viroArObject.rotZ = getOrDefault(ar_object.rot_z,0.0f);
        return viroArObject;
    }

    private static Float getOrDefault(Float val, Float def){
        return val == null? def : val;
    }

    public static Bitmap getImageAssetUri(String link) {
        try {
            Log.i("my_viro_log", link);
            URL aUrl = new URL(link);
            return BitmapFactory.decodeStream((InputStream) aUrl.getContent());
        } catch (Exception e){
            throw new RuntimeException(e);
        }
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
        apiClient.getImageTargetVsArObjectList("scene_1");
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

    public String get2(String endPoint) {
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
    
    public String get3(String endPoint) {
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

        @Override
        public int hashCode(){
            return name.hashCode();
        }

        @Override
        public String toString(){
            return name;
        }

        @Override
        public boolean equals(Object obj){
            return ((ImageTarget) obj).name.equals(name);
        }
    }

    public static class ArObject{
        String name;
        String link;
        Float scale_x, scale_y, scale_z, rot_x, rot_z, pos_offset_x, pos_offset_y, pos_offset_z;
        String model_type;
        String mtl_link;

        public Object3D.Type objType(){
            if(model_type == null){
                return null;
            }
            if(model_type.toLowerCase().equals("glb")){
                return Object3D.Type.GLB;
            } else if(model_type.toLowerCase().equals("fbx")){
                return Object3D.Type.FBX;
            }
            throw new RuntimeException("invalid object type");
        }
    }

    public class SceneList {
        String name;
    }

    public static class LinkResult{
        ImageTarget image_target;
        ArObject ar_object;

        public ImageTarget imageTarget(){
            return image_target;
        }
    }

    public static class LinkSceneResults{
        SceneList sceneName;
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
        List<SceneList> results;
    }

}
