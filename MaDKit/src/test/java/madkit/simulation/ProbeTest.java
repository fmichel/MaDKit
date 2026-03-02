package madkit.simulation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.lang.reflect.Field;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import madkit.kernel.Agent;
import madkit.kernel.MadkitConcurrentTestCase;
import madkit.kernel.Probe;
import madkit.kernel.Watcher;
import madkit.testing.agents.SimulatedAgent;

/**
 * Has to be outside of madkit.kernel for really testing visibility
 * 
 *
 * @since MaDKit 5.0.0.13
 * @version 0.9
 */
@SuppressWarnings("all")
public class ProbeTest extends MadkitConcurrentTestCase {

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
	public void givenNewProbe_whenAddedBeforeAgentsJoin_thenSizeIsCorrect() {
		runSimuTest(new Watcher() {
			@Override
			protected void onActivation() {
				Probe p = new Probe(getCommunity(), GROUP, ROLE);
				addProbe(p);
				assertThat(p.size()).as("probe initial size").isEqualTo(0);
				assertThat(launchAgent(new SimulatedAgent())).as("launch simulated agent").isEqualTo(SUCCESS);
				assertThat(p.size()).as("probe size after agent join").isEqualTo(1);
				resume();
			}
		});
	}

	@Test
	public void givenNewProbe_whenAddedAfterAgentsJoined_thenSizeIsCorrect() {
		runSimuTest(new Watcher() {
			@Override
			protected void onActivation() {
				assertThat(launchAgent(new SimulatedAgent())).as("launch simulated agent").isEqualTo(SUCCESS);
				Probe p = new Probe(getCommunity(), GROUP, ROLE);
				addProbe(p);
				assertThat(p.size()).as("probe size after add").isEqualTo(1);
				resume();
			}
		});
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
		assertThat(m).isNotNull();
		System.err.println(m.get(agt));
	}

	@Test
	public void testFindFieldOnPublic() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field m = a.findFieldOn(agt.getClass(), "bool2");
		assertThat(m).isNotNull();
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
		assertThat(m).isNotNull();
		System.err.println(m.get(agt));
	}

	@Test
	public void testFindFieldOnPrivate() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field m = a.findFieldOn(agt.getClass(), "something");
		assertThat(m).isNotNull();
		System.err.println(m.get(agt));
		m = a.findFieldOn(agt.getClass(), "alive");
		assertThat(m).isNotNull();
		System.err.println(m.get(agt));
	}
}

@SuppressWarnings("all")
class TestAgent extends Agent {

	public boolean bool = false;
	protected int value = 2;
	private double something = 3.0;
}