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

package com.baseandroid.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Comparator;

/**
 * Annotation used to define events.<br>
 * Such events can have a {@link Type} and a {@link Priority}.
 * <p>
 *     Different Types and Priorities define the order in which the events will be managed and on which bus they will be posted.
 * </p>
 *
 * @author Stefano Ciarcia'
 * created on 24/07/2015.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Event {

    /**
     * Defines the priority this event will have. In order they are:
     * <ol>
     *     <li><code>CRITICAL</code></li>
     *     <li><code>HIGH</code></li>
     *     <li><code>NORMAL</code></li>
     *     <li><code>LOW</code></li>
     *     <li><code>IRRELEVANT</code></li>
     * </ol>
     */
    enum Priority {
        /**
         * this priority means that the event must be dealt with immediately,
         * because it's a life and death matter
         */
        CRITICAL(5),
        /**
         * this priority means that the event should be dealt with asap
         */
        HIGH(4),
        /**
         * this priority means that the event can be dealt with normally
         */
        NORMAL(3),
        /**
         * this priority means that the event can be dealt with when possible
         */
        LOW(2),
        /**
         * this priority means that it's irrelevant if the event is dealt with
         */
        IRRELEVANT(1);

        private final int priorityLevel;

        Priority(int p) {
            this.priorityLevel = p;
        }
    }
    Priority priority() default Priority.NORMAL;

    /**
     * Defines the type of this event.<br>
     * <p>
     *     Events of type <code>GENERIC</code>, <code>DATA</code> and <code>NETWORK</code> will be posted on a Bus separated by the Android Main Thread.<br>
     *     Events of type <code>UI</code> will be posted on a Bus working on the Android Main Thread.
     * </p>
     */
    enum Type {
        /**
         * generic type of event, not falling into one of the other types
         */
        GENERIC,
        /**
         * event representing a request or passing of data
         */
        DATA,
        /**
         * event representing a network request
         */
        NETWORK,
        /**
         * event representing a context request
         */
        CONTEXT,
        /**
         * event representing a UI event
         */
        UI
    }
    Type type();

    /**
     * This comparator sorts the Events by {@link Priority}.
     */
    Comparator COMPARATOR = (lhs, rhs) -> lhs.getClass().getAnnotation(Event.class).priority().priorityLevel - rhs.getClass().getAnnotation(Event.class).priority().priorityLevel;
}
