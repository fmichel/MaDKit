package madkit.utils;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
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