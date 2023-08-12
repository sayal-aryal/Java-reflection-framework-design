package application;

import framework.FWContext;

public class Application {
    private static final String propertiesFilePath="E:\\ASD\\labs\\Final Project\\Java-reflection-framework-design\\PartC\\src\\main\\resources\\application.properties";
    public static void main(String[] args){
        FWContext fwContext = new FWContext();


        fwContext.start(propertiesFilePath);

        ICustomerService customerService =(CustomerService) FWContext.getClassByName("application.CustomerService");
        System.out.println(customerService.getAccountNumber());
        System.out.println(customerService.getBankName());
        System.out.println(customerService.getName());
    }

}
