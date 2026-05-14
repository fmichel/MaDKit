package madkit.sample.agent.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

public class LifecycleAgentTest {

    @Test
    public void givenLifecycleAgent_whenInspectingLifecycleMethods_thenActivationLiveAndEndArePresent() throws Exception {
        Method activation = LifecycleAgent.class.getDeclaredMethod("onActivation");
        Method live = LifecycleAgent.class.getDeclaredMethod("onLive");
        Method end = LifecycleAgent.class.getDeclaredMethod("onEnd");
        assertThat(activation).isNotNull();
        assertThat(live).isNotNull();
        assertThat(end).isNotNull();
    }
}
