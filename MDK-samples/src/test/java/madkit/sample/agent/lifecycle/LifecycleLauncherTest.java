package madkit.sample.agent.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

import madkit.kernel.Agent;

public class LifecycleLauncherTest {

    @Test
    public void givenLifecycleLauncher_whenInspected_thenItIsAnAgentWithThreadedLiveMethod() throws Exception {
        assertThat(Agent.class).isAssignableFrom(LifecycleLauncher.class);
        Method live = LifecycleLauncher.class.getDeclaredMethod("onLive");
        assertThat(live).isNotNull();
    }
}
