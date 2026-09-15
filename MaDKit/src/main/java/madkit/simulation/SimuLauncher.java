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
import madkit.gui.UIProperty;
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
 * simulation agents and viewers. See {@link #onActivation()} for a detailed description
 * of the simulation initialization process.
 * <p>
 * This class is intended to be extended by the user to define the simulation engine, if
 * the default setup should customized. The user can define the simulation environment,
 * model, and scheduler classes by overriding the {@link #onLaunchEnvironment()},
 * {@link #onLaunchModel()}, and {@link #onLaunchScheduler()} methods, respectively. The
 * user can also define the simulation agents and viewers by overriding the
 * {@link #onLaunchSimulatedAgents()} and {@link #onLaunchViewers()} methods,
 * respectively. The user can also define the simulation startup behavior by overriding
 * the {@link #onSetupSimulation()} method.
 * <p>
 * Crucially, this class is also responsible for initializing the pseudo random number
 * generator (PRNG) that has to be used by the simulation agents for ensuring the
 * reproducibility of the simulation. The PRNG is initialized with a seed that can be set
 * by the user. The seed is a long integer that can be set by the user by overriding the
 * {@link #onInitializeSimulationSeedIndex()} method. By default, the seed index is 0.
 * <p>
 * 
 * By default its logger level is set to {@link Level#INFO}.
 * 
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
	private static final String LAUNCHING = "Launching -> ";

	private String simuCommunity;
	private SimuModel model;
	private SimuEnvironment environment;
	private Scheduler<? extends SimuTimer<?>> scheduler;

	private RandomGenerator randomGenerator;
	/**
	 * Need a many seed bits, and then increment on it
	 */
	private static final long BASE_SEED = 0xFEDCBA0987654321L;

	@UIProperty(category = "Initialization", displayName = "Random generator seed Index")
	private int prngSeedIndex;

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
	 * {@link SimuOrganization#ENGINE_GROUP}.
	 * 
	 * <p>
	 * Then, it initiates the simulation by first creating the pseudo random number generator
	 * (prng) by calling the {@link #onCreateRandomGenerator()}.
	 * <p>
	 * Then, it launches the engine agents of the simulation in the following order:
	 * <ul>
	 * <li>the model agent by calling the {@link #onLaunchModel()} method
	 * <li>the environment agent by calling the {@link #onLaunchEnvironment()} method
	 * <li>the scheduler agent by calling the {@link #onLaunchScheduler()} method
	 * <li>the viewers agents by calling the {@link #onLaunchViewers()} method
	 * <li>the simulated agents by calling the {@link #onLaunchSimulatedAgents()} method
	 * </ul>
	 * <p>
	 * Then, it calls the {@link #onSetupSimulation()} method.
	 * <p>
	 * Finally, if the start switch is passed on the command line or through the arguments of
	 * the main method, it automatically starts the simulation by calling the
	 * {@link #startSimulation()}, which, by default, send the starting message to the
	 * scheduler.
	 * <p>
	 */

	@Override
	protected void onActivation() {
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
		onSetupSimulation();
		getViewers().forEach(v -> ((Viewer) v).display());
		if (getKernelConfig().getBoolean("start")) {
			startSimulation();
		}
	}

	/**
	 * Creates the pseudo random number generator that has to be used by the simulation. The
	 * seed index is taken using {@link #getSeedIndex()} from the kernel configuration. If the
	 * seed index is not set, the default value is 0.
	 * 
	 * @return the pseudo random number generator that will be used by the simulation
	 */
	protected RandomGenerator onCreateRandomGenerator() {
		randomGenerator = Randomness.getBestRandomGeneratorFactory().create(computePRNGSeed());
		getLogger().info(() -> " PRNG < Algo -> " + randomGenerator.getClass().getSimpleName() + " ; seed index ->  "
				+ getPrngSeedIndex() + " >");
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
	public void setPrngSeedIndex(int seedIndex) {
		this.prngSeedIndex = seedIndex;
	}

	/**
	 * Returns the index used to create the PRNG seed.
	 * 
	 * @return the index used to create the PRNG seed
	 */
	public int getPrngSeedIndex() {
		return prngSeedIndex;
	}

	/**
	 * computes the seed that will be used to create the PRNG by adding the seed index to the
	 * built-in long (0xFEDCBA0987654321L). This is done so that the obtained long respects
	 * the many seed bits characteristic. see #setPRNGSeedIndex(int) for more details and
	 * references about the choice of the seed index.
	 * 
	 */
	private long computePRNGSeed() {
		return BASE_SEED + prngSeedIndex;
	}

	/**
	 * Initializes the simulation seed index. By default, the seed index is taken from the
	 * kernel configuration, and if not set the seed index is set 0, which means that the seed
	 * used to create the PRNG will be the built-in long (0xFEDCBA0987654321L) plus 0, and
	 * thus the same for all simulations. This allows to have a reproducible simulation when
	 * the seed index is not set, and to have different simulations by using different seed
	 * indices, for example by using a sequence of integers such as 0, 1, 2... This method can
	 * be overridden by the user to define a custom seed index initialization.
	 */
	protected void onInitializeSimulationSeedIndex() {
		int seed = getKernelConfig().getInt("seed");
		seed = seed == Integer.MIN_VALUE ? 0 : seed;
		setPrngSeedIndex(seed);
		getLogger().finer(() -> " < Simulation seed index set to -> " + getPrngSeedIndex() + " >");
	}

	/**
	 *
	 * Called just before the simulation starts. By default, it calls in the following order,
	 * the {@link SimuModel#onSetupSimulation()}, {@link SimuEnvironment#onSetupSimulation()},
	 * {@link Scheduler#onSetupSimulation()}, and {@link SimuAgent#onSetupSimulation()}
	 * methods for each viewer.
	 * <p>
	 * This method can be overridden by the user to define fine tuning of the simulation
	 * initialization.
	 * 
	 * <p>
	 * It is worth noting that this method is the latest method called by the launcher on all
	 * the engine agents before giving to the scheduler the control of the simulation. This
	 * provided that, at this point of the launching process, the simulation is ready to
	 * start, i.e.: all the agents participating in the simulation have been launched, and
	 * already have their {@link SimuAgent#onActivation()} method called.
	 * 
	 * <p>
	 * By default, this method is not called on the simulated agents, which are not considered
	 * as engine agents, and thus not known by the launcher. However, the simulated agents
	 * have already been launched and have already had their {@link SimuAgent#onActivation()}
	 * method called, so they are ready to start the simulation as well.
	 * 
	 * <p>
	 * So, it is possible to override this method to call the
	 * {@link SimuAgent#onSetupSimulation()} method on the simulated agents as well, if
	 * needed.
	 * 
	 * <p>
	 * Beware that calling this method programmatically or using the GUI after the simulation
	 * has started will break reproducibility of the simulation. This facility is provided for
	 * testing purposes, and should be used with caution. The only way to reproduce a
	 * simulation is to relaunch it from scratch with the same seed index.
	 * 
	 */
	@Override
	public void onSetupSimulation() {
		getModel().onSetupSimulation();
		getEnvironment().onSetupSimulation();
		getScheduler().onSetupSimulation();
		for (SimuAgent viewer : getViewers()) {
			viewer.onSetupSimulation();
		}
	}

	/**
	 * Called when the simulation ends, as the launcher is killed by the scheduler. By
	 * default, the launcher then kills the model, environment, and viewers, so that their
	 * onEnd() methods are called.
	 * <p>
	 * This method can be overridden by the user to define fine tuning of the simulation
	 * ending process.
	 * <p>
	 */
	@Override
	protected void onEnd() {
		getLogger().info(() -> " Ending simulation! < " + simuCommunity + " >");
		killAgent(getModel());
		killAgent(getEnvironment());
		for (SimuAgent viewer : getViewers()) {
			killAgent(viewer);
		}
	}

//	/**
//	 * Called before the simulation starts. By default, it calls in the following order, the
//	 * {@link Scheduler#onSimulationSetup()}, {@link SimuModel#onSimulationSetup()},
//	 * {@link SimuEnvironment#onSimulationSetup()}, and {@link SimuAgent#onSimulationSetup()}
//	 * methods for each viewer.
//	 * <p>
//	 * This method can be overridden by the user to define fine tuning of the simulation
//	 * initialization.
//	 */
//	protected void onSimulationSetup() {
//		getScheduler().onSimulationSetup();
//		getModel().onSimulationSetup();
//		getEnvironment().onSimulationSetup();
//		for (SimuAgent viewer : getViewers()) {
//			viewer.onSimulationSetup();
//		}
//	}

	/**
	 * Launches the simulation model agent and logs the event. Defaultly, the model class is
	 * taken from the annotation {@link EngineAgents} if it is defined on the class, or from
	 * the kernel configuration. If none of these sources provide a model class, the fallback
	 * mode is used, which means that the model class is set to {@link SimuModel}. This method
	 * could be overridden by the user to define a custom model class or to customize the
	 * launch process of the model agent.
	 * 
	 * @param <M> the type of the model
	 * @return the model agent for this simulation
	 */
	protected <M extends SimuModel> M onLaunchModel() {
		String modelClass = getEngineClass(ENGINE.MODEL);
		getLogger().fine(() -> LAUNCHING + modelClass);
		M m = launchAgent(modelClass, Integer.MAX_VALUE);
		getLogger().fine(() -> getModel() + LAUNCHED);
		return m;
	}

	/**
	 * Launches the simulation environment agent and logs the event. Defaultly, the
	 * environment class is taken from the annotation {@link EngineAgents} if it is defined on
	 * the class, or from the kernel configuration. If none of these sources provide an
	 * environment class, the fallback mode is used, which means that the environment class is
	 * set to {@link SimuEnvironment}. This method could be overridden by the user to define a
	 * custom environment class or to customize the launch process of the environment agent.
	 *
	 * @param <E> the type of the environment
	 * @return the environment agent for this simulation
	 */
	protected <E extends SimuEnvironment> E onLaunchEnvironment() {
		String envClass = getEngineClass(ENGINE.ENVIRONMENT);
		getLogger().fine(() -> LAUNCHING + envClass);
		E e = launchAgent(envClass, Integer.MAX_VALUE);
		getLogger().fine(() -> e + LAUNCHED);
		return e;
	}

	/**
	 * Launches the simulation scheduler agent and logs the event. Defaultly, the scheduler
	 * class is taken from the annotation {@link EngineAgents} if it is defined on the class,
	 * or from the kernel configuration. If none of these sources provide a scheduler class,
	 * the fallback mode is used, which means that the scheduler class is set to
	 * {@link TickBasedScheduler}. This method could be overridden by the user to define a
	 * custom scheduler class or to customize the launch process of the scheduler agent.
	 * 
	 *
	 * @param <S> the type of the scheduler
	 * @return the scheduler agent for this simulation
	 */
	protected <S extends Scheduler<?>> S onLaunchScheduler() {
		String schedulerClass = getEngineClass(ENGINE.SCHEDULER);
		getLogger().fine(() -> LAUNCHING + schedulerClass);
		S s = launchAgent(schedulerClass, Integer.MAX_VALUE);
		getLogger().fine(() -> getScheduler() + LAUNCHED);
		return s;
	}

	/**
	 * Launches the simulation viewers agents and logs their launch. Defaultly, the viewers
	 * classes are taken from the annotation {@link EngineAgents} if it is defined on the
	 * class, or from the kernel configuration. If none of these sources provide viewers
	 * classes, the fallback mode is used, which means that no viewer is launched. This method
	 * could be overridden by the user to define custom viewers classes or to customize the
	 * launch process of the viewers agents.
	 */
	protected void onLaunchViewers() {
		if (!getKernelConfig().getBoolean("headless")) {
			for (String viewer : getViewerClasses()) {
				getLogger().fine(() -> LAUNCHING + viewer);
				SimuAgent v = launchAgent(viewer, Integer.MAX_VALUE);
				if (v == null) {
					throw new IllegalStateException("Could not launch viewer: " + viewer);
				}
				getLogger().fine(() -> v + LAUNCHED);
			}
		}
	}

	/**
	 * Launches the simulation agents. Defaultly, no simulated agent is launched. This method
	 * could be overridden by the user to define custom simulated agents or to customize the
	 * launch process of the simulated agents.
	 */
	protected void onLaunchSimulatedAgents() {
		getLogger().fine(() -> "Launching simulated agents");
	}

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
