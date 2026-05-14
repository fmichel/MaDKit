package madkit.samples.support;

import static org.assertj.core.api.Assertions.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.BeforeMethod;

import madkit.kernel.Agent;
import madkit.kernel.Agent.ReturnCode;
import madkit.kernel.Madkit;

public abstract class SamplesRuntimeTestSupport {

    protected Madkit madkit;
    private Object kernelAgent;
    private Method launchAgentWithTimeout;

    @BeforeMethod
    public void initKernel() {
        try {
            madkit = new Madkit();
            Field kernelField = Madkit.class.getDeclaredField("kernelAgent");
            kernelField.setAccessible(true);
            int attempts = 0;
            while (kernelAgent == null && attempts++ < 50) {
                kernelAgent = kernelField.get(madkit);
                if (kernelAgent == null) {
                    Thread.sleep(20);
                }
            }
            if (kernelAgent == null) {
                throw new IllegalStateException("Kernel agent is not initialized");
            }
            launchAgentWithTimeout = kernelAgent.getClass().getMethod("launchAgent", Agent.class, int.class);
            launchAgentWithTimeout.setAccessible(true);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot initialize runtime test harness", e);
        }
    }

    protected ReturnCode launch(Agent agent) {
        try {
            return (ReturnCode) launchAgentWithTimeout.invoke(kernelAgent, agent, 0);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot launch agent", e);
        }
    }

    protected void await(CountDownLatch latch, long timeoutMs) {
        try {
            if (!latch.await(timeoutMs, TimeUnit.MILLISECONDS)) {
                fail("Timeout while waiting for asynchronous agent behavior");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrupted while waiting for asynchronous agent behavior", e);
        }
    }
}