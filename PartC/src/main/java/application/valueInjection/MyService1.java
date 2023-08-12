package application.valueInjection;

import framework.Autowired;
import framework.Service;
import framework.Value;

@Service
public class MyService1 {
    private Dependency1 dependency;

    @Value("${testB.property}")
    private String name;

    @Autowired
    public void setDependency(Dependency1 dependency) {
        this.dependency = dependency;
    }

    public void performAction() {
        if (dependency != null) {
            dependency.doSomething();
        } else {
            System.out.println("Dependency not set.");
        }
    }
}


