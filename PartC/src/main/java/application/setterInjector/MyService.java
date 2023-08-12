package application.setterInjector;

import framework.Autowired;
import framework.Service;

@Service
public class MyService {
    private Dependency dependency;

    @Autowired
    public void setDependency(Dependency dependency) {
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


