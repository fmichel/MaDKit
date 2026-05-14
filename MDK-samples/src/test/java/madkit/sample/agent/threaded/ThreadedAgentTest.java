package madkit.sample.agent.threaded;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

public class ThreadedAgentTest {

    @Test
    public void givenThreadedAgent_whenInspected_thenLiveAndMainMethodsExist() throws Exception {
        assertThat(ThreadedAgent.class.getDeclaredMethod("onLive")).isNotNull();
        Method main = ThreadedAgent.class.getMethod("main", String[].class);
        assertThat(main).isNotNull();
    }
}
