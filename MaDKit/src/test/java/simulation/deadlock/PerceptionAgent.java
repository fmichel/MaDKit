
package simulation.deadlock;

import static madkit.kernel.JunitMadkit.COMMUNITY;
import static madkit.kernel.JunitMadkit.GROUP;
import static madkit.kernel.JunitMadkit.ROLE;
import static madkit.kernel.JunitMadkit.ROLE2;

import madkit.kernel.Agent;
import madkit.kernel.Probe;
import madkit.kernel.Watcher;
import madkit.test.agents.EmptyAgent;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.15
 * @version 0.9
 * 
 */
//FIXME
public class PerceptionAgent extends Watcher {

	Probe agents2probe;

	@Override
	public void onActivation() {
		requestRole(COMMUNITY, GROUP, "perception_agent");
		agents2probe = new Probe(COMMUNITY, GROUP, ROLE2);
		addProbe(new ProbeSAgent(COMMUNITY, GROUP, ROLE));
		addProbe(agents2probe);
	}

	class ProbeSAgent extends Probe {

		public ProbeSAgent(String _arg0, String _arg1, String _arg2) {
			super(_arg0, _arg1, _arg2);
		}

		public void adding(SAgent a) {
			System.out.println("ici1");
			launchAgent(new EmptyAgent());
			System.out.println("ici2");
			if (Math.random() < 0.01) {
				java.util.List<SAgent> l = agents2probe.getAgents();
				killAgent(l.get((int) (Math.random() * l.size())));
			}
		}

		@SuppressWarnings("unused")
		public void removing(SAgent a) {
			for (Agent b : getAgents()) {
				java.util.List<SAgent> l = agents2probe.getAgents();
				killAgent(l.get((int) (Math.random() * l.size())));
				if (Math.random() < 0.01) {
					launchAgent(new EmptyAgent());
				}
			}

		}
	}
}

class SAgent extends Agent {
	@Override
	protected void onActivation() {
		requestRole(COMMUNITY, GROUP, ROLE2);
	}
}
