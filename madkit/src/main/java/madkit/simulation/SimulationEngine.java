package madkit.simulation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static madkit.simulation.DefaultOrganization.ENGINE_GROUP;
import static madkit.simulation.DefaultOrganization.LAUNCHER_ROLE;
import static madkit.simulation.DefaultOrganization.MODEL_GROUP;
import static madkit.simulation.DefaultOrganization.MODEL_ROLE;

import madkit.action.SchedulingAction;
import madkit.kernel.AbstractScheduler;
import madkit.kernel.Agent;
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
public class SimulationEngine extends Agent implements SimuParticipant {

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
	private List<SimuParticipant> viewers;

	/**
	 * 
	 */
	public SimulationEngine() {
		simuCommunity = getClass().getSimpleName();
		try {
			Field f = Agent.class.getDeclaredField("simuEngine");
			f.setAccessible(true);
			f.set(this, this);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		viewers = new ArrayList<>();
	}

	@Override
	protected void onActivation() {
		getLogger().setLevel(Level.ALL);
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
	protected void createSimulationInstance() {
		launchModel();
		getLogger().fine(() -> getModel() + " launched");
		getLogger().fine(() -> "Launching environment ");
		launchEnvironment();
		getLogger().fine(() -> getEnvironment() + " launched");
		launchScheduler();
		getLogger().fine(() -> getScheduler() + " launched");
		launchSimulatedAgents();
		launchViewers();
		getLogger().fine(() -> getViewers() + " launched");
		onInitialization();
	}

	@Override
	public void onInitialization() {
		getModel().onInitialization();
		getEnvironment().onInitialization();
		for (SimuParticipant viewer : getViewers()) {
			viewer.onInitialization();
		}
		getScheduler().onInitialization();
	}

	protected void launchModel() {
		launchAgent(getEngineClass(ENGINE.MODEL), Integer.MAX_VALUE);
	}

	protected <E extends Environment> E launchEnvironment() {
		return launchAgent(getEngineClass(ENGINE.ENVIRONMENT), Integer.MAX_VALUE);
	}

	protected void launchScheduler() {
		launchAgent(getEngineClass(ENGINE.SCHEDULER), Integer.MAX_VALUE);
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
				SimuParticipant v = launchAgent(viewer, Integer.MAX_VALUE);
				viewers.add(v);
			}
		}
	}

	private List<String> getViewerClasses() {
		List<String> viewersClasses = getKernelConfig().getList(String.class, "viewers", Collections.emptyList());
		if (viewersClasses.isEmpty()) {
			Class<? extends SimuParticipant>[] classes = getEngineAgentsAnnotation().viewers();
			for (Class<? extends SimuParticipant> target : classes) {
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
			Class<? extends SimuParticipant>[] viewersList = annotation.viewers();
			for (Class<? extends SimuParticipant> targetClass : viewersList) {
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
		while (getOrgnization().isCommunity(simuCommunity)) {
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
	public void setScheduler(AbstractScheduler<? extends SimulationTimer<?>> scheduler) {
		this.scheduler = scheduler;
	}

	/**
	 * @param model the model to set
	 */
	void setModel(SimulationModel model) {
		this.model = model;
	}

	@Override
	public List<SimuParticipant> getViewers() {
		return viewers;
	}

}
