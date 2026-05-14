package madkit.sample.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

public class SenderAgentTest {

    @Test
    public void givenSenderAgent_whenInspectingMessagingMethods_thenSendToAndActivationArePresent() throws Exception {
        Method activation = SenderAgent.class.getDeclaredMethod("onActivation");
        Method sendTo = SenderAgent.class.getDeclaredMethod("sendTo", String.class, String.class, String.class);
        assertThat(activation).isNotNull();
        assertThat(sendTo).isNotNull();
    }
}
