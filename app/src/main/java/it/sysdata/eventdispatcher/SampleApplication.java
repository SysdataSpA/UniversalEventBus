package it.sysdata.eventdispatcher;

import android.app.Application;

import com.baseandroid.events.EventDispatcher;
import com.baseandroid.events.rx.RxEventProcessor;

public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // init event bus ASAP
        initEventBus();
    }

    private void initEventBus() {
        // choose the processor you want to use
        EventDispatcher.useEventProcessor(RxEventProcessor.newInstance());
    }
}
