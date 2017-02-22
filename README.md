# Universal Event BUs
An universal event bus by Sysdata
=============================

The EventDispatcher events is a bus designed to separate different parts of the application, while still allowing them to communicate efficiently.
The operation of the EventDispatcher is based on the publish-subscribe pattern: the bus asked a series of events that will be collected by those who joined them.
The publisher is, in this case, called RxBus and deals with post events using the Observable of RxJava. The event dispatcher contains two RxBuses: one dedicated to the UI thread, and the other for all the other events that have nothing to do with the UI (network calls, CRUD operations with the database etc.).
The events that are posted by the Event dispatcher are heard by all those who sign up. To register, you must write down the method that the signing will take as an argument the type of object that the public EventDispatcher and will be annotated with the notation @RxSubscribe

Usage
--------

1. You've got to initialize the EventDispatcher. We suggest to do it in the MainApplication's onCreate().

```java
  EventDispatcher.useEventProcessor(RxEventProcessor.newInstance());
```

2. Register the EventDispatcher when the Activity/Fragment/Service is created and unregister it when it is destroyed.

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
3. Create events, post and receive them: Once the EventDispatcher is initialized and register in your Android component, you can post and receive events easily. Use `EventDispatcher.post()` to post the event you want to stream in the bus. Create a public method that has the same event type in its signature and annotate it with the `@RxSubscribe` signature in order to receive the Object that has been posted.

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
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap].



License
-------

  Copyright (C) 2017 Sysdata Digital, S.r.l.
 
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
