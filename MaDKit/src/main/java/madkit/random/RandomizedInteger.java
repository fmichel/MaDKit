package madkit.random;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a field as an integer that should be randomized when
 * {@link Randomness#randomizeFields(Object, java.util.random.RandomGenerator)} is used.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RandomizedInteger {
	/**
	 * The minimum value for that integer
	 * 
	 * @return the minimum value
	 */
	int min() default Integer.MIN_VALUE;

	/**
	 * The maximum value for that integer
	 * 
	 * @return the maximum value
	 */
    int max() default Integer.MAX_VALUE;
}