package madkit.sample.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

public class BroadcastDemoTest {

    @Test
    public void givenBroadcastDemo_whenInspectingClass_thenActivationLiveAndMainArePresent() throws Exception {
        Method activation = BroadcastDemo.class.getDeclaredMethod("onActivation");
        Method live = BroadcastDemo.class.getDeclaredMethod("onLive");
        Method main = BroadcastDemo.class.getMethod("main", String[].class);
        assertThat(activation).isNotNull();
        assertThat(live).isNotNull();
        assertThat(main).isNotNull();
    }
}
