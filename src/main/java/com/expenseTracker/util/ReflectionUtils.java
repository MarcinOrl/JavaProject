package main.java.com.expenseTracker.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDate;

public class ReflectionUtils {
    public static void displayObjectDetails(Object obj) {
        if (obj == null) {
            System.out.println("Object is null.");
            return;
        }

        Class<?> cl = obj.getClass();
        System.out.println("Class: " + cl.getName());
        System.out.println("Fields:");

        Field[] fields = cl.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                System.out.printf(" %s (%s): %s%n", field.getName(), field.getType().getSimpleName(), value);
            } catch (IllegalAccessException e) {
                System.out.printf(" %s: [access denied]\n", field.getName());
            }
        }
    }

    public static Object createObjectFromClass(String classname, Object... values) throws Exception {
        Class<?> cl = Class.forName(classname);

        Constructor<?> constructor = cl.getConstructor(String.class, double.class, String.class, LocalDate.class);

        return constructor.newInstance(values);
    }
}
