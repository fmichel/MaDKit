package madkit.simulation;

import static madkit.simulation.SimuOrganization.ENGINE_GROUP;
import static madkit.simulation.SimuOrganization.LAUNCHER_ROLE;
import static madkit.simulation.SimuOrganization.MODEL_GROUP;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.random.RandomGenerator;

import madkit.action.SchedulingAction;
import madkit.kernel.Scheduler;
import madkit.messages.SchedulingMessage;
import madkit.random.Randomness;
import madkit.simulation.scheduler.SimuTimer;
import madkit.simulation.scheduler.TickBasedScheduler;

/**
 * Main class for launching a simulation. This class is responsible for initializing the
 * simulation environment, simulation model, and scheduler. It also launches the
 * simulation agents and viewers. This class is intended to be extended by the user to
 * define the simulation engine, if the default setup should customized. The user can
 * define the simulation environment, model, and scheduler classes by overriding the
 * {@link #onLaunchEnvironment()}, {@link #onLaunchModel()}, and
 * {@link #onLaunchScheduler()} methods, respectively. The user can also define the
 * simulation agents and viewers by overriding the {@link #onLaunchSimulatedAgents()} and
 * {@link #onLaunchViewers()} methods, respectively. The user can also define the
 * simulation startup behavior by overriding the {@link #onSimulationStart()} method.
 *
 */
@EngineAgents
public abstract class SimuLauncher extends SimuAgent {

	enum ENGINE {
		SCHEDULER, ENVIRONMENT, MODEL;
	}

	private static final String LAUNCHED = " launched";

	private String simuCommunity;
	private SimuModel model;
	private SimuEnvironment environment;
	private Scheduler<? extends SimuTimer<?>> scheduler;
	private List<SimuAgent> viewers;

	private RandomGenerator randomGenerator;
	/**
	 * Need a many seed bits, and then increment on it
	 */
	private static final long BASE_SEED = 0xFEDCBA0987654321L;
	private long simulationSeed = BASE_SEED;

	private int simulationIndex;

	/**
	 * Default constructor. It initializes the simulation community name to the
	 * class name of the simulation engine.
	 */
	protected SimuLauncher() {
		simuCommunity = getClass().getSimpleName();
		simuLauncher = this;
		viewers = new ArrayList<>();
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
		getLogger().info(() -> " Launching simulation! < " + simuCommunity + " >");
		onInitializeSimulationSeedIndex();
		onCreateRandomGenerator();
		onLaunchModel();
		onLaunchEnvironment();
		onLaunchScheduler();
		onLaunchSimulatedAgents();
		onLaunchViewers();
		onSimulationStart();
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
		getLogger().info(
				() -> " PRNG < " + randomGenerator.getClass().getSimpleName() + " ; seed index ->  " + getPRNGSeedIndex()
						+ " >");
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
	 * Launches the simulation model agent and logs the event
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
	 * Launches the simulation environment agent and logs the event
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
	 * Launches the simulation scheduler agent and logs the event
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
	 * Launches the simulation viewers agents and logs their launch
	 */
	protected void onLaunchViewers() {
		if (!getKernelConfig().getBoolean("headless")) {
			for (String viewer : getViewerClasses()) {
				SimuAgent v = launchAgent(viewer, Integer.MAX_VALUE);
				getLogger().info(() -> v + LAUNCHED);
				viewers.add(v);
			}
		}
	}

	/**
	 * Launches the simulation agents
	 */
	protected abstract void onLaunchSimulatedAgents();

	@Override
	public void onSimulationStart() {
		getModel().onSimulationStart();
		getEnvironment().onSimulationStart();
		for (SimuAgent viewer : getViewers()) {
			viewer.onSimulationStart();
		}
		getScheduler().onSimulationStart();
	}

	protected void startSimulation() {
		getScheduler().receiveMessage(new SchedulingMessage(SchedulingAction.RUN));
	}

	/**
	 * @return the scheduler
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <S extends Scheduler<?>> S getScheduler() {
		return (S) scheduler;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends SimuEnvironment> E getEnvironment() {
		return (E) environment;
	}

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
	 * use reflection to use the name of the annotation to find out the class to
	 * launch
	 * 
	 * @param agentClass
	 * @return the specified class or the one of fallbackmode
	 */
	private final String getEngineClass(ENGINE agentClass) {
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
				e.printStackTrace();
			}
		}
		return targetClass;
	}

	private static final String getValueFromName(ENGINE agent, EngineAgents annotation)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
			return ((Class<?>) annotation.getClass().getMethod(agent.name().toLowerCase()).invoke(annotation)).getName();
	}

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
	 */
	@Override
	public String getModelGroup() {
		return MODEL_GROUP;
	}

	/**
	 * Returns the name of the simulation community.
	 */
	@Override
	public String getCommunity() {
		return simuCommunity;
	}

	/**
	 * Initializes the name of the community which will be used for defining the
	 * organization in which the simulation participants will operate. By default,
	 * the class name of the SimuLauncher is used. It should be different for each
	 * instantiated simuLauncher
	 */
	private void initCommunityName() {
		int i = 1;
		while (getOrganization().isCommunity(simuCommunity)) {
			simuCommunity += "" + (++i);// NOSONAR see JEP 280: Indify String Concatenation https://openjdk.org/jeps/280
		}
	}

	/**
	 * Returns the engine group associated with the simulation
	 * 
	 * @return the name of the engine group
	 */
	@Override
	public String getEngineGroup() {
		return ENGINE_GROUP;
	}

	/**
	 * 
	 * @param environment the environment to set
	 */
	void setEnvironment(SimuEnvironment environment) {
		this.environment = environment;
	}

	/**
	 * @param scheduler the scheduler to set
	 */
	void setScheduler(Scheduler<? extends SimuTimer<?>> scheduler) {
		this.scheduler = scheduler;
	}

	/**
	 * @param model the model to set
	 */
	void setModel(SimuModel model) {
		this.model = model;
	}

	/**
	 * @return the viewers launched by this launcher
	 */
	@Override
	public List<SimuAgent> getViewers() {
		return viewers;
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
