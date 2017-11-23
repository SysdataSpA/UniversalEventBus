/*
 * Copyright (C) 2016 Sysdata Digital, S.r.l.
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

package com.baseandroid.events.otto;

import com.baseandroid.events.Event;
import com.baseandroid.events.EventProcessor;
import com.squareup.otto.Bus;
import com.squareup.otto.DeadEvent;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Class managing the events used throughout the application.
 * <p>
 * This class defines two {@link Bus}es:
 * <ul>
 * <li>{@link #BUS}: a Bus working on a thread separated by the Android Main Thread</li>
 * <li>{@link #UI_BUS}: a Bus working on the Android Main Thread</li>
 * </ul>
 * <br>
 * <p>
 * Events are stored in queues depending on their {@link Event.Type} and these queues are checked periodically in a specific order:
 * <ol>
 * <li><code>UI</code></li>
 * <li><code>NETWORK</code></li>
 * <li><code>DATA</code></li>
 * <li><code>GENERIC</code></li>
 * <li><code>CONTEXT</code></li>
 * </ol>
 * <br>
 * <p>
 * If an Event is not caught by anybody, it will eventually be caught by the {@link DeadEventManager},
 * which will do nothing more than log that an event has not been managed.
 * </p>
 *
 * @author Stefano Ciarcia'
 *         created on 22/07/2015.
 */
public final class OttoEventProcessor implements EventProcessor {

    /**
     * Bus working on the Android Main Thread
     */
    public static final Bus UI_BUS = new MainThreadBus();
    private static final Logger LOGGER = LoggerFactory.getLogger(OttoEventProcessor.class);

    /**
     * Defines the time interval (in milliseconds) used to poll the event queues
     */
    private static final long EVENT_CONSUMPTION_INTERVAL = 10; // 10ms

    /**
     * Bus working on a thread separated by the Android Main Thread
     */
    private static final Bus BUS = new Bus(ThreadEnforcer.ANY);
    /**
     * flag used to determine whether this EventDispatcher has been initialised
     */
    private static boolean mInitialised = false;
    /**
     * Synchronized Queues of <code>NETWORK</code> events
     */
    private static List<Object> mNetworkEvents;
    /**
     * Synchronized Queues of <code>DATA</code> events
     */
    private static List<Object> mDataEvents;
    /**
     * Synchronized Queues of <code>GENERIC</code> events
     */
    private static List<Object> mGenericEvents;
    /**
     * Synchronized Queues of <code>UI</code> events
     */
    private static List<Object> mUIEvents;
    /**
     * Synchronized Queues of <code>CONTEXT</code> events
     */
    private static List<Object> mContextEvents;

    DeadEventManager mDeadEventManager;

    private OttoEventProcessor() {
        // No instances.
    }

    public static EventProcessor newInstance() {
        return new OttoEventProcessor();
    }

    /**
     * This method starts some kind of polling of the events queues.
     * <p>
     * Once every 10 ms (or whatever is specified by the {@link #EVENT_CONSUMPTION_INTERVAL} field in this very class)
     * the queues will be checked for the presence of an event of some sort.
     * </p>
     * <p>
     * The UI events will be managed on their own, while the other events will be checked in the following order:
     * <ol>
     * <li>Network Events</li>
     * <li>Data Events</li>
     * <li>Generic Events</li>
     * <li>Context Events</li>
     * </ol>
     * Moreover, since the Events are ordered by Priority, the more urgent events of each queue will be processed in turn
     * <p>
     * If an event is present, it will be posted on the related bus to be processed.
     */
    private static void startEventsConsumption() {
        LOGGER.info("starting UI events consumption processors");

        /*
         * This Observable (Observable.interval) cycles every EVENT_CONSUMPTION_INTERVAL millis
         * we observe results on a new Thread and remove items from non-UI queues
         */
        rx.Observable.interval(EVENT_CONSUMPTION_INTERVAL, TimeUnit.MILLISECONDS)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(aLong1 -> {
                    if (!mNetworkEvents.isEmpty()) {
                        Object ev = mNetworkEvents.remove(0);
                        logEvent(ev, false);
                        BUS.post(ev);
                    } else if (!mDataEvents.isEmpty()) {
                        Object ev = mDataEvents.remove(0);
                        logEvent(ev, false);
                        BUS.post(ev);
                    } else if (!mGenericEvents.isEmpty()) {
                        Object ev = mGenericEvents.remove(0);
                        logEvent(ev, false);
                        BUS.post(ev);
                    } else if (!mContextEvents.isEmpty()) {
                        Object ev = mContextEvents.remove(0);
                        logEvent(ev, false);
                        BUS.post(ev);
                    }
                });

        /*
         * This Observable (Observable.interval) cycles every EVENT_CONSUMPTION_INTERVAL millis
         * we observe results on the UI Thread and remove items UI queues
         */
        rx.Observable.interval(EVENT_CONSUMPTION_INTERVAL, TimeUnit.MILLISECONDS)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())//UI Events must be posted on the Main Thread
                .subscribe(aLong -> {
                    if (!mUIEvents.isEmpty()) {
                        Object ev = mUIEvents.remove(0);
                        logEvent(ev, true);
                        UI_BUS.post(ev);
                    }
                });
    }

    /**
     * Convenience method used for debugging purposes. It will log what kind of event is being posted on what Bus.
     *
     * @param ev The event being posted
     * @param uiEvent whether it's a UI event or not.
     */
    private static void logEvent(Object ev, boolean uiEvent) {
        String bus;
        if (uiEvent) {
            bus = "UI_BUS";
        } else {
            bus = "BUS";
        }
        LOGGER.info("posting " + ev.getClass().getSimpleName() + " of type " + ev.getClass().getAnnotation(Event.class).type() + " on " + bus);
    }

    /**
     * Registers a given Object on both Buses
     *
     * @param o Object to register on the Buses
     */
    public void onRegister(Object o) {
        BUS.register(o);
        UI_BUS.register(o);
    }

    /**
     * Unregisters a given Object from both Buses
     *
     * @param o Object to unregister from the Buses
     */
    public void onUnregister(Object o) {
        BUS.unregister(o);
        UI_BUS.unregister(o);
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
    public void onPost(Object o) {
        LOGGER.info("received new object to post: " + o.getClass().getSimpleName());
        if (!mInitialised) {
            //init events lists implicetely synchronized
            mNetworkEvents = Collections.synchronizedList(new ArrayList<>());
            mGenericEvents = Collections.synchronizedList(new ArrayList<>());
            mDataEvents = Collections.synchronizedList(new ArrayList<>());
            mUIEvents = Collections.synchronizedList(new ArrayList<>());
            mContextEvents = Collections.synchronizedList(new ArrayList<>());
            //init dead events manager
            if(mDeadEventManager == null) {
                mDeadEventManager = new DeadEventManager();
            } else {
                BUS.unregister(mDeadEventManager);
                UI_BUS.unregister(mDeadEventManager);
            }
            BUS.register(mDeadEventManager);
            UI_BUS.register(mDeadEventManager);

            //mark us as initialised
            mInitialised = true;
            startEventsConsumption();
        }
        //check if it's an event we recognise
        if (o != null && o.getClass().isAnnotationPresent(Event.class)) {
            //put it in the right list and sort the list
            Event.Type t = o.getClass().getAnnotation(Event.class).type();
            LOGGER.info("object " + o.getClass().getSimpleName() + " is an event of type " + t);
            switch (t) {
                default:
                case GENERIC:
                    mGenericEvents.add(o);
                    Collections.sort(mGenericEvents, Event.COMPARATOR);
                    break;
                case DATA:
                    mDataEvents.add(o);
                    Collections.sort(mDataEvents, Event.COMPARATOR);
                    break;
                case NETWORK:
                    mNetworkEvents.add(o);
                    Collections.sort(mNetworkEvents, Event.COMPARATOR);
                    break;
                case UI:
                    mUIEvents.add(o);
                    Collections.sort(mUIEvents, Event.COMPARATOR);
                    break;
                case CONTEXT:
                    mContextEvents.add(o);
                    Collections.sort(mContextEvents, Event.COMPARATOR);
                    break;
            }
        } else if (o != null) {
            DeadEvent deadEvent = new DeadEvent(BUS, o);
            BUS.post(deadEvent);
        }
    }

    @Override
    public String onSavePoint(Object object) {
        return null;
    }

    @Override
    public void onLoadPoint(Object object, String key) {

    }

    /**
     * If an event is posted but nobody is {@link Subscribe}d to it, it will become a "dead" event.
     * This class is here to catch and log such occurrences, since this should never happen. This is very useful to debug possible leaks.
     */
    private static class DeadEventManager {
        /**
         * Catches every {@link DeadEvent} not being consumed by nobody.
         *
         * @param de the event that nobody caught
         */
        @Subscribe
        public void onConsumeDeadEvent(DeadEvent de) {
            LOGGER.warn("received DeadEvent of type " + de.event.getClass().getSimpleName());
        }
    }
}