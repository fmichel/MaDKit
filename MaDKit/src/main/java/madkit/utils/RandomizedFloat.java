
package madkit.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify that a float field should be randomized.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RandomizedFloat {

	/**
	 * The minimum value of the float.
	 *
	 * @return the minimum value
	 */
	float minValue() default Float.MIN_VALUE;

	/**
	 * The maximum value of the float.
	 *
	 * @return the maximum value
	 */
	float maxValue() default Float.MAX_VALUE;
}
