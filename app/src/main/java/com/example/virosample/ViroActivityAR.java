/*
 * Copyright (c) 2017-present, Viro, Inc.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.virosample;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.viro.core.ARAnchor;
import com.viro.core.ARImageTarget;
import com.viro.core.ARNode;
import com.viro.core.ARScene;
import com.viro.core.AnimationTimingFunction;
import com.viro.core.AnimationTransaction;
import com.viro.core.AsyncObject3DListener;
import com.viro.core.Box;
import com.viro.core.ClickListener;
import com.viro.core.ClickState;
import com.viro.core.Material;
import com.viro.core.Node;
import com.viro.core.Object3D;
import com.viro.core.Spotlight;
import com.viro.core.Surface;
import com.viro.core.Text;
import com.viro.core.Texture;
import com.viro.core.Vector;
import com.viro.core.ViroView;
import com.viro.core.ViroViewARCore;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class ViroActivityAR extends Activity implements ARScene.Listener {

    private static final String TAG = "my_viro_log";
    private ViroView mViroView;
    private ARScene mScene;
    private Map<String, Pair<ARImageTarget, Node>> mTargetedNodesMap;
    private Map<ImageTarget, ArObject> imageTargetVsObjectLocation = new HashMap<>();
    private Map<String, String> keyVsName = new HashMap<>();
    private Map<String, Node> keyVObjectNodeMap = new HashMap<>();
    private Map<String, Vector> keyVsScaleVectorMap = new HashMap<>();


//    private Material preloadCarColorTextures(Node node){
//        final Texture metallicTexture = new Texture(getBitmapFromAssets("object_car_main_Metallic.png"),
//                Texture.Format.RGBA8, true, true);
//        final Texture roughnessTexture = new Texture(getBitmapFromAssets("object_car_main_Roughness.png"),
//                Texture.Format.RGBA8, true, true);
//
//        Material material = new Material();
//        material.setMetalnessMap(metallicTexture);
//        material.setRoughnessMap(roughnessTexture);
//        material.setLightingModel(Material.LightingModel.PHYSICALLY_BASED);
//        node.getGeometry().setMaterials(Arrays.asList(material));
//
//        // Loop through color.
//        for (CAR_MODEL model : CAR_MODEL.values()) {
//            Bitmap carBitmap = getBitmapFromAssets(model.getCarSrc());
//            final Texture carTexture = new Texture(carBitmap, Texture.Format.RGBA8, true, true);
//            mCarColorTextures.put(model, carTexture);
//
//            // Preload our textures into the model
//            material.setDiffuseTexture(carTexture);
//        }
//
//        material.setDiffuseTexture(mCarColorTextures.get(CAR_MODEL.WHITE));
//        return material;
//    }




    // +---------------------------------------------------------------------------+
    //  Initialization
    // +---------------------------------------------------------------------------+

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTargetedNodesMap = new HashMap<String, Pair<ARImageTarget, Node>>();
        imageTargetVsObjectLocation = ArTestActivity.imageTargetVsObjLocationMap;
        mViroView = new ViroViewARCore(this, new ViroViewARCore.StartupListener() {
            @Override
            public void onSuccess() {
                onRenderCreate();
            }

            @Override
            public void onFailure(ViroViewARCore.StartupError error, String errorMessage) {
                Log.e(TAG, "Error initializing AR [" + errorMessage + "]");
            }
        });
        setContentView(mViroView);
        View.inflate(this, R.layout.ar_controls, ((ViewGroup) mViroView));
    }


    private void onRenderCreate() {
        // Create the base ARScene
        mScene = new ARScene();
        mScene.setListener(this);
        mViroView.setScene(mScene);
        imageTargetVsObjectLocation.entrySet().forEach(e ->{
            linkTargetWithNode(e.getKey(), e.getValue());
        });
    }

    public void linkTargetWithNode(ImageTarget imageTarget, ArObject arObject){

        Bitmap imageTargetBtm = imageTarget.btm;

        ARImageTarget arImageTarget = new ARImageTarget(imageTargetBtm, ARImageTarget.Orientation.Up, 0.188f);
        mScene.addARImageTarget(arImageTarget);

        Node arObjectNode = new Node();
        initARModel(arObjectNode, arObject, arImageTarget.getId());
        initSceneLights(arObjectNode);
        arObjectNode.setVisible(false);
        mScene.getRootNode().addChildNode(arObjectNode);

        linkTargetWithNode(arImageTarget, imageTarget.name,arObjectNode, arObject.objectName);
    }

    /*
     Link the given ARImageTarget with the provided Node. When the ARImageTarget is
     found in the scene (by onAnchorFound below), the Node will be made visible and
     the target's transformations will be applied to the Node, thereby rendering the
     Node over the target.
     */
    private void linkTargetWithNode(ARImageTarget imageToDetect, String imageTargetName, Node nodeToRender, String arObjectName){
        String key = imageToDetect.getId();
        keyVsName.put(key, imageTargetName);
        Log.i(TAG, "Adding to key " + key);
        mTargetedNodesMap.put(key, new Pair(imageToDetect, nodeToRender));
    }

    // +---------------------------------------------------------------------------+
    //  ARScene.Listener Implementation
    // +---------------------------------------------------------------------------+

    /*
     When an ARImageTarget is found, lookup the target's corresponding Node in the
     mTargetedNodesMap. Make the Node visible and apply the target's transformations
     to the Node. This makes the Node appear correctly over the target.

     (In this case, this makes the Tesla 3D model and color pickers appear directly
      over the detected Tesla logo)
     */
    @Override
    public void onAnchorFound(ARAnchor anchor, ARNode arNode) {
        String anchorId = anchor.getAnchorId();

        if(toName(anchorId) == null){
            return;
        }
        Node objectNode = keyVObjectNodeMap.get(anchorId);
        Log.i(TAG, "Anchor " + toName(anchorId) + " found, Loading model " + objectNode.getName());
        Toast.makeText(this, "Anchor " + toName(anchorId) + " found. Loading " + objectNode.getName(), Toast.LENGTH_LONG).show();
        if (!mTargetedNodesMap.containsKey(anchorId)) {
            Log.i(TAG, "Expected key " + anchorId + " not found");
            return;
        }
        Log.i(TAG, "Anchor found at " + anchor.getPosition());

        Node imageTargetNode = mTargetedNodesMap.get(anchorId).second;
        Vector rot = new Vector(0,anchor.getRotation().y, 0);
        imageTargetNode.setPosition(anchor.getPosition());
        imageTargetNode.setRotation(rot);
        imageTargetNode.setVisible(true);
        animateModelVisible(imageTargetNode, keyVsScaleVectorMap.get(anchorId));

        // Stop the node from moving in place once found
//        ARImageTarget imgTarget = mTargetedNodesMap.get(anchorId).first;
//        mScene.removeARImageTarget(imgTarget);
//        mTargetedNodesMap.remove(anchorId);
    }

    @Override
    public void onAnchorRemoved(ARAnchor anchor, ARNode arNode) {
        String anchorId = anchor.getAnchorId();
        Log.i(TAG, "Anchor " + toName(anchorId) + " removed");
//        Toast.makeText(this, "Anchor " + toName(anchorId) + " removed", Toast.LENGTH_LONG).show();
        if (!mTargetedNodesMap.containsKey(anchorId)) {
            return;
        }

        Node imageTargetNode = mTargetedNodesMap.get(anchorId).second;
        imageTargetNode.setVisible(false);
    }

    public String toName(String anchorId){
        return keyVsName.get(anchorId);
    }

    @Override
    public void onAnchorUpdated(ARAnchor anchor, ARNode arNode) {
        // No-op
    }

    // +---------------------------------------------------------------------------+
    //  Scene Building Methods
    // +---------------------------------------------------------------------------+

    /*
     Init, loads the the Tesla Object3D, and attaches it to the passed in groupNode.
     */

    private void initARModel(Node groupNode,ArObject arObject, String key) {
        // Creation of ObjectJni to the right
        Object3D objectNode = new Object3D();
//        Log.i(TAG, "Loading with scale ["
//                + arObject.scaleX + ", "
//                + arObject.scaleY + ", "
//                + arObject.scaleZ + "]");
//        Toast.makeText(this, "Loading with scale ["
//                + arObject.scaleX + ", "
//                + arObject.scaleY + ", "
//                + arObject.scaleZ + "]", Toast.LENGTH_LONG).show();

        Vector scale = new Vector(arObject.scaleX, arObject.scaleY, arObject.scaleZ);
        objectNode.loadModel(mViroView.getViroContext(), Uri.parse(arObject.objectWebLink), arObject.type, new AsyncObject3DListener() {
            @Override
            public void onObject3DLoaded(final Object3D object, final Object3D.Type type) {
                Log.i(TAG, "Model " + arObject.objectName + " successfully loaded");
                if(arObject.mtlWebLink != null){
                    loadTextures(object, arObject.mtlWebLink);
                } else{
                    Toast.makeText(ViroActivityAR.this, "No materials to load !!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onObject3DFailed(final String error) {
                Log.e(TAG,"Model Failed to load.");
            }
        });

        groupNode.addChildNode(objectNode);

        // Set click listeners.
        objectNode.setClickListener(new ClickListener() {

            @Override
            public void onClick(int i, Node node, Vector vector) {
                Log.i(TAG, "Making invisible");
                node.setVisible(false);
            }

            @Override
            public void onClickState(int i, Node node, ClickState clickState, Vector vector) {
                // No-op.
            }
        });
        keyVObjectNodeMap.put(key, objectNode);
        keyVsScaleVectorMap.put(key, scale);
        objectNode.setName(arObject.objectName);
    }



    private void loadTextures(Node node, String mtlWebLink) {
        try {
            Bitmap btm = ArTestActivity.getImageAssetUri(mtlWebLink);
            Texture texture = new Texture(btm, Texture.Format.RGBA8, true, true);
            Material material = new Material();
            material.setDiffuseTexture(texture);
            material.setLightingModel(Material.LightingModel.PHYSICALLY_BASED);
            node.getGeometry().setMaterials(Arrays.asList(material));
        } catch (Exception e){
            Log.i(TAG, e.getMessage());
        }
    }

    private void initSceneLights(Node groupNode){
        Node rootLightNode = new Node();

        // Construct a spot light for shadows
        Spotlight spotLight = new Spotlight();
        spotLight.setPosition(new Vector(0,5,0));
        spotLight.setColor(Color.parseColor("#FFFFFF"));
        spotLight.setDirection(new Vector(0,-1,0));
        spotLight.setIntensity(300);
        spotLight.setInnerAngle(5);
        spotLight.setOuterAngle(25);
        spotLight.setShadowMapSize(2048);
        spotLight.setShadowNearZ(2);
        spotLight.setShadowFarZ(7);
        spotLight.setShadowOpacity(.7f);
        spotLight.setCastsShadow(true);
        rootLightNode.addLight(spotLight);

        // Add our shadow planes.
        final Material material = new Material();
        material.setShadowMode(Material.ShadowMode.TRANSPARENT);
        Surface surface = new Surface(2, 2);
        surface.setMaterials(Arrays.asList(material));
        Node surfaceShadowNode = new Node();
        surfaceShadowNode.setRotation(new Vector(Math.toRadians(-90), 0, 0));
        surfaceShadowNode.setGeometry(surface);
        surfaceShadowNode.setPosition(new Vector(0, 0, -0.7));
        rootLightNode.addChildNode(surfaceShadowNode);
        groupNode.addChildNode(rootLightNode);

        Texture environment = Texture.loadRadianceHDRTexture(Uri.parse("file:///android_asset/garage_1k.hdr"));
        mScene.setLightingEnvironment(environment);
    }

    // +---------------------------------------------------------------------------+
    //  Animation Utilities
    // +---------------------------------------------------------------------------+

    private void animateScale(Node node, long duration, Vector targetScale,
                              AnimationTimingFunction fcn, final Runnable runnable) {
        AnimationTransaction.begin();
        AnimationTransaction.setAnimationDuration(duration);
        AnimationTransaction.setTimingFunction(fcn);
        node.setScale(targetScale);
        if (runnable != null){
            AnimationTransaction.setListener(new AnimationTransaction.Listener() {
                @Override
                public void onFinish(AnimationTransaction animationTransaction) {
                    runnable.run();
                }
            });
        }
        AnimationTransaction.commit();
    }

    private void animateModelVisible(Node model, Vector targetScale) {
        animateScale(model, 500, targetScale, AnimationTimingFunction.EaseInEaseOut, null);
    }


    // +---------------------------------------------------------------------------+
    //  Lifecycle
    // +---------------------------------------------------------------------------+

    @Override
    protected void onStart() {
        super.onStart();
        mViroView.onActivityStarted(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViroView.onActivityResumed(this);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mViroView.onActivityPaused(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mViroView.onActivityStopped(this);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mViroView.onActivityDestroyed(this);
    }

    @Override
    public void onTrackingInitialized() {
        // No-op
    }

    @Override
    public void onTrackingUpdated(ARScene.TrackingState state, ARScene.TrackingStateReason reason) {
        // No-op
    }

    @Override
    public void onAmbientLightUpdate(float value, Vector v) {
        // No-op
    }
}