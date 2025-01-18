package main.java.com.expenseTracker.service;

import main.java.com.expenseTracker.util.NotNull;
import java.lang.reflect.*;

public class Validator {
    public static void validateNotNull(Object obj) throws IllegalAccessException {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(NotNull.class)) {
                field.setAccessible(true);
                Object value = field.get(obj);

                // Sprawdzanie, czy pole jest null
                if (value == null) {
                    throw new IllegalArgumentException(field.getAnnotation(NotNull.class).message());
                }

                // Dodatkowa walidacja dla liczb
                if (value instanceof Number && ((Number) value).doubleValue() == 0) {
                    throw new IllegalArgumentException(field.getAnnotation(NotNull.class).message());
                }

                // Dodatkowa walidacja dla String
                if (value instanceof String && ((String) value).isEmpty()) {
                    throw new IllegalArgumentException(field.getAnnotation(NotNull.class).message());
                }
            }
        }
    }
}


