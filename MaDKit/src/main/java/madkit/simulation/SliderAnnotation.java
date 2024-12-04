package madkit.simulation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SliderAnnotation {
	public double minValue() default Double.MIN_VALUE;

	public double maxValue() default Double.MAX_VALUE;

	public double scrollPrecision() default 1;
}