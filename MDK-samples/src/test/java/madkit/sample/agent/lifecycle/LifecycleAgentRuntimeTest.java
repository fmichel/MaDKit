package madkit.sample.agent.lifecycle;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.testng.annotations.Test;

import madkit.samples.support.SamplesRuntimeTestSupport;

public class LifecycleAgentRuntimeTest extends SamplesRuntimeTestSupport {

    @Test
    public void givenLifecycleAgent_whenLaunched_thenActivationLiveAndEndExecute() {
        CountDownLatch ended = new CountDownLatch(1);
        AtomicBoolean activated = new AtomicBoolean(false);
        AtomicBoolean lived = new AtomicBoolean(false);

        LifecycleAgent sample = new LifecycleAgent() {
            @Override
            protected void onActivation() {
                super.onActivation();
                activated.set(true);
            }

            @Override
            protected void onLive() {
                lived.set(true);
                super.onLive();
            }

            @Override
            protected void onEnd() {
                super.onEnd();
                ended.countDown();
            }
        };

        assertThat(launch(sample)).isNotNull();
        await(ended, 5000);
        assertThat(activated.get()).isTrue();
        assertThat(lived.get()).isTrue();
        assertThat(sample.isAlive()).isFalse();
    }
}