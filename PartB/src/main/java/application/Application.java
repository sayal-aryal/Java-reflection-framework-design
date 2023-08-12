package application;

import application.setterInjector.Dependency;
import application.setterInjector.MyService;
import framework.FWContext;

public class Application {
    public static void main(String[] args){
        FWContext fwContext = new FWContext();
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
