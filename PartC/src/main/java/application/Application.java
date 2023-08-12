package application;

import application.setterInjector.Dependency;
import application.setterInjector.MyService;
import framework.FWContext;

public class Application {

    private static final String propertiesFilePath="E:\\ASD\\labs\\Final Project\\Java-reflection-framework-design\\PartC\\src\\main\\resources\\application.properties";
//    private static final String propertiesFilePath="application.properties";
    public static void main(String[] args){
        FWContext fwContext = new FWContext(propertiesFilePath);
        fwContext.start();

        ICustomerService customerService =(CustomerService) FWContext.getClassByName("application.CustomerService");
        System.out.println(customerService.getAccountNumber()); // field injection done here.

        // for setter injection
        MyService service = new MyService();
        Dependency dependency = new Dependency();
        FWContext.performSetterInjection(service, dependency); // perform setter Injection
        service.performAction();

        //for constructor
        application.constructorInjector.MyService myService = FWContext.createInstance(application.constructorInjector.MyService.class);
        myService.performAction();


    }

}
