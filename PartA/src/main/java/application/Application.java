package application;

import framework.FWContext;

public class Application {
    public static void main(String[] args){
        FWContext fwContext = new FWContext();
        fwContext.start();

        ICustomerService customerService =(CustomerService) FWContext.getClassByName("application.CustomerService");
        System.out.println(customerService.getAccountNumber());
        System.out.println(customerService.getBankName());
    }

}
