/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.simulation;

import static madkit.simulation.SimuOrganization.ENGINE_GROUP;
import static madkit.simulation.SimuOrganization.LAUNCHER_ROLE;
import static madkit.simulation.SimuOrganization.MODEL_GROUP;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.random.RandomGenerator;

import madkit.action.SchedulingAction;
import madkit.kernel.Probe;
import madkit.kernel.Scheduler;
import madkit.kernel.Watcher;
import madkit.messages.SchedulingMessage;
import madkit.random.Randomness;
import madkit.simulation.scheduler.SimuTimer;
import madkit.simulation.scheduler.TickBasedScheduler;

/**
 * Main class for launching a simulation. This class is responsible for initializing the
 * simulation environment, simulation model, and scheduler. It also launches the
 * simulation agents and viewers.
 * <p>
 * This class is intended to be extended by the user to define the simulation engine, if
 * the default setup should customized. The user can define the simulation environment,
 * model, and scheduler classes by overriding the {@link #onLaunchEnvironment()},
 * {@link #onLaunchModel()}, and {@link #onLaunchScheduler()} methods, respectively. The
 * user can also define the simulation agents and viewers by overriding the
 * {@link #onLaunchSimulatedAgents()} and {@link #onLaunchViewers()} methods,
 * respectively. The user can also define the simulation startup behavior by overriding
 * the {@link #onSimulationStart()} method.
 * <p>
 * Crucially, this class is also responsible for initializing the pseudo random number
 * generator (PRNG) that has to be used by the simulation agents for ensuring the
 * reproducibility of the simulation. The PRNG is initialized with a seed that can be set
 * by the user. The seed is a long integer that can be set by the user by overriding the
 * {@link #onInitializeSimulationSeedIndex()} method. By default, the seed index is 0.
 * 
 * See {@link #onInitializeSimulationSeedIndex()} method.
 * 
 */
@EngineAgents
public abstract class SimuLauncher extends Watcher {

	/**
	 * The Enum ENGINE.
	 */
	enum ENGINE {

		/** The scheduler. */
		SCHEDULER,
		/** The environment. */
		ENVIRONMENT,
		/** The model. */
		MODEL;
	}

	private static final String LAUNCHED = " launched";

	private String simuCommunity;
	private SimuModel model;
	private SimuEnvironment environment;
	private Scheduler<? extends SimuTimer<?>> scheduler;

	private RandomGenerator randomGenerator;
	/**
	 * Need a many seed bits, and then increment on it
	 */
	private static final long BASE_SEED = 0xFEDCBA0987654321L;
	private long simulationSeed = BASE_SEED;

	private int simulationIndex;

	private Probe viewersProbe;

	/**
	 * Default constructor. It initializes the simulation community name to the class name of
	 * the simulation engine.
	 */
	protected SimuLauncher() {
		simuCommunity = getClass().getSimpleName();
		try {
			Field f = Probe.findFieldOn(SimuAgent.class, "simuLauncher");
			f.setAccessible(true);// NOSONAR
			f.set(this, this);// NOSONAR
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException _) {
		}
	}

	/**
	 * This method is called when the simulation engine is activated. It initializes the
	 * simulation community, creates the engine and model groups, and requests the role
	 * {@link SimuOrganization#LAUNCHER_ROLE} in the group
	 * {@link SimuOrganization#ENGINE_GROUP}. Then, it initiates the simulation by first
	 * creating the pseudo random number generator by calling the
	 * {@link #onCreateRandomGenerator()}. Then, it launches the simulation model,
	 * environment, scheduler, and viewers by calling in order the {@link #onLaunchModel()},
	 * {@link #onLaunchEnvironment()}, {@link #onLaunchScheduler()},
	 * {@link #onLaunchViewers()}, and {@link #onLaunchSimulatedAgents()} methods. Finally, it
	 * calls the {@link #onSimulationStart()} method. If the start parameter is set to
	 * <code>true</code>, it starts the simulation by calling the {@link #startSimulation()}
	 * <p>
	 * By default the logger level is set to {@link Level#INFO}.
	 */

	@Override
	protected void onActivation() {
		getLogger().setLevel(Level.INFO);
		initCommunityName();
		createGroup(getCommunity(), getEngineGroup());
		createGroup(getCommunity(), getModelGroup());
		requestRole(getCommunity(), getEngineGroup(), LAUNCHER_ROLE);
		viewersProbe = new Probe(getEngineGroup(), SimuOrganization.VIEWER_ROLE);
		addProbe(viewersProbe);
		getLogger().info(() -> " Launching simulation! < " + simuCommunity + " >");
		onInitializeSimulationSeedIndex();
		onCreateRandomGenerator();
		onLaunchModel();
		onLaunchEnvironment();
		onLaunchScheduler();
		onLaunchSimulatedAgents();
		onLaunchViewers();
		onSimulationStart();
		getViewers().forEach(v -> ((Viewer) v).display());
		if (getKernelConfig().getBoolean("start")) {
			startSimulation();
		}
	}

	/**
	 * Creates the pseudo random number generator that has to be used by the simulation. The
	 * seed index is taken from the kernel configuration. If the seed index is not set, the
	 * default value is 0.
	 * 
	 * @return the pseudo random number generator that will be used by the simulation
	 */
	public RandomGenerator onCreateRandomGenerator() {
		randomGenerator = Randomness.getBestRandomGeneratorFactory().create(getPRNGSeed());
		getLogger().info(() -> " PRNG < " + randomGenerator.getClass().getSimpleName() + " ; seed index ->  "
				+ getPRNGSeedIndex() + " >");
		return randomGenerator;
	}

	/**
	 * Sets the seed which is used to create a PRNG. The actual seed that will be used will be
	 * computed by adding seedIndex to the built-in long (0xFEDCBA0987654321L), which is used
	 * as initial seed. This is done so that the obtained long respects the many seed bits
	 * characteristic. Moreover it is known that a good practice, considering how seeds should
	 * be chosen, is to take them in sequence. See this blog: <a href=
	 * "https://www.johndcook.com/blog/2016/01/29/random-number-generator-seed-mistakes">Random
	 * number generator seed mistakes</a> So a simulation suite can be obtained by using this
	 * method with a consecutive list of int: 1, 2, 3...
	 * 
	 * @param seedIndex the seed index to set. Privilege the use of sequence of integers such
	 *                  as 0, 1, 2...
	 */
	public void setPRNGSeedIndex(int seedIndex) {
		simulationIndex = seedIndex;
		simulationSeed += BASE_SEED + seedIndex;
	}

	/**
	 * Returns the seed used to create the PRNG.
	 * 
	 * @return the seed used to create the PRNG
	 */
	public long getPRNGSeedIndex() {
		return simulationIndex;
	}

	/**
	 * Returns the seed used to create the PRNG.
	 * 
	 * @return the seed used to create the PRNG
	 */
	private long getPRNGSeed() {
		return simulationSeed;
	}

	/**
	 * Initializes the simulation seed index. By default, the seed index is taken from the
	 * kernel configuration, and if not set the seed index is set 0.
	 */
	public void onInitializeSimulationSeedIndex() {
		int seed = getKernelConfig().getInt("seed");
		seed = seed == Integer.MIN_VALUE ? 0 : seed;
		setPRNGSeedIndex(seed);
		getLogger().finer(() -> " < Simulation seed set to -> " + getPRNGSeedIndex() + " >");
	}

	/**
	 *
	 * Called before the simulation starts. By default, it calls the
	 * {@link Scheduler#onSimulationStart()}, {@link SimuModel#onSimulationStart()},
	 * {@link SimuEnvironment#onSimulationStart()}, and {@link SimuAgent#onSimulationStart()}
	 * methods for each viewer.
	 * <p>
	 * This method can be overridden by the user to define fine tuning of the simulation
	 * initialization.
	 */
	@Override
	public void onSimulationStart() {
		getScheduler().onSimulationStart();
		getModel().onSimulationStart();
		getEnvironment().onSimulationStart();
		for (SimuAgent viewer : getViewers()) {
			viewer.onSimulationStart();
		}
	}

	/**
	 * Returns the seed used to create the PRNG.
	 * 
	 * @param <M> the type of the model
	 * @return the model agent for this simulation
	 */
	protected <M extends SimuModel> M onLaunchModel() {
		M m = launchAgent(getEngineClass(ENGINE.MODEL), Integer.MAX_VALUE);
		getLogger().info(() -> getModel() + LAUNCHED);
		return m;
	}

	/**
	 * Launches the simulation environment agent and logs the event.
	 *
	 * @param <E> the type of the environment
	 * @return the environment agent for this simulation
	 */
	protected <E extends SimuEnvironment> E onLaunchEnvironment() {
		E e = launchAgent(getEngineClass(ENGINE.ENVIRONMENT), Integer.MAX_VALUE);
		getLogger().info(() -> getEnvironment() + LAUNCHED);
		return e;
	}

	/**
	 * Launches the simulation scheduler agent and logs the event.
	 *
	 * @param <S> the type of the scheduler
	 * @return the scheduler agent for this simulation
	 */
	protected <S extends Scheduler<?>> S onLaunchScheduler() {
		S s = launchAgent(getEngineClass(ENGINE.SCHEDULER), Integer.MAX_VALUE);
		getLogger().info(() -> getScheduler() + LAUNCHED);
		return s;
	}

	/**
	 * Launches the simulation viewers agents and logs their launch.
	 */
	protected void onLaunchViewers() {
		if (!getKernelConfig().getBoolean("headless")) {
			for (String viewer : getViewerClasses()) {
				SimuAgent v = launchAgent(viewer, Integer.MAX_VALUE);
				getLogger().info(() -> v + LAUNCHED);
			}
		}
	}

	/**
	 * Launches the simulation agents.
	 */
	protected abstract void onLaunchSimulatedAgents();

	/**
	 * Start simulation.
	 */
	protected void startSimulation() {
		getScheduler().receiveMessage(new SchedulingMessage(SchedulingAction.RUN));
	}

	/**
	 * Gets the scheduler.
	 *
	 * @param <S> the generic type
	 * @return the scheduler
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <S extends Scheduler<?>> S getScheduler() {
		return (S) scheduler;
	}

	/**
	 * Gets the environment.
	 *
	 * @param <E> the element type
	 * @return the environment
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <E extends SimuEnvironment> E getEnvironment() {
		return (E) environment;
	}

	/**
	 * Gets the model.
	 *
	 * @param <M> the generic type
	 * @return the model
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <M extends SimuModel> M getModel() {
		return (M) model;
	}

	private List<String> getViewerClasses() {
		List<String> viewersClasses = getKernelConfig().getList(String.class, "viewers", Collections.emptyList());
		if (viewersClasses.isEmpty()) {
			Class<? extends SimuAgent>[] classes = getEngineAgentsAnnotation().viewers();
			for (Class<? extends SimuAgent> target : classes) {
				viewersClasses.add(target.getName());
			}
		}
		return viewersClasses;
	}

	private EngineAgents getEngineAgentsAnnotation() {
		EngineAgents annotation = getClass().getDeclaredAnnotation(EngineAgents.class);
		if (annotation == null) {
			getLogger().warning(() -> "ENGINE AGENTS UNDEFINED! -> Fallback mode");
			annotation = getClass().getAnnotation(EngineAgents.class);
		}
		return annotation;
	}

	/**
	 * use reflection to use the name of the annotation to find out the class to launch
	 * 
	 * @param agentClass
	 * @return the specified class or the one of fallbackmode
	 */
	private String getEngineClass(ENGINE agentClass) {
		String engineRole = agentClass.name().toLowerCase();
		String targetClass = getKernelConfig().getString(engineRole);
		if (targetClass == null) {
			EngineAgents annotation = getClass().getDeclaredAnnotation(EngineAgents.class);
			if (annotation == null) {
				getLogger().warning(() -> engineRole + " UNDEFINED! -> Fallback mode");
				annotation = getClass().getAnnotation(EngineAgents.class);
			}
			try {
				targetClass = getValueFromName(agentClass, annotation);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				getLogger().severe(() -> "Cannot find or instantiate engine class: " + agentClass);
			}
		}
		return targetClass;
	}

	private static String getValueFromName(ENGINE agent, EngineAgents annotation)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return ((Class<?>) annotation.getClass().getMethod(agent.name().toLowerCase()).invoke(annotation)).getName();
	}

	/**
	 * Gets the engine agents args from.
	 *
	 * @param target the target
	 * @return the engine agents args from
	 * @throws IllegalAccessException    the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 * @throws NoSuchMethodException     the no such method exception
	 * @throws SecurityException         the security exception
	 */
	public static final List<String> getEngineAgentsArgsFrom(Class<?> target)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
		EngineAgents annotation = target.getAnnotation(EngineAgents.class);
		List<String> l = new ArrayList<>();
		if (annotation != null) {
			ENGINE[] tab = ENGINE.values();
			for (int i = 0; i < tab.length; i++) {
				String cName = getValueFromName(tab[i], annotation);
				if (!(cName.equals(SimuEnvironment.class.getName()) || cName.equals(SimuModel.class.getName())
						|| cName.equals(TickBasedScheduler.class.getName()))) {
					l.add("--" + tab[i].name().toLowerCase());
					l.add(cName);
				}
			}
			Class<? extends SimuAgent>[] viewersList = annotation.viewers();
			for (Class<? extends SimuAgent> targetClass : viewersList) {
				l.add("-v");
				l.add(targetClass.getName());
			}
		}
		return l;
	}

	/**
	 * Returns the model group associated with the simulation.
	 *
	 * @return the model group
	 */
	@Override
	public String getModelGroup() {
		return MODEL_GROUP;
	}

	/**
	 * Returns the name of the simulation community.
	 *
	 * @return the community
	 */
	@Override
	public String getCommunity() {
		return simuCommunity;
	}

	/**
	 * Initializes the name of the community which will be used for defining the organization
	 * in which the simulation participants will operate. By default, the class name of the
	 * SimuLauncher is used. It should be different for each instantiated simuLauncher
	 */
	private void initCommunityName() {
		int i = 1;
		while (getOrganization().isCommunity(simuCommunity)) {
			simuCommunity += "" + (++i);// NOSONAR see JEP 280: Indify String Concatenation https://openjdk.org/jeps/280
		}
	}

	/**
	 * Returns the engine group associated with the simulation.
	 *
	 * @return the name of the engine group
	 */
	@Override
	public String getEngineGroup() {
		return ENGINE_GROUP;
	}

	/**
	 * Sets the environment.
	 *
	 * @param environment the environment to set
	 */
	void setEnvironment(SimuEnvironment environment) {
		this.environment = environment;
	}

	/**
	 * Sets the scheduler.
	 *
	 * @param scheduler the scheduler to set
	 */
	void setScheduler(Scheduler<? extends SimuTimer<?>> scheduler) {
		this.scheduler = scheduler;
	}

	/**
	 * Sets the model.
	 *
	 * @param model the model to set
	 */
	void setModel(SimuModel model) {
		this.model = model;
	}

	/**
	 * Returns the the viewers that are actually running.
	 *
	 * @return the running viewers
	 */
	@Override
	public List<SimuAgent> getViewers() {
		return viewersProbe.getAgents();
	}

	/**
	 * Returns the pseudo random number generator that has to be used by the simulation
	 * agents.
	 * 
	 * @return the pseudo random number generator of the simulation
	 */
	@Override
	public RandomGenerator prng() {
		return randomGenerator;
	}

	/**
	 * Sets the pseudo random number generator that has to be used by the simulation.
	 * 
	 * @param randomGenerator the randomGenerator to set
	 */
	public void setRandomGnerator(RandomGenerator randomGenerator) {
		this.randomGenerator = randomGenerator;
	}

}
