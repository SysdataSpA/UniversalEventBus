/*
 * Copyright (C) 2020 Sysdata Digital, S.r.l.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baseandroid.events;

import android.util.Log;

/**
 * Class managing the events used throughout the application.
 * <br>
 * Events are stored in queues depending on their {@link Event.Type} and these queues are checked periodically in a specific order:
 * <ol>
 * <li><code>UI</code></li>
 * <li><code>NETWORK</code></li>
 * <li><code>DATA</code></li>
 * <li><code>GENERIC</code></li>
 * <li><code>CONTEXT</code></li>
 * </ol>
 * <br>
 *
 * @author Andrea Guitto
 *         created on 12/04/2016.
 */
public final class EventDispatcher {

    // Using Otto as default
    private static EventProcessor INSTANCE = StubEventProcessors.newInstance();

    private EventDispatcher() {
        // No instances.
    }

    /**
     * Setup an instance of {@link EventProcessor} to handle events.
     * @param processor
     */
    public static void useEventProcessor(EventProcessor processor) {
        INSTANCE = processor;
    }

    /**
     * Registers a given Object on both Buses
     *
     * @param o Object to register on the Buses
     */
    public static void register(Object o) {
        INSTANCE.onRegister(o);
    }

    /**
     * Unregisters a given Object from both Buses
     *
     * @param o Object to unregister from the Buses
     */
    public static void unregister(Object o) {
        INSTANCE.onUnregister(o);
    }

    /**
     * After checking whether the object being posted is annotated with the {@link Event} Annotation,
     * it will be placed in the corresponding queue and such queue will be then sorted by {@link Event.Priority}.
     * <p>
     * <b>NOTE: if an object being posted has not been annotated with the {@link Event} Annotation it will be disregarded!!!</b>
     * </p>
     *
     * @param o the Object we want to post as an event
     */
    public static void post(Object o) {
        INSTANCE.onPost(o);
    }

    /**
     * This method return a string used by event processor to save the state of the object in configuration changes.<br>
     * You should use the string returned by this method with {@code EventDispatcher.loadPoint()}<br>
     * This method must be called before {@code EventDispatcher.unregister(...)} (in {@code OnPause(...)} method for example)
     * <p>
     * <b>NOTE: this string should be saved on instance state in Activity and retrieved when it restart on OnCreate(...) method</b>
     * </p>
     *
     * @param object
     * @return
     */
    public static String savePoint(Object object) { return INSTANCE.onSavePoint(object);    }

    /**
     * This method load the configuration variables used by EventDispatcher to handle the configuration changes.<br>
     * You should use this method in pair with {@code EventDispatcher.savePoint(...)}.<br>
     * This method must be called before {@code EventDispatcher.register(...)} (in {@code OnResume(...)} method for example)<br>
     *
     * @param object
     *
     */
    public static void loadPoint(Object object, String tag) {   INSTANCE.onLoadPoint(object, tag);  }

    /**
     * This class is used to create a Stub event processors and alert developer about a wrond use of {@link EventDispatcher}
     */
    private static class StubEventProcessors implements EventProcessor {
        private static final String LOG_TAG = StubEventProcessors.class.getSimpleName();

        /**
         * This method return a new {@link StubEventProcessors} instance
         * @return
         */
        public static EventProcessor newInstance() {
            return new StubEventProcessors();
        }

        @Override
        public void onRegister(Object o) {
            Log.e(LOG_TAG, "onRegister: No EventProcessor declared, you should declare calling EventDispatcher.useEventProcessor(...)! All events will be misses!!");
        }

        @Override
        public void onUnregister(Object o) {
            Log.e(LOG_TAG, "onUnregister: No EventProcessor declared, you should declare calling EventDispatcher.useEventProcessor(...)! All events will be misses!!");
        }

        @Override
        public void onPost(Object o) {
            Log.e(LOG_TAG, "onPost: No EventProcessor declared, you should declare calling EventDispatcher.useEventProcessor(...)! Event of type "+o.getClass().getName()+" will be missed.");
        }

        @Override
        public String onSavePoint(Object object) {
            Log.e(LOG_TAG, "onSavePoint: No EventProcessor declared, you should declare calling EventDispatcher.useEventProcessor(...)!");
            return null;
        }

        @Override
        public void onLoadPoint(Object object, String key) {
            Log.e(LOG_TAG, "onLoadPoint: No EventProcessor declared, you should declare calling EventDispatcher.useEventProcessor(...)!");
        }
    }
}