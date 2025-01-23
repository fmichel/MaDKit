
package madkit.random;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify that a float field should be randomized when
 * {@link Randomness#randomizeFields(Object, java.util.random.RandomGenerator)} is used.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RandomizedFloat {

	/**
	 * The minimum value of the float.
	 *
	 * @return the minimum value
	 */
	float min() default Float.MIN_VALUE;

	/**
	 * The maximum value of the float.
	 *
	 * @return the maximum value
	 */
	float max() default Float.MAX_VALUE;
}
