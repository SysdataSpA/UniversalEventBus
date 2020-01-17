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

/**
 * Created by Andrea Guitto on 12/04/2016.
 */
public interface EventProcessor {

    /**
     * Registers a given Object on both Buses
     *
     * @param o Object to register on the Buses
     */
    void onRegister(Object o);

    /**
     * Unregisters a given Object from both Buses
     *
     * @param o Object to unregister from the Buses
     */
    void onUnregister(Object o);

    /**
     * After checking whether the object being posted is annotated with the {@link Event} Annotation,
     * it will be placed in the corresponding queue and such queue will be then sorted by {@link Event.Priority}.
     * <p>
     *     <b>NOTE: if an object being posted has not been annotated with the {@link Event} Annotation it will be disregarded!!!</b>
     * </p>
     *
     * @param o the Object we want to post as an event
     */
    void onPost(Object o);

    /**
     * This method return a string used by {@link EventProcessor} to save the state of the object in configuration changes.<br>
     * You should use the string returned by this method with {@code EventDispatcher.loadPoint()}<br>
     * This method must be called before {@code EventDispatcher.unregister(...)} (in {@code OnPause(...)} method for example)
     * <p>
     * <b>NOTE: this string should be saved on instance state in Activity and retrieved when it restart on OnCreate(...) method</b>
     * </p>
     *
     * @param object
     * @return
     */
    String onSavePoint(Object object);

    /**
     * This method load the configuration variables used by {@link EventDispatcher} to handle the configuration changes.<br>
     * You should use this method in pair with {@code EventDispatcher.savePoint(...)}.<br>
     * This method must be called before {@code EventDispatcher.register(...)} (in {@code OnResume(...)} method for example)<br>
     *
     * @param object
     *
     */
    void onLoadPoint(Object object, String key);
}
