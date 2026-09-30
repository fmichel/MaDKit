package madkit.sample.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import net.jodah.concurrentunit.Waiter;
import org.testng.annotations.Test;

import madkit.samples.support.SamplesRuntimeTestSupport;

public class RequestReplyDemoRuntimeTest extends SamplesRuntimeTestSupport {

    @Test
    public void givenRequestReplyDemo_whenRunningTheExchange_thenReplyIsObserved() throws Exception {
        // Given
        Waiter waiter = new Waiter();
        List<String> outcomes = new ArrayList<>();
        RequestReplyDemo sample = new RequestReplyDemo() {
            @Override
            protected void reportOutcome(String result) {
                synchronized (outcomes) {
                    outcomes.add(result);
                    waiter.resume();
                }
            }
        };

        // When
        assertThat(launch(sample)).isNotNull();
        waiter.await(4000);

        // Then
        synchronized (outcomes) {
            assertThat(outcomes).containsExactly("Pong!");
        }
    }
}
