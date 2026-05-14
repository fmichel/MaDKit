package madkit.sample.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

public class MessagingLauncherTest {

    @Test
    public void givenMessagingLauncher_whenInspectingClass_thenActivationAndMainArePresent() throws Exception {
        Method activation = MessagingLauncher.class.getDeclaredMethod("onActivation");
        Method main = MessagingLauncher.class.getMethod("main", String[].class);
        assertThat(activation).isNotNull();
        assertThat(main).isNotNull();
    }
}
