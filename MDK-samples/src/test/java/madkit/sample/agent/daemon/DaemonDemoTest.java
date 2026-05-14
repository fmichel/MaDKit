package madkit.sample.agent.daemon;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

public class DaemonDemoTest {

    @Test
    public void givenDaemonDemo_whenInspected_thenActivationLiveAndMainMethodsExist() throws Exception {
        assertThat(DaemonDemo.class.getDeclaredMethod("onActivation")).isNotNull();
        assertThat(DaemonDemo.class.getDeclaredMethod("onLive")).isNotNull();
        Method main = DaemonDemo.class.getMethod("main", String[].class);
        assertThat(main).isNotNull();
    }
}
