package madkit.sample.launching;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

import madkit.kernel.Agent;

public class MultipleLaunchingTest {

    @Test
    public void givenMultipleLaunchingClass_whenInspected_thenItIsAnAgentWithMainMethod() throws Exception {
        assertThat(Agent.class).isAssignableFrom(MultipleLaunching.class);
        Method main = MultipleLaunching.class.getMethod("main", String[].class);
        assertThat(main).isNotNull();
    }
}
