package com.nickonline.android.nickapp.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.nickonline.android.nickapp.R;
import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.view.ConfigChooser;
import org.andengine.opengl.view.EngineRenderer;
import org.andengine.opengl.view.IRendererListener;
import org.andengine.util.color.Color;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created By: Connor Fraser
 */
public class PhysicsTest extends Activity implements IAccelerationListener, IRendererListener, IUpdateHandler, GLSurfaceView.Renderer{
    private PhysicsWorld physicsWorld;
    private GLSurfaceView renderSurfaceView;
    private int cameraWidth;
    private int cameraHeight;
    protected Engine mEngine;
    private RelativeLayout contentView;
    private Bitmap bitmap;
    private LinearLayout linearLayout;
    private BitmapTextureAtlas playerTexture;
    private ITextureRegion playerTextureRegion;
    private ConfigChooser configChooser;


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
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(lp);



        ImageView answerImage = new ImageView(this);
        answerImage.setImageResource(R.drawable.ticker_day);
        linearLayout.addView(answerImage);



        //-------End Create a poll item----
        Display mDisplay = getWindowManager().getDefaultDisplay();
        cameraWidth  = mDisplay.getWidth();
        cameraHeight = mDisplay.getHeight();
        Camera camera = new Camera(0,0, cameraWidth, cameraHeight);

        mEngine = new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
                cameraWidth, cameraHeight), camera));
        mEngine.startUpdateThread();
        mEngine.enableAccelerationSensor(this, this);

        setContentView(R.layout.physics);
        contentView = (RelativeLayout) findViewById(R.id.contentView);
        LinearLayout bottomText = (LinearLayout)findViewById(R.id.linear_layout);
        bottomText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "hello", Toast.LENGTH_SHORT).show();
            }
        });

        configChooser = new ConfigChooser(false);
        renderSurfaceView = (GLSurfaceView) findViewById(R.id.surfaceView);
        renderSurfaceView.setEGLContextClientVersion(2);
        renderSurfaceView.setZOrderOnTop(true);
        renderSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
        renderSurfaceView.setEGLConfigChooser(8,8,8,8,16,0);
        renderSurfaceView.setRenderer(new EngineRenderer(mEngine, configChooser, this));
        renderSurfaceView.setEnabled(true);
        renderSurfaceView.setFocusable(true);
        renderSurfaceView.setOnTouchListener(mEngine);

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

        final BitmapReferenceTexture bitmapReferenceTexture = new BitmapReferenceTexture(mEngine.getTextureManager(), null, out); // TODO: callbacks
        final TextureRegion tagTextureRegion = new TextureRegion(bitmapReferenceTexture, 0, 0, out.getWidth(), out.getHeight());
        bitmapReferenceTexture.load(); // probably call this each time the tag is updated?

        //------End Load Resources------

        //-----Start Setting up Scene-----
        Scene scene = new Scene();

        scene.setOnSceneTouchListener(new IOnSceneTouchListener() {
            @Override
            public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
                //TODO: Delegate to other views below this one.
                if(pSceneTouchEvent.isActionDown()){
                    final ViewGroup vg = (ViewGroup) renderSurfaceView.getParent();
                    if(vg.getChildAt(1) == renderSurfaceView){
                        new Handler(getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                vg.getChildAt(0).performClick();
                            }
                        });

                    }
                }
                return false;
            }
        });

        scene.setBackground(new Background(Color.TRANSPARENT));
//        scene.setBackgroundEnabled(false);

        //-----End Setting up Scene------

        //-----Populate Scene-----------

        //Start Sprite------------------------------
        final Sprite tag = new Sprite(cameraWidth/2 - (tagTextureRegion.getWidth()/2), 100, tagTextureRegion, mEngine.getVertexBufferObjectManager()){
//            protected void applyRotation(final GLState pGLState){
//                final float rotation = this.mRotation;
//                if(rotation != 0){
//                    final float rotationCenterX = this.mRotationCenterX;
//                    final float rotationCenterY = this.mRotationCenterY;
//                    pGLState.translateModelViewGLMatrixf(rotationCenterX, rotationCenterY, 0);
//                    pGLState.rotateModelViewGLMatrixf(rotation, 0,1,0);
//                    pGLState.translateModelViewGLMatrixf(-rotationCenterX, rotationCenterY,0);
////                    linearLayout.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
////                    linearLayout.layout(0, 0, linearLayout.getRight(), linearLayout.getBottom());
////                    linearLayout.draw(c);
////                    bitmapReferenceTexture.load();
//                }
//            }
            @Override
            public boolean onAreaTouched(TouchEvent pEvent, float pX, float pY){
                Log.d("Connor", "Do I ever get friggen called?");
//                Toast.makeText(getApplicationContext(), "You clicked the floating thingy", Toast.LENGTH_SHORT).show();
                return true;
            }
        };
        physicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
        final FixtureDef TAG_DEF = PhysicsFactory.createFixtureDef(5.0f, 0.5f, 0.5f);
        Body body = PhysicsFactory.createBoxBody(physicsWorld, tag, BodyDef.BodyType.DynamicBody, TAG_DEF);
        body.setLinearDamping(0.3f);

        //End Sprite ---------------------------------

        final Rectangle greenRectangle = new Rectangle(cameraWidth/2, 0, 40, 40, mEngine.getVertexBufferObjectManager());
        greenRectangle.setColor(Color.GREEN);
        scene.attachChild(greenRectangle);

        final Body greenBody = PhysicsFactory.createBoxBody(physicsWorld, greenRectangle, BodyDef.BodyType.StaticBody, PhysicsFactory.createFixtureDef(0, 0, 0));

        //tag.registerEntityModifier(new LoopEntityModifier(new RotationModifier(1,0,360)));



        final RevoluteJointDef revoluteJointDef = new RevoluteJointDef();
        revoluteJointDef.initialize(greenBody, body, greenBody.getWorldCenter());
//        revoluteJointDef.enableMotor = true;
//        revoluteJointDef.motorSpeed = -1;
//        revoluteJointDef.maxMotorTorque = 100;
        physicsWorld.createJoint(revoluteJointDef);


        scene.registerTouchArea(tag);
        scene.attachChild(tag);
        scene.registerUpdateHandler(physicsWorld);
        scene.registerUpdateHandler(this);
        physicsWorld.registerPhysicsConnector(new PhysicsConnector(tag,body,true,true));
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
//        GLES20.glClearColor(0f,0f,0f,0f);
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void reset() {
//        GLES20.glClearColor(0f,0f,0f,0f);
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i2) {
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
//        GLES20.glClearColor(0f,0f,0f,0f);
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }
}












