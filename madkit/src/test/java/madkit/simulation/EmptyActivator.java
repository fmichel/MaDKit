package madkit.simulation;

import madkit.kernel.Agent;
import madkit.kernel.Activator;

/**
 * @author Fabien Michel
 */
public class EmptyActivator extends Activator {

	public EmptyActivator(String community, String group, String role) {
		super(community, group, role);
	}

	@Override
	protected void adding(Agent agent) {
		super.adding(agent);
		System.err.println(this + " adding agent -> " + agent);
	}

	@Override
	protected void removing(Agent agent) {
		super.removing(agent);
		System.err.println(this + " removing agent -> " + agent);
	}

	@Override
	public void execute(Object... args) {
	}

}
