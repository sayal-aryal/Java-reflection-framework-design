package application.event;

import framework.EventListenerMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class EventListener {
    private List<Class<? extends Event>> supportedEventTypes;

    public EventListener() {
        supportedEventTypes = new ArrayList<>();
        extractSupportedEventTypes();
    }

    private void extractSupportedEventTypes() {
        Class<? extends EventListener> listenerClass = this.getClass();
        Method handleMethod = findHandleMethod(listenerClass);
        if (handleMethod != null) {
            EventListenerMethod annotation = handleMethod.getAnnotation(EventListenerMethod.class);
            Class<? extends Event>[] eventTypes = annotation.value();
            supportedEventTypes = Arrays.asList(eventTypes);
        }
    }

    private Method findHandleMethod(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals("handleEvent") && method.getParameterCount() == 1
                    && Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
                return method;
            }
        }
        return null;
    }

    public abstract void handleEvent(Event event);

    public List<Class<? extends Event>> getSupportedEventTypes() {
        return supportedEventTypes;
    }
}
