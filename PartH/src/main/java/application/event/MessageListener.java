package application.event;

import framework.EventListenerMethod;

public class MessageListener extends EventListener{

    @Override
    @EventListenerMethod(value = {  NewClientEvent.class })
    public void handleEvent(Event event) {
        System.out.println("Message received: " + event.getClass().getSimpleName());
    }
}
