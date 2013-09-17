/*
 * Copyright 1997-2013 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MaDKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.performance;

import java.util.List;
import java.util.logging.Level;

import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.LevelOption;
import madkit.testing.util.agent.NormalAA;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class MassLaunchBench extends JunitMadkit {

	@Test
	public void massAALaunchWithBucketAndNoRoles() {
		final int tries = 1;//should be at least 100 to bench, this has no use for only testing
		launchTest(new AbstractAgent() {
			protected void activate() {
				setLogLevel(Level.INFO);
				long total = 0;
				createGroup("test", "group", false, null);
				for (int i = 0; i < tries; i++) {
					startTimer();
					assertNotNull(launchAgentBucket(AbstractAgent.class.getName(), 1000000));
					total += stopTimer("bucket launch time = ");
				}
				if(logger != null)
					logger.info("average launch time = "+(total / (tries * 1000000))+" ms");
			}
		});
	}

	@Test
	public void massAALaunchWithBucketRoles() {
		final int tries = 1;//should be at least 100 to bench, this has no use for only testing
		launchTest(new AbstractAgent() {
			protected void activate() {
				setLogLevel(Level.INFO);
				long total = 0;
//				createGroup("test", "group", false, null);
				for (int i = 0; i < tries; i++) {
					startTimer();
					assertNotNull(launchAgentBucket(AbstractAgent.class.getName(), 1000000, COMMUNITY+","+GROUP+","+ROLE));
					total += stopTimer("bucket launch time = ");
				}
				if(logger != null)
					logger.info("average launch time = "+(total / (tries * 1000000))+" ms");
			}
		});
	}

	@Test
	public void massAALaunchWithBucketRolesAndRequestIgnored() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				System.err.println("begin");
				startTimer();
				List<AbstractAgent> l =  launchAgentBucket(MiniAgent.class.getName(), 1000000,COMMUNITY+","+GROUP+","+ROLE);
				stopTimer("bucket launch time = ");
				System.err.println("done\n\n");
				System.err.println(l.get(0).requestRole(COMMUNITY, GROUP, ROLE));
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
				startTimer();
				System.err.println("begin");
				launchAgentBucket(NormalAA.class.getName(), 30100);
				stopTimer("done ");
			}
		});
	}
}