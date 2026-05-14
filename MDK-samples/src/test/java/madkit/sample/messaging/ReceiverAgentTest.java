package madkit.sample.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

public class ReceiverAgentTest {

    @Test
    public void givenReceiverAgent_whenInspectingMessagingMethods_thenActivationAndLiveArePresent() throws Exception {
        Method activation = ReceiverAgent.class.getDeclaredMethod("onActivation");
        Method live = ReceiverAgent.class.getDeclaredMethod("onLive");
        assertThat(activation).isNotNull();
        assertThat(live).isNotNull();
    }
}
