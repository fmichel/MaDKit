package madkit.sample.agent.management;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

public class AgentLauncherTest {

    @Test
    public void givenAgentLauncher_whenInspected_thenLiveAndMainMethodsExist() throws Exception {
        assertThat(AgentLauncher.class.getDeclaredMethod("onLive")).isNotNull();
        Method main = AgentLauncher.class.getMethod("main", String[].class);
        assertThat(main).isNotNull();
    }
}