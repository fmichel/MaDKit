/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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
package madkit.launching;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.junit.Test;

import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.MadkitClassLoader;
import madkit.performance.MiniAgent;
import madkit.testing.util.agent.MinimalAgent;
import madkit.testing.util.agent.NormalAA;
import madkit.testing.util.agent.PongAgent;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 0.9
 * 
 */
public class MassLaunchBench extends JunitMadkit {

    @Test
    public void massAALaunchWithBucket() {// TODO more cases
	launchTest(new AbstractAgent() {

	    protected void activate() {
		getLogger().setLevel(Level.OFF);
		createGroup("test", "group", false, null);
		System.err.println("begin");
		for (int i = 0; i < 10; i++) {
		    startTimer();
		    // launchAgentBucket("madkit.agentLifeCycle.NormalAbstractLife",
		    // 3000100);
		    launchAgentBucket("madkit.kernel.AbstractAgent", 10_000_000, 11);
		    stopTimer("bucket launch time = ");
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
	    }
	});
    }

    @Test
    public void massAALaunchWithBucketRoles() {
	launchTest(new AbstractAgent() {

	    protected void activate() {
		System.err.println("begin");
		startTimer();
		launchAgentBucket(AbstractAgent.class.getName(), 1000000, COMMUNITY + "," + GROUP + "," + ROLE);
		stopTimer("bucket launch time = ");
		System.err.println("done\n\n");
	    }
	});
    }

    @Test
    public void massAALaunchWithBucketRolesAndRequestIgnored() {
	addMadkitArgs(LevelOption.agentLogLevel.toString(), "OFF");
	launchTest(new AbstractAgent() {

	    protected void activate() {
		System.err.println("begin");
		startTimer();
		List<AbstractAgent> l = launchAgentBucket(MiniAgent.class.getName(), 1000000, COMMUNITY + "," + GROUP + "," + ROLE);
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
		getLogger().setLevel(Level.OFF);
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

    @SuppressWarnings("unchecked")
    @Test
    public void massInstantiation() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
	final Class<? extends AbstractAgent> constructor = (Class<? extends AbstractAgent>) MadkitClassLoader.getLoader().loadClass(AbstractAgent.class.getName());
	int nbOfAgentsPerTask = 1_000_000;
	long sum = 0;
	long min = Long.MAX_VALUE;
	long max = Long.MIN_VALUE;
	int nbIteration = 1500;
	for (int i = 0; i < nbIteration; i++) {
	    startTimer();
	    final List<AbstractAgent> list = new ArrayList<>(nbOfAgentsPerTask);
	    for (int j = nbOfAgentsPerTask; j > 0; j--) {
		list.add(constructor.newInstance());
	    }
	    if (i > 1) {
		long delta = stopTimer("done ");
		sum += delta;
		min = Math.min(min, delta);
		max = Math.max(max, delta);
	    }
	}
	long average = sum / (nbIteration - 2);// the first two are ignored
	System.out.println("Min    : " + min + " ms");
	System.out.println("Average: " + average + " ms");
	System.out.println("Max    : " + max + " ms");
    }
    // as of beltegeuse -Xmx huge
    // Min : 82 ms
    // Average: 101 ms
    // Max : 1329 ms

    @Test
    public void massLaunch() {
	addMadkitArgs(LevelOption.agentLogLevel.toString(), "OFF");
	launchTest(new AbstractAgent() {

	    protected void activate() {
		getLogger().info("\n******************* STARTING MASS LAUNCH *******************\n");
		for (int j = 0; j < 3; j++) {
		    startTimer();
		    System.err.println("begin");
		    for (int i = 0; i < 1_000_000; i++) {
			launchAgent(new AbstractAgent(), 0);
		    }
		    stopTimer("done ");
		}
	    }
	});
    }

    public void massLaunchWithList(final Class<? extends AbstractAgent> agentType) {
	addMadkitArgs(LevelOption.agentLogLevel.toString(), "OFF");
	launchTest(new AbstractAgent() {

	    protected void activate() {
		getLogger().info("\n******************* STARTING MASS LAUNCH *******************\n");
		List<AbstractAgent> agents = new ArrayList<>(1_000_00);
		for (int i = 0; i < 1_000_00; i++) {
		    try {
			agents.add(agentType.newInstance());
		    }
		    catch(InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
		startTimer();
		System.err.println("begin");
		for (AbstractAgent abstractAgent : agents) {
		    launchAgent(abstractAgent);
		}
		stopTimer("done ");
		startTimer();
		System.err.println("begin kills");
		for (AbstractAgent abstractAgent : agents) {
		    killAgent(abstractAgent);
		}
		stopTimer("kill done ");
	    }
	});
    }

    @Test
    public void massLaunchWithList() {
	addMadkitArgs(LevelOption.agentLogLevel.toString(), "OFF");
	launchTest(new AbstractAgent() {

	    protected void activate() {
		getLogger().info("\n******************* STARTING MASS LAUNCH *******************\n");
		List<AbstractAgent> agents = new ArrayList<>(1_000_00);
		for (int i = 0; i < 1_000_00; i++) {
		    agents.add(new AbstractAgent());
		    startTimer();
		    System.err.println("begin");
		    for (AbstractAgent abstractAgent : agents) {
			launchAgent(abstractAgent);
		    }
		    stopTimer("done ");
		    startTimer();
		    System.err.println("begin kills");
		    for (AbstractAgent abstractAgent : agents) {
			killAgent(abstractAgent);
		    }
		    stopTimer("kill done ");
		}
	    }
	});
    }

    @Test
    public void massAgentLaunch() {
	addMadkitArgs(LevelOption.agentLogLevel.toString(), "OFF");
	launchTest(new AbstractAgent() {

	    protected void activate() {
		getLogger().info("\n******************* STARTING MASS LAUNCH *******************\n");
		List<AbstractAgent> agents = new ArrayList<>(10_000);
		for (int i = 0; i < 10_000; i++) {
		    agents.add(new PongAgent());
		}
		startTimer();
		System.err.println("begin");
		for (AbstractAgent abstractAgent : agents) {
		    launchAgent(abstractAgent);
		}
		stopTimer("done ");
		pause(100000);
	    }
	});
    }

    @Test
    public void massAgentsLaunch() {
	massLaunchWithList(MinimalAgent.class);
    }

    @Test
    public void massNormalLifeLaunch() {// TODO more cases
	launchTest(new AbstractAgent() {

	    protected void activate() {
		AbstractAgent a;
		getLogger().info("\n******************* STARTING MASS LAUNCH *******************\n");
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