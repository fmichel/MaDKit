package madkit.sample.launching;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

import madkit.kernel.Agent;

public class CommandLineOptionsTest {

    @Test
    public void givenCommandLineOptionsClass_whenInspected_thenItIsAnAgentWithMainMethod() throws Exception {
        assertThat(Agent.class).isAssignableFrom(CommandLineOptions.class);
        Method main = CommandLineOptions.class.getMethod("main", String[].class);
        assertThat(main).isNotNull();
    }
}
