/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.State.TERMINATED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import madkit.action.KernelAction;
import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Madkit.Option;
import madkit.testing.util.agent.ForEverAgent;

import org.junit.Rule;
import org.junit.rules.TestName;

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
	public static final String GROUP = "Tgroup";
	public static final String ROLE = "Trole";
	public static final String ROLE2 = "Trole2";

	public static String testTitle;
	protected Madkit madkit;

	protected List<String> mkArgs = new ArrayList<>(Arrays.asList(
			// "--"+Madkit.warningLogLevel,"INFO",
			BooleanOption.desktop.toString(), "false", Option.launchAgents.toString(),
			"madkit.kernel.AbstractAgent",// to not have the desktop mode by
													// default
			Option.logDirectory.toString(), getBinTestDir(), LevelOption.agentLogLevel.toString(), "ALL",
			LevelOption.madkitLogLevel.toString(), "INFO"));

	public Madkit launchTest(AbstractAgent a, ReturnCode expected, boolean gui) {
		System.err.println("\n\n------------------------ " + name.getMethodName() + " TEST START ---------------------");
		try {
			String[] args = null;
			if (mkArgs != null) {
				args = mkArgs.toArray(new String[mkArgs.size()]);
			}
			madkit = new Madkit(args);
			AbstractAgent kernelAgent = madkit.getKernel()
					.getAgentWithRole(null, LocalCommunity.NAME, Groups.SYSTEM, LocalCommunity.GROUP_MANAGER_ROLE).getAgent();
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
		return madkit;
	}

	public void lineBreak() {
		System.err.println("---------------------------------");
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
			/**
			 * 
			 */
			private static final long serialVersionUID = 1287673873282151865L;

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

	public void launchTest(AbstractAgent a, boolean all) {
		if (all) {
			addMadkitArgs(LevelOption.agentLogLevel.toString(), "ALL");
			addMadkitArgs(LevelOption.kernelLogLevel.toString(), "FINEST");
		} else {
			addMadkitArgs(LevelOption.agentLogLevel.toString(), "INFO");
			addMadkitArgs(LevelOption.kernelLogLevel.toString(), "OFF");
		}
		launchTest(a, SUCCESS);
	}

	public MadkitKernel getKernel() {
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

	public static long stopTimer(String message) {
		final long t = System.nanoTime() - time;
		System.err.println(message + (t / 1000000) + " ms");
		return t;
	}

	protected void assertAgentIsTerminated(AbstractAgent a) {
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

	public void launchThreadedMKNetworkInstance() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String[] args = { BooleanOption.network.toString(), 
//						LevelOption.networkLogLevel.toString(), Level.FINE.toString(), 
						Option.launchAgents.toString(), ForEverAgent.class.getName() };
				Madkit.main(args);
			}
		}).start();

	}

	public Madkit launchMKNetworkInstance() {
		return launchMKNetworkInstance(Level.INFO);
	}


	public Madkit launchMKNetworkInstance(Level l) {
		return new Madkit(
				BooleanOption.network.toString(), 
				Option.launchAgents.toString(), ForEverAgent.class.getName(),
				LevelOption.networkLogLevel.toString(),l.toString(),
				LevelOption.kernelLogLevel.toString(),l.toString());
//				BooleanOption.createLogFiles.toString()};
	}

	public Madkit launchCustomNetworkInstance(Level l, Class<? extends AbstractAgent> agentTolaunch) {
		return new Madkit(
				BooleanOption.network.toString(), 
				Option.launchAgents.toString(), agentTolaunch.getName(),
				LevelOption.networkLogLevel.toString(),l.toString(),
				LevelOption.kernelLogLevel.toString(),l.toString());
//				BooleanOption.createLogFiles.toString()};
	}

}
