package madkit.kernel;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import madkit.test.agents.CGRAgent;
import madkit.test.agents.EmptyAgent;

public class MadkitClassLoaderTest {
	@Test
	public void givenValidAgentClassName_whenGetAgentInstance_thenReturnAgentInstance() {
		// Given
		String agentClassName = "madkit.test.agents.CGRAgent";

		// When
		Agent agentInstance = MadkitClassLoader.getAgentInstance(agentClassName);

		// Then
		assertNotNull(agentInstance, "The agent instance should not be null");
		assertEquals(agentClassName, agentInstance.getClass().getName(),
				"The agent instance should be of the correct type");
	}

	@Test
	public void givenInvalidAgentClassName_whenGetAgentInstance_thenReturnNull() {
		// Given
		String agentClassName = "madkit.kernel.NonExistentAgent";

		// When
		Agent agentInstance = MadkitClassLoader.getAgentInstance(agentClassName);

		// Then
		assertNull(agentInstance, "The agent instance should be null for a non-existent class");
	}

	@Test(expectedExceptions = ClassCastException.class)
	public void givenInvalidReturnType_whenGetAgentInstance_thenClassCastException() {
		// Given
		String agentClassName = EmptyAgent.class.getName();

		// When
		CGRAgent agentInstance = MadkitClassLoader.getAgentInstance(agentClassName);
	}
}
