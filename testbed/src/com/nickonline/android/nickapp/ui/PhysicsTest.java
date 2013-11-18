package com.nickonline.android.nickapp.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Display;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.nickonline.android.nickapp.R;
import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.view.IRendererListener;
import org.andengine.opengl.view.RenderSurfaceView;

/**
 * Created By: Connor Fraser
 */
public class PhysicsTest extends Activity implements IAccelerationListener, IRendererListener{
    private PhysicsWorld physicsWorld;
    private RenderSurfaceView renderSurfaceView;
    private int cameraWidth;
    private int cameraHeight;
    protected Engine mEngine;
    private Bitmap bitmap;
    private LinearLayout linearLayout;


    public PhysicsTest(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //-------Create a poll item -------
        linearLayout = new LinearLayout(this);
        linearLayout.setDrawingCacheEnabled(true);
        linearLayout.setBackgroundResource(R.drawable.polltagfront);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView answer = new TextView(this);
        answer.setText("answer goes here");
        linearLayout.addView(answer);

        ImageView answerImage = new ImageView(this);
        answerImage.setImageResource(R.drawable.placeholder);
        linearLayout.addView(answerImage);


        //-------End Create a poll item----
        Display mDisplay = getWindowManager().getDefaultDisplay();
        cameraWidth  = mDisplay.getWidth();
        cameraHeight = (int) (mDisplay.getHeight()*0.70);
        Camera camera = new Camera(0,0, cameraWidth, cameraHeight);

        mEngine = new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
                cameraWidth, cameraHeight), camera));
        mEngine.startUpdateThread();
        mEngine.enableAccelerationSensor(this, this);
        setContentView(R.layout.physics);
        renderSurfaceView = (RenderSurfaceView) findViewById(R.id.surfaceView);
        renderSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        renderSurfaceView.setRenderer(mEngine, this);
        renderSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        renderSurfaceView.setZOrderMediaOverlay(true);
        onCreateGame();
    }

    public void onCreateGame(){
        //------Load Resources------
        final Bitmap out = Bitmap.createBitmap(1024, 768, Bitmap.Config.ARGB_8888);
        final Canvas c = new Canvas();
        c.setBitmap(out);
        linearLayout.draw(c);

        final BitmapReferenceTexture bitmapReferenceTexture = new BitmapReferenceTexture(mEngine.getTextureManager(), null, out); // TODO: callbacks
        final TextureRegion tagTextureRegion = new TextureRegion(bitmapReferenceTexture, 0, 0, out.getWidth(), out.getHeight());
        bitmapReferenceTexture.load(); // probably call this each time the tag is updated?
        //------End Load Resources------

        //-----Start Setting up Scene-----
        Scene scene = new Scene();
        scene.setBackground(new Background(255,255,255));
        physicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
        scene.registerUpdateHandler(physicsWorld);
        //-----End Setting up Scene------

        //-----Populate Scene-----------
        Sprite tag = new Sprite(cameraWidth/2, 0, tagTextureRegion, mEngine.getVertexBufferObjectManager());
        final FixtureDef TAG_DEF = PhysicsFactory.createFixtureDef(10.0f, 1.0f, 0.0f);
        Body body = PhysicsFactory.createBoxBody(physicsWorld, tag, BodyDef.BodyType.DynamicBody, TAG_DEF);
        body.setLinearDamping(1.0f);
        body.setAngularDamping(1.0f);
        scene.attachChild(tag);
        physicsWorld.registerPhysicsConnector(new PhysicsConnector(tag,body,true,false));
        //-----End Populate Scene--------

        // TODO: Create scene stuff (onCreateScene from docs) set callbacks
        mEngine.setScene(scene);
        // TODO: Populate scene stuff (onPopulateScene from docs) set callbacks

        mEngine.onReloadResources();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEngine.start();
            }
        });
    }

    @Override
    public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
    }

    @Override
    public void onAccelerationChanged(AccelerationData pAccelerationData) {
        final Vector2 gravity = Vector2Pool.obtain(pAccelerationData.getX(), pAccelerationData.getY());
        physicsWorld.setGravity(gravity);
        Vector2Pool.recycle(gravity);
    }

    @Override
    public void onSurfaceCreated(GLState pGlState) {
    }

    @Override
    public void onSurfaceChanged(GLState pGlState, int pWidth, int pHeight) {
    }


//    @Override
//    public EngineOptions onCreateEngineOptions() {
//        Camera camera = new Camera(0,0,cameraWidth, cameraHeight);
//        EngineOptions options = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
//                cameraWidth, cameraHeight), camera);
//        return options;
//    }
//
//    @Override
//    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) {
//        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
//        tagTexture = new BitmapTextureAtlas(getTextureManager(), 1024, 768);
//        tagTextureRegion = BitmapTextureAtlasTextureRegionFactory
//                .createFromAsset(tagTexture, this, "polltagfront.png", 0, 0);
//        tagTexture.load();
//        pOnCreateResourcesCallback.onCreateResourcesFinished();
//    }
//
//    @Override
//    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) {
//        scene = new Scene();
//        scene.setBackground(new Background(0, 125, 58));
//        physicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
//        scene.registerUpdateHandler(physicsWorld);
//        pOnCreateSceneCallback.onCreateSceneFinished(this.scene);
//    }
//
//    @Override
//    public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) {
//        Sprite tag = new Sprite(cameraWidth/2, 0, tagTextureRegion, getEngine().getVertexBufferObjectManager());
//        final FixtureDef TAG_DEF = PhysicsFactory.createFixtureDef(10.0f, 1.0f, 0.0f);
//        Body body = PhysicsFactory.createBoxBody(physicsWorld, tag, BodyDef.BodyType.DynamicBody, TAG_DEF);
//        body.setLinearDamping(2.5f);
//        body.setAngularDamping(2.5f);
//        scene.attachChild(tag);
//        physicsWorld.registerPhysicsConnector(new PhysicsConnector(tag,body,true,false));
//        pOnPopulateSceneCallback.onPopulateSceneFinished();
//    }
//
//    @Override
//    public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
//
//    }
//
//    @Override
//    public void onAccelerationChanged(AccelerationData pAccelerationData) {
//        final Vector2 gravity = Vector2Pool.obtain(pAccelerationData.getX(), pAccelerationData.getY());
//        physicsWorld.setGravity(gravity);
//        Vector2Pool.recycle(gravity);
//    }
//
//    @Override
//    public void onResumeGame() {
//        super.onResumeGame();
//        enableAccelerationSensor(this);
//    }
//
//    @Override
//    public void onPauseGame() {
//        super.onPauseGame();
//        disableAccelerationSensor();
//    }
}












