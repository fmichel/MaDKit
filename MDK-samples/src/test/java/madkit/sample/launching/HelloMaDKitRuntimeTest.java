package madkit.sample.launching;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;

import org.testng.annotations.Test;

import madkit.samples.support.SamplesRuntimeTestSupport;

public class HelloMaDKitRuntimeTest extends SamplesRuntimeTestSupport {

    @Test
    public void givenHelloMaDKitAgent_whenActivated_thenLaunchReturnsSuccessAndActivationRuns() {
        CountDownLatch activated = new CountDownLatch(1);
        HelloMaDKit sample = new HelloMaDKit() {
            @Override
            protected void onActivation() {
                super.onActivation();
                activated.countDown();
            }
        };

        assertThat(launch(sample)).isNotNull();
        await(activated, 2000);
    }
}
