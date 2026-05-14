package madkit.sample.launching;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

import madkit.kernel.Agent;

public class HelloMaDKitTest {

    @Test
    public void givenHelloMaDKitClass_whenInspected_thenItIsAnAgentWithMainMethod() throws Exception {
        assertThat(Agent.class).isAssignableFrom(HelloMaDKit.class);
        Method main = HelloMaDKit.class.getMethod("main", String[].class);
        assertThat(main).isNotNull();
    }
}
