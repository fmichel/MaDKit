package madkit.sample.agent.basic;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

public class SimpleAgentTest {

    @Test
    public void givenSimpleAgent_whenInspected_thenActivationAndMainMethodsExist() throws Exception {
        assertThat(SimpleAgent.class.getDeclaredMethod("onActivation")).isNotNull();
        Method main = SimpleAgent.class.getMethod("main", String[].class);
        assertThat(main).isNotNull();
    }
}
