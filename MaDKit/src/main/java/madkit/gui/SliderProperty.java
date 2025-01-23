package madkit.gui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.controlsfx.control.PropertySheet;

/**
 * Annotation to specify which fields, among those annotated with {@link UIProperty},
 * should be displayed as sliders when using {@link PropertySheetFactory} methods to
 * create a {@link PropertySheet}
 * 
 * Sliders from JavaFX only work with double values, so the annotated field must be of
 * type double.
 * 
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SliderProperty {
	/**
	 * The minimum value of the slider
	 * 
	 * @return the minimum value
	 */
	public double min() default Double.MIN_VALUE;

	/**
	 * The maximum value of the slider
	 * 
	 * @return the maximum value
	 */
	public double max() default Double.MAX_VALUE;

	/**
	 * The precision of the slider when using the mouse wheel. Default is 1 unit.
	 * 
	 * @return the precision of the scrolling
	 */
	public double scrollPrecision() default 1;
}