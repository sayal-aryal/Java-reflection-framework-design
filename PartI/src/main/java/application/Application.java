package application;

import application.async.Building;
import application.async.Factory;
import application.async.Room;
import application.event.*;
import framework.FWContext;

public class Application {
    private static final String propertiesFilePath = "E:\\ASD\\labs\\Final Project\\Java-reflection-framework-design\\PartH\\src\\main\\resources\\application.properties";
    private static FWContext fwContext;

    public static void main(String[] args) {
        fwContext = new FWContext();
        fwContext.start(propertiesFilePath);

        String activeProfile = fwContext.getActiveProfile();
        System.out.println("Active Profile: " + activeProfile);

        testForProfile();

        Application application = new Application();
        application.run();

        testObserverPattern();

        testAsynchronous();
    }

    public static void testAsynchronous(){
        Building room = Factory.createObject(Room.class);
        room.manage();
        room.clean();
    }

    private static void testForProfile(){
        ProfileTest test= (ProfileTest) FWContext.getClassByName("application.ProfileTest");
        System.out.println(test==null);

    }

    public void run() {
        ICustomerService customerService = (CustomerService) fwContext.getClassByName("application.CustomerService");
        System.out.println(customerService.getAccountNumber());
        System.out.println(customerService.getBankName());
        System.out.println(customerService.getName());
    }

    public static void testObserverPattern(){

        NotificationListener notificationListener = new NotificationListener();
        MessageListener messageListener = new MessageListener();


        EventBus.registerListener(notificationListener);
        EventBus.registerListener(messageListener);


        Client client = new Client("sayal", "sayal@gamil.com.com");


        NewClientEvent newClientEvent = new NewClientEvent(client);
        EventBus.notifyEvent(newClientEvent);


        Transaction transaction = new Transaction("1111", 10000.0);


        TransactionEvent transactionEvent = new TransactionEvent(transaction);
        EventBus.notifyEvent(transactionEvent);
    }

}
