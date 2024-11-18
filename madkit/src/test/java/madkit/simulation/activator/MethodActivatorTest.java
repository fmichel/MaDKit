package madkit.simulation.activator;

import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import madkit.kernel.Agent;
import madkit.kernel.GenericTestAgent;
import madkit.kernel.JunitMadkit;

public class MethodActivatorTest extends JunitMadkit {

	@Test
	public void executeTest() {
		testBehavior(a -> {
			List<GenericTestAgent> agents = getNewAgentsList(a);
			MethodActivator activator = new MethodActivator("test", "test", "privateMethod");
			activator.execute(agents);
			checkActivation(agents);
		});
	}

	@Test
	public void givenAnActivatorWithTypes_whenExecute_thenShouldWorks() {
		testBehavior(a -> {
			List<GenericTestAgent> agents = getNewAgentsList(a);
			MethodActivator activator = new MethodActivator("test", "test",
					"privateMethodWithPrimitiveArgs", String.class, int.class);
			activator.execute(agents, "hello", 1);
			checkActivation(agents);
		});
	}

	@Test
	public void givenAnActivatorNoTypesAtCreation_whenExecute_thenShouldWorks() {
		testBehavior(a -> {
			List<GenericTestAgent> agents = getNewAgentsList(a);
			MethodActivator activator = new MethodActivator("test", "test",
					"privateMethodWithPrimitiveArgs");
			activator.execute(agents, "hello", 1);
			checkActivation(agents);
		});
	}

	@Test
	public void givenAgentList_whenExecuteInParallel_shouldWorks() {
		testBehavior(a -> {
			List<GenericTestAgent> agents = getNewAgentsList(a);
			MethodActivator activator = new MethodActivator("test", "test", "privateMethod");
			activator.executeInParallel(agents);
			checkActivation(agents);
		});
	}

	@Test
	public void givenAnActivatorNoTypesAtCreation_whenExecuteInParallel_thenShouldWorks() {
		testBehavior(a -> {
			List<GenericTestAgent> agents = getNewAgentsList(a);
			MethodActivator activator = new MethodActivator("test", "test",
					"privateMethodWithPrimitiveArgs");
			activator.executeInParallel(agents, "hello", 1);
			checkActivation(agents);
		});
	}

	void checkActivation(List<GenericTestAgent> agents) {
		for (GenericTestAgent agent : agents) {
			assertTrue(agent.isOneMethodHasBeenActivated());
		}
	}

	List<GenericTestAgent> getNewAgentsList(Agent creator) {
		GenericTestAgent a1 = creator.launchAgent(GenericTestAgent.class.getName(), 1);
		GenericTestAgent a2 = creator.launchAgent(GenericTestAgent.class.getName(), 1);
		return List.of(a1, a2);
	}

}