package framework;

import org.reflections.Reflections;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FWContext {
    private static List<Object> serviceObjectList = new ArrayList<>();

    private static Properties properties = new Properties();

    private String activeProfile;

    private List<Object> scheduledBeans = new ArrayList<>();

    private ScheduledExecutorService scheduler;

    private List<ScheduledFuture<?>> scheduledTasks = new ArrayList<>();

    public String getActiveProfile() {
        return activeProfile;
    }

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

    public void start(String propertiesFilePath) {
        scheduler = Executors.newScheduledThreadPool(1);
        try (InputStream resourceStream = new FileInputStream(propertiesFilePath)) {
            properties.load(resourceStream);
            activeProfile = properties.getProperty("profiles.active");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Reflections reflections = new Reflections("");
            Set<Class<?>> serviceClasses = reflections.getTypesAnnotatedWith(Service.class);
            for (Class<?> serviceClass : serviceClasses) {

                if(checkIfActiveProfile(serviceClass)){
                    serviceObjectList.add((Object) serviceClass.getDeclaredConstructor().newInstance());
                }

            }
            performDI();

            startScheduler();


        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void startScheduler() {
        try{
            for (Object bean : scheduledBeans) {
                for (Method method : bean.getClass().getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Scheduled.class)) {
                        Scheduled scheduledAnnotation = method.getAnnotation(Scheduled.class);
                        long initialDelay = scheduledAnnotation.initialDelay();
                        TimeUnit timeUnit = scheduledAnnotation.timeUnit();
                        String cronExpression = scheduledAnnotation.cron();

                        long fixedRate = scheduledAnnotation.fixedRate();
                        long updatedFixedRate;
                        if (fixedRate >= 0) {
                            updatedFixedRate = fixedRate;
                            ScheduledFuture<?> scheduledTask = scheduler.scheduleAtFixedRate(() -> {
                                try {
                                    method.invoke(bean);
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }, initialDelay, updatedFixedRate, timeUnit);

                            scheduledTasks.add(scheduledTask);
                        }  else if (!cronExpression.isEmpty()) {
                            updatedFixedRate = CronUtil.getFixedRate(cronExpression);

                            ScheduledFuture<?> scheduledTask = scheduler.scheduleAtFixedRate(() -> {
                                try {
                                    method.invoke(bean);
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }, initialDelay, updatedFixedRate, timeUnit);
                            scheduledTasks.add(scheduledTask);
                        }
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private boolean checkIfActiveProfile(Class<?> clazz){
        Profile profileAnnotation = clazz.getAnnotation(Profile.class);
        if(profileAnnotation!=null){
            String profileName=clazz.getAnnotation(Profile.class).value();
            if(!profileName.equals(activeProfile)) return false;
        }
        return true;
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

                injectValues(serviceClass); // for field injection

                if (isScheduledBean(serviceClass.getClass())) {
                    scheduledBeans.add(serviceClass);
                }

            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    private boolean isScheduledBean(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Scheduled.class)) {
                return true;
            }
        }
        return false;
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
