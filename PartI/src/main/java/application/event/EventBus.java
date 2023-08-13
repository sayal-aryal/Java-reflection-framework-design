package application.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventBus {
    private static Map<Class<? extends Event>, List<EventListener>> eventListeners = new HashMap<>();

    public EventBus() {

    }

    public static void registerListener(EventListener listener) {
        List<Class<? extends Event>> supportedEventTypes = listener.getSupportedEventTypes();
        for (Class<? extends Event> eventType : supportedEventTypes) {
            List<EventListener> listeners = eventListeners.getOrDefault(eventType, new ArrayList<>());
            listeners.add(listener);
            eventListeners.put((Class<? extends Event>) eventType, listeners);
        }
    }

    public static void unregisterListener(EventListener listener) {
        List<Class<? extends Event>> supportedEventTypes = listener.getSupportedEventTypes();
        for (Class<? extends Event> eventType : supportedEventTypes) {
            List<EventListener> listeners = eventListeners.get(eventType);
            if (listeners != null) {
                listeners.remove(listener);
            }
        }
    }

    public static void notifyEvent(Event event) {
        List<EventListener> listeners = eventListeners.get(event.getClass());
        if (listeners != null) {
            for (EventListener listener : listeners) {
                listener.handleEvent(event);
            }
        }
    }
}
