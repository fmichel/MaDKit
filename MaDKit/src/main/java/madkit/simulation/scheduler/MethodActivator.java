package madkit.simulation.scheduler;

import java.lang.invoke.MethodHandle;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import madkit.kernel.Activator;
import madkit.kernel.Agent;
import madkit.kernel.AgentInterruptedException;
import madkit.kernel.Scheduler;
import madkit.reflection.MethodHandleFinder;
import madkit.reflection.ReflectionUtils;
import madkit.simulation.SimuException;

/**
 * An activator that invokes a single method on a group of agents. This class
 * encapsulates behavior invocation on MaDKit agents for scheduler agents. It
 * allows to call a particular Java method on agents regardless of their actual
 * class type as long as they extend {@link Agent}. This has to be used by
 * {@link Scheduler} subclasses to create simulation applications.
 *
 * @author Fabien Michel
 * @since MaDKit 5.0.0.1
 * @version 6.0
 */
public class MethodActivator extends Activator {

	/**
	 * Maps an agent class to its corresponding Method object for runtime
	 * invocation.
	 */
	private final Map<Class<?>, MethodHandle> methods;
	/** The name of the method to be invoked on the agents. */
	private final String method;
	/** The class types of the method arguments. */
	private Class<?>[] argTypes;
	/** Cached MethodHandle for the last accessed agent class. */
	private MethodHandle cachedMethod;
	/** Cached class of the last accessed agent. */
	private Class<?> cachedClass;
	/**
	 * Indicates whether the activation list should be shuffled before execution.
	 */
	private boolean shufflingMode;
	private boolean parallelMode = false;

	/**
	 * Builds a new GenericBehaviorActivator on the given CGR location of the
	 * artificial society. Once created, it has to be added by a {@link Scheduler}
	 * agent using the {@link Scheduler#addActivator(Activator)} method. Once added,
	 * it could be used to trigger the behavior on all the agents which are at this
	 * CGR location, regardless of their class type as long as they extend
	 * {@link Agent}.
	 *
	 * @param group      the group of the agents
	 * @param role       the role of the agents
	 * @param methodName the name of the Java method which will be invoked
	 * @param argTypes   the class types of the method arguments
	 */
	public MethodActivator(String group,
			String role,
			String methodName,
			Class<?>... argTypes) {
		super(group, role);
		methods = new ConcurrentHashMap<>();
		method = methodName;
		if (argTypes.length != 0) {
			this.argTypes = argTypes;
		}
	}

	/**
	 * Executes the behavior on the agents.
	 * <p>
	 * If the
	 *
	 * @param args the arguments to pass to the behavior
	 */
	@Override
	public void execute(Object... args) {
		List<Agent> currentAgentsList = getAgents();
		if (isParallelMode()) {
			executeInParallel(currentAgentsList, args);
		} else {
			if (isShufflingMode()) {
				Collections.shuffle(currentAgentsList, getScheduler().prng());
			}
			execute(currentAgentsList, args);
		}
	}

	/**
	 * Executes the behavior on the given list of agents.
	 *
	 * @param agents the list of agents
	 * @param args   the arguments to pass to the behavior
	 */
	public void execute(List<? extends Agent> agents, Object... args) {
		if (args.length != 0) {
			exexecuteBehaviorsWithArgs(agents, args);
		} else {
			executeBehaviors(agents);
		}
	}

	/**
	 * Handles exceptions thrown during method invocation.
	 *
	 * @param a the agent on which the exception occurred
	 * @param e the exception thrown
	 */
	private void handleInbvokeException(Agent a, Throwable e) {
		if (e instanceof AgentInterruptedException aie) {
			throw aie;
		}
		throw new SimuException(toString() + " on " + method + " " + a, e);
	}

	/**
	 * Executes the behavior on the given list of agents without arguments.
	 *
	 * @param agents the list of agents
	 */
	private void executeBehaviors(List<? extends Agent> agents) {
		for (Agent a : agents) {
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

	/**
	 * Executes the behavior on the given list of agents in parallel.
	 *
	 * @param agents the list of agents
	 * @param args   the arguments to pass to the behavior
	 */
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

	/**
	 * Executes the behavior on the given list of agents in parallel with arguments.
	 *
	 * @param agents the list of agents
	 * @param args   the arguments to pass to the behavior
	 */
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

	/**
	 * Executes the behavior on the given agent without caching and without
	 * arguments.
	 *
	 * @param a    the agent
	 * @param args the arguments to pass to the behavior
	 * @throws Throwable if an error occurs during method invocation
	 */
	private void executeBehaviorNoCacheNoArg(Agent a, Object[] args) throws Throwable {
		if (a.isAlive()) {
			getMethodHandle(a.getClass(), args).invoke(a);
		}
	}

	/**
	 * Executes the behavior on the given list of agents with arguments.
	 *
	 * @param agents the list of agents
	 * @param args   the arguments to pass to the behavior
	 */
	private void exexecuteBehaviorsWithArgs(List<? extends Agent> agents, Object... args) {
		Object[] params = new Object[args.length + 1];
		System.arraycopy(args, 0, params, 1, args.length);
		for (Agent a : agents) {
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

	/**
	 * Refreshes the cached method handle for the given agent.
	 *
	 * @param a    the agent
	 * @param args the arguments to pass to the behavior
	 */
	private void refreshCaches(Agent a, Object... args) {
		Class<?> agentClass = a.getClass();
		if (cachedClass != agentClass) {
			cachedClass = agentClass;
			cachedMethod = getMethodHandle(cachedClass, args);
		}
	}

	/**
	 * Returns the method handle for the given agent class and arguments.
	 *
	 * @param agentClass the class of the agent
	 * @param args       the arguments to pass to the behavior
	 * @return the method handle
	 */
	private MethodHandle getMethodHandle(Class<?> agentClass, Object[] args) {
		return methods.computeIfAbsent(agentClass, c -> {
			try {
				if (argTypes == null) {
					argTypes = ReflectionUtils.convertArgToTypes(args);
				}
				return MethodHandleFinder.findMethodHandle(c, method, argTypes);
			} catch (NoSuchMethodException e) {
				throw new SimuException(toString(), e);
			}
		});
	}

	/**
	 * Returns the name of the method to be invoked on the agents.
	 *
	 * @return the method name
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * Returns whether the activation list should be shuffled before execution.
	 *
	 * @return true if the activation list should be shuffled, false otherwise
	 */
	public boolean isShufflingMode() {
		return shufflingMode;
	}

	/**
	 * Sets whether the activation list should be shuffled before execution. The
	 * shuffling mode is done using the PRNG defined by the Model agent. If the
	 * parallel mode is enabled, the shuffling mode has no effect.
	 *
	 * @param shufflingMode true if the activation list should be shuffled, false
	 *                      otherwise
	 */
	public void setShufflingMode(boolean shufflingMode) {
		this.shufflingMode = shufflingMode;
	}

	/**
	 * Returns whether the activator is in parallel mode.
	 * 
	 * @return <code>true</code> if the activator is in parallel mode,
	 *         <code>false</code> otherwise
	 */
	public boolean isParallelMode() {
		return parallelMode;
	}

	/**
	 * Sets the activator in parallel mode.
	 * 
	 * @param parallelMode the parallelMode to set
	 */
	public void setParallelMode(boolean parallelMode) {
		this.parallelMode = parallelMode;
	}
}
