/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.madkit.simulation.deadlock;

import static com.distrimind.madkit.kernel.JunitMadkit.GROUP;
import static com.distrimind.madkit.kernel.JunitMadkit.ROLE;
import static com.distrimind.madkit.kernel.JunitMadkit.ROLE2;

import com.distrimind.madkit.kernel.Group;
import com.distrimind.madkit.kernel.Probe;
import com.distrimind.madkit.kernel.Watcher;
import com.distrimind.madkit.testing.util.agent.SimulatedAgent;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.0.15
 * @since MadkitLanEdition 1.0
 * @version 1.0
 * 
 */
public class PerceptionAgent extends Watcher {

	Probe<SimulatedAgent2> agents2probe;

	public void activate() {
		requestRole(GROUP, "perception_agent");
		agents2probe = new Probe<>(GROUP, ROLE2);
		addProbe(new ProbeSAgent(GROUP, ROLE));
		addProbe(agents2probe);
	}

	class ProbeSAgent extends Probe<SimulatedAgent> {

		public ProbeSAgent(Group _arg1, String _arg2) {
			super(_arg1, _arg2);
		}

		public void adding(SimulatedAgent a) {
			launchAgent(new SimulatedAgent2());
			if (Math.random() < 0.01) {
				java.util.List<SimulatedAgent2> l = agents2probe.getCurrentAgentsList();
				killAgent(l.get((int) (Math.random() * l.size())));
			}
		}

		@SuppressWarnings("unused")
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

class SimulatedAgent2 extends SimulatedAgent {
	@Override
	protected void activate() {
		requestRole(GROUP, ROLE2);
	}
}
