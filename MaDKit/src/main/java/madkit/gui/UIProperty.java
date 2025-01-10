package madkit.gui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a field as a parameter in GUI built using predefined
 * JavaFX agents GUI such as {@link AgentDefaultGUI}. This annotation can be
 * used to specify metadata for the parameter, such as its category and display
 * name.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UIProperty {

	/**
	 * Specifies the category of the parameter.
	 * 
	 * @return the category of the parameter
	 */
	String category() default "";

	/**
	 * Specifies the display name of the parameter.
	 * 
	 * @return the display name of the parameter
	 */
	String displayName() default "";

}