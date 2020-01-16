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
package com.baseandroid.events.rx2;

import com.baseandroid.events.rx2.annotations.RxAnnotatedHandlerFinder;

import org.reactivestreams.Subscription;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.util.AppendOnlyLinkedArrayList;
import io.reactivex.internal.util.NotificationLite;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;

/**
 * This class implements the Event Bus pattern using RxJava2 {@link Subject}.
 * The subject serialize a wrapped subject, the wrapped object depends on constructor called.
 *
 * @author Andrea Guitto
 */
public class RxBus<T> extends Subject<T> implements AppendOnlyLinkedArrayList.NonThrowingPredicate<Object> {

    /**
     * The actual subscriber to serialize Subscriber calls to.
     */
    final Subject<T> actual;
    /**
     * This map handle the subscriptions list, keys contains {@link Observer}, values contains {@link Subscription}
     */
    private final Map<Observer, Disposable> mSubscriptionsList;
    /**
     * Indicates an emission is going on, guarded by this.
     */
    boolean emitting;
    /**
     * If not null, it holds the missed NotificationLite events.
     */
    AppendOnlyLinkedArrayList<Object> queue;
    /**
     * Indicates a terminal event has been received and all further events will be dropped.
     */
    volatile boolean done;

    /**
     * With this constructor the {@link RxBus} use a {@link PublishSubject} and instantiate it calling the static method {@code PublishSubject.create()}
     *
     * @see PublishSubject
     */
    public RxBus() {
        actual = (Subject<T>) PublishSubject.create().toSerialized();
        mSubscriptionsList = new WeakHashMap<>();
    }

    /**
     * With this constructor the {@link RxBus} use a {@link ReplaySubject} and instantiate it calling the static method {@code ReplaySubject.createWithSize(cacheSize)}
     *
     * @param cacheSize numbers of max events retained
     * @see ReplaySubject
     */
    public RxBus(int cacheSize) {
        actual = (Subject<T>) ReplaySubject.createWithSize(cacheSize).toSerialized();
        mSubscriptionsList = new WeakHashMap<>();
    }

    /**
     * With this constructor the {@link RxBus} use a {@link ReplaySubject} and instantiate it calling the static method  {@code ReplaySubject.createWithTime(retainTime, timeUnit, scheduler)}
     *
     * @param retainTime events retain time, passed to {@link ReplaySubject}
     * @param timeUnit events retain time unit, passed to {@link ReplaySubject}
     * @param scheduler events retain time scheduler, passed to {@link ReplaySubject}
     * @see ReplaySubject
     */
    public RxBus(int retainTime, TimeUnit timeUnit, Scheduler scheduler) {
        actual = (Subject<T>) ReplaySubject.createWithTime(retainTime, timeUnit, scheduler).toSerialized();
        mSubscriptionsList = new WeakHashMap<>();
    }

    /**
     * This method is used to register an {@link Observer} to this bus.
     * Here is called the {@code subscribe()} to subject
     *
     * @param o
     * @return
     */
    public synchronized void register(RxEventProcessor.ObserverWrapper o) {
        if (o != null) {
            subscribe(o);
            mSubscriptionsList.put(o, o.getDisposable());
        }
    }

    /**
     * This method is used to unregister an {@link Observer} from this bus
     * Here is called the {@code unsubscribe()}
     *
     * @param o
     * @return
     */
    public synchronized void unregister(Observer o) {
        if (o != null) {
            if (mSubscriptionsList.containsKey(o)) {
                Disposable subscription = mSubscriptionsList.get(o);
                if (subscription != null) {
                    subscription.dispose();
                    mSubscriptionsList.remove(o);
                }
            }
            RxAnnotatedHandlerFinder.clearResources(o);
        }
    }

    /**
     * This method post events on bus, it use {@code onNext(ev)} method of subject
     *
     * @param ev
     */
    public synchronized void post(T ev) {
        onNext(ev);
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        actual.subscribe(observer);
    }

    @Override
    public void onSubscribe(Disposable d) {
        boolean cancel;
        if (!done) {
            synchronized (this) {
                if (done) {
                    cancel = true;
                } else {
                    if (emitting) {
                        AppendOnlyLinkedArrayList<Object> q = queue;
                        if (q == null) {
                            q = new AppendOnlyLinkedArrayList<Object>(4);
                            queue = q;
                        }
                        q.add(NotificationLite.disposable(d));
                        return;
                    }
                    emitting = true;
                    cancel = false;
                }
            }
        } else {
            cancel = true;
        }
        if (cancel) {
            d.dispose();
        } else {
            actual.onSubscribe(d);
            emitLoop();
        }
    }

    @Override
    public void onNext(T t) {
        if (done) {
            return;
        }
        synchronized (this) {
            if (done) {
                return;
            }
            if (emitting) {
                AppendOnlyLinkedArrayList<Object> q = queue;
                if (q == null) {
                    q = new AppendOnlyLinkedArrayList<Object>(4);
                    queue = q;
                }
                q.add(NotificationLite.next(t));
                return;
            }
            emitting = true;
        }
        actual.onNext(t);
        emitLoop();
    }

    @Override
    public void onError(Throwable t) {
        if (done) {
            RxJavaPlugins.onError(t);
            return;
        }
        boolean reportError;
        synchronized (this) {
            if (done) {
                reportError = true;
            } else {
                done = true;
                if (emitting) {
                    AppendOnlyLinkedArrayList<Object> q = queue;
                    if (q == null) {
                        q = new AppendOnlyLinkedArrayList<Object>(4);
                        queue = q;
                    }
                    q.setFirst(NotificationLite.error(t));
                    return;
                }
                reportError = false;
                emitting = true;
            }
        }
        if (reportError) {
            RxJavaPlugins.onError(t);
            return;
        }
        actual.onError(t);
    }

    @Override
    public void onComplete() {
        if (done) {
            return;
        }
        synchronized (this) {
            if (done) {
                return;
            }
            done = true;
            if (emitting) {
                AppendOnlyLinkedArrayList<Object> q = queue;
                if (q == null) {
                    q = new AppendOnlyLinkedArrayList<Object>(4);
                    queue = q;
                }
                q.add(NotificationLite.complete());
                return;
            }
            emitting = true;
        }
        actual.onComplete();
    }

    /**
     * Loops until all notifications in the queue has been processed.
     */
    void emitLoop() {
        for (; ; ) {
            AppendOnlyLinkedArrayList<Object> q;
            synchronized (this) {
                q = queue;
                if (q == null) {
                    emitting = false;
                    return;
                }
                queue = null;
            }
            q.forEachWhile(this);
        }
    }

    @Override
    public boolean test(Object o) {
        return NotificationLite.acceptFull(o, actual);
    }

    @Override
    public boolean hasObservers() {
        return actual.hasObservers();
    }

    @Override
    public boolean hasThrowable() {
        return actual.hasThrowable();
    }

    @Override
    @Nullable
    public Throwable getThrowable() {
        return actual.getThrowable();
    }

    @Override
    public boolean hasComplete() {
        return actual.hasComplete();
    }
}
