package framework;

import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FWContext {
    private static List<Object> serviceObjectList = new ArrayList<>();

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

                        // get the type of the field
                        Class<?> theFieldType = field.getType();

                        //getting the object instance of type
                        Object instance = getServiceBeanOfType(theFieldType);

                        // Doing Injection - Make the field accessible before setting its value
                        field.setAccessible(true); // Add this line
                        field.set(serviceClass, instance);
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    public Object getServiceBeanOfType(Class classImplementingTheInterface) {
        Object service = null;
        try {
           for (Object theClass : serviceObjectList){

               Class<?>[] interfaces = theClass.getClass().getInterfaces();


               for (Class<?> theInterface:interfaces){
                     if (theInterface.getName().contentEquals(classImplementingTheInterface.getName()))
                     {

                         service = theClass;
                         return service ;

                     }
               }
           }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return service;
    }

    public static Object getClassByName (String name){

        Object classToReturn = null ;
        for(Object o : serviceObjectList){

            if(o.getClass().getName().equals(name)){
                return o ;
            }

        }

        return classToReturn;

    }
}
