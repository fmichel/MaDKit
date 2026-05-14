package madkit.sample.agent.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

public class CrashInLiveDemoTest {

    @Test
    public void givenCrashInLiveDemo_whenInspectingClass_thenLiveAndEndMethodsArePresent() throws Exception {
        Method live = CrashInLiveDemo.class.getDeclaredMethod("onLive");
        Method end = CrashInLiveDemo.class.getDeclaredMethod("onEnd");
        assertThat(live).isNotNull();
        assertThat(end).isNotNull();
    }
}
