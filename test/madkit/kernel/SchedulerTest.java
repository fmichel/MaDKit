package madkit.kernel;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;


public class SchedulerTest {

	@Test
	public final void testSetDelay() {
		Scheduler name = new Scheduler();
		name.setDelay(-10);
		assertEquals(0, name.getDelay());
		name.setDelay(500);
		assertEquals(400, name.getDelay());
	}

}
