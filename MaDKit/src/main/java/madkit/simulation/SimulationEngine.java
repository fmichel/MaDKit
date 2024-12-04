package madkit.simulation;

import static madkit.simulation.DefaultOrganization.ENGINE_GROUP;
import static madkit.simulation.DefaultOrganization.LAUNCHER_ROLE;
import static madkit.simulation.DefaultOrganization.MODEL_GROUP;
import static madkit.simulation.DefaultOrganization.MODEL_ROLE;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import madkit.action.SchedulingAction;
import madkit.kernel.AbstractScheduler;
import madkit.messages.SchedulingMessage;
import madkit.simulation.scheduler.TickBasedScheduler;

/**
 * Main class for initializing a MaDKit simulation.
 * 
 * 
 * @author Fabien Michel
 *
 */
@EngineAgents
public class SimulationEngine extends SimuAgent {

	/**
	 * @author Fabien Michel
	 * 
	 *         since MaDKit 6.0
	 */
	enum ENGINE {
		SCHEDULER, ENVIRONMENT, MODEL;
	}

	private String simuCommunity;

	private SimulationModel model;
	private Environment environment;
	private AbstractScheduler<? extends SimulationTimer<?>> scheduler;
	private List<SimuAgent> viewers;

	/**
	 * 
	 */
	public SimulationEngine() {
		simuCommunity = getClass().getSimpleName();
		simuEngine = this;
		viewers = new ArrayList<>();
	}

	@Override
	protected void onActivation() {
		getLogger().setLevel(Level.INFO);
		initCommunityName();
		createGroup(getCommunity(), getEngineGroup());
		createGroup(getCommunity(), getModelGroup());
		requestRole(getCommunity(), getEngineGroup(), LAUNCHER_ROLE);
		requestRole(getCommunity(), getEngineGroup(), MODEL_ROLE);
		getLogger().info(() -> " Started! Community is < " + simuCommunity + " >");
		createSimulationInstance();
	}

	/**
	 * Launches the simulation engine and the simulated agents.
	 *
	 * <ul>
	 * <li>1. launch //TODO
	 * 
	 * 1. launch core agents : Environment -> Scheduler -> Simulation model 2.
	 * launchConfigTurtles : launchXmlTurtles -> launch args turtles 3. launch
	 * viewers 4. launch xml Agent tag
	 */
	private void createSimulationInstance() {
		onLaunchModel();
		getLogger().fine(() -> getModel() + " launched");
		getLogger().fine(() -> "Launching environment ");
		onLaunchEnvironment();
		getLogger().fine(() -> getEnvironment() + " launched");
		onLaunchScheduler();
		getLogger().fine(() -> getScheduler() + " launched");
		launchSimulatedAgents();
		launchViewers();
		getLogger().fine(() -> getViewers() + " launched");
		onSimuStartup();
		if (getKernelConfig().getBoolean("start")) {
			startSimulation();
		}
	}

	@Override
	public void onSimuStartup() {
		getModel().onSimuStartup();
		getEnvironment().onSimuStartup();
		for (SimuAgent viewer : getViewers()) {
			viewer.onSimuStartup();
		}
		getScheduler().onSimuStartup();
	}

	protected <M extends SimulationModel> M onLaunchModel() {
		return launchAgent(getEngineClass(ENGINE.MODEL), Integer.MAX_VALUE);
	}

	protected <E extends Environment> E onLaunchEnvironment() {
		return launchAgent(getEngineClass(ENGINE.ENVIRONMENT), Integer.MAX_VALUE);
	}

	protected <S extends AbstractScheduler<?>> S onLaunchScheduler() {
		return launchAgent(getEngineClass(ENGINE.SCHEDULER), Integer.MAX_VALUE);
	}

	/**
	 * 
	 */
	protected void launchSimulatedAgents() {

	}

	/**
	 * @return the scheduler
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <S extends AbstractScheduler<?>> S getScheduler() {
		return (S) scheduler;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Environment> E getEnvironment() {
		return (E) environment;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <M extends SimulationModel> M getModel() {
		return (M) model;
	}

	protected void launchViewers() {
		if (!getKernelConfig().getBoolean("headless")) {
			for (String viewer : getViewerClasses()) {
				SimuAgent v = launchAgent(viewer, Integer.MAX_VALUE);
				viewers.add(v);
			}
		}
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
			targetClass = getValueFromName(agentClass, annotation);
		}
		return targetClass;
	}

	private static final String getValueFromName(ENGINE agent, EngineAgents annotation) {
		try {
			return ((Class<?>) annotation.getClass().getMethod(agent.name().toLowerCase()).invoke(annotation)).getName();
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static final List<String> getEngineAgentsArgsFrom(Class<?> target) {
		EngineAgents annotation = target.getAnnotation(EngineAgents.class);
		List<String> l = new ArrayList<>();
		if (annotation != null) {
			ENGINE[] tab = ENGINE.values();
			for (int i = 0; i < tab.length; i++) {
				String cName = getValueFromName(tab[i], annotation);
				if (!(cName.equals(Environment.class.getName()) || cName.equals(SimulationModel.class.getName())
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

	@Override
	public String getModelGroup() {
		return MODEL_GROUP;
	}

	protected void startSimulation() {
		getScheduler().receiveMessage(new SchedulingMessage(SchedulingAction.RUN));
	}

	@Override
	public String getCommunity() {
		return simuCommunity;
	}

	/**
	 * Initializes the name of the community which will be used for defining the
	 * organization in which the simulation participants will operate. By default,
	 * the class name of the SimuEngine is used. It should be different for each
	 * instantiated simuEngine
	 */
	private void initCommunityName() {
		int i = 1;
		while (getOrganization().isCommunity(simuCommunity)) {
			simuCommunity += "" + (++i);
		}
	}

	public static void main(String[] args) {
		executeThisAgent();
	}

	/**
	 * @return
	 */
	@Override
	public String getEngineGroup() {
		return ENGINE_GROUP;
	}

	/**
	 * @param environment the environment to set
	 */
	void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	/**
	 * @param scheduler the scheduler to set
	 */
	void setScheduler(AbstractScheduler<? extends SimulationTimer<?>> scheduler) {
		this.scheduler = scheduler;
	}

	/**
	 * @param model the model to set
	 */
	void setModel(SimulationModel model) {
		this.model = model;
	}

	public List<SimuAgent> getViewers() {
		return viewers;
	}

}
