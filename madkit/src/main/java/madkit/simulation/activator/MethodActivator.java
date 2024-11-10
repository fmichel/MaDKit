package madkit.simulation.activator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import madkit.kernel.Activator;
import madkit.kernel.Agent;
import madkit.kernel.AgentInterruptedException;
import madkit.reflection.MethodFinder;
import madkit.simulation.SimulationException;

/**
 * An activator that invokes a single method with no parameters on a group of
 * agents. This class encapsulates behavior invocation on MaDKit agents for
 * scheduler agents. This activator allows to call a particular Java method on
 * agents regardless of their actual class type as long as they extend
 * {@link Agent}. This has to be used by {@link Scheduler} subclasses to create
 * simulation applications.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.1
 * @version 1.0
 * 
 */
public class MethodActivator extends Activator {

	/**
	 * methods maps an agent class to its corresponding Method object for runtime
	 * invocation
	 */
	private final Map<Class<?>, Method> methods;
	private final String method;
	private final Class<?>[] argTypes;

	/**
	 * Builds a new GenericBehaviorActivator on the given CGR location of the
	 * artificial society. Once created, it has to be added by a {@link Scheduler}
	 * agent using the {@link Scheduler#addActivator(Activator)} method. Once added,
	 * it could be used to trigger the behavior on all the agents which are at this
	 * CGR location, regardless of their class type as long as they extend
	 * {@link Agent}
	 * 
	 * @param community
	 * @param group
	 * @param role
	 * @param methodName name of the Java method which will be invoked
	 */
	public MethodActivator(final String community, final String group, final String role, final String methodName,
			Class<?>... argTypes) {
		super(community, group, role);
		methods = new ConcurrentHashMap<>();
		method = methodName;
		this.argTypes = argTypes;
	}

	public String getMethod() {
		return method;
	}
	
	@Override
	public String getName() {
		return method;
	}

	@Override
	public void execute(Object... args) {
		if (isMulticoreOn()) {
			parallelExecute(args);
		} else {
			sequentialExecute(getCurrentAgentsList(), args);
		}
	}

	public void parallelExecute(Object... args) {
		getCurrentAgentsList().parallelStream().forEach(a -> {
			if (a.isAlive()) {
				invoke(getMethod(a.getClass()), a, args);
			}
		});
	}

	private void invoke(Method m, Agent a, Object... args) {
		try {
			m.invoke(a, args);
		} catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
			Throwable cause = e.getCause();
			if(cause instanceof AgentInterruptedException aie) {
				throw aie;
			}
			throw new SimulationException(toString() + " on " + method + " " + a, cause);
		}
	}

	public void sequentialExecute(List<Agent> agents, Object... args) {
		Method cachedM = null;
		Class<?> cachedC = null;
		for (final Agent a : agents) {
			if (a.isAlive()) {
				final Class<?> agentClass = a.getClass();
				if (agentClass != cachedC) {
					cachedC = agentClass;
					cachedM = getMethod(cachedC);
				}
				invoke(cachedM, a, args);
			}
		}
	}

	private Method getMethod(Class<?> agentClass) {
		return methods.computeIfAbsent(agentClass, c -> {
			try {
				return MethodFinder.getMethodFromTypes(c, method, argTypes);
			} catch (NoSuchMethodException e) {
				throw new SimulationException(toString(), e);
			}
		});
	}

}
