package madkit.simulation.activator;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import madkit.kernel.Activator;
import madkit.kernel.Agent;
import madkit.kernel.AgentInterruptedException;
import madkit.reflection.MethodHandleFinder;
import madkit.reflection.ReflectionUtils;
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
/**
 * @author Fabien Michel
 * 
 * @since 6.0
 */
public class MethodHandleActivator extends Activator {

	/**
	 * methods maps an agent class to its corresponding Method object for runtime
	 * invocation
	 */
	private final Map<Class<?>, MethodHandle> methods;
	private final String method;
	private Class<?>[] argTypes;
	private MethodHandle cachedMethod;
	private Class<?> cachedClass;

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
	public MethodHandleActivator(final String community, final String group, final String role, final String methodName,
			Class<?>... argTypes) {
		super(community, group, role);
		methods = new ConcurrentHashMap<>();
		method = methodName;
		if (argTypes.length != 0) {
			this.argTypes = argTypes;
		}
	}

	@Override
	public void execute(Object... args) {
		if (isMulticoreOn()) {
			executeInParallel(getCurrentAgentsList(), args);
		} else {
			execute(getCurrentAgentsList(), args);
		}
	}

	public void execute(List<? extends Agent> agents, Object... args) {
		if (args.length != 0) {
			exexecuteBehaviorsWithArgs(agents, args);
		} else {
			executeBehaviors(agents);
		}
	}

	/**
	 * @param a
	 * @param e
	 */
	private void handleInbvokeException(Agent a, Throwable e) {
		Throwable cause = e.getCause();
		if (cause instanceof AgentInterruptedException aie) {
			throw aie;
		}
		throw new SimulationException(toString() + " on " + method + " " + a, cause);
	}

	/**
	 * @param agents
	 * @param args
	 */
	private void executeBehaviors(List<? extends Agent> agents) {
		for (final Agent a : agents) {
			if (a.isAlive()) {
				refreshCaches(a);
				try {
					cachedMethod.invoke(a);
				} catch (Throwable e) {
					handleInbvokeException(a, e);
				}
			}
		}
	}

	public void executeInParallel(List<? extends Agent> agents, Object... args) {
		if (args.length != 0) {
			executeInParallelWithArgs(agents, args);
		} else {
			agents.parallelStream().forEach(a -> {
				try {
					executeBehaviorNoCacheNoArg(a, args);
				} catch (Throwable e) {
					handleInbvokeException(a, e);
				}
			});
		}
	}

	private void executeInParallelWithArgs(List<? extends Agent> agents, Object... args) {
		agents.parallelStream().forEach(a -> {
			try {
				Object[] params = new Object[args.length + 1];
				System.arraycopy(args, 0, params, 1, args.length);
				params[0] = a;
				getMethodHandle(a.getClass(), args).invokeWithArguments(params);
			} catch (Throwable e) {
				handleInbvokeException(a, e);
			}
		});
	}

	private void executeBehaviorNoCacheNoArg(Agent a, Object[] args) throws Throwable {
		if (a.isAlive()) {
			getMethodHandle(a.getClass(), args).invoke(a);
		}
	}

	/**
	 * build the array needed for {@link MethodHandle#invokeWithArguments(List)}
	 * 
	 * @param agents
	 * @param args
	 */
	private void exexecuteBehaviorsWithArgs(List<? extends Agent> agents, Object... args) {
		Object[] params = new Object[args.length + 1];
		System.arraycopy(args, 0, params, 1, args.length);
		for (final Agent a : agents) {
			if (a.isAlive()) {
				refreshCaches(a, args);
				try {
					params[0] = a;
					cachedMethod.invokeWithArguments(params);
				} catch (Throwable e) {
					handleInbvokeException(a, e);
				}
			}
		}
	}

	private void refreshCaches(Agent a, Object... args) {
		Class<?> agentClass = a.getClass();
		if (cachedClass != agentClass) {
			cachedClass = agentClass;
			cachedMethod = getMethodHandle(cachedClass, args);
		}
	}

	private MethodHandle getMethodHandle(Class<?> agentClass, Object[] args) {
		return methods.computeIfAbsent(agentClass, c -> {
			try {
				if (argTypes == null) {
					argTypes = ReflectionUtils.convertArgToTypes(args);
				}
				return MethodHandleFinder.findMethodHandle(c, method, argTypes);
			} catch (NoSuchMethodException e) {
				throw new SimulationException(toString(), e);
			}
		});
	}

}
