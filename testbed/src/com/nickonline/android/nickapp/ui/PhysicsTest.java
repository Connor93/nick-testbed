package com.nickonline.android.nickapp.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
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
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.modifier.LoopEntityModifier;
import org.andengine.entity.modifier.RotationModifier;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.view.IRendererListener;
import org.andengine.opengl.view.RenderSurfaceView;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created By: Connor Fraser
 */
public class PhysicsTest extends Activity implements IAccelerationListener, IRendererListener, IUpdateHandler {
    private PhysicsWorld physicsWorld;
    private RenderSurfaceView renderSurfaceView;
    private int cameraWidth;
    private int cameraHeight;
    protected Engine mEngine;
    private Bitmap bitmap;
    private LinearLayout linearLayout;
    private BitmapTextureAtlas playerTexture;
    private ITextureRegion playerTextureRegion;


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
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(200, 450);
        linearLayout.setLayoutParams(lp);

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
        camera.setZClippingPlanes(-100,100);

        mEngine = new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
                cameraWidth, cameraHeight), camera));
        mEngine.startUpdateThread();
        mEngine.enableAccelerationSensor(this, this);

        setContentView(R.layout.physics);

        renderSurfaceView = (RenderSurfaceView) findViewById(R.id.surfaceView);
        renderSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        renderSurfaceView.setRenderer(mEngine, this);
        renderSurfaceView.setZOrderOnTop(true);
        renderSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        onCreateGame();
    }

    public void onCreateGame(){
        linearLayout.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout.layout(0, 0, linearLayout.getRight(), linearLayout.getBottom());

        //------Load Resources------
        final Bitmap out = Bitmap.createBitmap(linearLayout.getMeasuredWidth(), linearLayout.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        final Canvas c = new Canvas();
        c.setBitmap(out);
        linearLayout.draw(c);
        renderSurfaceView.draw(c);

        final BitmapReferenceTexture bitmapReferenceTexture = new BitmapReferenceTexture(mEngine.getTextureManager(), null, out); // TODO: callbacks
        final TextureRegion tagTextureRegion = new TextureRegion(bitmapReferenceTexture, 0, 0, out.getWidth(), out.getHeight());
        bitmapReferenceTexture.load(); // probably call this each time the tag is updated?

        //Load a different resource for testing purposes.

        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        playerTexture = new BitmapTextureAtlas(mEngine.getTextureManager(), 1024, 768);
        playerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(playerTexture, this, "placeholder.jpg",0,0);
        playerTexture.load();




        //------End Load Resources------

        //-----Start Setting up Scene-----
        Scene scene = new Scene();
        scene.setBackgroundEnabled(false);
//        scene.setBackground(new Background(255,255,255));
        physicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
        scene.registerUpdateHandler(physicsWorld);
        scene.registerUpdateHandler(this);
        //-----End Setting up Scene------

        //-----Populate Scene-----------
        Sprite tag = new Sprite(cameraWidth/2, 0, tagTextureRegion, mEngine.getVertexBufferObjectManager()){
            @Override
            protected void applyRotation(final GLState pGLState){
                final float rotation = this.mRotation;
                if(rotation != 0){
                    final float rotationCenterX = this.mRotationCenterX;
                    final float rotationCenterY = this.mRotationCenterY;
                    pGLState.translateModelViewGLMatrixf(rotationCenterX, rotationCenterY, 0);
                    pGLState.rotateModelViewGLMatrixf(rotation, 0,1,0);
                    pGLState.translateModelViewGLMatrixf(-rotationCenterX, rotationCenterY,0);
//                    linearLayout.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                    linearLayout.layout(0, 0, linearLayout.getRight(), linearLayout.getBottom());
//                    linearLayout.draw(c);
//                    bitmapReferenceTexture.load();
                }
            }
        };
        //tag.registerEntityModifier(new LoopEntityModifier(new RotationModifier(1,0,360)));
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

    @Override
    public void onUpdate(float v) {
        GLES20.glClearColor(0f,0f,0f,0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void reset() {
        GLES20.glClearColor(0f,0f,0f,0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

//    @Override
//    public void onUpdate(float pSecondsElapsed) {
//        GLES20.glClearColor(0f, 0f, 0f, 0f);
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//    }
//
//    @Override
//    public void reset() {
//        GLES20.glClearColor(0f, 0f, 0f, 0f);
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//    }
}












