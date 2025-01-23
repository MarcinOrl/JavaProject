package main.java.com.expenseTracker.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MaxDate {
    String message() default "Date cannot be in the future.";
}
