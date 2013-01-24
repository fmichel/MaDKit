package madkit.simulation;

import java.util.List;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Activator;

public class EmptyActivator extends Activator<AbstractAgent> {

	public EmptyActivator(String community, String group, String role) {
		super(community, group, role);
	}

	@Override
	public void execute(List<AbstractAgent> currentAgentsList, Object... args) {
	}

}
