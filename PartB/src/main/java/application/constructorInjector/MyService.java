package application.constructorInjector;

import framework.Autowired;
import framework.Service;

@Service
public class MyService {

    private final Dependency dependency;
    @Autowired
    public MyService(Dependency dependency) {
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
