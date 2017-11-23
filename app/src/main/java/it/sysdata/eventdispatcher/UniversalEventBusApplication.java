package it.sysdata.eventdispatcher;

import android.app.Application;

import com.baseandroid.eventbus.GreenRobotEventProcessor;
import com.baseandroid.events.EventDispatcher;
import com.baseandroid.events.EventProcessor;
import com.baseandroid.events.otto.OttoEventProcessor;
import com.baseandroid.events.rx.RxEventProcessor;

/**
 * Created by Conversano Luca on 20/11/17.
 */

public class UniversalEventBusApplication extends Application {
    private final static String LOG_TAG = UniversalEventBusApplication.class.getSimpleName();

    /**
     * Defines the type of the event bus.<br>
     */
    public enum EventBusType {
        /**
         * Otto Event Bus
         */
        OTTO,
        /**
         * Rx Event Bus
         */
        RX,
        /**
         * GreenRobot Event Bus
         */
        GREENROBOT
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // init event bus
        initEventBus();
    }

    private void initEventBus() {

        //EventBusType eventBusType = EventBusType.OTTO;
        //EventBusType eventBusType = EventBusType.RX;
        EventBusType eventBusType = EventBusType.GREENROBOT;

        switch (eventBusType) {
            case OTTO:
                initOttoEventBus();
                break;
            case RX:
                initRxEventBus();
                break;
            case GREENROBOT:
                initGreenRobotEventBus();
                break;
        }

    }

    private void initRxEventBus() {
        RxEventProcessor eventProcessor = (RxEventProcessor) RxEventProcessor.newInstance();
        eventProcessor.setVerbose(true);
        EventDispatcher.useEventProcessor(eventProcessor);
    }

    private void initOttoEventBus() {
        EventProcessor eventProcessor = (EventProcessor) OttoEventProcessor.newInstance();
        EventDispatcher.useEventProcessor(eventProcessor);
    }

    private void initGreenRobotEventBus() {
        GreenRobotEventProcessor eventProcessor = (GreenRobotEventProcessor) GreenRobotEventProcessor.newInstance();
        eventProcessor.setVerbose(true);
        EventDispatcher.useEventProcessor(eventProcessor);
    }

}
