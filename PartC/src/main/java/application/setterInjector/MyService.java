package application.setterInjector;

import framework.Autowired;
import framework.Service;
import framework.Value;

@Service
public class MyService {
    private Dependency dependency;

    @Value("${my.property.key}")
    private String name;

    @Autowired
    public void setDependency(Dependency dependency) {
        this.dependency = dependency;
    }

    public void performAction() {
        System.out.println("Test for value setter: "+name);
        if (dependency != null) {
            dependency.doSomething();
        } else {
            System.out.println("Dependency not set.");
        }
    }
}


