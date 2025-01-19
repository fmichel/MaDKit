package madkit.random;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a field as an integer that should be randomized when a
 * {@link Randomness#randomizeFields(Object, java.util.random.RandomGenerator)} is used.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RandomizedInteger {
    int minValue() default Integer.MIN_VALUE;
    int maxValue() default Integer.MAX_VALUE;
}