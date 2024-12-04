package madkit.simulation;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import madkit.kernel.Activator;
import madkit.kernel.Agent;
import madkit.kernel.GenericTestAgent;

/**
 * Has to be outside of madkit.kernel for really testing visibility
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.13
 * @version 0.9
 * 
 */
public class ActivatorTest {

	private Agent emptyAgent;
	private Activator noSuchMethodActivator;
	private Activator getLoggerActivator;

	@BeforeMethod
	public void initActivator() {
		emptyAgent = new Agent();
		emptyAgent = new GenericTestAgent();
		noSuchMethodActivator = new Activator("test", "test") {
			@Override
			public void execute(Object... args) {
			};
		};
		getLoggerActivator = new Activator("test", "test") {
			@Override
			public void execute(Object... args) {
				executeBehaviorOf(emptyAgent, getCommunity(), args);
			};
		};
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void givenEmptyAgent_whenExecuteBehavior_thenThrowsNoSuchMethod() {
		Activator.executeBehaviorOf(emptyAgent, "test");
	}

	@Test
	public void givenGenericAgent_whenNoArgs_thenExecuteBehavior_shouldWorks() {
		Activator.executeBehaviorOf(new GenericTestAgent(), "privateMethod");
	}

	@Test
	public void givenGenericAgent_whenArgs_thenExecuteBehavior_shouldWorks() {
		Activator.executeBehaviorOf(new GenericTestAgent(), "privateMethodWithPrimitiveArgs", "hello", 1);
	}
}
