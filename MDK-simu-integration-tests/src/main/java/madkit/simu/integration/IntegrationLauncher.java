package madkit.simu.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

import madkit.kernel.Scheduler;
import madkit.simu.integration.common.IntegrationAgent;
import madkit.simu.integration.common.IntegrationEnvironment;
import madkit.simu.integration.common.IntegrationModel;
import madkit.simu.integration.common.IntegrationScheduler;
import madkit.simulation.EngineAgents;
import madkit.simulation.SimuEnvironment;
import madkit.simulation.SimuLauncher;
import madkit.simulation.SimuModel;

/**
 * Minimal headless launcher used to validate the public simulation consumer contract from
 * a separate named module.
 */
@EngineAgents(scheduler = IntegrationScheduler.class, environment = IntegrationEnvironment.class, model = IntegrationModel.class)
public class IntegrationLauncher extends SimuLauncher {

	private final List<String> events = new ArrayList<>();

	@Override
	protected void onActivation() {
		getLogger().setLevel(java.util.logging.Level.ALL);
		super.onActivation();
	}

	@Override
	protected void onInitializeSimulationSeedIndex() {
		super.onInitializeSimulationSeedIndex();
		events.add("seed-index");
	}

	@Override
	protected RandomGenerator onCreateRandomGenerator() {
		RandomGenerator generator = super.onCreateRandomGenerator();
		events.add("random-generator");
		return generator;
	}

	@Override
	protected <M extends SimuModel> M onLaunchModel() {
		M model = super.onLaunchModel();
		events.add("model");
		return model;
	}

	@Override
	protected <E extends SimuEnvironment> E onLaunchEnvironment() {
		E environment = super.onLaunchEnvironment();
		events.add("environment");
		return environment;
	}

	@Override
	protected <S extends Scheduler<?>> S onLaunchScheduler() {
		S scheduler = super.onLaunchScheduler();
		events.add("scheduler");
		return scheduler;
	}

	@Override
	protected void onLaunchSimulatedAgents() {
		launchAgent(new IntegrationAgent());
		events.add("simulated-agents");
	}

	@Override
	protected void onLaunchViewers() {
		super.onLaunchViewers();
		events.add("viewers");
	}

	@Override
	public void onSetupSimulation() {
		super.onSetupSimulation();
		events.add("simulation-start");
	}

	/** @return a snapshot of the observed startup lifecycle */
	public List<String> events() {
		return List.copyOf(events);
	}

	/** Launches this consumer application through the MaDKit command line. */
	public static void main(String[] args) {
		executeThisAgent();
	}
}
