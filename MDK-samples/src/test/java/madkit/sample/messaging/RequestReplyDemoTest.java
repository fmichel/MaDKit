package madkit.sample.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

public class RequestReplyDemoTest {

    @Test
    public void givenRequestReplyDemo_whenInspectingClass_thenActivationLiveAndMainArePresent() throws Exception {
        Method activation = RequestReplyDemo.class.getDeclaredMethod("onActivation");
        Method live = RequestReplyDemo.class.getDeclaredMethod("onLive");
        Method main = RequestReplyDemo.class.getMethod("main", String[].class);
        assertThat(activation).isNotNull();
        assertThat(live).isNotNull();
        assertThat(main).isNotNull();
    }
}
