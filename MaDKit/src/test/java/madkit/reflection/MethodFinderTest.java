package madkit.reflection;

import static org.testng.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.testng.annotations.Test;

import madkit.kernel.Activator;
import madkit.kernel.Agent;
import madkit.kernel.GenericTestAgent;
import madkit.kernel.Message;

/**
 * Has to be outside of madkit.kernel for really testing visibility
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.13
 * @version 0.9
 * 
 */
public class MethodFinderTest {

	Method m;
	Agent agent = new GenericTestAgent();

	@Test
	public void getClassMethodOnNoArg() throws Throwable {
		m = MethodFinder.getMethodOn(GenericTestAgent.class, "privateMethod");
		m.invoke(agent);
		m = MethodFinder.getMethodOn(GenericTestAgent.class, "protectedMethod");
		m.invoke(agent);
		m = MethodFinder.getMethodOn(GenericTestAgent.class, "publicMethod");// public
		m.invoke(agent);
	}

	@Test
	public void getClassInheritedMethodOnPublicNoArg() throws Throwable {
		try {
			m = MethodFinder.getMethodOn(GenericTestAgent.class, "getLogger");// public
			m.invoke(agent);
			m = MethodFinder.getMethodOn(GenericTestAgent.class, "activate");// protected
			m.invoke(agent);
			m = MethodFinder.getMethodOn(GenericTestAgent.class, "getKernel");// private
			m.invoke(agent);
		} catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void getMethodOnArg() throws Throwable {
		try {
			m = MethodFinder.getMethodOn(Agent.class, "receiveMessage", new Message());
			m.invoke(agent, new Message());
			m = MethodFinder.getMethodOn(GenericTestAgent.class, "privateMethod");// private
			m.invoke(agent);
			m = MethodFinder.getMethodOn(GenericTestAgent.class, "privateMethodWithArgs", "test", new Object());// private
			m.invoke(agent, "test", new Object());
			m = MethodFinder.getMethodOn(GenericTestAgent.class, "privateMethodWithPrimitiveArgs", "test",
					Integer.valueOf(1));// private
			m.invoke(agent, "test", 1);
			m.invoke(agent, "test", Integer.valueOf(1));
			m = MethodFinder.getMethodOn(GenericTestAgent.class, "privateMethodWithPrimitiveArgs", "test", 1);// private
			m.invoke(agent, "test", 1);
			m.invoke(agent, "test", Integer.valueOf(1));
		} catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void getMethodOnPrimitiveArg() throws Throwable {
		m = MethodFinder.getMethodOn(GenericTestAgent.class, "privateMethodWithPrimitiveArgs", "test", 1);// private
		m.invoke(agent, "test", 1);
		m.invoke(agent, "test", Integer.valueOf(1));
		m = MethodFinder.getMethodOn(GenericTestAgent.class, "privateMethodWithPrimitiveArgs", "test", 1);// private
	}

	@Test
	public void getMethodFromTypesNoArg() throws Throwable {
		m = MethodFinder.getMethodFromTypes(GenericTestAgent.class, "getLogger");// public
		m.invoke(agent);
		m = MethodFinder.getMethodFromTypes(GenericTestAgent.class, "isThreaded");// protected
		m.invoke(agent);
		m = MethodFinder.getMethodFromTypes(GenericTestAgent.class, "privateMethod");// private
		m.invoke(agent);
	}

	@Test
	public void getMethodFromTypesArg() throws Throwable {
		MethodFinder.getMethodFromTypes(GenericTestAgent.class, "receiveMessage", Message.class);
		MethodFinder.getMethodFromTypes(GenericTestAgent.class, "privateMethodWithArgs", String.class, Object.class);
		MethodFinder.getMethodFromTypes(GenericTestAgent.class, "privateMethodWithArgs", String.class, Integer.class);
	}

	@Test
	public void getMethodFromPrimitiveTypesArg() throws Throwable {
		MethodFinder.getMethodFromTypes(GenericTestAgent.class, "privateMethodWithArgs", String.class, int.class);
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void noSuchMethod() {
		Agent a = new Agent();
		Activator activator = new Activator("test", "test") {
			@Override
			public void execute(Object... args) {
			};
		};
		activator.executeBehaviorOf(a, "test");
	}

}
