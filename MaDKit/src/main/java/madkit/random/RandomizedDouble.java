package madkit.random;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a field as a double that should be randomized when a
 * {@link Randomness#randomizeFields(Object, java.util.random.RandomGenerator)} is used.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RandomizedDouble {

	/**
	 * The minimum value of the slider
	 * 
	 * @return the minimum value
	 */
	public double minValue() default Double.MIN_VALUE;

	/**
	 * The maximum value of the slider
	 * 
	 * @return the maximum value
	 */
	public double maxValue() default Double.MAX_VALUE;

}