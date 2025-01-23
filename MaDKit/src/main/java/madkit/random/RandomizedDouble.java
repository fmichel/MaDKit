package madkit.random;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a field as a double that should be randomized when
 * {@link Randomness#randomizeFields(Object, java.util.random.RandomGenerator)} is used.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RandomizedDouble {

	/**
	 * The minimum value for that double
	 * 
	 * @return the minimum value
	 */
	public double min() default Double.MIN_VALUE;

	/**
	 * The maximum value for that double
	 * 
	 * @return the maximum value
	 */
	public double max() default Double.MAX_VALUE;

}