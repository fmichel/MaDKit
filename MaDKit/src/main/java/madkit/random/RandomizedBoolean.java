package madkit.random;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a field as a boolean that should be randomized when a
 * {@link Randomness#randomizeFields(Object, java.util.random.RandomGenerator)} is used.
 * 
 * @see Randomness
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RandomizedBoolean {
}
