package madkit.sample.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

public class RequestReplyDemoTest {

    @Test
    public void givenRequestReplyDemo_whenInspectingClass_thenActivationLiveAndMainArePresent() throws Exception {
        // Given
        Method activation = RequestReplyDemo.class.getDeclaredMethod("onActivation");
        Method live = RequestReplyDemo.class.getDeclaredMethod("onLive");
        Method main = RequestReplyDemo.class.getMethod("main", String[].class);
        // When
        boolean methodsPresent = activation != null && live != null && main != null;

        // Then
        assertThat(methodsPresent).isTrue();
    }
}
