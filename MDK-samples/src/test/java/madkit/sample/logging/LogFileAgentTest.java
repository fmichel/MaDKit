package madkit.sample.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

public class LogFileAgentTest {

    @Test
    public void givenLogFileAgent_whenInspected_thenActivationAndMainMethodsExist() throws Exception {
        assertThat(LogFileAgent.class.getDeclaredMethod("onActivation")).isNotNull();
        Method main = LogFileAgent.class.getMethod("main", String[].class);
        assertThat(main).isNotNull();
    }
}
