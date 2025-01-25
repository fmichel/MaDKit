package madkit.integration;

import org.controlsfx.control.action.Action;

import madkit.action.KernelAction;
import madkit.kernel.Agent;

/**
 * since MaDKit 6.0
 */
public class TestAgent extends Agent{

	@Override
	protected void activate() {
		super.activate();
		setupGUI();
	}
	
	@Override
	protected void live() {
		super.live();
		getLogger().info("runningd");
		super.activate();
		Action action = KernelAction.EXIT.newActionFor(this);
		action = KernelAction.EXIT.newActionFor(this);
		pause(10000);
//		KernelAction.RESTART.request(this);
		getLogger().info("runningdddd");
	}

	public static void main(String[] args) {
//		executeThisAgent("--headless");
		executeThisAgent();
	}
}

