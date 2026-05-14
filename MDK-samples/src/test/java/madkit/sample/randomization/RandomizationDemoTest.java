package madkit.sample.randomization;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.testng.annotations.Test;

public class RandomizationDemoTest {

    @Test
    public void givenRandomizationDemo_whenInspectingClass_thenAgentCountAndMainArePresent() throws Exception {
        Field count = RandomizationDemo.class.getDeclaredField("AGENT_COUNT");
        count.setAccessible(true);
        assertThat(count.getInt(null)).isEqualTo(3);

        Method main = RandomizationDemo.class.getMethod("main", String[].class);
        assertThat(main).isNotNull();
    }
}
