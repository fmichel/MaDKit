package madkit.kernel;

import static java.util.logging.Level.FINEST;
import static madkit.kernel.Agent.ReturnCode.INVALID_AGENT_ADDRESS;
import static madkit.kernel.Agent.ReturnCode.NO_RECIPIENT_FOUND;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.random.RandomGenerator;

import it.unimi.dsi.fastutil.objects.Reference2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import javafx.scene.Scene;
import madkit.agr.SystemRoles;
import madkit.gui.FXExecutor;
import madkit.gui.FXOutputPane;
import madkit.messages.ConversationFilter;
import madkit.messages.EnumMessage;
import madkit.random.Randomness;
import madkit.reflection.MethodFinder;
import madkit.reflection.MethodHandleFinder;
import madkit.reflection.ReflectionUtils;
import madkit.simulation.SimuAgent;

/**
 * The super class of all MaDKit agents. It provides support for
 * <ul>
 * <li>Agent's Life cycle, logging, and naming.
 * <li>Agent launching and killing.
 * <li>Artificial society creation and management.
 * <li>Messaging.
 * <li>Minimal graphical interface management.
 * </ul>
 * <p>
 * The agent's behavior is <i>intentionally not defined</i>. It is up to the agent
 * developer to choose an agent model or to develop his specific agent library on top of
 * the facilities provided by the MaDKit API. However, all the launched agents share the
 * same organizational view, and the basic messaging code, so integration of different
 * agents is quite easy, even when they are coming from different developers or have
 * heterogeneous models.
 * <p>
 * An Agent will be given its own thread if it overrides the {@link #onLive()} method
 * <p>
 * Agent-related methods (most of this API) is only effective after the agent has been
 * launched and thus registered in the current MaDKit session. Especially, that means that
 * most of the API has no effect in the constructor method of an Agent and will just fail
 * if used.
 * <p>
 * Agents are identified and localized within the artificial society. An agent is no
 * longer
 * <p>
 * A replying mechanism can be used to answer messages using <code><i>SendReply</i></code>
 * methods. It enables the agent with the possibility of replying directly to a given
 * message. Also, it is possible to get the reply to a message, or to wait for a reply.
 * See {@link #reply(Message, Message)} for more details. <br>
 * <p>
 * One of the most convenient part of the API is the logging mechanism which is provided.
 * See the {@link #getLogger()} method for more details.
 *
 * @author Fabien Michel
 * @author Olivier Gutknecht
 * @version 6.0
 * @since MaDKit 6.0
 */
public abstract class Agent {

	private static final Reference2BooleanMap<Class<?>> threadedClasses = new Reference2BooleanArrayMap<>();
	private static final AtomicInteger agentCounter = new AtomicInteger(-1);

	private static final ExecutorService virtualThreadsExecutor = Executors.newVirtualThreadPerTaskExecutor();

	private final int hashCode;
	final AtomicBoolean alive = new AtomicBoolean(); // default false

	volatile KernelAgent kernel;

	private Mailbox mailbox;

	AgentLogger logger;

	/**
	 * Constructs a new Agent instance.
	 * <p>
	 * The agent's hashCode is set when the agent is created. This hashCode is unique for each
	 * agent instance and is used to uniquely identify the agent within the artificial
	 * society.
	 * <p>
	 * It is important to note that the agent is not yet alive at this point.
	 */
	protected Agent() {
		hashCode = agentCounter.getAndIncrement();
	}

	/**
	 * First method called when the agent is launched. This method should be overridden to
	 * define custom activation behavior.
	 */
	protected void onActivation() {
	}

	/**
	 * Defines the main behavior of the agent. This method should be overridden to define
	 * custom behavior while the agent is alive. It is automatically called when the
	 * onActivation method finishes.
	 * 
	 * If implemented, the agent will be given its own thread, thus making the agent
	 * completely autonomous. The agent will be alive until this method returns or it is
	 * killed.
	 */
	protected void onLive() {
	}

	/**
	 * This method is called when the agent finishes its life cycle. Either because the agent
	 * was killed or because the onLive method returned. This method can be overridden to
	 * define custom cleanup behavior.
	 */
	protected void onEnd() {
	}

	/**
	 * Checks if the agent class is threaded based on the presence of specific methods.
	 * 
	 * @return true if the agent class is threaded, false otherwise.
	 */
	final boolean isThreaded() {
		return threadedClasses.computeIfAbsent(getClass(), (Class<?> c) -> {
			while (c != Agent.class) {
				try {
					c.getDeclaredMethod("onLive");
					return true;
				} catch (NoSuchMethodException | SecurityException e) {
				}
				try {
					c.getDeclaredMethod("live");
					return true;
				} catch (NoSuchMethodException | SecurityException e) {
					c = c.getSuperclass();
				}
			}
			return false;
		});
	}

	/**
	 * Starts the agent life cycle asynchronously. Not convince that virtual threads are the
	 * best solution here.
	 * 
	 * @param activationPromise a CompletableFuture to complete upon activation.
	 */
	final void startAgentLifeCycle(CompletableFuture<ReturnCode> activationPromise) {
		CompletableFuture.runAsync(() -> activating(activationPromise), getExecutor());
	}

	/**
	 * Checks if the agent is alive. An agent is considered alive if it has been launched and
	 * has not yet been killed.
	 * 
	 * @return {@code true} if the agent is alive, {@code false} otherwise.
	 */
	public final boolean isAlive() {
		return alive.get();
	}

	/**
	 * Terminates the agent's life cycle. This method logs the termination event, closes the
	 * logger if it exists, and removes the agent from the organization.
	 */
	void terminate() {
		logIfLoggerNotNull(Level.FINER, () -> "- - -> TERMINATED **");
		if (logger != null) {
			logger.close();
		}
		synchronized (alive) {
			getOrganization().removeAgent(this);
			kernel = KernelAgent.deadKernel;
			alive.notifyAll();
		}
	}

	/**
	 * Logs a message if the logger is not null. This method is optimized for built-in method
	 * log.
	 * 
	 * @param level       the logging level.
	 * @param msgSupplier a supplier that provides the log message.
	 */
	void logIfLoggerNotNull(Level level, Supplier<String> msgSupplier) {
		if (logger != null) {
			logger.log(level, msgSupplier);
		}
	}

	/**
	 * Activates the agent asynchronously. This method sets the thread name, logs the
	 * activation event, and calls the {@link #onActivation()} method. It also manages the
	 * agent's lifecycle state and handles exceptions.
	 * 
	 * @param lifeCycle a CompletableFuture to complete upon activation.
	 */
	private void activating(CompletableFuture<ReturnCode> lifeCycle) {
		Thread.currentThread().setName(String.valueOf(hashCode()));
		logIfLoggerNotNull(Level.FINER, () -> "- - -> ACTIVATE...");
		try {
			onActivation();
		} catch (Exception e) {
			handleException(e);
			terminate();
			lifeCycle.complete(ReturnCode.AGENT_CRASH);
			return;
		}
		alive.set(true);
		lifeCycle.complete(ReturnCode.SUCCESS);
		if (isThreaded()) {
			if (!(this instanceof DaemonAgent)) {
				synchronized (kernel.threadedAgents) {
					kernel.threadedAgents.add(this);
				}
			}
			living();
			ending();
		} else {
			logIfLoggerNotNull(Level.FINER, () -> "** ACTIVATED **");
		}
	}

	/**
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * 
	 */
	private void randomizeFields() throws IllegalArgumentException, IllegalAccessException {
		if (!getKernelConfig().getBoolean(MDKCommandLine.NO_RANDOM)) {
			if (this instanceof SimuAgent sa) {
				Randomness.randomizeFields(this, sa.prng());
			} else {
				Randomness.randomizeFields(this, new Random());
			}
		}
	}

	/**
	 * Executes the agent's living behavior. This method logs the living event and calls the
	 * {@link #onLive()} method. It also handles any exceptions that may occur during
	 * execution.
	 */
	final void living() {
		logIfLoggerNotNull(Level.FINER, () -> "ACTIVATE - - -> LIVE...");
		try {
			onLive();
		} catch (Exception e) {
			handleException(e);
		}
		logIfLoggerNotNull(Level.FINER, () -> "LIVE - - -> END...");
	}

	/**
	 * Handles the agent's termination process when it is killed. This method sets the thread
	 * name and logs the killing event, then calls the {@link #onEnd()} method.
	 */
	final void killed() {
		Thread.currentThread().setName(String.valueOf(hashCode()));
		logIfLoggerNotNull(Level.FINER, () -> "** KILLED ** - - -> END...");
		ending();
	}

	/**
	 * Checks if the current agent's thread has been interrupted, for instance, due to a kill
	 * attempt. If it has, an {@link AgentInterruptedException} is thrown.
	 * 
	 * @throws AgentInterruptedException if the thread has been interrupted.
	 */
	protected void exitOnKill() throws AgentInterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new AgentInterruptedException();
		}
	}

	/**
	 * Handles the ending process of the agent. This method calls the {@link #onEnd()} method
	 * and sets the agent's alive state to false. It also terminates the agent's life cycle.
	 */
	final void ending() {
		try {
			onEnd();
		} catch (Exception e) {
			handleException(e);
		}
		alive.set(false);
		terminate();
	}

	/**
	 * Handles exceptions that occur during the agent's execution. If the exception is an
	 * instance of {@link AgentInterruptedException}, a fine log message is generated.
	 * Otherwise, a severe log message is created.
	 * 
	 * @param ex the Exception exception to handle.
	 */
	private final void handleException(Exception ex) {
		if (ex instanceof AgentInterruptedException) {
			getLogger().fine(() -> "** INTERRUPTED **");
		} else {
			getLogger().log(Level.SEVERE, ex, () -> "** CRASHED **");
		}
	}

	/**
	 * Launches a new agent and waits until the launched agent has completed its
	 * {@link Agent#onActivation()} method. This has the same effect as
	 * {@link #launchAgent(Agent, int)} but with a default timeout of
	 * {@link Integer#MAX_VALUE}.
	 * 
	 * @param a the agent to launch.
	 * @return the result of the launch operation.
	 */
	public ReturnCode launchAgent(Agent a) {
		return launchAgent(a, Integer.MAX_VALUE);
	}

	/**
	 * Launches a new agent and waits until the launched agent has completed its
	 * {@link Agent#onActivation()} method or until the timeout is elapsed.
	 * <p>
	 * The launch is logged at the {@link Level#FINER} level.
	 * 
	 * 
	 * @param a              the agent to launch.
	 * @param timeOutSeconds the maximum time to wait for the agent to launch.
	 * @return the result of the launch operation.
	 */
	public ReturnCode launchAgent(Agent a, int timeOutSeconds) {
		Randomness.randomizeFields(a, prng());
		return kernel.launchAgent(a, timeOutSeconds);
	}

	public RandomGenerator prng() {
		return kernel.getPRNG();
	}

	/**
	 * Launches a new agent using its full class name and returns when the launched agent has
	 * completed its {@link Agent#onActivation()} method or when the timeout is elapsed. This
	 * has the same effect as {@link #launchAgent(Agent, int)} but allows launching an agent
	 * using a class name found reflexively. The targeted agent class should have a default
	 * constructor for this to work. Additionally, this method will launch the last compiled
	 * bytecode of the corresponding class if it has been reloaded using
	 * {@link MadkitClassLoader#reloadClass(String)}. Finally, if the launch succeeds within
	 * the timeout, this method returns the instance of the created agent.
	 *
	 * @param <T>            the type of the agent to launch.
	 * @param timeOutSeconds time to wait for the end of the agent's activation until
	 *                       returning {@code null}.
	 * @param agentClass     the full class name of the agent to launch.
	 * @return the instance of the launched agent or {@code null} if the operation times out
	 *         or fails.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Agent> T launchAgent(String agentClass, int timeOutSeconds) {
		logIfLoggerNotNull(Level.FINEST, () -> Words.LAUNCH + " " + agentClass);
		Agent a = MadkitClassLoader.getAgentInstance(agentClass);
		if (ReturnCode.SUCCESS == launchAgent(a, timeOutSeconds)) {
			return (T) a;
		}
		return null;
	}

	/**
	 * Kills the specified agent, waiting forever for this agent to end.
	 * 
	 * @param a the agent to be killed.
	 * @return the result of the kill operation.
	 */
	protected ReturnCode killAgent(Agent a) {
		return killAgent(a, Integer.MAX_VALUE);
	}

	/**
	 * Kills the specified agent with a specified timeout.
	 * 
	 * @param agent          the agent to be killed.
	 * @param timeOutSeconds the maximum time to wait for the agent to be killed.
	 * @return the result of the kill operation.
	 */
	protected ReturnCode killAgent(Agent agent, int timeOutSeconds) {
		if (agent.alive.compareAndSet(true, false)) {
			return kernel.killAgent(agent, timeOutSeconds);
		}
		return ReturnCode.NOT_YET_LAUNCHED;
	}

	/**
	 * @deprecated Use {@link #onActivation()} instead.
	 */
	@Deprecated(since = "6", forRemoval = true)
	protected void activate() {
		onActivation();
	}

	/**
	 * @deprecated Use {@link #onLive()} instead.
	 */
	@Deprecated(since = "6", forRemoval = true)
	protected void live() {
		onLive();
	}

	/**
	 * @deprecated Use {@link #onEnd()} instead.
	 */
	@Deprecated(since = "6", forRemoval = true)
	protected void end() {
		onEnd();
	}

	/**
	 * Gets the executor for the agent.
	 * 
	 * @return the executor associated with the agent.
	 */
	Executor getExecutor() {
		return kernel.getAgentExecutor(this);
	}

	/**
	 * Returns the agent's logger. It should not be used before {@link #onActivation()} to get
	 * the proper logging level, that is the one set using "agentLogLevel".
	 *
	 * @return the agent's logger.
	 * @see AgentLogger
	 * @since MaDKit 5.0.0.6
	 */
	public AgentLogger getLogger() {
		if (logger == null) {
			logger = kernel != null && getKernelConfig().getBoolean("noLog") ? AgentLogger.NO_LOG_LOGGER
					: new AgentLogger(this);
		}
		return logger;
	}

	/**
	 * Stops the agent's process for a specified duration.
	 * 
	 * @param milliSeconds the number of milliseconds for which the agent should pause.
	 */
	protected void pause(int milliSeconds) {
		try {
			Thread.sleep(milliSeconds);
		} catch (InterruptedException e) {
			throw new AgentInterruptedException();
		}
	}

	/**
	 * Gets the agent's name. The default name is "<i>class name + internal ID</i>". This name
	 * is used in logger info, GUI title, and so on. This method can be overridden to obtain a
	 * customized name.
	 *
	 * @return the agent's name.
	 */
	public String getName() {
		return getClass().getSimpleName() + '-' + hashCode;
	}

	/**
	 * Gets the agent's hash code. This hash code is unique for each agent instance. It is
	 * equal to the agent's internal ID, which is incremented for each agent created.
	 *
	 * @return the agent's hash code.
	 */
	@Override
	public int hashCode() { // NOSONAR Super version of equals is perfect here
		return hashCode;
	}

	/**
	 * Gets the kernel configuration associated with the agent.
	 * 
	 * @return the kernel configuration.
	 */
	public KernelConfig getKernelConfig() {
		return kernel.getKernelConfig();
	}

	// ///////////////////////////////////////////////////////// GROUP & ROLE
	// METHODS AGR

	/**
	 * Creates a new Group within a community. This has the same effect as
	 * <code>createGroup(community, group, false, null)</code>
	 *
	 * @param community the community within which the group will be created. If this
	 *                  community does not exist it will be created.
	 * @param group     the name of the new group
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the group has been successfully
	 *         created.</li>
	 *         <li><code>{@link ReturnCode#ALREADY_GROUP}</code>: If the operation failed
	 *         because such a group already exists.</li>
	 *         </ul>
	 * @see Agent#createGroup(String, String, boolean, Gatekeeper)
	 * @since MaDKit 5.0
	 */
	public ReturnCode createGroup(String community, String group) {
		return createGroup(community, group, false, null);
	}

	/**
	 * Creates a new Group within a community. This has the same effect as
	 * <code>createGroup(community, group, isDistributed, null)</code>
	 *
	 * @param community     the community within which the group will be created. If this
	 *                      community does not exist it will be created.
	 * @param group         the name of the new group.
	 * @param isDistributed if <code>true</code> the new group will be distributed when
	 *                      multiple MaDKit kernels are connected.
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the group has been successfully
	 *         created.</li>
	 *         <li><code>{@link ReturnCode#ALREADY_GROUP}</code>: If the operation failed
	 *         because such a group already exists.</li>
	 *         </ul>
	 * @see Agent#createGroup(String, String, boolean, Gatekeeper)
	 * @since MaDKit 5.0
	 */
	public ReturnCode createGroup(String community, String group, boolean isDistributed) {
		return createGroup(community, group, isDistributed, null);
	}

	/**
	 * Gets the organization. This method should not be used before the agent has been
	 * launched. The organization is the agent's view of the artificial society.
	 *
	 * @return the organization to which the agent is attached.
	 */
	public Organization getOrganization() {
		return kernel.getOrganization();
	}

	/**
	 * Creates a new Group within a community.
	 * <p>
	 * If this operation succeed, the agent will automatically handle the role defined by
	 * {@link SystemRoles#GROUP_MANAGER_ROLE}, which value is <i>
	 * {@value madkit.agr.SystemRoles#GROUP_MANAGER_ROLE}</i>, in this created group.
	 * Especially, if the agent leaves the role of <i>
	 * {@value madkit.agr.SystemRoles#GROUP_MANAGER_ROLE}</i>, it will also automatically
	 * leave the group and thus all the roles it has in this group.
	 * <p>
	 * Agents that want to enter the group may send messages to the <i>
	 * {@value madkit.agr.SystemRoles#GROUP_MANAGER_ROLE}</i> using the role defined by
	 * {@link SystemRoles#GROUP_CANDIDATE_ROLE}, which value is <i>
	 * {@value madkit.agr.SystemRoles#GROUP_CANDIDATE_ROLE}</i>.
	 *
	 * @param community     the community within which the group will be created. If this
	 *                      community does not exist it will be created.
	 * @param group         the name of the new group.
	 * @param isDistributed if <code>true</code> the new group will be distributed when
	 *                      multiple MaDKit kernels are connected.
	 * @param keyMaster     any object that implements the {@link Gatekeeper} interface. If
	 *                      not <code>null</code>, this object will be used to check if an
	 *                      agent can be admitted in the group. When this object is null,
	 *                      there is no group access control.
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the group has been successfully
	 *         created.</li>
	 *         <li><code>
	 *         {@link ReturnCode#ALREADY_GROUP}</code>: If the operation failed because such a
	 *         group already exists.</li>
	 *         </ul>
	 * @see Gatekeeper
	 * @see ReturnCode
	 * @since MaDKit 5.0
	 */
	public ReturnCode createGroup(String community, String group, boolean isDistributed, Gatekeeper keyMaster) {
		return kernel.createGroup(this, community, group, keyMaster, isDistributed);
	}

	/**
	 * Requests a role within a group of a particular community. This has the same effect as
	 * <code>requestRole(community, group, role, null, false)</code>. So the passKey is
	 * <code>null</code> and the group must not be secured for this to succeed.
	 *
	 * @param community the group's community.
	 * @param group     the targeted group.
	 * @param role      the desired role.
	 * @see #requestRole(String, String, String, Object)
	 * @return the result of the request operation.
	 * @since MaDKit 5.0
	 */
	public ReturnCode requestRole(String community, String group, String role) {
		return requestRole(community, group, role, null);
	}

	/**
	 * Requests a role within a group of a particular community using a passKey.
	 *
	 * @param community the group's community.
	 * @param group     the targeted group.
	 * @param role      the desired role.
	 * @param passKey   the <code>passKey</code> to enter a secured group. It is generally
	 *                  delivered by the group's <i>group manager</i>. It could be
	 *                  <code>null</code>, which is sufficient to enter an unsecured group.
	 *                  Especially, {@link #requestRole(String, String, String)} uses a
	 *                  <code>null</code> <code>passKey</code>.
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the operation has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#ROLE_ALREADY_HANDLED}</code>: If this role is
	 *         already handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#ACCESS_DENIED}</code>: If the access denied by the
	 *         manager of that secured group.</li>
	 *         </ul>
	 * @see Agent.ReturnCode
	 * @see Gatekeeper
	 * @since MaDKit 5.0
	 */
	public ReturnCode requestRole(String community, String group, String role, Object passKey) {
		return kernel.requestRole(this, community, group, role, passKey);
	}

	/**
	 * Makes this agent leaves the group of a particular community.
	 *
	 * @param community the community name
	 * @param group     the group name
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the operation has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is not a member
	 *         of this group.</li>
	 *         </ul>
	 * @since MaDKit 5.0
	 * @see ReturnCode
	 */
	public ReturnCode leaveGroup(String community, String group) {
		return kernel.leaveGroup(this, community, group);
	}

	/**
	 * Abandons an handled role within a group of a particular community.
	 *
	 * @param community the community name
	 * @param group     the group name
	 * @param role      the role name
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the operation has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#ROLE_NOT_HANDLED}</code>: If this role is not
	 *         handled by this agent.</li>
	 *         </ul>
	 * @see Agent.ReturnCode
	 * @since MaDKit 5.0
	 */
	public ReturnCode leaveRole(String community, String group, String role) {
		return kernel.leaveRole(this, community, group, role);
	}

	/**
	 * Returns an {@link AgentAddress} corresponding to an agent having this position in the
	 * organization. The caller is excluded from the search.
	 *
	 * @param community the community name
	 * @param group     the group name
	 * @param role      the role name
	 * @return an {@link AgentAddress} corresponding to an agent handling this role or
	 *         <code>null</code> if such an agent does not exist.
	 */
	public AgentAddress getAgentWithRole(String community, String group, String role) {
		try {
			return getOrganization().getRole(community, group, role).getAnotherAddress(this);
		} catch (CGRNotAvailable e) {
			return null;
		}
	}

	/**
	 * Returns a string representing a unique identifier for the agent over the network.
	 *
	 * @return the agent's network identifier
	 */
	public final String getNetworkID() {
		return hashCode + "@" + getKernelAddress().hashCode();
	}

	@Override
	public String toString() {
		return getName();
	}

	//////////////////////////////////////////////// MESSAGING
	private final AgentAddress getActualAddress(AgentAddress receiver) {
		Role roleObject = receiver.getRoleObject();
		if (roleObject != null) {
			if (roleObject.players == null) {// has been traveling
				return roleObject.resolveDistantAddress(receiver);
			}
			return receiver;
		}
		return null;
	}

	/**
	 * Kills the caller and launches a new instance of this agent using the latest byte code
	 * available for the corresponding class.
	 */
	public void reload() {
		try {
			MadkitClassLoader.reloadClass(getClass().getName());
			Agent a = MadkitClassLoader.getAgentInstance(getClass().getName());
			launchAgent(a, 0);
		} catch (IllegalArgumentException | SecurityException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		killAgent(this, 1);
	}

	/**
	 * Sends a message, using an agent address, specifying explicitly the role used to send
	 * it.
	 * 
	 * @param message    the message to send
	 * @param receiver   the targeted agent
	 * @param senderRole the agent's role with which the message has to be sent
	 *
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is not a member
	 *         of the receiver's group.</li>
	 *         <li><code>{@link ReturnCode#ROLE_NOT_HANDLED}</code>: If
	 *         <code>senderRole</code> is not handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AGENT_ADDRESS}</code>: If the receiver
	 *         address is no longer valid. This is the case when the corresponding agent has
	 *         leaved the role corresponding to the receiver agent address.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @see AgentAddress
	 */
	public ReturnCode sendWithRole(Message message, AgentAddress receiver, String senderRole) {
		AgentAddress target = getActualAddress(receiver);
		if (target == null && !(receiver instanceof CandidateAgentAddress)) {
			return INVALID_AGENT_ADDRESS;
		}
		try {
			AgentAddress sender = kernel.getSenderAgentAddress(this, target, senderRole);
			return kernel.buildAndSendMessage(sender, receiver, message);
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
	}

	/**
	 * Sends a message to an agent using an agent address. This has the same effect as
	 * <code>sendWithRole(receiver, messageToSend, null)</code>.
	 * 
	 * @param message  the message to send
	 * @param receiver the targeted agent
	 *
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is not a member
	 *         of the receiver's group.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AGENT_ADDRESS}</code>: If the receiver
	 *         address is no longer valid. This is the case when the corresponding agent has
	 *         leaved the role corresponding to the receiver agent address.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @see AgentAddress
	 */
	public ReturnCode send(Message message, AgentAddress receiver) {
		return sendWithRole(message, receiver, null);
	}

	/**
	 * Sends a message to an agent having this position in the organization, specifying
	 * explicitly the role used to send it. This has the same effect as
	 * sendMessageWithRole(community, group, role, messageToSend,null). If several agents
	 * match, the target is chosen randomly. The sender is excluded from this search.
	 * 
	 * @param message   the message to send
	 * @param community the community name
	 * @param group     the group name
	 * @param role      the role name
	 *
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is not a member
	 *         of the targeted group.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent was found
	 *         as recipient, i.e. the sender was the only agent having this role.</li>
	 *         </ul>
	 * @see ReturnCode
	 */
	public ReturnCode send(Message message, String community, String group, String role) {
		return sendWithRole(message, community, group, role, null);
	}

	/**
	 * Sends a message to an agent having this position in the organization. This has the same
	 * effect as <code>sendMessageWithRole(community, group, role, messageToSend,null)</code>
	 * . If several agents match, the target is chosen randomly. The sender is excluded from
	 * this search.
	 * 
	 * @param message    the message to send
	 * @param community  the community name
	 * @param group      the group name
	 * @param role       the role name
	 * @param senderRole the agent's role with which the message has to be sent
	 *
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not exist.</li>
	 *         <li><code>{@link ReturnCode#ROLE_NOT_HANDLED}</code>: If
	 *         <code>senderRole</code> is not handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is not a member
	 *         of the targeted group.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent was found
	 *         as recipient, i.e. the sender was the only agent having this role.</li>
	 *         </ul>
	 * @see ReturnCode
	 */
	public ReturnCode sendWithRole(Message message, String community, String group, String role, String senderRole) {
		try {
			AgentAddress receiver = getOrganization().getRole(community, group, role).getAnotherAddress(this);
			if (receiver == null) {
				return NO_RECIPIENT_FOUND;
			}
			return sendWithRole(message, receiver, senderRole);
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
	}

	/**
	 * Sends a message to an agent having this position in the organization and waits for an
	 * answer to it. The targeted agent is selected randomly among matched agents. The sender
	 * is excluded from this search.
	 * 
	 * @param community           the community name
	 * @param group               the group name
	 * @param role                the role name
	 * @param messageToSend       the message to send.
	 * @param senderRole          the role with which the sending is done.
	 * @param timeOutMilliSeconds the maximum time to wait. If <code>null</code> the agent
	 *                            will wait indefinitely.
	 * @param <T>                 the type of the message to get
	 * @return the reply received as soon as available, or <code>null</code> if the time out
	 *         has elapsed or if there was an error when sending the message.
	 * @since MaDKit 5
	 */
	protected <T extends Message> T sendWithRoleWaitReply(Message messageToSend, String community, String group,
			String role, String senderRole, Integer timeOutMilliSeconds) {
//		if(logger != null)
//			logger.finest(() -> "sendWithRoleWaitReply : sending "+messageToSend+" to any "+I18nUtilities.getCGRString(community, group, role)+
//					(timeOutMilliSeconds == null ? "":", and waiting reply for "+TimeUnit.MILLISECONDS.toSeconds(timeOutMilliSeconds)+" s..."));
		if (sendWithRole(messageToSend, community, group, role, senderRole) != SUCCESS) {
			return null;// TODO log
		}
		return waitAnswer(messageToSend, timeOutMilliSeconds);
	}

	/**
	 * Sends a message and waits for an answer to it. Additionally, the sending is done using
	 * a specific role for the sender.
	 * 
	 * @param messageToSend       the message to send.
	 * @param receiver            the targeted agent by the send.
	 * @param senderRole          the role with which the sending is done.
	 * @param timeOutMilliSeconds the maximum time to wait. If <code>null</code> the agent
	 *                            will wait indefinitely.
	 * @param <T>                 the type of the message to get
	 * 
	 * @return the reply received as soon as available, or <code>null</code> if the time out
	 *         has elapsed or if there was an error when sending the message,
	 * 
	 * @since MaDKit 5
	 */
	protected <T extends Message> T sendWithRoleWaitReply(Message messageToSend, AgentAddress receiver,
			String senderRole, Integer timeOutMilliSeconds) {
		// no need to checkAliveness : this is done in noLogSendingMessage
		logIfLoggerNotNull(FINEST, () -> "sendMessageAndWaitForReply : sending " + messageToSend + " to " + receiver
				+ ", and waiting reply...");
		if (getKernel().sendMessage(this, receiver, messageToSend, senderRole) != SUCCESS) {
			return null;
		}
		return waitAnswer(messageToSend, timeOutMilliSeconds);
	}

	/**
	 * Sends a message to an agent having this position in the organization and waits for an
	 * answer to it. The targeted agent is selected randomly among matched agents. The sender
	 * is excluded from this search.
	 * 
	 * @param community           the community name
	 * @param group               the group name
	 * @param role                the role name
	 * @param messageToSend       the message to send.
	 * @param timeOutMilliSeconds the maximum time to wait. If <code>null</code> the
	 * @param <T>                 the type of the message to get agent will wait indefinitely.
	 * @return the reply received as soon as available, or <code>null</code> if the time out
	 *         has elapsed or if there was an error when sending the message.
	 * @since MaDKit 5
	 */
	protected <T extends Message> T sendWaitReply(Message messageToSend, String community, String group, String role,
			Integer timeOutMilliSeconds) {
		return sendWithRoleWaitReply(messageToSend, community, group, role, null, timeOutMilliSeconds);
	}

	/**
	 * Broadcasts a message to every agent having a role in a group in a community using a
	 * specific role for the sender. The sender is excluded from the search.
	 *
	 * @param message   message to send
	 * @param receivers list of agent addresses to send the message to
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is not a member
	 *         of the targeted group.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent was found
	 *         as recipient, i.e. the sender was the only agent having this role.</li>
	 *         </ul>
	 * @see ReturnCode
	 */
	public ReturnCode broadcast(Message message, List<AgentAddress> receivers) {
		return broadcastWithRole(message, receivers, null);
	}

	/**
	 * Broadcasts a message to every agent having a role in a group in a community using a
	 * specific role for the sender. The sender is excluded from the search.
	 *
	 * @param message   message to send
	 * @param receivers list of agent addresses to send the message to
	 * @param role      the agent's role with which the message should be sent
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is not a member
	 *         of the targeted group.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent was found
	 *         as recipient, i.e. the sender was the only agent having this role.</li>
	 *         </ul>
	 * @see ReturnCode
	 */
	public ReturnCode broadcastWithRole(Message message, List<AgentAddress> receivers, String role) {
		if (receivers.isEmpty())
			return NO_RECIPIENT_FOUND;
		return kernel.broadcastMessageWithRole(this, receivers, message, role);
	}

	/**
	 * Broadcasts a message and wait for answers considering a timeout duration.
	 * 
	 * @param <T>                 the type of the message to get
	 * @param message             the message to broadcast
	 * @param receivers           the list of agent addresses to send the message to
	 * @param senderRole          the agent's role with which the message should be
	 * @param timeOutMilliSeconds the maximum time to wait.
	 * 
	 * @return a list of messages which are answers to the <code>message</code> which has been
	 *         broadcasted.
	 */
	protected <T extends Message> List<T> broadcastWithRoleWaitForReplies(Message message, List<AgentAddress> receivers,
			String senderRole, Integer timeOutMilliSeconds) {
		if (broadcastWithRole(message, receivers, senderRole) != SUCCESS) {
			return Collections.emptyList();
		}
		return getMailbox().waitAnswers(message, receivers.size(), timeOutMilliSeconds);
	}
//	catch(CGRNotAvailable e) {
//	    ReturnCode r = e.getCode();
//	    if (r == NO_RECIPIENT_FOUND) {
//		requester.handleWarning(Influence.BROADCAST_MESSAGE_AND_WAIT, () -> new MadkitWarning(r));
//	    }
//	    else if (r == ROLE_NOT_HANDLED) {
//		requester.handleWarning(Influence.BROADCAST_MESSAGE_AND_WAIT, () -> new OrganizationWarning(r, community, group, senderRole));
//	    }
//	    else {
//		requester.handleWarning(Influence.BROADCAST_MESSAGE_AND_WAIT, () -> new OrganizationWarning(r, community, group, role));
//	    }
//	    return null;
//	}

	/**
	 * Returns a list of agent addresses corresponding to agents having this role in the
	 * organization. The sender is excluded from this search.
	 * 
	 * @param community the community name
	 * @param group     the group name
	 * @param role      the role name
	 * @return a list of agent addresses corresponding to agents handling this role
	 */
	public List<AgentAddress> getAgentsWithRole(String community, String group, String role) {
		try {
			Role targetedRole = getOrganization().getRole(community, group, role);
			return targetedRole.getOtherRolePlayers(this);
		} catch (CGRNotAvailable e) {// LOg why
			return Collections.emptyList();
		}
	}

	/**
	 * Checks if this agent address is still valid. <i>I.e.</i> the corresponding agent is
	 * still playing this role.
	 *
	 * @param agentAddress the agent address to check
	 * @return <code>true</code> if the address still exists in the organization.
	 * @since MaDKit 5.0.4
	 */
	public boolean checkAgentAddress(AgentAddress agentAddress) {
		return kernel.getActualAddress(agentAddress) != null;
	}

//    /**
//     * Sends a message to an agent having this position in the organization, specifying explicitly the role used to send it.
//     * This has the same effect as sendMessageWithRole(community, group, role, messageToSend,null). If several agents match,
//     * the target is chosen randomly. The sender is excluded from this search.
//     *
//     * @param community
//     *            the community name
//     * @param group
//     *            the group name
//     * @param role
//     *            the role name
//     * @param message
//     *            the message to send
//     * @return
//     *         <ul>
//     *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has succeeded.</li>
//     *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community does not exist.</li>
//     *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not exist.</li>
//     *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not exist.</li>
//     *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is not a member of the targeted group.</li>
//     *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent was found as recipient, i.e. the sender
//     *         was the only agent having this role.</li>
//     *         </ul>
//     * @see ReturnCode
//     */
//    public ReturnCode sendMessage(String community, String group, String role, Message message) {
//	return sendWithRole(community, group, role, message, null);
//    }

	/**
	 * Sends a message by replying to a previously received message. The sender is excluded
	 * from this search.
	 * 
	 * @param reply            the reply itself.
	 * @param messageToReplyTo the previously received message.
	 * @param senderRole       the agent's role with which the message should be sent
	 *
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is no longer a
	 *         member of the corresponding group.</li>
	 *         <li><code>{@link ReturnCode#ROLE_NOT_HANDLED}</code>: If
	 *         <code>senderRole</code> is not handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AGENT_ADDRESS}</code>: If the receiver
	 *         address is no longer valid. This is the case when the corresponding agent has
	 *         leaved the role corresponding to the receiver agent address.</li>
	 *         </ul>
	 * @see ReturnCode
	 */
	public ReturnCode replyWithRole(Message reply, Message messageToReplyTo, String senderRole) {
		AgentAddress target = messageToReplyTo.getSender();
		if (target == null)
			return ReturnCode.CANT_REPLY;
		reply.setIDFrom(messageToReplyTo);
		return kernel.sendMessage(this, target, reply, senderRole);
	}

	/**
	 * Sends a message by replying to a previously received message. This has the same effect
	 * as <code>sendReplyWithRole(messageToReplyTo, reply, null)</code>.
	 * 
	 * @param reply            the reply itself.
	 * @param messageToReplyTo the previously received message.
	 *
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the reply has succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is no longer a
	 *         member of the corresponding group.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AGENT_ADDRESS}</code>: If the receiver
	 *         address is no longer valid. This is the case when the corresponding agent has
	 *         leaved the role corresponding to the receiver agent address.</li>
	 *         </ul>
	 * @see Agent#replyWithRole(Message, Message, String)
	 */
	public ReturnCode reply(Message reply, Message messageToReplyTo) {
		return replyWithRole(reply, messageToReplyTo, null);
	}

	/**
	 * Gets the next message which is a reply to the <i>originalMessage</i>.
	 *
	 * @param <T>             the type of the message to get
	 * @param originalMessage the message to which a reply is searched.
	 * @return a reply to the <i>originalMessage</i> or <code>null</code> if no reply to this
	 *         message has been received.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Message> T getReplyTo(Message originalMessage) {
		return (T) getMailbox().next(new ConversationFilter(originalMessage));
	}

	//////////////////////////////////// WAIT
	/**
	 * This method is the blocking version of nextMessage(). If there is no message in the
	 * mailbox, it suspends the agent life until a message is received
	 *
	 * @param <T> the type of the message to get
	 * @see #waitNextMessage(long)
	 * @return the first received message
	 */
	protected <T extends Message> T waitNextMessage() {
		getLogger().finest(() -> "waitNextMessage...");
		T m = getMailbox().waitNext();
		getLogger().finest(() -> "..." + Words.NEW_MSG + ": " + m);
		return m;
	}

	/**
	 * This method gets the next message of the mailbox or waits for a new incoming message
	 * considering a certain delay.
	 * 
	 * @param <T>                 the type of the message to get
	 * @param timeOutMilliseconds the maximum time to wait, in milliseconds.
	 * 
	 * @return the first message in the mailbox, or <code>null</code> if no message has been
	 *         received before the time out delay is elapsed
	 */
	@SuppressWarnings("unchecked")
	protected <T extends Message> T waitNextMessage(long timeOutMilliseconds) {
		getLogger().finest(() -> "Waiting next message during " + timeOutMilliseconds + " milliseconds...");
		Message m = getMailbox().waitNext(timeOutMilliseconds);
		if (m != null)
			getLogger().finest(() -> "waitNextMessage->" + Words.NEW_MSG + ": " + m);
		else
			getLogger().finest(() -> "waitNextMessage time out !");
		return (T) m;
	}

	/**
	 * Retrieves and removes the next message that is a reply to the query message, waiting
	 * for ever if necessary until a matching reply becomes available.
	 * 
	 * @param query the message for which a reply is waited for
	 * @param <T>   the type of the message to get
	 * 
	 * @return the first reply to the query message
	 * @since MadKit 5.0.4
	 */
	@SuppressWarnings("unchecked")
	protected <T extends Message> T waitAnswer(Message query) {
		return (T) getMailbox().waitNext(new ConversationFilter(query));
	}

	/**
	 * Retrieves and removes the next message that is a reply to the query message, waiting
	 * for ever if necessary until a matching reply becomes available.
	 * 
	 * @param query               the message for which a reply is waited for
	 * @param timeOutMilliSeconds the maximum time to wait, in milliseconds.
	 * @param <T>                 the type of the message to get
	 * 
	 * @return the first reply to the query message
	 * @since MadKit 5.0.4
	 */
	@SuppressWarnings("unchecked")
	protected <T extends Message> T waitAnswer(Message query, Integer timeOutMilliSeconds) {
		return (T) getMailbox().waitNext(timeOutMilliSeconds, new ConversationFilter(query));
	}

	/**
	 * This method offers a convenient way for regular object to send messages to Agents,
	 * especially threaded agents. For instance when a GUI wants to discuss with its linked
	 * agent: This allows to enqueue work to do in their life cycle
	 *
	 * @param message the message to send
	 */
	public void receiveMessage(Message message) {
		getMailbox().add(message);
	}

	private KernelAgent getKernel() {
		return kernel;
	}

	/**
	 * Returns the agent's mailbox
	 * 
	 * @return the mailbox
	 */
	protected Mailbox getMailbox() {
		if (mailbox == null) {
			mailbox = new Mailbox();
		}
		return mailbox;
	}

	/**
	 * Creates a default frame for the agent. It is made of a {@link FXAgentStage} containing
	 * a {@link FXOutputPane}
	 * 
	 */
	public void setupDefaultGUI() {
		FXExecutor.runAndWait(() -> {
			FXAgentStage stage = new FXAgentStage(this);
			Scene scene = new Scene(new FXOutputPane(this));
			stage.setScene(scene);
			stage.show();
		});
	}

	/**
	 * This class enumerates all the return codes which could be obtained with essential
	 * methods of the {@link Agent} and {@link Agent} classes.
	 *
	 * @author Fabien Michel
	 * @since MaDKit 5.0
	 */
	public enum ReturnCode {

		/**
		 * Indicates that the operation has succeeded
		 */
		SUCCESS,
		/**
		 * Indicates that a community does not exist
		 */
		NOT_COMMUNITY,
		/**
		 * Indicates that a group does not exist
		 */
		NOT_GROUP,
		/**
		 * Indicates that a role does not exist
		 */
		NOT_ROLE,
		/**
		 * Indicates that the agent is not in a group
		 */
		NOT_IN_GROUP,
		// TERMINATED_AGENT,
		/**
		 * Returned when the agent already has the requested role
		 */
		ROLE_ALREADY_HANDLED,
		/**
		 * Returned when requesting a role in a secured group fails
		 */
		ACCESS_DENIED,
		/**
		 * Returned when the agent does not have a role that it is supposed to have doing a
		 * particular action, e.g. {@link Agent#sendWithRole(Message, AgentAddress, String)}
		 */
		ROLE_NOT_HANDLED,
		/**
		 * Returned when using {@link Agent#createGroup(String, String, boolean, Gatekeeper)} and
		 * that a group already exists
		 */
		ALREADY_GROUP,
		/**
		 * Returned when launching an agent which is already launched
		 */
		ALREADY_LAUNCHED,
		/**
		 * Returned by various timed primitives of the Agent class like
		 * {@link Agent#sendWaitReply(Message, String, String, String, Integer)} or
		 * {@link Agent#launchAgent(Agent, int)}
		 */
		TIMEOUT,
		/**
		 * Returned by launch primitives when the launched agent crashes in activate
		 */
		AGENT_CRASH,
		// NOT_AN_AGENT_CLASS,
		/**
		 * Returned by kill primitives when the targeted agent has not been launched priorly
		 */
		NOT_YET_LAUNCHED,
		/**
		 * Returned by kill primitives when the targeted agent is already terminated
		 */
		ALREADY_KILLED,
		/**
		 * Returned by send primitives when the targeted agent address does not exist anymore,
		 * i.e. the related agent has leaved the corresponding role
		 */
		INVALID_AGENT_ADDRESS,
		/**
		 * Returned by send primitives when the targeted CGR location does not exist or contain
		 * any agent
		 */
		NO_RECIPIENT_FOUND,
//		/**
//		 * Returned when {@link Agent#requestRole(String, String, String, Object)} or
//		 * {@link Agent#createGroup(String, String, boolean, Gatekeeper)} is used in
//		 * activate and that the agent has been launched using
//		 * {@link Agent#launchAgentBucket(List, String...)} or
//		 * {@link Agent#launchAgentBucket(String, int, String...)}</li>
//		 */
//		IGNORED,
		/**
		 * Returned when an agent tries to reply to a message which has not been received from
		 * another agent, e.g. newly created or sent directly by an object using
		 * {@link Agent#receiveMessage(Message)}.
		 */
		CANT_REPLY,
		/**
		 * Returned on special errors. This should not be encountered
		 */
		SEVERE;

		// NETWORK_DOWN;

//	static final ResourceBundle messages = I18nUtilities.getResourceBundle(ReturnCode.class.getSimpleName());

		// static ResourceBundle messages =
		// I18nUtilities.getResourceBundle(ReturnCode.class);

//	@Override
//	public String toString() {
//	    return messages.getString(name());
//	}
	}

	final void logMethod(String lifeCycleMethod, boolean entering) {
		getLogger().finer(() -> "** " + (entering ? Words.ENTERING : Words.EXITING) + " " + lifeCycleMethod + " **");
	}

	/**
	 * The kernel's address on which this agent is running.
	 *
	 * @return the kernel address representing the MaDKit kernel on which the agent is running
	 */
	public KernelAddress getKernelAddress() {
		return kernel.kernelAddress;
	}

	/**
	 * Retrieves the next message from the mailbox.
	 *
	 * <p>
	 * It returns the next message of type {@code M} from the mailbox. The type parameter
	 * {@code M} must extend the {@link Message} class, allowing for flexibility in the types
	 * of messages that can be processed.
	 * </p>
	 *
	 * @param <M> the type of the message to get
	 * @return the next message of type {@code M} from the mailbox.
	 */
	public <M extends Message> M nextMessage() {
		return getMailbox().next();
	}

	//////////////////////////////////////////// LAUNCHING

	/**
	 * This offers a convenient way to create main a main method that launches the agent class
	 * under development. The agent is launched in a new instance MaDKit. This call only works
	 * in the main method of the agent's class. MaDKit. Here is an example of use that will
	 * work in any subclass of {@link Agent}:
	 *
	 * To launch 10 instances of an agent, you can use:
	 * 
	 * <pre>
	 *                     <code>
	 *                     public static void main(String[] args) {
	 *                     executeThisAgent(10, args);
	 *                     }
	 * </code>
	 * </pre>
	 *
	 * Still, the agent must have a default constructor for that to work.
	 *
	 * @param nbOfInstances specify how many of this kind should be launched
	 * @param args          MaDKit options.
	 * 
	 * @return the kernel instance that actually launches this agent, so that it is possible
	 *         to do other actions after the launch
	 * @since MaDKit 5.0.0.14
	 */
	protected static Madkit executeThisAgent(int nbOfInstances, String... args) {
		final List<String> arguments;
		if (args == null)
			arguments = new ArrayList<>();
		else
			arguments = new ArrayList<>(List.of(args));
		StackTraceElement element = Arrays.stream(new Throwable().getStackTrace())
				.filter(s -> s.getMethodName().equals("main")).findFirst().orElse(null);

		arguments.addAll(List.of("-la", element.getClassName() + "," + nbOfInstances));
		// NOSONAR just cannot be null
		Madkit.oneFileLauncher = element.getClassName();
		Madkit.oneFileLauncherArgs = args;
		return new Madkit(arguments.toArray(new String[arguments.size()]));
	}

	/**
	 * This offers a convenient way to create a main method that launches the agent class
	 * under development. This call only works in the main method of the agent's class. This
	 * call is equivalent to <code>executeThisAgent(1, true, args)</code>
	 *
	 * @param args MaDKit options
	 * @return the kernel instance that actually launches this agent, so that it is possible
	 *         to do other actions after the launch using {@link Madkit#launchAgent(Agent)}
	 * @see #executeThisAgent(int, String...)
	 * @since MaDKit 5.0.0.14
	 */
	protected static Madkit executeThisAgent(String... args) {
		return executeThisAgent(1, args);
	}

	////////////////////////////////////////////////////////// Reflection

	void executeBehavior(String methodName, Object... args) throws NoSuchMethodException {
		try {
			MethodHandleFinder.findMethodHandleFromArgs(getClass(), methodName, args).invoke(this, args);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Proceeds an {@link EnumMessage} so that if it is correctly built, the agent will
	 * trigger its corresponding behavior using the parameters of the message.
	 *
	 * @param message the message to proceed
	 * @param <E>     the type of the enum used to build the message
	 * @since MaDKit 5.0.0.14
	 * @see EnumMessage
	 */
	public <E extends Enum<E>> void proceedEnumMessage(EnumMessage<E> message) {
		logIfLoggerNotNull(FINEST, () -> "proceeding command message " + message);
		Object[] parameters = message.getContent();
		Method m = null;
		try {
			m = MethodFinder.getMethodOn(getClass(), ReflectionUtils.enumToMethodName(message.getCode()), parameters);
			m.invoke(this, parameters);
		} catch (NoSuchMethodException e) {
			if (logger != null)
				logger.warning(() -> "I do not know how to " + ReflectionUtils.enumToMethodName(message.getCode())
						+ Arrays.deepToString(parameters));
			logForSender(() -> "I have sent a message which has not been understood", message);
		} catch (IllegalArgumentException e) {
			if (logger != null)
				logger.warning("Cannot proceed message : wrong argument " + m);
			logForSender(() -> "I have sent an incorrect command message ", message);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			Throwable t = e.getCause();
			t.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void logForSender(Supplier<String> msg, EnumMessage<?> cm) {
		try {
			cm.getSender().getAgent().logger.warning(() -> msg.get() + cm);
		} catch (NullPointerException e1) {
			// logger is off or sender is null
		}
	}

}

enum Words {
	ENTERING, EXITING, NEW_MSG, LAUNCH;
}

enum Influence {
	CREATE_GROUP, REQUEST_ROLE, LEAVE_ROLE, LEAVE_GROUP, GET_AGENTS_WITH_ROLE, GET_AGENT_WITH_ROLE, SEND_MESSAGE,
	BROADCAST_MESSAGE, BROADCAST_MESSAGE_AND_WAIT, LAUNCH_AGENT, KILL_AGENT, GET_AGENT_ADDRESS_IN, RELOAD_CLASS;

	public String failedString() {
		return toString() + "failed : ";
	}

	@Override
	public String toString() {
		return name() + " ";
	}

	String successString() {
		return toString() + "success" + " : ";
	}

	/**
	 * This offers a convenient way to create main a main method that launches the agent class
	 * under development. The agent is launched in a new instance MaDKit. This call only works
	 * in the main method of the agent's class. MaDKit. Here is an example of use that will
	 * work in any subclass of {@link Agent}:
	 *
	 * <pre>
	 * <code>
	 * public static void main(String[] args) {
	 * 	executeThisAgent(args);
	 * }
	 * </code>
	 * </pre>
	 *
	 * Still, the agent must have a default constructor for that to work.
	 *
	 * @param nbOfInstances specify how many of this kind should be launched
	 * @param createFrame
	 * @param args          MaDKit options. For example, this will launch the agent in desktop
	 *                      mode :
	 *
	 *                      <pre>
	 *                      <code>
	 * public static void main(String[] args) {
	 * 	executeThisAgent(BooleanOption.desktop.toString());
	 * }
	 * </code>
	 * @return the kernel instance that actually launches this agent, so that it is possible
	 *         to do other actions after the launch using
	 *         {@link Madkit#doAction(madkit.action.KernelAction, Object...)}
	 * @see Option BooleanOption LevelOption
	 * @since MaDKit 5.0.0.14
	 */
//	protected static Madkit executeThisAgent(int nbOfInstances, boolean createFrame, String... args) {
//		
//		StackTraceElement element = null;
//		for (StackTraceElement stackTraceElement : new Throwable().getStackTrace()) {
//			if (stackTraceElement.getMethodName().equals("main")) {
//				element = stackTraceElement;
//				break;
//			}
//		}
//		@SuppressWarnings("null")
//		final ArrayList<String> arguments = new ArrayList<>(Arrays.asList(Madkit.Option.launchAgents.toString(),
//				element.getClassName() + "," + createFrame + "," + nbOfInstances));
//		if (args != null) {
//			arguments.addAll(Arrays.asList(args));
//		}
//		return new Madkit(arguments.toArray(new String[arguments.size()]));
//	}

}
