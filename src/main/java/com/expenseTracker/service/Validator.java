package main.java.com.expenseTracker.service;

import main.java.com.expenseTracker.util.NotNull;
import main.java.com.expenseTracker.util.MinValue;
import main.java.com.expenseTracker.util.ValidCategory;
import java.lang.reflect.Field;

public class Validator {
    public static void validate(Object obj) throws IllegalAccessException {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);

            // Sprawdzanie @NotNull
            if (field.isAnnotationPresent(NotNull.class)) {
                if (field.get(obj) == null || field.get(obj).toString().isEmpty()) {
                    throw new IllegalArgumentException(field.getAnnotation(NotNull.class).message());
                }
            }

            // Sprawdzanie @MinValue
            if (field.isAnnotationPresent(MinValue.class)) {
                double value = (double) field.get(obj);
                MinValue minValue = field.getAnnotation(MinValue.class);
                if (value < minValue.value()) {
                    throw new IllegalArgumentException(minValue.message());
                }
            }

            // Sprawdzanie @ValidCategory
            if (field.isAnnotationPresent(ValidCategory.class)) {
                String string = (String) field.get(obj);
                ValidCategory validCategory = field.getAnnotation(ValidCategory.class);
                boolean isValid = false;
                for (String category : validCategory.allowedCategories()) {
                    if (category.equals(string)) {
                        isValid = true;
                        break;
                    }
                }
                if (!isValid) {
                    throw new IllegalArgumentException(validCategory.message());
                }
            }
        }
    }
}


