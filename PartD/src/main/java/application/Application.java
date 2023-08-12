package application;

import framework.FWContext;

public class Application {
    private static final String propertiesFilePath = "E:\\ASD\\labs\\Final Project\\Java-reflection-framework-design\\PartC\\src\\main\\resources\\application.properties";
    private static FWContext fwContext;

    public static void main(String[] args) {
        fwContext = new FWContext();
        fwContext.start(propertiesFilePath);

        Application application = new Application();
        application.run();
    }

    public void run() {
        ICustomerService customerService = (CustomerService) fwContext.getClassByName("application.CustomerService");
        System.out.println(customerService.getAccountNumber());
        System.out.println(customerService.getBankName());
        System.out.println(customerService.getName());
    }
}
