package madkit.sample.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import net.jodah.concurrentunit.Waiter;
import org.testng.annotations.Test;

import madkit.samples.support.SamplesRuntimeTestSupport;

public class RequestReplyTimeoutDemoRuntimeTest extends SamplesRuntimeTestSupport {

    @Test
    public void givenTimeoutDemo_whenRunningAllScenarios_thenSuccessMissingAndTimeoutAreObserved() throws Exception {
        // Given
        Waiter waiter = new Waiter();
        List<String> outcomes = new ArrayList<>();
        RequestReplyTimeoutDemo sample = new RequestReplyTimeoutDemo() {
            @Override
            protected void reportOutcome(String scenario, String result) {
                synchronized (outcomes) {
                    outcomes.add(scenario + ": " + result);
                    if (outcomes.size() == 3) {
                        waiter.resume();
                    }
                }
            }
        };

        // When
        assertThat(launch(sample)).isNotNull();
        waiter.await(4000);

        // Then
        synchronized (outcomes) {
            assertThat(outcomes).hasSize(3);
            assertThat(outcomes.get(0)).isEqualTo("successful reply: Pong!");
            assertThat(outcomes.get(1)).startsWith("missing recipient: no recipient (immediate, ");
            assertThat(outcomes.get(1)).endsWith(" ms)");
            assertThat(outcomes.get(2)).startsWith("bounded no-response: no reply after ");
            assertThat(outcomes.get(2)).endsWith(" ms");
            assertThat(extractMilliseconds(outcomes.get(1))).isLessThan(250);
            assertThat(extractMilliseconds(outcomes.get(2))).isBetween(150L, 1500L);
        }
    }

    private long extractMilliseconds(String outcome) {
        int unitStart = outcome.lastIndexOf(" ms");
        int valueStart = outcome.lastIndexOf(' ', unitStart - 1) + 1;
        return Long.parseLong(outcome.substring(valueStart, unitStart));
    }
}
