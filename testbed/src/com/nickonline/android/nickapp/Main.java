package com.nickonline.android.nickapp;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import com.nickonline.android.nickapp.ui.PhysicsTest;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mark McKenna &layoutTransition;mark.mckenna@teamspace.ca&gt;
 * @version 0.2
 * @since 8/16/13
 *
 * Static holding-place for things that for some reason can't be injected--such as, for instance,
 * the injector itself.
 */
public class Main extends Application {

    public static final String CONTENT_ITEM_EXTRA = String.format("%s.contentItem", Main.class.getName());

    private static Main instance;

    public static Main getInstance() {
        if (instance == null) throw new RuntimeException("Application not yet initialized!"); // TODO: Specific exception
        return instance;
    }

    /**
     * Set up the dependency injector, and trigger injection on this object.
     */
    public Main() {
        super();
        Main.instance = this;
    }

    /**
     * Set up/start all services, background tasks and threads, etc.
     */
    @Override
    public void onCreate() {
        registerActivityLifecycleCallbacks(lifecycleCallbacks);
        // TODO: Use intent to start poll activity
        Intent i = new Intent(this, PhysicsTest.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }


    private final @NotNull LifecycleCallbacks lifecycleCallbacks = new LifecycleCallbacks();
    private class LifecycleCallbacks implements ActivityLifecycleCallbacks {
        private int liveActivities = 0; // All activity mgmt on UI thread, therefore this is safe to manipulate
        private boolean frozen = false;

        /**
         * Called when a new activity is created.
         */
        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
            // TODO: Implement
        }

        /**
         * Called when a new activity is started, and every time an activity comes back onto the
         * screen after being fully obscured.
         */
        @Override
        public void onActivityStarted(Activity activity) {
            // TODO: Test

        }

        /**
         * Called when a partially obscured activity returns to fully visible.
         */
        @Override
        public void onActivityResumed(Activity activity) {
        }

        /**
         * Called when an activity becomes partially obscured.
         */
        @Override
        public void onActivityPaused(Activity activity) {
        }

        /**
         * Called when an activity becomes fully invisible, either by being replaced by another activity,
         * the application going into the background, or the application shutting down.
         */
        @Override
        public void onActivityStopped(Activity activity) {
            // TODO: Test

        }

        /**
         * Called when an activity is destroyed due to the application shutting down.  This can't be
         * guaranteed to be invoked (app termination skips it).
         */
        @Override
        public void onActivityDestroyed(Activity activity) {
            // TODO: Implement

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
            // TODO: Implement

        }

    }
}
