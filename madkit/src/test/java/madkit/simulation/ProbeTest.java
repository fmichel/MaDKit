
package madkit.simulation;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.lang.reflect.Field;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import madkit.kernel.Agent;
import madkit.kernel.Probe;

/**
 * Has to be outside of madkit.kernel for really testing visibility
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.13
 * @version 0.9
 */
@SuppressWarnings("all")
public class ProbeTest {

	Probe a;
	TestAgent agt;

	@BeforeMethod
	public void setUp() throws Exception {
		a = new Probe("t", "t", "t");
		agt = new TestAgent() {

			boolean bool2 = false;
		};
	}

	@Test
	public void testActivator() {
		try {
			a = new Probe(null, null, null);
			fail("ex not thrown");
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testFindFieldOnInheritedPublic()
			throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field m = a.findFieldOn(agt.getClass(), "bool");
		assertNotNull(m);
		System.err.println(m.get(agt));
	}

	@Test
	public void testFindFieldOnPublic() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field m = a.findFieldOn(agt.getClass(), "bool2");
		assertNotNull(m);
		System.err.println(m.get(agt));
	}

	@Test
	public void testFindFieldOnNotExist() {
		try {
			Field m = a.findFieldOn(agt.getClass(), "notExist");
			fail("ex not thrown");
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testFindFieldOnProtected()
			throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field m = a.findFieldOn(agt.getClass(), "value");
		assertNotNull(m);
		System.err.println(m.get(agt));
	}

	@Test
	public void testFindFieldOnPrivate() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field m = a.findFieldOn(agt.getClass(), "something");
		assertNotNull(m);
		System.err.println(m.get(agt));
		m = a.findFieldOn(agt.getClass(), "simuEngine");
		assertNotNull(m);
		System.err.println(m.get(agt));
	}
}

@SuppressWarnings("all")
class TestAgent extends Agent {

	public boolean bool = false;
	protected int value = 2;
	private double something = 3.0;
}
