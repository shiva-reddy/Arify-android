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

import com.viro.core.ARAnchor;
import com.viro.core.ARImageTarget;
import com.viro.core.ARNode;
import com.viro.core.ARScene;
import com.viro.core.AnimationTimingFunction;
import com.viro.core.AnimationTransaction;
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

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class ViroActivityAR extends Activity implements ARScene.Listener {

    private static final String TAG = "my_viro_log";
    private ViroView mViroView;
    private ARScene mScene;
    private Node mModelNode;
    private Map<String, Pair<ARImageTarget, Node>> mTargetedNodesMap;
    private Map<Bitmap, File> imageTargetVsObjectLocation = new HashMap<>();


    // +---------------------------------------------------------------------------+
    //  Initialization
    // +---------------------------------------------------------------------------+

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTargetedNodesMap = new HashMap<String, Pair<ARImageTarget, Node>>();
        imageTargetVsObjectLocation = MainActivity.imageTargetVsObjLocationMap;
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

    public void linkTargetWithNode(Bitmap imageTargetBtm, File arObjectFile){

        ARImageTarget arImageTarget = new ARImageTarget(imageTargetBtm, ARImageTarget.Orientation.Up, 0.188f);
        mScene.addARImageTarget(arImageTarget);

        Node arObjectNode = new Node();
        initARModel(arObjectNode, arObjectFile);
        initSceneLights(arObjectNode);
        arObjectNode.setVisible(false);
        mScene.getRootNode().addChildNode(arObjectNode);

        linkTargetWithNode(arImageTarget, arObjectNode);
    }

    /*
     Link the given ARImageTarget with the provided Node. When the ARImageTarget is
     found in the scene (by onAnchorFound below), the Node will be made visible and
     the target's transformations will be applied to the Node, thereby rendering the
     Node over the target.
     */
    private void linkTargetWithNode(ARImageTarget imageToDetect, Node nodeToRender){
        String key = imageToDetect.getId();
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
        Log.i(TAG, "Anchor found");
        String anchorId = anchor.getAnchorId();
        if (!mTargetedNodesMap.containsKey(anchorId)) {
            Log.i(TAG, "Expected key " + anchorId + " not found");
            return;
        }

        Node imageTargetNode = mTargetedNodesMap.get(anchorId).second;
        Vector rot = new Vector(0,anchor.getRotation().y, 0);
        imageTargetNode.setPosition(anchor.getPosition());
        imageTargetNode.setRotation(rot);
        imageTargetNode.setVisible(true);
        animateModelVisible(mModelNode);

        // Stop the node from moving in place once found
//        ARImageTarget imgTarget = mTargetedNodesMap.get(anchorId).first;
//        mScene.removeARImageTarget(imgTarget);
//        mTargetedNodesMap.remove(anchorId);
    }

    @Override
    public void onAnchorRemoved(ARAnchor anchor, ARNode arNode) {
        String anchorId = anchor.getAnchorId();
        Log.i(TAG, "Anchor removed");
        if (!mTargetedNodesMap.containsKey(anchorId)) {
            return;
        }

        Node imageTargetNode = mTargetedNodesMap.get(anchorId).second;
        imageTargetNode.setVisible(false);
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
    private void initARModel(Node groupNode, File file) {
        // Creation of ObjectJni to the right
        Object3D fbxNode = new Object3D();
        fbxNode.setScale(new Vector(0.00f, 0.00f, 0.00f));
        fbxNode.loadModel(mViroView.getViroContext(),
//                Uri.parse("file:///storage/emulated/0/Models/ar_object22.obj"),
                Uri.fromFile(file),
                Object3D.Type.OBJ, new AsyncObject3DListener() {
            @Override
            public void onObject3DLoaded(final Object3D object, final Object3D.Type type) {
                Log.i(TAG, "Model successfully loaded");
            }

            @Override
            public void onObject3DFailed(final String error) {
                Log.e(TAG,"Model Failed to load.");
            }
        });

        groupNode.addChildNode(fbxNode);
        mModelNode = fbxNode;

        // Set click listeners.
        mModelNode.setClickListener(new ClickListener() {
            @Override
            public void onClick(int i, Node node, Vector vector) {

            }

            @Override
            public void onClickState(int i, Node node, ClickState clickState, Vector vector) {
                // No-op.
            }
        });
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

    private void animateModelVisible(Node model) {
        animateScale(model, 500, new Vector(0.09f, 0.09f, 0.09f), AnimationTimingFunction.EaseInEaseOut, null);
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