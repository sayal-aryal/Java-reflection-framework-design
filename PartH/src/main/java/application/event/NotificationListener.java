package application.event;

import framework.EventListenerMethod;



public class NotificationListener extends EventListener {

    @Override
    @EventListenerMethod(value = {NewClientEvent.class, TransactionEvent.class})
    public void handleEvent(Event event) {
        System.out.println("Notification received: " + event.getClass().getSimpleName());
    }

}

