package madkit.kernel;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MadkitKernelTest extends JunitMadkit {

	@Test
	public void testCreateBucket() {
		launchTest(new AbstractAgent() {
			/**
			 * 
			 */
			private static final long	serialVersionUID	= 1L;

			@Override
			protected void activate() {
				try {
					for (int i = 0; i < 1000; i++) {
						assertEquals(i, getKernel().createBucket(AbstractAgent.class.getName(),i, 6).size());
					}
					assertEquals(0, getKernel().createBucket(AbstractAgent.class.getName(),0, 6).size());
				} catch (InstantiationException | IllegalAccessException
						| ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		});
	}

}
