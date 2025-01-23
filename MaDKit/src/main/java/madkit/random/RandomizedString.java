package madkit.random;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is used to mark a field as a string that should be randomized when
 * {@link Randomness#randomizeFields(Object, java.util.random.RandomGenerator)} is used.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RandomizedString {
	String[] values();
}
