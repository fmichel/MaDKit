package madkit.sample.agent.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

public class CrashInActivateDemoTest {

    @Test
    public void givenCrashInActivateDemo_whenInspectingClass_thenActivationAndEndMethodsArePresent() throws Exception {
        Method activation = CrashInActivateDemo.class.getDeclaredMethod("onActivation");
        Method end = CrashInActivateDemo.class.getDeclaredMethod("onEnd");
        assertThat(activation).isNotNull();
        assertThat(end).isNotNull();
    }
}
