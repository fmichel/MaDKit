package madkit.kernel;

import static madkit.kernel.Agent.ReturnCode.INVALID_AGENT_ADDRESS;
import static madkit.kernel.Agent.ReturnCode.NO_RECIPIENT_FOUND;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;
import static java.util.logging.Level.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;

import it.unimi.dsi.fastutil.objects.Reference2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import javafx.scene.Scene;
import madkit.agr.DefaultMaDKitRoles;
import madkit.gui.fx.FXManager;
import madkit.gui.fx.FXOutputPane;
import madkit.messages.ConversationFilter;
import madkit.messages.EnumMessage;
import madkit.reflection.MethodFinder;
import madkit.reflection.MethodHandleFinder;
import madkit.reflection.ReflectionUtils;
import madkit.simulation.DateBasedTimer;
import madkit.simulation.SimulationEngine;
import madkit.simulation.SimulationTimer;
import madkit.simulation.activator.DateBasedDiscreteEventActivator;
import madkit.simulation.activator.DiscreteEventAgentsActivator;

/**
 * @author Fabien Michel
 *
 *         since MaDKit 6.0
 */
public class Agent {

	private static final Reference2BooleanMap<Class<?>> threaedClasses = new Reference2BooleanArrayMap<>();
	private static final AtomicInteger agentCounter = new AtomicInteger(-1);

	private final int hashCode;
	final AtomicBoolean alive = new AtomicBoolean(); // default false

	volatile KernelAgent kernel;

	private Mailbox mailbox;

	AgentLogger logger;

	public Agent() {
		hashCode = agentCounter.getAndIncrement();
	}

	protected void onActivation() {
	}

	protected void onLiving() {
	}

	protected void onEnding() {
	}

	final boolean isThreaded() {
		return threaedClasses.computeIfAbsent(getClass(), (Class<?> c) -> {// FIXME remove live on next major release
			while (c != Agent.class) {
				try {
					try {
						c.getDeclaredMethod("onLiving", (Class<?>[]) null);
						return true;
					} catch (NoSuchMethodException | SecurityException e) {
					}
					c.getDeclaredMethod("live", (Class<?>[]) null);
					return true;
				} catch (NoSuchMethodException | SecurityException e) {
					c = c.getSuperclass();
				}
			}
			return false;
		});
	}

	final void startAgentLifeCycle(CompletableFuture<ReturnCode> activationPromise) {
		CompletableFuture.runAsync(() -> activating(activationPromise), getExecutor());
	}

	public final boolean isAlive() {
		return alive.get();
	}

	/**
	 * 
	 */
	void terminate() {
		synchronized (alive) {
			getOrgnization().removeAgent(this);
			kernel = KernelAgent.deadKernel;
			alive.notifyAll();
		}
		logIfLoggerNotNull(FINER, () -> "- - -> TERMINATED **");
	}

	/**
	 * Optimized version for built-in methods
	 * 
	 * @param level
	 * @param msgSupplier
	 */
	private void logIfLoggerNotNull(Level level, Supplier<String> msgSupplier) {
		if (logger != null)
			logger.log(level, msgSupplier);
	}

	private final void activating(CompletableFuture<ReturnCode> lifeCycle) {
		Thread.currentThread().setName(String.valueOf(hashCode()));
		logIfLoggerNotNull(FINER, () -> "- - -> ACTIVATE...");
		try {
			onActivation();
		} catch (Throwable e) {
			handleException(e);
			terminate();
			lifeCycle.complete(ReturnCode.AGENT_CRASH);
			return;
		}
		alive.set(true);
		lifeCycle.complete(ReturnCode.SUCCESS);
		if (isThreaded()) {
			if (!(this instanceof DaemonAgent)) {
				kernel.threadedAgents.add(this);
			}
			living();
			ending();
		} else {
			logIfLoggerNotNull(FINER, () -> "** ACTIVATED **");
		}
	}

	final void living() {
		logIfLoggerNotNull(FINER, () -> "ACTIVATE - - -> LIVE...");
		try {
			onLiving();
		} catch (Throwable e) {
			handleException(e);
		}
		logIfLoggerNotNull(FINER, () -> "LIVE - - -> END...");
	}

	/**
	 * 
	 */
	final void killed() {
		Thread.currentThread().setName(String.valueOf(hashCode()));
		logIfLoggerNotNull(FINER, () -> "** KILLED ** - - -> END...");
		ending();
	}

	protected void exitOnKill() {
		if (Thread.currentThread().isInterrupted()) {
			throw new AgentInterruptedException();
		}
	}

	/**
	 * 
	 */
	final void ending() {
		try {
			onEnding();
		} catch (Throwable e) {
			handleException(e);
		}
		alive.set(false);
		terminate();
	}

	private final void handleException(Throwable ex) {
		if (ex instanceof AgentInterruptedException) {
			getLogger().fine(() -> "** INTERRUPTED **");
		} else {
			getLogger().log(Level.SEVERE, ex, () -> "** CRASHED **");
		}
	}

	protected ReturnCode launchAgent(Agent a) {
		return launchAgent(a, Integer.MAX_VALUE);
	}

	/**
	 * @param a
	 * @param timeout
	 * @return
	 */
	protected ReturnCode launchAgent(Agent a, int timeout) {
		return kernel.launchAgent(a, timeout);
	}

	/**
	 * Launches a new agent using its full class name and returns when the launched
	 * agent has completed its {@link Agent#onActivation()} method or when the time
	 * out is elapsed. This has the same effect as
	 * {@link #launchAgent(AbstractAgent, int, boolean)} but allows to launch agent
	 * using a class name found reflexively. The targeted agent class should have a
	 * default constructor for this to work. Additionally, this method will launch
	 * the last compiled byte code of the corresponding class if it has been
	 * reloaded using {@link MadkitClassLoader#reloadClass(String)}. Finally, if the
	 * launch timely succeeded, this method returns the instance of the created
	 * agent.
	 *
	 * @param timeOutSeconds time to wait the end of the agent's activation until
	 *                       returning <code>null</code>
	 * @param createFrame    if <code>true</code> a default GUI will be associated
	 *                       with the launched agent
	 * @param agentClass     the full class name of the agent to launch
	 * @return the instance of the launched agent or <code>null</code> if the
	 *         operation times out or failed.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Agent> T launchAgent(String agentClass, int timeOutSeconds) {// TODO with args, and
		// regardless of
		// visibility
		logIfLoggerNotNull(FINEST, () -> Words.LAUNCH + " " + agentClass);
//		try {
////			final Constructor<? extends Agent> c = (Constructor<? extends Agent>) ClassLoader.getSystemClassLoader().loadClass(agentClass).getDeclaredConstructor();
////			final Constructor<? extends Agent> c = (Constructor<? extends Agent>) MadkitClassLoader.getLoader()
////					.loadClass(agentClass).getDeclaredConstructor();// NOSONAR mcl must not be closed
////			final Agent a = c.newInstance();
//		} catch (InvocationTargetException | InstantiationException | IllegalAccessException | IllegalArgumentException
//				| NoSuchMethodException | SecurityException | ClassNotFoundException e) {
////	    getLogger().severeLog(Influence.LAUNCH_AGENT.failedString(), e);
//			e.printStackTrace();// TODO
//		}
		final Agent a = MadkitClassLoader.getInstance(agentClass);
		if (ReturnCode.SUCCESS == launchAgent(a, timeOutSeconds))
			return (T) a;
		return null;
	}

	public ReturnCode killAgent(Agent a) {
		return killAgent(a, Integer.MAX_VALUE);
	}

	/**
	 * @param a
	 * @param maxValue
	 * @return
	 */
	protected ReturnCode killAgent(Agent a, int maxValue) {
		if (a.alive.compareAndSet(true, false)) {
			return kernel.killAgent(a, maxValue);
		}
		return ReturnCode.NOT_YET_LAUNCHED;
	}

	/**
	 * @deprecated Use {@link #onActivation()} instead
	 */
	@Deprecated(since = "6", forRemoval = true)
	protected void activate() {
		onActivation();
	}

	/**
	 * @deprecated Use {@link #onLiving()} instead
	 */
	@Deprecated(since = "6", forRemoval = true)
	protected void live() {
		onLiving();
	}

	/**
	 * @deprecated Use {@link #onEnding()} instead
	 */
	@Deprecated(since = "6", forRemoval = true)
	protected void end() {
		onEnding();
	}

	Executor getExecutor() {
		return kernel.getAgentExecutor(this);
	}

	/**
	 * Returns the agent's logger. It is lazily created so that if
	 * {@link #getLogger()} is not used, there is no memory foot print at all, which
	 * could be crucial when working with thousands of abstract agents in simulation
	 * mode.
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
	 * Stops the agent's process for a while.
	 * 
	 * @param milliSeconds the number of milliseconds for which the agent should
	 *                     pause.
	 */
	protected void pause(final int milliSeconds) {
		try {
			Thread.sleep(milliSeconds);
		} catch (InterruptedException e) {
			throw new AgentInterruptedException();
		}
	}

	/**
	 * Gets the agent's name. Default is "<i>class name + internal ID</i>". This
	 * name is used in logger info, GUI title and so on. This method could be
	 * overridden to obtain a customized name.
	 *
	 * @return the agent's name
	 */
	public String getName() {
		return getClass().getSimpleName() + '-' + hashCode;
	}

	@Override
	
	public int hashCode() {//NOSONAR Super version of equals is perfect here
		return hashCode;
	}

	public KernelConfig getKernelConfig() {
		return kernel.getKernelConfig();
	}

	// ///////////////////////////////////////////////////////// GROUP & ROLE
	// METHODS AGR

	/**
	 * Creates a new Group within a community. This has the same effect as
	 * <code>createGroup(community, group, false, null)</code>
	 *
	 * @param community the community within which the group will be created. If
	 *                  this community does not exist it will be created.
	 * @param group     the name of the new group
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the group has been
	 *         successfully created.</li>
	 *         <li><code>{@link ReturnCode#ALREADY_GROUP}</code>: If the operation
	 *         failed because such a group already exists.</li>
	 *         <li><code>
	 *         {@link ReturnCode#IGNORED}</code>: If this method is used in activate
	 *         and this agent has been launched using
	 *         {@link Agent#launchAgentBucket(List, String...)} with non
	 *         <code>null</code> roles</li>
	 *         </ul>
	 * @see Agent#createGroup(String, String, boolean, Gatekeeper)
	 * @since MaDKit 5.0
	 */
	public ReturnCode createGroup(final String community, final String group) {
		return createGroup(community, group, false, null);
	}

	/**
	 * Creates a new Group within a community. This has the same effect as
	 * <code>createGroup(community, group, isDistributed, null)</code>
	 *
	 * @param community     the community within which the group will be created. If
	 *                      this community does not exist it will be created.
	 * @param group         the name of the new group.
	 * @param isDistributed if <code>true</code> the new group will be distributed
	 *                      when multiple MaDKit kernels are connected.
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the group has been
	 *         successfully created.</li>
	 *         <li><code>{@link ReturnCode#ALREADY_GROUP}</code>: If the operation
	 *         failed because such a group already exists.</li>
	 *         <li><code>
	 *         {@link ReturnCode#IGNORED}</code>: If this method is used in activate
	 *         and this agent has been launched using
	 *         {@link Agent#launchAgentBucket(List, String...)} with non
	 *         <code>null</code> roles</li>
	 *         </ul>
	 * @see Agent#createGroup(String, String, boolean, Gatekeeper)
	 * @since MaDKit 5.0
	 */
	public ReturnCode createGroup(final String community, final String group, boolean isDistributed) {
		return createGroup(community, group, isDistributed, null);
	}

	public Organization getOrgnization() {
		return kernel.getOrgnization();
	}

	/**
	 * Creates a new Group within a community.
	 * <p>
	 * If this operation succeed, the agent will automatically handle the role
	 * defined by {@link DefaultMaDKitRoles#GROUP_MANAGER_ROLE}, which value is <i>
	 * {@value madkit.agr.DefaultMaDKitRoles#GROUP_MANAGER_ROLE}</i>, in this
	 * created group. Especially, if the agent leaves the role of <i>
	 * {@value madkit.agr.DefaultMaDKitRoles#GROUP_MANAGER_ROLE}</i>, it will also
	 * automatically leave the group and thus all the roles it has in this group.
	 * <p>
	 * Agents that want to enter the group may send messages to the <i>
	 * {@value madkit.agr.DefaultMaDKitRoles#GROUP_MANAGER_ROLE}</i> using the role
	 * defined by {@link DefaultMaDKitRoles#GROUP_CANDIDATE_ROLE}, which value is
	 * <i> {@value madkit.agr.DefaultMaDKitRoles#GROUP_CANDIDATE_ROLE}</i>.
	 *
	 * @param community     the community within which the group will be created. If
	 *                      this community does not exist it will be created.
	 * @param group         the name of the new group.
	 * @param isDistributed if <code>true</code> the new group will be distributed
	 *                      when multiple MaDKit kernels are connected.
	 * @param keyMaster     any object that implements the {@link Gatekeeper}
	 *                      interface. If not <code>null</code>, this object will be
	 *                      used to check if an agent can be admitted in the group.
	 *                      When this object is null, there is no group access
	 *                      control.
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the group has been
	 *         successfully created.</li>
	 *         <li><code>
	 *         {@link ReturnCode#ALREADY_GROUP}</code>: If the operation failed
	 *         because such a group already exists.</li>
	 *         <li><code>{@link ReturnCode#IGNORED}</code>: If the agent has been
	 *         launched using a <code>launchAgentBucket</code> method such as
	 *         {@link Agent#launchAgentBucket(List, String...)} with non
	 *         <code>null</code> roles. This for optimization purposes.</li>
	 *         </ul>
	 * @see Gatekeeper
	 * @see ReturnCode
	 * @since MaDKit 5.0
	 */
	public ReturnCode createGroup(final String community, final String group, boolean isDistributed,
			final Gatekeeper keyMaster) {
//		if (getState() == INITIALIZING) {
//			handleWarning(Influence.CREATE_GROUP,
//					() -> new OrganizationWarning(ReturnCode.IGNORED, community, group, null));
//			return ReturnCode.IGNORED;
//		}
		return kernel.createGroup(this, community, group, keyMaster, isDistributed);
	}

	/**
	 * Requests a role within a group of a particular community. This has the same
	 * effect as <code>requestRole(community, group, role, null, false)</code>. So
	 * the passKey is <code>null</code> and the group must not be secured for this
	 * to succeed.
	 *
	 * @param community the group's community.
	 * @param group     the targeted group.
	 * @param role      the desired role.
	 * @see #requestRole(String, String, String, Object)
	 * @since MaDKit 5.0
	 */
	public ReturnCode requestRole(final String community, final String group, final String role) {
		return requestRole(community, group, role, null);
	}

	/**
	 * Requests a role within a group of a particular community using a passKey.
	 *
	 * @param community the group's community.
	 * @param group     the targeted group.
	 * @param role      the desired role.
	 * @param passKey   the <code>passKey</code> to enter a secured group. It is
	 *                  generally delivered by the group's <i>group manager</i>. It
	 *                  could be <code>null</code>, which is sufficient to enter an
	 *                  unsecured group. Especially,
	 *                  {@link #requestRole(String, String, String)} uses a
	 *                  <code>null</code> <code>passKey</code>.
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the operation has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community
	 *         does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#ROLE_ALREADY_HANDLED}</code>: If this
	 *         role is already handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#ACCESS_DENIED}</code>: If the access
	 *         denied by the manager of that secured group.</li>
	 *         <li><code>{@link ReturnCode#IGNORED}</code>: If the agent has been
	 *         launched using a <code>launchAgentBucket</code> method such as
	 *         {@link Agent#launchAgentBucket(List, String...)} with non
	 *         <code>null</code> roles. This for optimization purposes.</li>
	 *         </ul>
	 * @see Agent.ReturnCode
	 * @see Gatekeeper
	 * @since MaDKit 5.0
	 */
	public ReturnCode requestRole(final String community, final String group, final String role, final Object passKey) {
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
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community
	 *         does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of this group.</li>
	 *         </ul>
	 * @since MaDKit 5.0
	 * @see ReturnCode
	 */
	public ReturnCode leaveGroup(final String community, final String group) {
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
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community
	 *         does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#ROLE_NOT_HANDLED}</code>: If this role is
	 *         not handled by this agent.</li>
	 *         </ul>
	 * @see Agent.ReturnCode
	 * @since MaDKit 5.0
	 */
	public ReturnCode leaveRole(final String community, final String group, final String role) {
		return kernel.leaveRole(this, community, group, role);
	}

	/**
	 * Returns an {@link AgentAddress} corresponding to an agent having this
	 * position in the organization. The caller is excluded from the search.
	 *
	 * @param community the community name
	 * @param group     the group name
	 * @param role      the role name
	 * @return an {@link AgentAddress} corresponding to an agent handling this role
	 *         or <code>null</code> if such an agent does not exist.
	 */
	public AgentAddress getAgentWithRole(final String community, final String group, final String role) {
		try {
			return getOrgnization().getRole(community, group, role).getAnotherAddress(this);
		} catch (CGRNotAvailable e) {
			return null;
		}
	}

	/**
	 * Return a string representing a unique identifier for the agent over the
	 * network.
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
		final Role roleObject = receiver.getRoleObject();
		if (roleObject != null) {
			if (roleObject.players == null) {// has been traveling
				return roleObject.resolveDistantAddress(receiver);
			}
			return receiver;
		}
		return null;
	}

	/**
	 * Kills the caller and launches a new instance of this agent using the latest
	 * byte code available for the corresponding class.
	 */
	public void reload() {
		try {
			MadkitClassLoader.reloadClass(getClass().getName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
			Agent a = getClass().getDeclaredConstructor().newInstance();// FIXME why not with string
			launchAgent(a, 0);
//			launchAgent(getClass().getName(), 0);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
//		killAgent(this,1);
	}

	/**
	 * Sends a message, using an agent address, specifying explicitly the role used
	 * to send it.
	 * 
	 * @param message  the message to send
	 * @param receiver the targeted agent
	 *
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of the receiver's group.</li>
	 *         <li><code>{@link ReturnCode#ROLE_NOT_HANDLED}</code>: If
	 *         <code>senderRole</code> is not handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AGENT_ADDRESS}</code>: If the
	 *         receiver address is no longer valid. This is the case when the
	 *         corresponding agent has leaved the role corresponding to the receiver
	 *         agent address.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @see AgentAddress
	 */
	public ReturnCode sendWithRole(final Message message, final AgentAddress receiver, final String senderRole) {
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
//		return kernel.sendMessage(this, receiver, message, senderRole);
	}

	/**
	 * Sends a message to an agent using an agent address. This has the same effect
	 * as <code>sendWithRole(receiver, messageToSend, null)</code>.
	 * 
	 * @param message
	 * @param receiver
	 *
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of the receiver's group.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AGENT_ADDRESS}</code>: If the
	 *         receiver address is no longer valid. This is the case when the
	 *         corresponding agent has leaved the role corresponding to the receiver
	 *         agent address.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @see AgentAddress
	 */
	public ReturnCode send(final Message message, final AgentAddress receiver) {
		return sendWithRole(message, receiver, null);
	}

	/**
	 * Sends a message to an agent having this position in the organization,
	 * specifying explicitly the role used to send it. This has the same effect as
	 * sendMessageWithRole(community, group, role, messageToSend,null). If several
	 * agents match, the target is chosen randomly. The sender is excluded from this
	 * search.
	 * 
	 * @param message   the message to send
	 * @param community the community name
	 * @param group     the group name
	 * @param role      the role name
	 *
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community
	 *         does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of the targeted group.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent
	 *         was found as recipient, i.e. the sender was the only agent having
	 *         this role.</li>
	 *         </ul>
	 * @see ReturnCode
	 */
	public ReturnCode send(final Message message, final String community, final String group, final String role) {
		return sendWithRole(message, community, group, role, null);
	}

	/**
	 * Sends a message to an agent having this position in the organization. This
	 * has the same effect as
	 * <code>sendMessageWithRole(community, group, role, messageToSend,null)</code>
	 * . If several agents match, the target is chosen randomly. The sender is
	 * excluded from this search.
	 * 
	 * @param message    the message to send
	 * @param community  the community name
	 * @param group      the group name
	 * @param role       the role name
	 * @param senderRole the agent's role with which the message has to be sent
	 *
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community
	 *         does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#ROLE_NOT_HANDLED}</code>: If
	 *         <code>senderRole</code> is not handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of the targeted group.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent
	 *         was found as recipient, i.e. the sender was the only agent having
	 *         this role.</li>
	 *         </ul>
	 * @see ReturnCode
	 */
	public ReturnCode sendWithRole(final Message message, final String community, final String group, final String role,
			final String senderRole) {
		try {
			AgentAddress receiver = getOrgnization().getRole(community, group, role).getAnotherAddress(this);
			if (receiver == null) {
				return NO_RECIPIENT_FOUND;
			}
			return sendWithRole(message, receiver, senderRole);
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
	}

	/**
	 * Sends a message to an agent having this position in the organization and
	 * waits for an answer to it. The targeted agent is selected randomly among
	 * matched agents. The sender is excluded from this search.
	 * 
	 * @param community           the community name
	 * @param group               the group name
	 * @param role                the role name
	 * @param messageToSend       the message to send.
	 * @param senderRole          the role with which the sending is done.
	 * @param timeOutMilliSeconds the maximum time to wait. If <code>null</code> the
	 *                            agent will wait indefinitely.
	 * @return the reply received as soon as available, or <code>null</code> if the
	 *         time out has elapsed or if there was an error when sending the
	 *         message.
	 * @since MaDKit 5
	 */
	public <T extends Message> T sendWithRoleWaitReply(Message messageToSend, final String community, final String group,
			final String role, final String senderRole, final Integer timeOutMilliSeconds) {
//		if(logger != null)
//			logger.finest(() -> "sendWithRoleWaitReply : sending "+messageToSend+" to any "+I18nUtilities.getCGRString(community, group, role)+
//					(timeOutMilliSeconds == null ? "":", and waiting reply for "+TimeUnit.MILLISECONDS.toSeconds(timeOutMilliSeconds)+" s..."));
		if (sendWithRole(messageToSend, community, group, role, senderRole) != SUCCESS) {
			return null;// TODO log
		}
		return waitAnswer(messageToSend, timeOutMilliSeconds);
	}

	/**
	 * Sends a message and waits for an answer to it. Additionally, the sending is
	 * done using a specific role for the sender.
	 * 
	 * @param messageToSend       the message to send.
	 * @param receiver            the targeted agent by the send.
	 * @param senderRole          the role with which the sending is done.
	 * @param timeOutMilliSeconds the maximum time to wait. If <code>null</code> the
	 *                            agent will wait indefinitely.
	 * 
	 * @return the reply received as soon as available, or <code>null</code> if the
	 *         time out has elapsed or if there was an error when sending the
	 *         message, that is any {@link AbstractAgent.ReturnCode} different from
	 *         {@link AbstractAgent.ReturnCode#SUCCESS} (see
	 *         {@link Agent#sendMessageWithRole(AgentAddress, Message, String)}).
	 * 
	 * @see #sendMessageWithRole(AgentAddress, Message, String)
	 * @see AbstractAgent.ReturnCode
	 * @since MaDKit 5
	 */
	public <T extends Message> T sendWithRoleWaitReply(Message messageToSend, final AgentAddress receiver,
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
	 * Sends a message to an agent having this position in the organization and
	 * waits for an answer to it. The targeted agent is selected randomly among
	 * matched agents. The sender is excluded from this search.
	 * 
	 * @param community           the community name
	 * @param group               the group name
	 * @param role                the role name
	 * @param messageToSend       the message to send.
	 * @param senderRole          the role with which the sending is done.
	 * @param timeOutMilliSeconds the maximum time to wait. If <code>null</code> the
	 *                            agent will wait indefinitely.
	 * @return the reply received as soon as available, or <code>null</code> if the
	 *         time out has elapsed or if there was an error when sending the
	 *         message.
	 * @since MaDKit 5
	 */
	public <T extends Message> T sendWaitReply(Message messageToSend, final String community, final String group,
			final String role, final Integer timeOutMilliSeconds) {
		return sendWithRoleWaitReply(messageToSend, community, group, role, null, timeOutMilliSeconds);
	}

//	/**
//	 * Broadcasts a message to every agent having a role in a group in a community,
//	 * but not to the sender.
//	 *
//	 * @param community the community name
//	 * @param group     the group name
//	 * @param role      the role name
//	 * @param message
//	 * @return
//	 *         <ul>
//	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
//	 *         succeeded.</li>
//	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community
//	 *         does not exist.</li>
//	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
//	 *         exist.</li>
//	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not
//	 *         exist.</li>
//	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent
//	 *         was found as recipient, i.e. the sender was the only agent having
//	 *         this role.</li>
//	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
//	 *         not a member of the targeted group.</li>
//	 *         </ul>
//	 * @see ReturnCode
//	 */
//	public ReturnCode broadcast(final String community, final String group, final String role, final Message message) {
//		return broadcastWithRole(community, group, role, message, null);
//	}

//	/**
//	 * Broadcasts a message to every agent having a role in a group in a community
//	 * using a specific role for the sender. The sender is excluded from the search.
//	 *
//	 * @param community     the community name
//	 * @param group         the group name
//	 * @param role          the role name
//	 * @param messageToSend
//	 * @param senderRole    the agent's role with which the message should be sent
//	 * @return
//	 *         <ul>
//	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
//	 *         succeeded.</li>
//	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community
//	 *         does not exist.</li>
//	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
//	 *         exist.</li>
//	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not
//	 *         exist.</li>
//	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
//	 *         not a member of the targeted group.</li>
//	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent
//	 *         was found as recipient, i.e. the sender was the only agent having
//	 *         this role.</li>
//	 *         </ul>
//	 * @see ReturnCode
//	 */
//	public ReturnCode broadcastWithRole(final String community, final String group, final String role,
//			final Message messageToSend, final String senderRole) {
//		try {
//			final List<AgentAddress> receivers = getAgentsWithRole(community, group, role);
//			if (receivers.isEmpty())
//				// the requester is the only agent in this group
//				return NO_RECIPIENT_FOUND;
//			return kernel.broadcastMessageWithRole(this, receivers, messageToSend, senderRole);
//		} catch (CGRNotAvailable e) {
//			return e.getCode();
//		}
//	}

	/**
	 * Broadcasts a message to every agent having a role in a group in a community
	 * using a specific role for the sender. The sender is excluded from the search.
	 *
	 * @param SIM_COMMUNITY the community name
	 * @param group         the group name
	 * @param role          the role name
	 * @param message
	 * @param role          the agent's role with which the message should be sent
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community
	 *         does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of the targeted group.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent
	 *         was found as recipient, i.e. the sender was the only agent having
	 *         this role.</li>
	 *         </ul>
	 * @see ReturnCode
	 */
	public ReturnCode broadcast(final Message message, List<AgentAddress> receivers) {
		return broadcastWithRole(message, receivers, null);
	}

	/**
	 * Broadcasts a message to every agent having a role in a group in a community
	 * using a specific role for the sender. The sender is excluded from the search.
	 *
	 * @param SIM_COMMUNITY the community name
	 * @param group         the group name
	 * @param role          the role name
	 * @param message
	 * @param role          the agent's role with which the message should be sent
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community
	 *         does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of the targeted group.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent
	 *         was found as recipient, i.e. the sender was the only agent having
	 *         this role.</li>
	 *         </ul>
	 * @see ReturnCode
	 */
	public ReturnCode broadcastWithRole(final Message message, List<AgentAddress> receivers, final String role) {
		if (receivers.isEmpty())
			return NO_RECIPIENT_FOUND;
		return kernel.broadcastMessageWithRole(this, receivers, message, role);
	}

//	/**
//	 * Broadcasts a message and wait for answers considering a timeout duration.
//	 * 
//	 * @param message
//	 * @param community
//	 * @param group
//	 * @param role
//	 * @param senderRole
//	 * @param timeOutMilliSeconds
//	 * 
//	 * @return a list of messages which are answers to the <code>message</code>
//	 *         which has been broadcasted.
//	 */
//	public <T extends Message> List<T> broadcastWithRoleWaitForReplies(Message message, final String community,
//			final String group, final String role, final String senderRole, final Integer timeOutMilliSeconds) {
//		final List<AgentAddress> receivers = getAgentsWithRole(community, group, role);
//		if (receivers.isEmpty())
//			// the requester is the only agent in this group
//			return Collections.emptyList();// TODO log why
//		kernel.broadcastMessageWithRole(this, receivers, message, senderRole);
//		return (List<T>) getMailbox().waitAnswers(message, receivers.size(), timeOutMilliSeconds);
//	}

	/**
	 * Broadcasts a message and wait for answers considering a timeout duration.
	 * 
	 * @param message
	 * @param SIM_COMMUNITY
	 * @param group
	 * @param role
	 * @param senderRole
	 * @param timeOutMilliSeconds
	 * 
	 * @return a list of messages which are answers to the <code>message</code>
	 *         which has been broadcasted.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Message> List<T> broadcastWithRoleWaitForReplies(Message message, List<AgentAddress> receivers,
			final String senderRole, final Integer timeOutMilliSeconds) {
		if (broadcastWithRole(message, receivers, senderRole) != SUCCESS) {
			return Collections.emptyList();// TODO log why
		}
		return (List<T>) getMailbox().waitAnswers(message, receivers.size(), timeOutMilliSeconds);
	}
//	catch(CGRNotAvailable e) {
//	    final ReturnCode r = e.getCode();
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
	 * private is about not poluting the API
	 * 
	 * @param community
	 * @param group
	 * @param role
	 * @return
	 */
	public List<AgentAddress> getAgentsWithRole(final String community, final String group, final String role) {
		try {
			Role targetedRole = getOrgnization().getRole(community, group, role);
			return targetedRole.getOtherRolePlayers(this);
		} catch (CGRNotAvailable e) {// LOg why
			return Collections.emptyList();
		}
	}

	/**
	 * Checks if this agent address is still valid. <i>I.e.</i> the corresponding
	 * agent is still playing this role.
	 *
	 * @return <code>true</code> if the address still exists in the organization.
	 * @since MaDKit 5.0.4
	 */
	public boolean checkAgentAddress(final AgentAddress agentAddress) {
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
//    public ReturnCode sendMessage(final String community, final String group, final String role, final Message message) {
//	return sendWithRole(community, group, role, message, null);
//    }

	/**
	 * Sends a message by replying to a previously received message. The sender is
	 * excluded from this search.
	 * 
	 * @param reply            the reply itself.
	 * @param messageToReplyTo the previously received message.
	 * @param senderRole       the agent's role with which the message should be
	 *                         sent
	 *
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is no
	 *         longer a member of the corresponding group.</li>
	 *         <li><code>{@link ReturnCode#ROLE_NOT_HANDLED}</code>: If
	 *         <code>senderRole</code> is not handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AGENT_ADDRESS}</code>: If the
	 *         receiver address is no longer valid. This is the case when the
	 *         corresponding agent has leaved the role corresponding to the receiver
	 *         agent address.</li>
	 *         </ul>
	 * @see ReturnCode
	 */
	public ReturnCode replyWithRole(final Message reply, final Message messageToReplyTo, final String senderRole) {
		final AgentAddress target = messageToReplyTo.getSender();
		if (target == null)
			return ReturnCode.CANT_REPLY;
		reply.getIDFrom(messageToReplyTo);
		return kernel.sendMessage(this, target, reply, senderRole);
	}

	/**
	 * Sends a message by replying to a previously received message. This has the
	 * same effect as <code>sendReplyWithRole(messageToReplyTo, reply, null)</code>.
	 * 
	 * @param reply            the reply itself.
	 * @param messageToReplyTo the previously received message.
	 *
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the reply has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is no
	 *         longer a member of the corresponding group.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AGENT_ADDRESS}</code>: If the
	 *         receiver address is no longer valid. This is the case when the
	 *         corresponding agent has leaved the role corresponding to the receiver
	 *         agent address.</li>
	 *         </ul>
	 * @see Agent#replyWithRole(Message, Message, String)
	 */
	public ReturnCode reply(final Message reply, final Message messageToReplyTo) {
		return replyWithRole(reply, messageToReplyTo, null);
	}

	/**
	 * Gets the next message which is a reply to the <i>originalMessage</i>.
	 *
	 * @param originalMessage the message to which a reply is searched.
	 * @return a reply to the <i>originalMessage</i> or <code>null</code> if no
	 *         reply to this message has been received.
	 */
	public <T extends Message> T getReplyTo(final Message originalMessage) {
		return getMailbox().next(new ConversationFilter(originalMessage));
	}

	//////////////////////////////////// WAIT
	/**
	 * This method is the blocking version of nextMessage(). If there is no message
	 * in the mailbox, it suspends the agent life until a message is received
	 *
	 * @see #waitNextMessage(long)
	 * @return the first received message
	 */
	@SuppressWarnings("unchecked")
	public <T extends Message> T waitNextMessage() {
		getLogger().finest(() -> "waitNextMessage...");
		final Message m = getMailbox().waitingNextForEver();
		getLogger().finest(() -> "..." + Words.NEW_MSG + ": " + m);
		return (T) m;
	}

	/**
	 * This method gets the next message of the mailbox or waits for a new incoming
	 * message considering a certain delay.
	 * 
	 * @param timeOutMilliseconds the maximum time to wait, in milliseconds.
	 * 
	 * @return the first message in the mailbox, or <code>null</code> if no message
	 *         has been received before the time out delay is elapsed
	 */
	@SuppressWarnings("unchecked")
	public <T extends Message> T waitNextMessage(final long timeOutMilliseconds) {
		getLogger().finest(() -> "Waiting next message during " + timeOutMilliseconds + " milliseconds...");
		final Message m = getMailbox().waitNext(timeOutMilliseconds, TimeUnit.MILLISECONDS);
		if (m != null)
			getLogger().finest(() -> "waitNextMessage->" + Words.NEW_MSG + ": " + m);
		else
			getLogger().finest(() -> "waitNextMessage time out !");
		return (T) m;
	}

	/**
	 * Retrieves and removes the next message that is a reply to the query message,
	 * waiting for ever if necessary until a matching reply becomes available.
	 * 
	 * @param query the message for which a reply is waited for
	 * 
	 * @return the first reply to the query message
	 * @since MadKit 5.0.4
	 */
	public <T extends Message> T waitAnswer(final Message query) {
		return getMailbox().waitNext(new ConversationFilter(query));
	}

	/**
	 * Retrieves and removes the next message that is a reply to the query message,
	 * waiting for ever if necessary until a matching reply becomes available.
	 * 
	 * @param query               the message for which a reply is waited for
	 * @param timeOutMilliSeconds the maximum time to wait, in milliseconds.
	 * 
	 * @return the first reply to the query message
	 * @since MadKit 5.0.4
	 */
	public <T extends Message> T waitAnswer(final Message query, final Integer timeOutMilliSeconds) {
		return getMailbox().waitNext(new ConversationFilter(query), timeOutMilliSeconds);
	}

	/**
	 * This method offers a convenient way for regular object to send messages to
	 * Agents, especially threaded agents. For instance when a GUI wants to discuss
	 * with its linked agent: This allows to enqueue work to do in their life cycle
	 *
	 * @param m
	 */
	public void receiveMessage(final Message m) {
		getMailbox().getMessageBox().offer(m);
	}

	private KernelAgent getKernel() {
		return kernel;
	}

	public Mailbox getMailbox() {
		if (mailbox == null) {
			mailbox = new Mailbox();
		}
		return mailbox;
	}

	/**
	 * Creates a default frame for the agent
	 */
	public void setupGUI() {
		FXManager.runAndWait(() -> {
			AgentFxStage stage = new AgentFxStage(this);
			Scene scene = new Scene(new FXOutputPane(this));
			stage.setScene(scene);
			stage.show();
		});
	}

	/**
	 * This class enumerates all the return codes which could be obtained with
	 * essential methods of the {@link Agent} and {@link Agent} classes.
	 *
	 * @author Fabien Michel
	 * @since MaDKit 5.0
	 */
	public enum ReturnCode {

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
		 * Returned when the agent does not have a role that it is supposed to have
		 * doing a particular action, e.g.
		 * {@link Agent#sendMessageWithRole(AgentAddress, Message, String)}
		 */
		ROLE_NOT_HANDLED,
		/**
		 * Returned when using
		 * {@link Agent#createGroup(String, String, boolean, Gatekeeper)} and that a
		 * group already exists
		 */
		ALREADY_GROUP,
		/**
		 * Returned when launching an agent which is already launched
		 */
		ALREADY_LAUNCHED,
		/**
		 * Returned by various timed primitives of the Agent class like
		 * {@link Agent#sendMessageAndWaitForReply(AgentAddress, Message)} or
		 * {@link Agent#launchAgent(Agent, int, boolean)}
		 */
		TIMEOUT,
		/**
		 * Returned by launch primitives when the launched agent crashes in activate
		 */
		AGENT_CRASH,
		// NOT_AN_AGENT_CLASS,
		/**
		 * Returned by kill primitives when the targeted agent has not been launched
		 * priorly
		 */
		NOT_YET_LAUNCHED,
		/**
		 * Returned by kill primitives when the targeted agent is already terminated
		 */
		ALREADY_KILLED,
		/**
		 * Returned by send primitives when the targeted agent address does not exist
		 * anymore, i.e. the related agent has leaved the corresponding role
		 */
		INVALID_AGENT_ADDRESS,
		/**
		 * Returned by send primitives when the targeted CGR location does not exist or
		 * contain any agent
		 */
		NO_RECIPIENT_FOUND,
		/**
		 * Returned when {@link Agent#requestRole(String, String, String, Object)} or
		 * {@link Agent#createGroup(String, String, boolean, Gatekeeper)} is used in
		 * activate and that the agent has been launched using
		 * {@link Agent#launchAgentBucket(List, String...)} or
		 * {@link Agent#launchAgentBucket(String, int, String...)}</li>
		 */
		IGNORED,
		/**
		 * Returned when an agent tries to reply to a message which has not been
		 * received from another agent, e.g. newly created or sent directly by an object
		 * using {@link Agent#receiveMessage(Message)}.
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

	final void logMethod(final String lifeCycleMethod, final boolean entering) {
		getLogger().finer(() -> "** " + (entering ? Words.ENTERING : Words.EXITING) + " " + lifeCycleMethod + " **");
	}

	/**
	 * The kernel's address on which this agent is running.
	 *
	 * @return the kernel address representing the MaDKit kernel on which the agent
	 *         is running
	 */
	public KernelAddress getKernelAddress() {
		return kernel.kernelAddress;
	}

	/**
	 * @return
	 */
	public <M extends Message> M nextMessage() {
		return getMailbox().next();
	}

	//////////////////////////////////////////// LAUNCHING

	/**
	 * This offers a convenient way to create main a main method that launches the
	 * agent class under development. The agent is launched in a new instance
	 * MaDKit. This call only works in the main method of the agent's class. MaDKit.
	 * Here is an example of use that will work in any subclass of {@link Agent}:
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
	 * @param args          MaDKit options. For example, this will launch the agent
	 *                      in desktop mode :
	 *
	 *                      <pre>
	 *                      <code>
	 * public static void main(String[] args) {
	 * 	executeThisAgent(BooleanOption.desktop.toString());
	 * }
	 * </code>
	 * @return the kernel instance that actually launches this agent, so that it is
	 *         possible to do other actions after the launch using
	 *         {@link Madkit#doAction(madkit.action.KernelAction, Object...)}
	 * @see Option BooleanOption LevelOption
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
	 * This offers a convenient way to create a main method that launches the agent
	 * class under development. This call only works in the main method of the
	 * agent's class. This call is equivalent to
	 * <code>executeThisAgent(1, true, args)</code>
	 *
	 * @param args MaDKit options
	 * @return the kernel instance that actually launches this agent, so that it is
	 *         possible to do other actions after the launch using
	 *         {@link Madkit#doAction(madkit.action.KernelAction, Object...)}
	 * @see #executeThisAgent(int, boolean, String...)
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
	 * Proceeds an {@link EnumMessage} so that if it is correctly built, the agent
	 * will trigger its corresponding behavior using the parameters of the message.
	 *
	 * @param message the message to proceed
	 * @since MaDKit 5.0.0.14
	 * @see EnumMessage
	 */
	public <E extends Enum<E>> void proceedEnumMessage(EnumMessage<E> message) {
		logIfLoggerNotNull(FINEST, () -> "proceeding command message " + message);
		Object[] parameters = message.getContent();
		Method m = null;
		try {
			m = MethodFinder.getMethodOn(getClass(), ReflectionUtils.enumToMethodName(message.getCode()), parameters);
//	    m = Activator.findMethodOnFromArgsSample(getClass(), ActionInfo.enumToMethodName(message.getCode()), parameters);
			m.invoke(this, parameters);
		} catch (NoSuchMethodException e) {
			if (logger != null)
				logger.warning(() -> "I do not know how to " + ReflectionUtils.enumToMethodName(message.getCode())
						+ Arrays.deepToString(parameters));
			logForSender(() -> "I have sent a message which has not been understood", message);// TODO i18n
		} catch (IllegalArgumentException e) {
			if (logger != null)
				logger.warning("Cannot proceed message : wrong argument " + m);
			logForSender(() -> "I have sent an incorrect command message ", message);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {// TODO dirty : think about that
			Throwable t = e.getCause();
			t.printStackTrace();
		} catch (Throwable e) {
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
	 * This offers a convenient way to create main a main method that launches the
	 * agent class under development. The agent is launched in a new instance
	 * MaDKit. This call only works in the main method of the agent's class. MaDKit.
	 * Here is an example of use that will work in any subclass of {@link Agent}:
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
	 * @param args          MaDKit options. For example, this will launch the agent
	 *                      in desktop mode :
	 *
	 *                      <pre>
	 *                      <code>
	 * public static void main(String[] args) {
	 * 	executeThisAgent(BooleanOption.desktop.toString());
	 * }
	 * </code>
	 * @return the kernel instance that actually launches this agent, so that it is
	 *         possible to do other actions after the launch using
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
