package org.andengine.ui.activity;

import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import org.andengine.engine.Engine;
import org.andengine.engine.options.EngineOptions;
import org.andengine.entity.scene.Scene;
import org.andengine.input.sensor.acceleration.AccelerationSensorOptions;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.opengl.font.FontManager;
import org.andengine.opengl.shader.ShaderProgramManager;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.opengl.view.IRendererListener;
import org.andengine.opengl.view.RenderSurfaceView;
import org.andengine.ui.IGameInterface;

/**
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga Inc.
 * 
 * @author Nicolas Gramlich
 * @since 11:27:06 - 08.03.2010
 */
public abstract class BaseGameActivity extends BaseActivity implements IGameInterface, IRendererListener {
	protected Engine mEngine;
	protected RenderSurfaceView mRenderSurfaceView;
	private boolean mGamePaused;
	private boolean mGameCreated;
	private boolean mCreateGameCalled;
	private boolean mOnReloadResourcesScheduled;


	@Override
	protected void onCreate(final Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);
        mGamePaused = true;
        mEngine = onCreateEngine(onCreateEngineOptions());
        mEngine.startUpdateThread();
        onSetContentView();
	}

	@Override
	public Engine onCreateEngine(final EngineOptions pEngineOptions) {
		return new Engine(pEngineOptions);
	}

    //Called when the surface changes size or is created (GLSurfaceView)
	@Override
	public synchronized void onSurfaceCreated(final GLState pGLState) {
		if(mGameCreated) {
            onReloadResources();
			if(mGamePaused && mGameCreated) {
                onResumeGame();
			}
		} else {
			if(mCreateGameCalled) {
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
		final OnPopulateSceneCallback onPopulateSceneCallback = new OnPopulateSceneCallback() {
			@Override
			public void onPopulateSceneFinished() {
				try {
                    onGameCreated();
				} catch(final Throwable pThrowable) {
					//TODO:Something
				}
                callGameResumedOnUIThread();
			}
		};

		final OnCreateSceneCallback onCreateSceneCallback = new OnCreateSceneCallback() {
			@Override
			public void onCreateSceneFinished(final Scene pScene) {
                mEngine.setScene(pScene);
				try {
                    onPopulateScene(pScene, onPopulateSceneCallback);
				} catch(final Throwable pThrowable) {
					//TODO: Something
				}
			}
		};

		final OnCreateResourcesCallback onCreateResourcesCallback = new OnCreateResourcesCallback() {
			@Override
			public void onCreateResourcesFinished() {
				try {
                    onCreateScene(onCreateSceneCallback);
				} catch(final Throwable pThrowable) {
					//TODO: Something
				}
			}
		};

		try {
			this.onCreateResources(onCreateResourcesCallback);
		} catch(final Throwable pThrowable) {
			//TODO: Something
		}

	}

	@Override
	public synchronized void onGameCreated() {
		this.mGameCreated = true;

		/* Since the potential asynchronous resource creation,
		 * the surface might already be invalid
		 * and a resource reloading might be necessary. */
		if(this.mOnReloadResourcesScheduled) {
			this.mOnReloadResourcesScheduled = false;
			try {
				this.onReloadResources();
			} catch(final Throwable pThrowable) {
				//TODO: Something
			}
		}
	}

	@Override
	protected synchronized void onResume() {
		super.onResume();
		this.mRenderSurfaceView.onResume();
	}

	@Override
	public synchronized void onResumeGame() {
		this.mEngine.start();
		this.mGamePaused = false;
	}

	@Override
	public synchronized void onWindowFocusChanged(final boolean pHasWindowFocus) {
		super.onWindowFocusChanged(pHasWindowFocus);

		if(pHasWindowFocus && this.mGamePaused && this.mGameCreated) {
			this.onResumeGame();
		}
	}

	@Override
	public void onReloadResources() {
		this.mEngine.onReloadResources();
	}

	@Override
	protected void onPause() {
		super.onPause();

		this.mRenderSurfaceView.onPause();

		if(!this.mGamePaused) {
			this.onPauseGame();
		}
	}

	@Override
	public synchronized void onPauseGame() {
		this.mGamePaused = true;
		this.mEngine.stop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		this.mEngine.onDestroy();

		try {
			this.onDestroyResources();
		} catch (final Throwable pThrowable) {
            //TODo: Something
		}

		this.onGameDestroyed();

		this.mEngine = null;
	}

	@Override
	public void onDestroyResources() throws Exception {}

	@Override
	public synchronized void onGameDestroyed() {
		this.mGameCreated = false;
	}

	public Engine getEngine() {
		return this.mEngine;
	}

	public boolean isGamePaused() {
		return this.mGamePaused;
	}

	public boolean isGameRunning() {
		return !this.mGamePaused;
	}

	public boolean isGameLoaded() {
		return this.mGameCreated;
	}

	public VertexBufferObjectManager getVertexBufferObjectManager() {
		return this.mEngine.getVertexBufferObjectManager();
	}

	public TextureManager getTextureManager() {
		return this.mEngine.getTextureManager();
	}

	public FontManager getFontManager() {
		return this.mEngine.getFontManager();
	}

	public ShaderProgramManager getShaderProgramManager() {
		return this.mEngine.getShaderProgramManager();
	}

	private void callGameResumedOnUIThread() {
        runOnUiThread(new Runnable() {
			@Override
			public void run() {
                onResumeGame();
			}
		});
	}

	protected void onSetContentView() {
		this.mRenderSurfaceView = new RenderSurfaceView(this);
		this.mRenderSurfaceView.setRenderer(this.mEngine, this);

		this.setContentView(this.mRenderSurfaceView, BaseGameActivity.createSurfaceViewLayoutParams());
	}

	protected static LayoutParams createSurfaceViewLayoutParams() {
		final LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		layoutParams.gravity = Gravity.CENTER;
		return layoutParams;
	}

	protected boolean enableAccelerationSensor(final IAccelerationListener pAccelerationListener) {
		return this.mEngine.enableAccelerationSensor(this, pAccelerationListener);
	}

	protected boolean enableAccelerationSensor(final IAccelerationListener pAccelerationListener, final AccelerationSensorOptions pAccelerationSensorOptions) {
		return this.mEngine.enableAccelerationSensor(this, pAccelerationListener, pAccelerationSensorOptions);
	}

	protected boolean disableAccelerationSensor() {
		return this.mEngine.disableAccelerationSensor(this);
	}
}
