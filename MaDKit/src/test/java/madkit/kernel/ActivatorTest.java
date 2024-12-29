
package madkit.kernel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.testng.annotations.Test;

import madkit.test.agents.EmptyAgent;

/**
 * Tests for Activator
 */
public class ActivatorTest {

	@Test
	public void givenAgentAndBehavior_whenExecuteBehaviorOf_thenNoExceptionThrown() {
		// Given
		Agent agent = new Agent() {
			public void testBehavior() {
				// Behavior implementation
			}
		};
		String behaviorName = "testBehavior";

		// When & Then
		assertThatCode(() -> Activator.executeBehaviorOf(agent, behaviorName))
				.as("Executing behavior '%s' on agent should not throw any exception", behaviorName)
				.doesNotThrowAnyException();
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void givenEmptyAgent_whenExecuteBehavior_thenThrowsNoSuchMethod() {
		Activator.executeBehaviorOf(new EmptyAgent(), "test");
	}

	@Test
	public void givenGenericAgent_whenNoArgs_thenExecuteBehavior_shouldWorks() {
		GenericTestAgent genericTestAgent = new GenericTestAgent();
		Activator.executeBehaviorOf(genericTestAgent, "privateMethod");
		assertThat(genericTestAgent.isOneMethodHasBeenActivated()).isTrue();
	}

	@Test
	public void givenGenericAgent_whenArgs_thenExecuteBehavior_shouldWorks() {
		GenericTestAgent genericTestAgent = new GenericTestAgent();
		Activator.executeBehaviorOf(genericTestAgent, "privateMethodWithPrimitiveArgs", "hello", 1);
		assertThat(genericTestAgent.isOneMethodHasBeenActivated()).isTrue();
	}
}
