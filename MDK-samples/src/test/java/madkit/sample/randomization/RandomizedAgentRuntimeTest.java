package madkit.sample.randomization;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.testng.annotations.Test;

import madkit.samples.support.SamplesRuntimeTestSupport;

public class RandomizedAgentRuntimeTest extends SamplesRuntimeTestSupport {

    @Test
    public void givenRandomizedAgent_whenLaunched_thenFieldsAreInitializedWithinExpectedBounds() throws Exception {
        CountDownLatch activated = new CountDownLatch(1);
        RandomizedAgent sample = new RandomizedAgent() {
            @Override
            protected void onActivation() {
                super.onActivation();
                activated.countDown();
            }
        };

        assertThat(launch(sample)).isNotNull();
        await(activated, 2000);

        assertThat(readDouble(sample, "speed")).isGreaterThanOrEqualTo(0.0).isLessThan(100.0);
        assertThat(readInt(sample, "strength")).isGreaterThanOrEqualTo(1).isLessThan(10);
        assertThat(readFloat(sample, "accuracy")).isGreaterThanOrEqualTo(0.0f).isLessThan(1.0f);
        assertThat(readString(sample, "characterClass")).isIn(Set.of("Warrior", "Mage", "Rogue"));
    }

    private double readDouble(Object target, String name) throws Exception {
        Field f = target.getClass().getSuperclass().getDeclaredField(name);
        f.setAccessible(true);
        return f.getDouble(target);
    }

    private int readInt(Object target, String name) throws Exception {
        Field f = target.getClass().getSuperclass().getDeclaredField(name);
        f.setAccessible(true);
        return f.getInt(target);
    }

    private float readFloat(Object target, String name) throws Exception {
        Field f = target.getClass().getSuperclass().getDeclaredField(name);
        f.setAccessible(true);
        return f.getFloat(target);
    }

    private String readString(Object target, String name) throws Exception {
        Field f = target.getClass().getSuperclass().getDeclaredField(name);
        f.setAccessible(true);
        return (String) f.get(target);
    }
}
