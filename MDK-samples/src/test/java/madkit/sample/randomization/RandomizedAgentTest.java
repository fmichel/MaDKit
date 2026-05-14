package madkit.sample.randomization;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;

import org.testng.annotations.Test;

import madkit.random.RandomizedBoolean;
import madkit.random.RandomizedDouble;
import madkit.random.RandomizedFloat;
import madkit.random.RandomizedInteger;
import madkit.random.RandomizedString;

public class RandomizedAgentTest {

    @Test
    public void givenRandomizedAgent_whenInspectingFields_thenRandomizationAnnotationsArePresent() throws Exception {
        assertThat(annotationPresent("speed", RandomizedDouble.class)).isTrue();
        assertThat(annotationPresent("strength", RandomizedInteger.class)).isTrue();
        assertThat(annotationPresent("active", RandomizedBoolean.class)).isTrue();
        assertThat(annotationPresent("accuracy", RandomizedFloat.class)).isTrue();
        assertThat(annotationPresent("characterClass", RandomizedString.class)).isTrue();
    }

    private boolean annotationPresent(String fieldName, Class<?> annotationType) throws Exception {
        Field field = RandomizedAgent.class.getDeclaredField(fieldName);
        return field.getAnnotation((Class) annotationType) != null;
    }
}
