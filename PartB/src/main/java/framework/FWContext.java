package framework;

import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FWContext {
    private static List<Object> serviceObjectList = new ArrayList<>();

    private static boolean isSetterMethod(Method method) {
        return method.getName().startsWith("set") &&
                method.getParameterCount() == 1 &&
                method.getReturnType().equals(void.class);
    }

    private static boolean hasMatchingParameter(Method method, Class<?> parameterType) {
        return method.getParameterTypes()[0].equals(parameterType);
    }

    public static Object getClassByName(String name) {

        Object classToReturn = null;
        for (Object o : serviceObjectList) {

            if (o.getClass().getName().equals(name)) {
                return o;
            }

        }

        return classToReturn;

    }

    public void start() {
        try {
            Reflections reflections = new Reflections("");
            Set<Class<?>> serviceClasses = reflections.getTypesAnnotatedWith(Service.class);
            for (Class<?> serviceClass : serviceClasses) {
                serviceObjectList.add((Object) serviceClass.getDeclaredConstructor().newInstance());
            }
            performDI();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void performDI() {
        try {
            for (Object serviceClass : serviceObjectList) {
                //find annotated field
                for (Field field : serviceClass.getClass().getDeclaredFields()) {
                    if (field.isAnnotationPresent(Autowired.class)) {
                        //getting the object instance of type
                        Object instance = getServiceBeanOfType(field, field.getType());
                        // Doing Injection - Make the field accessible before setting its value
                        field.setAccessible(true);
                        field.set(serviceClass, instance);
                    }
                }

                Class<?> targetClass = serviceClass.getClass();
                Method[] methods = targetClass.getMethods();

                for (Method method : methods) {
                    if (isSetterMethod(method)) {

                        Class<?> type = method.getParameterTypes()[0];
                        try {
                            Object instance = getServiceBeanOfType(null, type);
                            method.invoke(serviceClass, instance);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static Object[] getParametersForConstructor(Constructor<?> constructor) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Object[] parameters = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameters[i] = createDependencyInstance(parameterTypes[i]);// Obtain or create the dependency instance for parameterTypes[i]
        }
        return parameters;
    }

    private static Object createDependencyInstance(Class<?> dependencyType) {
        try {
            // Assuming each dependency type has a no-argument constructor
            return dependencyType.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object getServiceBeanOfType(Field field, Class type) {

//        Class classImplementingTheInterface = field.getType();

        String qualifierValue = null;
        Object service = null;
        try {

            boolean isQualifierPresent = false;
            if (field != null) {
                if (field.isAnnotationPresent(Qualifier.class)) {
                    Qualifier qualifierAnnotation = field.getAnnotation(Qualifier.class);
                    qualifierValue = qualifierAnnotation.value();
                    isQualifierPresent = true;

                }
            }

            for (Object theClass : serviceObjectList) {

                Class<?>[] interfaces = theClass.getClass().getInterfaces();


                for (Class<?> theInterface : interfaces) {


                    if (theInterface.getName().contentEquals(type.getName())) {
                        Service serviceAnnotation = theClass.getClass().getAnnotation(Service.class);
                        String serviceName = serviceAnnotation.value();

                        if (isQualifierPresent) {

                            if (qualifierValue.equals(serviceName)) {
                                service = theClass;
                                return service;
                            }

                        } else {
                            service = theClass;
                            return service;
                        }

                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return service;
    }
}
