/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MadKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.performance;

import madkit.kernel.AbstractAgent;
import test.util.JUnitBooterAgent;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.6
 * @version 0.9
 * 
 */
public class LaunchPerformance extends JUnitBooterAgent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3751374630788416913L;

	/**
	 * 
	 */
	private void massLaunch() {
		AbstractAgent a;
		if(logger != null){
			logger.info("\n******************* STARTING MASS LAUNCH *******************\n");
		}
		a = launchAgent("madkit.kernel.AbstractAgent",0);
		a.createGroup("test", "group", false, null);
//		System.err.println("begin");
//		for (int i = 0; i < 2000000; i++) {
//			if(i%100000==0){
//				System.err.println("launched "+i);
//				if (logger != null) {
//					logger.info("nb of launched agents " + i);
//				}
//			}
//			launchAgent(new test.madkit.agentLifeCycle.NormalAbstractLife());
//		}
//		System.err.println("done\n\n");
		startTimer();
		System.err.println("begin");
		launchAgentBucket("madkit.agentLifeCycle.NormalAbstractLife", 30100);
//		launchAgentBucket("madkit.kernel.AbstractAgent", 6000100);
		stopTimer("done");
	}

}
