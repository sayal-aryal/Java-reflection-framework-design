package application;

import framework.FWContext;

public class Application {
    private static final String propertiesFilePath = "E:\\ASD\\labs\\Final Project\\Java-reflection-framework-design\\PartG\\src\\main\\resources\\application.properties";
    private static FWContext fwContext;

    public static void main(String[] args) {
        fwContext = new FWContext();
        fwContext.start(propertiesFilePath);

        String activeProfile = fwContext.getActiveProfile();
        System.out.println("Active Profile: " + activeProfile);

        testForProfile();

        Application application = new Application();
        application.run();
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

}
