package application.async;

import framework.Async;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Factory {
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public static <T> T createObject(Class<? extends T> concreteClass, Object... constructorArgs) {
        try {
            Class<T> targetInterface = getInterfaceFromConcreteClass(concreteClass);
            T targetObject = createInstance(concreteClass, constructorArgs);
            return createProxy(targetInterface, targetObject, concreteClass);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static <T> Class<T> getInterfaceFromConcreteClass(Class<? extends T> concreteClass) {
        Class<?>[] interfaces = concreteClass.getInterfaces();
        if (interfaces.length == 0) {
            throw new IllegalArgumentException("The concrete class does not implement any interfaces.");
        }
        // Assuming the first interface is the target interface
        @SuppressWarnings("unchecked")
        Class<T> targetInterface = (Class<T>) interfaces[0];
        return targetInterface;
    }

    private static <T> T createInstance(Class<? extends T> concreteClass, Object... constructorArgs) throws Exception {
        if (constructorArgs.length == 0) {
            return concreteClass.getDeclaredConstructor().newInstance();
        } else {
            Class<?>[] argumentTypes = getArgumentTypes(constructorArgs);
            return concreteClass.getDeclaredConstructor(argumentTypes).newInstance(constructorArgs);
        }
    }

    private static Class<?>[] getArgumentTypes(Object[] constructorArgs) {
        Class<?>[] argumentTypes = new Class<?>[constructorArgs.length];
        for (int i = 0; i < constructorArgs.length; i++) {
            argumentTypes[i] = constructorArgs[i].getClass();
        }
        return argumentTypes;
    }

    private static <T> T createProxy(Class<T> targetInterface, T targetObject, Class<?> targetClass) {
        return targetInterface.cast(Proxy.newProxyInstance(
                targetInterface.getClassLoader(),
                new Class[]{targetInterface},
                new AsyncInvocationHandler(targetObject, targetClass)
        ));
    }

    private static class AsyncInvocationHandler implements InvocationHandler {
        private final Object targetObject;
        private final Class<?> targetClass;

        public AsyncInvocationHandler(Object targetObject, Class<?> targetClass) {
            this.targetObject = targetObject;
            this.targetClass = targetClass;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Method targetMethod = targetClass.getMethod(method.getName(), method.getParameterTypes());
            if (isAsyncMethod(targetMethod)) {
                System.out.println("Executing method asynchronously: " + method.getName());
                CompletableFuture.runAsync(() -> {
                    try {
                        method.invoke(targetObject, args);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }, executorService);
                return null;
            } else {
                System.out.println("Executing method synchronously: " + method.getName());
                return method.invoke(targetObject, args);
            }
        }

        private boolean isAsyncMethod(Method method) {
            return method.isAnnotationPresent(Async.class);
        }
    }

}
