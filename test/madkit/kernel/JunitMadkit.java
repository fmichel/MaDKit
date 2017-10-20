/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */
package madkit.kernel;

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.State.TERMINATED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.junit.Rule;
import org.junit.rules.TestName;

import madkit.action.KernelAction;
import madkit.agr.DefaultMaDKitRoles;
import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.NetworkCommunity;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Madkit.Option;
import madkit.testing.util.agent.ForEverAgent;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */
public class JunitMadkit {

	@Rule
	public TestName name = new TestName();

	/**
	 * 	 */
	public static String aString = "a";
	public static final String COMMUNITY = "Tcommunity";
	public static final String COMMUNITY2 = "Tcommunity2";
	public static final String GROUP = "Tgroup";
	public static final String GROUP2 = "Tgroup2";
	public static final String ROLE = "Trole";
	public static final String ROLE2 = "Trole2";

	public static String testTitle;
	protected Madkit madkit;
	protected static List<Madkit> helperInstances = new ArrayList<>();
	
//	static{
//		Runtime.getRuntime().addShutdownHook(new Thread(){
//			@Override
//			public void run() {
//				cleanHelperMDKs();
//			}
//		});
//	}

	protected List<String> mkArgs = new ArrayList<>(Arrays.asList(
			// "--"+Madkit.warningLogLevel,"INFO",
			BooleanOption.desktop.toString(), "false", Option.launchAgents.toString(),
			"madkit.kernel.AbstractAgent",// to not have the desktop mode by
													// default
			Option.logDirectory.toString(), getBinTestDir(), LevelOption.agentLogLevel.toString(), "ALL",
			LevelOption.madkitLogLevel.toString(), "INFO"));

	private static List<Process>	externalProcesses = new ArrayList<>();

	public Madkit launchTest(AbstractAgent a, ReturnCode expected, boolean gui) {
		System.err.println("\n\n------------------------ " + name.getMethodName() + " TEST START ---------------------");
		try {
			String[] args = null;
			if (mkArgs != null) {
				args = mkArgs.toArray(new String[mkArgs.size()]);
			}
			madkit = new Madkit(args);
			AbstractAgent kernelAgent = madkit.getKernel()
					.getAgentWithRole(null, LocalCommunity.NAME, Groups.SYSTEM, DefaultMaDKitRoles.GROUP_MANAGER_ROLE).getAgent();
			// kernelAgent.receiveMessage(new
			// KernelMessage(MadkitAction.LAUNCH_AGENT, a, false));
			a.setName(name.getMethodName());
			assertEquals(expected, kernelAgent.launchAgent(a,gui));
			if(testFailed){
				if (testException != null) {
					testException.printStackTrace();
				}
				fail();
			}
		} catch (Throwable e) {
			System.err.println("\n\n\n------------------------------------");
			while (e.getCause() != null)
				e = e.getCause();
			e.printStackTrace();
			System.err.println("------------------------------------\n\n\n");
			fail(JunitMadkit.class.getSimpleName());
		} finally {
			System.err.println("\n\n------------------------ " + name.getMethodName() + " TEST FINISHED ---------------------\n\n");
		}
		madkit.doAction(KernelAction.EXIT);
		cleanHelperMDKs();
		return madkit;
	}

	public void lineBreak() {
		System.err.println("---------------------------------");
	}
	
	public void assertKernelIsAlive(){
		assertTrue(getKernel().isAlive());
	}

	public static void noExceptionFailure() {
		fail("Exception not thrown");
	}

	public Madkit launchTest(AbstractAgent a) {
		return launchTest(a, SUCCESS);
	}
	
	public Madkit launchTest(AbstractAgent a, ReturnCode expected) {
		return launchTest(a, expected, false);
	}

	public void launchDefaultAgent(AbstractAgent a){
		a.launchAgent(new AbstractAgent(){
			@Override
			protected void activate() {
				createGroup(COMMUNITY, GROUP);
				requestRole(COMMUNITY, GROUP, ROLE);
			}
		});
	}
	
	public void everythingOK(){
		if(testFailed){
			if (testException != null) {
				testException.printStackTrace();
			}
			fail();
		}
	}

	public Madkit launchTest(AbstractAgent a, boolean all) {
		if (all) {
			addMadkitArgs(LevelOption.agentLogLevel.toString(), "ALL");
			addMadkitArgs(LevelOption.kernelLogLevel.toString(), "FINEST");
		} else {
			addMadkitArgs(LevelOption.agentLogLevel.toString(), "INFO");
			addMadkitArgs(LevelOption.kernelLogLevel.toString(), "OFF");
		}
		return launchTest(a, SUCCESS);
	}

	public AbstractAgent getKernel() {
		return madkit.getKernel();
	}

	public void addMadkitArgs(String... string) {
		mkArgs.addAll(Arrays.asList(string));
	}

	public static String getBinTestDir() {
		return "bin";
	}

	public void test() {
		launchTest(new AbstractAgent());
	}

	public static String aa() {
		return aString += "a";
	}

	static long time;

	public static boolean testFailed = false;

	private static Throwable testException = null;

	public static void startTimer() {
		time = System.nanoTime();
	}

	/**
	 * @param message
	 * @return the total time in ms
	 */
	public static long stopTimer(String message) {
		final long t = System.nanoTime() - time;
		System.err.println(message + (t / 1000000) + " ms");
		return (t / 1000000);
	}

	protected void assertAgentIsTerminated(AbstractAgent a) {
		System.err.println(a);
		assertEquals(TERMINATED, a.getState());
		assertFalse(a.isAlive());
	}

	static public void printMemoryUsage() {
		// System.gc();
		Long mem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
		System.err.println("\n----used memory: " + mem.toString().substring(0, 3) + " Mo\n");
	}

	/**
	 * @param i
	 */
	public static void pause(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public static void printAllStacks() {
		for (Map.Entry<Thread, StackTraceElement[]> t : Thread.getAllStackTraces().entrySet()) {
			System.err.println("------------- " + t.getKey());
			for (StackTraceElement ste : t.getValue()) {
				System.err.println(ste);
			}
		}
	}

	public static void createDefaultCGR(AbstractAgent a) {
		a.createGroup(COMMUNITY, GROUP, false, null);
		try {
			assertEquals(SUCCESS, a.requestRole(COMMUNITY, GROUP, ROLE, null));
		} catch (AssertionError e) {
			JunitMadkit.testFails(e);
		}
	}

	public static void testFails(Throwable a) {
		testFailed = true;
		testException = a;
	}

	public void launchThreadedMKNetworkInstance(final Level l) {
		launchThreadedMKNetworkInstance(l, ForEverAgent.class);
	}
	
	public void launchThreadedMKNetworkInstance(final Level l, final Class<? extends AbstractAgent> agentClass) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				launchCustomNetworkInstance(l, agentClass);
			}
		}).start();
	}
	
	
	public static void cleanHelperMDKs(int pauseTime){
		if (! helperInstances.isEmpty() || ! externalProcesses.isEmpty()) {
			for (Madkit m : helperInstances) {
				m.doAction(KernelAction.STOP_NETWORK);
				m.doAction(KernelAction.EXIT);
			}
			for (Process p : externalProcesses) {
				p.destroy();
				try {
					p.waitFor();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			helperInstances.clear();
			externalProcesses.clear();
			pause(pauseTime);
			System.err.println("------------Cleaning help instances done ---------------------\n\n");
		}
	}

	public static void cleanHelperMDKs(){
		cleanHelperMDKs(100);
	}
	public void launchThreadedMKNetworkInstance() {
		launchThreadedMKNetworkInstance(Level.OFF);
	}

	public Madkit launchMKNetworkInstance() {
		return launchMKNetworkInstance(Level.INFO);
	}


	public Madkit launchMKNetworkInstance(Level l) {
		return launchCustomNetworkInstance(l, ForEverAgent.class);
	}

	public Madkit launchCustomNetworkInstance(Level l, Class<? extends AbstractAgent> agentTolaunch) {
		Madkit m = new Madkit(
				BooleanOption.network.toString(), 
				Option.launchAgents.toString(), agentTolaunch.getName(),
				LevelOption.networkLogLevel.toString(),l.toString(),
				LevelOption.kernelLogLevel.toString(),l.toString());
//				BooleanOption.createLogFiles.toString()};
		helperInstances.add(m);
		return m;
	}
	
	public void launchExternalNetworkInstance() {
		launchExternalNetworkInstance(ForEverAgent.class);
	}

	public void launchExternalNetworkInstance(Class<? extends AbstractAgent> agentTolaunch) {
		launchExternalMDKInstance(
				BooleanOption.createLogFiles.toString(),
				BooleanOption.network.toString(),
				Option.launchAgents.toString(),agentTolaunch.getName());
	}
	
	public void launchExternalMDKInstance(String... args){
		String cmdLince = "java -Xms512m -cp build:bin:build/test/classes:lib/junit-4.12.jar:lib/hamcrest-core-1.3.jar madkit.kernel.Madkit";
		for (String string : args) {
			cmdLince += " "+string;
		}
		try {
			Process p = Runtime.getRuntime().exec(cmdLince);
			externalProcesses.add(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("null")
	public void checkConnectedIntancesNb(AbstractAgent agent, int nb) {
		List<AgentAddress> l = null;
		startTimer();
		do {
			if(nb == 0 && ! agent.isCommunity(NetworkCommunity.NAME))
				break;
			l = agent.getAgentsWithRole(NetworkCommunity.NAME, NetworkCommunity.Groups.NETWORK_AGENTS, NetworkCommunity.Roles.NET_AGENT);
			if (l != null) {
				System.err.println("others =" + l.size());
			}
			pause(1000);
		}
		while (stopTimer("") < 300000 && l == null || l.size() != nb);
		if (nb > 0) {
			assertEquals(nb, l.size());
		}
		else{
			assertFalse(agent.isCommunity(NetworkCommunity.NAME));
		}
	}



}
