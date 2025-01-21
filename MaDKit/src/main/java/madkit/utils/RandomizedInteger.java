package madkit.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RandomizedInteger {
    int minValue() default Integer.MIN_VALUE;
    int maxValue() default Integer.MAX_VALUE;
}