package madkit.simulation;

import madkit.kernel.Activator;
import madkit.kernel.Agent;

/**
 * @author Fabien Michel
 */
public class EmptyActivator extends Activator {

	public EmptyActivator(String group, String role) {
		super(group, role);
	}

	@Override
	protected void onAdding(Agent agent) {
		super.onAdding(agent);
		System.err.println(this + " adding agent -> " + agent);
	}

	@Override
	protected void onRemoving(Agent agent) {
		super.onRemoving(agent);
		System.err.println(this + " removing agent -> " + agent);
	}

	@Override
	public void execute(Object... args) {
	}

}
