package framework;

import org.reflections.Reflections;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class FWContext {
    private static List<Object> serviceObjectList = new ArrayList<>();

    private static Properties properties = new Properties();

    public FWContext(String propertiesFilePath) {
        try (InputStream resourceStream = new FileInputStream(propertiesFilePath)) {
            properties.load(resourceStream);
//            activeProfile = properties.getProperty("profiles.active");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean hasNoParameterConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                return true;
            }
        }
        return false;
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

    public static void performSetterInjection(Object targetObject, Object dependencyObject) {
        Class<?> targetClass = targetObject.getClass();
        Class<?> dependencyClass = dependencyObject.getClass();

        Method[] methods = targetClass.getMethods();
        for (Method method : methods) {
            if (isSetterMethod(method) && hasMatchingParameter(method, dependencyClass)) {
                try {
                    method.invoke(targetObject, dependencyObject);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        injectValues(targetObject); // for field injection
    }

    private static void injectValues(Object bean) {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Value.class)) {
                Value valueAnnotation = field.getAnnotation(Value.class);
                String key = valueAnnotation.value();
                key = key.replace("${", "").replace("}", ""); // Remove "${" and "}" characters

                String propertyValue = properties.getProperty(key);

                if (propertyValue != null) {
                    field.setAccessible(true);
                    try {
                        Object convertedValue = convertValue(propertyValue, field.getType());
                        field.set(bean, convertedValue);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    private static Object convertValue(String propertyValue, Class<?> targetType) {
        if (targetType.equals(String.class)) {
            return propertyValue;
        } else if (targetType.equals(int.class) || targetType.equals(Integer.class)) {
            return Integer.parseInt(propertyValue);
        } else if (targetType.equals(long.class) || targetType.equals(Long.class)) {
            return Long.parseLong(propertyValue);
        } else if (targetType.equals(float.class) || targetType.equals(Float.class)) {
            return Float.parseFloat(propertyValue);
        } else if (targetType.equals(double.class) || targetType.equals(Double.class)) {
            return Double.parseDouble(propertyValue);
        } else if (targetType.equals(boolean.class) || targetType.equals(Boolean.class)) {
            return Boolean.parseBoolean(propertyValue);
        }
        return null;
    }
    private static boolean isSetterMethod(Method method) {
        return method.getName().startsWith("set") &&
                method.getParameterCount() == 1 &&
                method.getReturnType().equals(void.class);
    }

    private static boolean hasMatchingParameter(Method method, Class<?> parameterType) {
        return method.getParameterTypes()[0].equals(parameterType);
    }

    public static <T> T createInstance(Class<T> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Autowired.class)) {
                try {
                    constructor.setAccessible(true);
                    Object[] parameters = getParametersForConstructor(constructor);
                    return clazz.cast(constructor.newInstance(parameters));
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
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

    public void start() {
        try {
            Reflections reflections = new Reflections("");
            Set<Class<?>> serviceClasses = reflections.getTypesAnnotatedWith(Service.class);

            boolean hasNoparameterConstructor = false;
            for (Class<?> serviceClass : serviceClasses) {
                hasNoparameterConstructor = hasNoParameterConstructor(serviceClass);
                if (hasNoparameterConstructor) {
                    serviceObjectList.add((Object) serviceClass.getDeclaredConstructor().newInstance());
                }
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
                        Object instance = getServiceBeanOfType(field);
                        // Doing Injection - Make the field accessible before setting its value
                        field.setAccessible(true);
                        field.set(serviceClass, instance);
                    }
                }

            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public Object getServiceBeanOfType(Field field) {

        Class classImplementingTheInterface = field.getType();

        String qualifierValue = null;
        Object service = null;
        try {

            boolean isQualifierPresent = false;
            if (field.isAnnotationPresent(Qualifier.class)) {
                Qualifier qualifierAnnotation = field.getAnnotation(Qualifier.class);
                qualifierValue = qualifierAnnotation.value();
                isQualifierPresent = true;

            }
            for (Object theClass : serviceObjectList) {

                Class<?>[] interfaces = theClass.getClass().getInterfaces();


                for (Class<?> theInterface : interfaces) {


                    if (theInterface.getName().contentEquals(classImplementingTheInterface.getName())) {
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
