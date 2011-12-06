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

import java.util.ArrayList;
import java.util.logging.Level;

import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Madkit.LevelOption;
import madkit.testing.util.agent.NormalAA;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.10
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class MassLaunchBench extends JunitMadKit {

	@Test
	public void massAALaunchWithBucket() {// TODO more cases
		launchTest(new AbstractAgent() {
			protected void activate() {
				createGroup("test", "group", false, null);
				System.err.println("begin");
				startTimer();
				// launchAgentBucket("madkit.agentLifeCycle.NormalAbstractLife",
				// 3000100);
				launchAgentBucketWithRoles("madkit.kernel.AbstractAgent", 1000000, new ArrayList<String>());
				stopTimer("bucket launch time = ");
				System.err.println("done\n\n");
				// JUnitBooter.stopTimer("old launch time = "); // 6000000 min = 7s
				// startTimer();
				// ArrayList<AbstractAgent> agents = new
				// ArrayList<AbstractAgent>(6000000);
				// for (int i = 6000000-1; i >=0 ; i--) {
				// if(i%1000000==0)
				// System.err.println("launched "+i);
				// agents.add(new AbstractAgent());
				// }
				// ArrayList<AbstractAgent> agents = new
				// ArrayList<AbstractAgent>(6000000);
				// for (int i = 6000000-1; i >=0 ; i--) {
				// agents.add(new AbstractAgent());
				// }
				// stopTimer("old launch time = ");
			}
		});
	}

	@Test
	public void massAALaunch() {// TODO more cases
		addMadkitArgs(LevelOption.agentLogLevel.toString(), "OFF");
		launchTest(new AbstractAgent() {
			protected void activate() {
				setLogLevel(Level.OFF);
				createGroup("test", "group", false, null);
				System.err.println("begin");
				long total = 0;
				int j = 0;
				for (j = 0; j < 4; j++) {
					startTimer();
					for (int i = 0; i < 100000; i++) {
						launchAgent(new AbstractAgent());
					}
					total += stopTimer("launch time = ");
				}
				System.err.println("done\n\naverage time is " + (total / (j * 1000000)) + " ms");
			}
		});
	}

	@Test
	public void massNormalLifeLaunch() {// TODO more cases
		launchTest(new AbstractAgent() {
			protected void activate() {
				AbstractAgent a;
				if (logger != null) {
					logger.info("\n******************* STARTING MASS LAUNCH *******************\n");
				}
				a = launchAgent("madkit.kernel.AbstractAgent");
				a.createGroup("test", "group", false, null);
				// System.err.println("begin");
				// for (int i = 0; i < 2000000; i++) {
				// if(i%100000==0){
				// System.err.println("launched "+i);
				// if (logger != null) {
				// logger.info("nb of launched agents " + i);
				// }
				// }
				// launchAgent(new test.madkit.agentLifeCycle.NormalAbstractLife());
				// }
				// System.err.println("done\n\n");
				startTimer();
				System.err.println("begin");
				launchAgentBucket(NormalAA.class.getName(), 30100);
				// launchAgentBucket("madkit.kernel.AbstractAgent", 6000100);
				stopTimer("done ");
			}
		});
	}
}