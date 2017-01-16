# EventDispatcher
Event dispatcher - An event bus by Sysdata
=============================

The EventDispatcher events is a bus designed to separate different parts of the application, while still allowing them to communicate efficiently.
The operation of the EventDispatcher is based on the publish-subscribe pattern: the bus asked a series of events that will be collected by those who joined them.
The publisher is, in this case, called RxBus and deals with post events using the Observable of RxJava. The event dispatcher contains two RxBuses: one dedicated to the UI thread, and the other for all the other events that have nothing to do with the UI (network calls, CRUD operations with the database etc.).
The events that are posted by the Event dispatcher are heard by all those who sign up. To register, you must write down the method that the signing will take as an argument the type of object that the public EventDispatcher and will be annotated with the notation @RxSubscribe

Usage
--------


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
