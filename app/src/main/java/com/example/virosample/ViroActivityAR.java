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
import com.viro.core.AsyncObject3DListener;
import com.viro.core.ClickListener;
import com.viro.core.ClickState;
import com.viro.core.Material;
import com.viro.core.Node;
import com.viro.core.Object3D;
import com.viro.core.Spotlight;
import com.viro.core.Surface;
import com.viro.core.Texture;
import com.viro.core.Vector;
import com.viro.core.ViroView;
import com.viro.core.ViroViewARCore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ViroActivityAR extends Activity implements ARScene.Listener {

    private static final String TAG = "my_viro_log";
    private ViroView mViroView;
    private ARScene mScene;

    private Map<String, Pair<ARImageTarget, List<Node>>> mTargetedNodesMap;

    private Map<ViroImageTarget, List<ViroArObject>> imageTargetVsObjects = new HashMap<>();

    private Map<String, String> keyVsName = new HashMap<>();

    private Map<String, List<String>> imageTargetKeyVsNodeNamesList = new HashMap<>();

    private Map<String, ViroArObject> nameVsArObjectMap = new HashMap<>();

    private List<String> activeImageTargets = new ArrayList<>();

    // +---------------------------------------------------------------------------+
    //  Initialization
    // +---------------------------------------------------------------------------+

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTargetedNodesMap = new HashMap<String, Pair<ARImageTarget, List<Node>>>();
        imageTargetVsObjects = ArTestActivity.imageTargetVsArObjectListMap;

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
        findViewById(R.id.reload).setOnClickListener((v) -> {
            Log.i(TAG, "Clicked reload!!!");
            removeActiveObjects();
        });
    }

    public void removeActiveObjects(){
        mTargetedNodesMap.values().stream()
                .filter(pair -> activeImageTargets.contains(pair.first.getId()))
                .forEach(pair -> {
                    activeImageTargets.remove(pair.first.getId());
                    pair.second.forEach(node -> node.setVisible(false));
                });
    }

    private void onRenderCreate() {
        // Create the base ARScene
        mScene = new ARScene();
        mScene.setListener(this);
        mViroView.setScene(mScene);
        imageTargetVsObjects.entrySet().forEach(e -> {
            linkTargetWithNode(e.getKey(), e.getValue());
        });
    }

    public void linkTargetWithNode(ViroImageTarget viroImageTarget, List<ViroArObject> linkedViroArObjects){
        Bitmap imageTargetBtm = viroImageTarget.btm;
        ARImageTarget arImageTarget = new ARImageTarget(imageTargetBtm, ARImageTarget.Orientation.Up, 0.188f);
        mScene.addARImageTarget(arImageTarget);
        String key = arImageTarget.getId();
        keyVsName.put(key, viroImageTarget.name);

        List<Node> nodes = new ArrayList<>();
        List<String> nodeNames = new ArrayList<>();

        for(ViroArObject viroArObject : linkedViroArObjects){
            Node arObjectNode = new Node();
            initARModel(arObjectNode, viroArObject, arImageTarget.getId());
            initSceneLights(arObjectNode);
            arObjectNode.setVisible(false);
            mScene.getRootNode().addChildNode(arObjectNode);
            nodes.add(arObjectNode);
            nodeNames.add(viroArObject.objectName);
            Log.i(TAG, "Loaded model " + viroArObject.objectName);
        }
        imageTargetKeyVsNodeNamesList.put(key, nodeNames);
        mTargetedNodesMap.put(key, new Pair<>(arImageTarget, nodes));
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
        Toast.makeText(this, "Anchor found for " + toName(anchorId), Toast.LENGTH_LONG).show();
        if (!mTargetedNodesMap.containsKey(anchorId)) {
            Log.i(TAG, "Expected key " + anchorId + " not found");
            return;
        }
        activeImageTargets.add(anchorId);
        setNodeNames(anchorId, mTargetedNodesMap.get(anchorId).second);
        makeVisible(anchor, mTargetedNodesMap.get(anchorId).second);
    }

    private void setNodeNames(String anchorId, List<Node> nodesToActivate) {
        List<String> names = imageTargetKeyVsNodeNamesList.get(anchorId);
        for(int i = 0; i < names.size(); i++){
            nodesToActivate.get(i).setName(names.get(i));
        }
    }

    public void makeVisible(ARAnchor anchor, List<Node> arNodes){
        arNodes.forEach(arObjectNode -> {
            ViroArObject viroArObject = nameVsArObjectMap.get(arObjectNode.getName());

            //Position vector
            Vector pos = anchor.getPosition();
            pos.x += viroArObject.XOffset;
            pos.y += viroArObject.YOffset;
            pos.z += viroArObject.ZOffset;
            arObjectNode.setPosition(pos);

            //Rotation vector
            Vector rot = new Vector(viroArObject.rotX, anchor.getRotation().y, viroArObject.rotZ);
            arObjectNode.setRotation(rot);

            //Scale vector
            Vector scale = new Vector();
            scale.x = viroArObject.scaleX;
            scale.y = viroArObject.scaleY;
            scale.z = viroArObject.scaleZ;
            arObjectNode.setScale(scale);

            arObjectNode.setVisible(true);

            Log.i(TAG, "Made object  "  + viroArObject.objectName + " visible");
        });
    }

    @Override
    public void onAnchorRemoved(ARAnchor anchor, ARNode arNode) {
        String anchorId = anchor.getAnchorId();
        Log.i(TAG, "Anchor " + toName(anchorId) + " removed");
        if (!mTargetedNodesMap.containsKey(anchorId)) {
            return;
        }
        mTargetedNodesMap.get(anchorId).second.forEach(arObject ->{
            arObject.setVisible(false);
        });
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

    private void initARModel(Node groupNode, ViroArObject viroArObject, String key) {
        // Creation of ObjectJni to the right
        Object3D objectNode = new Object3D();

        Vector scale = new Vector(viroArObject.scaleX, viroArObject.scaleY, viroArObject.scaleZ);
        objectNode.setScale(scale);
        objectNode.loadModel(mViroView.getViroContext(), Uri.parse(viroArObject.objectWebLink), viroArObject.type, new AsyncObject3DListener() {
            @Override
            public void onObject3DLoaded(final Object3D object, final Object3D.Type type) {
                Log.i(TAG, "Model " + viroArObject.objectName + " successfully loaded");
                if(viroArObject.mtlWebLink != null){
                    loadTextures(object, viroArObject.mtlWebLink);
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
        nameVsArObjectMap.put(viroArObject.objectName, viroArObject);
        objectNode.setName(viroArObject.objectName);
    }

    private void loadTextures(Node node, String mtlWebLink) {
        try {
            Bitmap btm = ApiClient.getImageAssetUri(mtlWebLink);
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
//        AnimationTransaction.begin();
//        AnimationTransaction.setAnimationDuration(duration);
//        AnimationTransaction.setTimingFunction(fcn);
        node.setScale(targetScale);
//        if (runnable != null){
//            AnimationTransaction.setListener(new AnimationTransaction.Listener() {
//                @Override
//                public void onFinish(AnimationTransaction animationTransaction) {
//                    runnable.run();
//                }
//            });
//        }
//        AnimationTransaction.commit();
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