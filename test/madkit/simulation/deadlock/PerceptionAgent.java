/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.simulation.deadlock;

import static madkit.kernel.JunitMadKit.COMMUNITY;
import static madkit.kernel.JunitMadKit.GROUP;
import static madkit.kernel.JunitMadKit.ROLE;
import static madkit.kernel.JunitMadKit.ROLE2;
import madkit.kernel.Probe;
import madkit.kernel.Watcher;
import madkit.testing.util.agent.SimulatedAgent;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.15
 * @version 0.9
 * 
 */
public class PerceptionAgent extends Watcher {

	Probe<SimulatedAgent2>	agents2probe;

	public void activate() {
		requestRole(COMMUNITY, GROUP, "perception_agent");
		agents2probe = new Probe<SimulatedAgent2>(COMMUNITY, GROUP, ROLE2);
		addProbe(new ProbeSAgent(COMMUNITY, GROUP, ROLE));
		addProbe(agents2probe);
	}

	
	
	class ProbeSAgent extends Probe<SimulatedAgent> {

		public ProbeSAgent(String _arg0, String _arg1, String _arg2) {
			super(_arg0, _arg1, _arg2);
		}

		public void adding(SimulatedAgent a) {
			System.out.println("ici1");
			launchAgent(new SimulatedAgent2());
			System.out.println("ici2");
			if (Math.random() < 0.01) {
				java.util.List<SimulatedAgent2> l = agents2probe.getCurrentAgentsList();
				killAgent(l.get((int) (Math.random() * l.size())));
			}
		}

		public void removing(SimulatedAgent a) {
			for (SimulatedAgent b : getCurrentAgentsList()) {
				java.util.List<SimulatedAgent2> l = agents2probe.getCurrentAgentsList();
				killAgent(l.get((int) (Math.random() * l.size())));
				if (Math.random() < 0.01) {
					launchAgent(new SimulatedAgent2());
				}
			}

		}
	}
}

class SimulatedAgent2 extends SimulatedAgent{
	@Override
	protected void activate() {
		requestRole(COMMUNITY, GROUP, ROLE2);
	}
}
