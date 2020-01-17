# Universal Event Bus
=============================

The Universal Event Bus is an event dispatcher architecture which help you to use most common event bus implementation as Otto in a structured mode.

An events is a bus designed to separate different parts of the application, while still allowing them to communicate efficiently.
The operation of the EventDispatcher is based on the publish-subscribe pattern: the bus asked a series of events that will be collected by those who joined them.

The publisher is, in this case, called Bus or RxBus and deals with post events using the Observable of RxJava. The event dispatcher contains two RxBuses: one dedicated to the UI thread, and the other for all the other events that have nothing to do with the UI (network calls, CRUD operations with the database etc.).

The events that are posted by the Event dispatcher are heard by all those who sign up. To register, you must write down the method that the signing will take as an argument the type of object that the public EventDispatcher and will be annotated with the notation @Subscribe or @RxSubscribe

![alt tag](http://sharing.sysdata.it/fvgmock/ues-diagram01.png)

Usage
--------

1) You've got to initialize the EventDispatcher with your favorite process. 
We suggest to do it in the MainApplication's onCreate() and use RxEventProcessor which use RxJava.

```java
  EventDispatcher.useEventProcessor(RxEventProcessor.newInstance());
```
We also implemented an EventProcessor which use Otto as Event Bus.

```java
  EventDispatcher.useEventProcessor(EventProcessor.newInstance());
```
2) Register the EventDispatcher when the Activity/Fragment/Service is created and unregister it when it is destroyed.

```java
    
    @Override
    public void onCreate() {
        super.onCreate();
        // register the event dispatcher in order to receive 
        // events that are posted on the Bus.
        EventDispatcher.register(this);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // unregister the event dispatcher
        EventDispatcher.unregister(this);
    }
```
3) Create events, post and receive them: Once the EventDispatcher is initialized and register in your Android component, you can post and receive events easily. Use `EventDispatcher.post()` to post the event you want to stream in the bus. Create a public method that has the same event type in its signature and annotate it with the `@RxSubscribe` signature in order to receive the Object that has been posted.

```java
    @Override
    public void onCreate() {
        super.onCreate();
        // post an example event 
        EventDispatcher.post(new ExampleEvent());
    }
    
    @RxSubscribe
    public void onConsumeExampleEvent(ExampleEvent event) {
        // do what you want with the incoming event
    }
    
    /**
    * This is an example empty event: remember to add the "Event" annotation!!
    */
    @Event(type = Event.Type.UI)
    public class ExampleEvent {
    
        public ExampleEvent() {
          // empty constructor
        }
        
    }
```

The posting class can be different from the receiving one: both must be registered to the EventDispatcher, though! Remember that each Class that you want to use as an event MUST have the `@Event` annotation. You can choose between 5 type of events based on which is the use of the designed event: GENERIC, DATA, NETWORK, CONTEXT and UI. The difference is that UI events will be posted on the UI thread, meanwhile the others will be posted in a separated Thread.

Handle configuration changes
--------

EventDispatcher allows you to handle configuration changes easily. By using `EventDispatcher.loadPoint()` and `EventDispatcher.savePoint()` you will be able to receive posted events even after a configuration change (i.e. rotation or other lifecycle events).

```java
    
    private String mEventDispatcherTag;
    
    @Override
    public void onCreate() {
        super.onCreate();
        // retrieve the saved state, if present
        if (savedInstanceState != null && savedInstanceState.containsKey("ett")) {
            this.mEventDispatcherTag = savedInstanceState.getString("ett");
        }
        // loadPoint() is used to handle events' saved state between configuration changes 
        EventDispatcher.loadPoint(this, this.mEventDispatcherTag);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save the EventDispatcher's tag
        outState.putString("ett", mEventDispatcherTag);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // save point is used to save the state in order to restore it later, after the configuration change.
        this.mEventDispatcherTag = EventDispatcher.savePoint(this);
    }
```

Create custom EventProcessor
--------

You can implement your own EventProcessor if you want, you need to create a class which implements the interface EventProcessor

```java
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
```


Download
--------

Downloadable .jars can be found on the [Bintray download page][2].

You can also depend on the .jar through Maven:
```xml
<dependency>
  <groupId>com.baseandroid</groupId>
  <artifactId>baseandroid-rxeventdispatcher</artifactId>
  <version>0.0.14</version>
</dependency>
```
or Gradle:
```groovy
compile "com.baseandroid:baseandroid-rxeventdispatcher:0.0.14"
// rxJava
compile 'io.reactivex:rxandroid:1.2.1'
// Because RxAndroid releases are few and far between, it is recommended you also
// explicitly depend on RxJava's latest version for bug fixes and new features.
compile 'io.reactivex:rxjava:1.2.6'
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap].



License
-------

  Copyright (C) 2020 Sysdata, S.p.a.
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.



 [1]: http://square.github.com/otto/
 [2]: https://dl.bintray.com/sysdata/maven/com/baseandroid/baseandroid-eventdispatcher/
 [snap]: https://oss.sonatype.org/content/repositories/snapshots/
