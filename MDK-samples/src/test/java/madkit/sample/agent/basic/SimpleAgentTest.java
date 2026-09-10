package madkit.sample.agent.basic;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

import madkit.kernel.DefaultTestAgent;
import madkit.kernel.MadkitConcurrentTestCase;

public class SimpleAgentTest extends MadkitConcurrentTestCase {

    @Test
    public void givenSimpleAgent_whenLaunched_thenAgentCompletesActivation() {
        // Given / When
        runTest(new DefaultTestAgent() {
            @Override
            public void behaviorInActivate() {
                SimpleAgent agent = new SimpleAgent();
                launchAgent(agent);

                // Then
                assertThat(agent.isAlive()).isTrue();
                resume();
            }
        });
    }
}