package com.nickonline.android.nickapp.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Display;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.WakeLockOptions;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.view.IRendererListener;
import org.andengine.opengl.view.RenderSurfaceView;
import org.andengine.ui.activity.BaseActivity;
import org.andengine.util.ActivityUtils;
import org.andengine.util.Constants;

/**
 * Created By: Connor Fraser
 */
public abstract class BaseGameActivity extends BaseActivity implements IRendererListener {
    protected Engine mEngine;

    private PowerManager.WakeLock mWakeLock;

    protected RenderSurfaceView mRenderSurfaceView;


    private boolean mGameCreated;
    private boolean mCreateGameCalled;
    private boolean mOnReloadResourcesScheduled;

    protected void onCreate(final Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);
        Display mDisplay = this.getWindowManager().getDefaultDisplay();
        int cameraWidth = mDisplay.getWidth();
        int cameraHeight = mDisplay.getHeight();
        Camera camera = new Camera(0,0, cameraWidth, cameraHeight);
        mEngine = new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
                cameraWidth, cameraHeight), camera));
        mEngine.startUpdateThread();

        mRenderSurfaceView = new RenderSurfaceView(this);
        mRenderSurfaceView.setRenderer(mEngine, this);

        setContentView(mRenderSurfaceView, createSurfaceViewLayoutParams());
    }

    public synchronized void onSurfaceCreated(final GLState pGLState) {
        if(mGameCreated) {
            mEngine.onReloadResources();
            mEngine.start();
        } else {
            if (mCreateGameCalled) {
                mOnReloadResourcesScheduled = true;
            } else {
                mCreateGameCalled = true;
                onCreateGame();
            }
        }
    }

    @Override
    public synchronized void onSurfaceChanged(final GLState pGLState, final int pWidth, final int pHeight) {
    }

    protected synchronized void onCreateGame() {
        // TODO: Create resources to draw (onCreateResources from docs) set callbacks
        Scene pScene = new Scene();
        // TODO: Create scene stuff (onCreateScene from docs) set callbacks
        mEngine.setScene(pScene);
        // TODO: Populate scene stuff (onPopulateScene from docs) set callbacks
        mGameCreated = true;

        if(mOnReloadResourcesScheduled) {
            mOnReloadResourcesScheduled = false;
            mEngine.onReloadResources();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEngine.start();
            }
        });
    }

    @Override
    protected synchronized void onResume() {
        super.onResume();
        acquireWakeLock();
        mRenderSurfaceView.onResume();
        mEngine.start();
    }

    @Override
    public synchronized void onWindowFocusChanged(final boolean pHasWindowFocus) {
        super.onWindowFocusChanged(pHasWindowFocus);

        if(pHasWindowFocus && mGameCreated)
            mEngine.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mRenderSurfaceView.onPause();
        releaseWakeLock();

        mEngine.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEngine.onDestroy();

        mGameCreated = false;

        mEngine = null;
    }

    private void acquireWakeLock() {
        acquireWakeLock(mEngine.getEngineOptions().getWakeLockOptions());
    }

    private void acquireWakeLock(final WakeLockOptions pWakeLockOptions) {
        if (pWakeLockOptions == WakeLockOptions.SCREEN_ON) {
            ActivityUtils.keepScreenOn(this);
        } else {
            final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(pWakeLockOptions.getFlag() | PowerManager.ON_AFTER_RELEASE, Constants.DEBUGTAG);
            mWakeLock.acquire();
        }
    }

    private void releaseWakeLock() {
        if(mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    protected static FrameLayout.LayoutParams createSurfaceViewLayoutParams() {
        final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        return layoutParams;
    }
}
