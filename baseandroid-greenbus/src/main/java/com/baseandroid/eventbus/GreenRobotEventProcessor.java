package com.baseandroid.eventbus;

import android.util.Log;

import com.baseandroid.events.Event;
import com.baseandroid.events.EventProcessor;

import org.greenrobot.eventbus.EventBus;

/**
 * Class managing the events used throughout the application.
 *
 * @author Luca Conversano
 *         created on 20/11/2017.
 */
public final class GreenRobotEventProcessor implements EventProcessor {

    private static final String LOG_TAG = GreenRobotEventProcessor.class.getSimpleName();
    private static boolean verbose = false;

    private GreenRobotEventProcessor() {
        // No instances.
    }

    public static EventProcessor newInstance() {
        return new GreenRobotEventProcessor();
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public void onRegister(Object o) {
        EventBus.getDefault().register(o);
    }

    @Override
    public void onUnregister(Object o) {
        EventBus.getDefault().unregister(o);
    }

    @Override
    public void onPost(Object o) {
        if (o != null) {
            if (verbose) {
                Log.i(LOG_TAG, "received new object to post: " + o.getClass().getSimpleName());
            }
            //check if it's an event we recognise
            if (o.getClass().isAnnotationPresent(Event.class)) {
                Event.Type eventType = o.getClass().getAnnotation(Event.class).type();
                if (verbose) {
                    Log.d(LOG_TAG, "object " + o.getClass().getSimpleName() + " is an event of type " + eventType);
                }
                EventBus.getDefault().post(o);
            } else {
                Log.d(LOG_TAG, "received dead event: " + o.getClass().getSimpleName());
            }
        }
    }

    @Override
    public String onSavePoint(Object object) {
        return null;
    }

    @Override
    public void onLoadPoint(Object object, String key) {

    }

}
