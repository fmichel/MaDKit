/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/

package madkit.kernel;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeMethod;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import madkit.kernel.Agent.ReturnCode;
import madkit.simulation.SimuAgent;
import madkit.test.utils.EmptySimuLauncher;
import net.jodah.concurrentunit.ConcurrentTestCase;

/**
 *
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */
public class MadkitUnitTestCase extends ConcurrentTestCase {

	/** The a string. */
	public static String aString = "dontExist";

	/** The Constant COMMUNITY. */
	public static final String COMMUNITY = "Tcommunity";

	/** The Constant COMMUNITY2. */
	public static final String COMMUNITY2 = "Tcommunity2";

	/** The Constant GROUP. */
	public static final String GROUP = "Tgroup";

	/** The Constant GROUP2. */
	public static final String GROUP2 = "Tgroup2";

	/** The Constant ROLE. */
	public static final String ROLE = "Trole";

	/** The Constant ROLE2. */
	public static final String ROLE2 = "Trole2";

	/** The madkit. */
	protected Madkit madkit;
	private Lock lock;
	private Condition testDone;

	/** The kernel agent. */
	protected KernelAgent kernelAgent;

	/** The helper instances. */
	protected static List<Madkit> helperInstances = new ArrayList<>();

	// static{
	// Runtime.getRuntime().addShutdownHook(new Thread(){
	// @Override
	// public void run() {
	// cleanHelperMDKs();
	// }
	// });
	// }

	@BeforeMethod
	public void handleTestMethodName(Method method) {
		String testName = method.getName();
		System.err.println("\n\n\n************* " + testName + " *****************");
	}

	private static AssertionError firstFailure;

	/** The assertion errors. */
	protected static Map<Agent, AssertionError> assertionErrors;

	public static String cgrDontExist() {
		return aString += "a";
	}

	@BeforeMethod
	@BeforeGroups
	public void initMDK() {
		madkit = new Madkit();
		Field f;
		try {
			f = madkit.getClass().getDeclaredField("kernelAgent");
			f.setAccessible(true);
			kernelAgent = (KernelAgent) f.get(madkit);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public Agent launchAgent(Agent a) {
		kernelAgent.launchAgent(a);
		return a;
	}

	/**
	 * @return the assertionErrors
	 */
	@SuppressWarnings("serial")
	public static Map<Agent, AssertionError> getAssertionErrors() {
		if (assertionErrors == null) {
			assertionErrors = new HashMap<Agent, AssertionError>() {
				@Override
				public AssertionError put(Agent key, AssertionError value) {
					if (firstFailure == null) {
						firstFailure = value;
					}
					return super.put(key, value);
				}
			};
		}
		return assertionErrors;
	}

	public void checkAssertions() {
		if (assertionErrors != null) {
//			System.err.println("\n\n------------------------ " + name.getMethodName()
//					+ " TEST FAILED ---------------------\n\nFAILURES SUMMARY-------->");
			for (Map.Entry<Agent, AssertionError> failure : assertionErrors.entrySet()) {
				System.err.println("\n<--------------- Agent \"" + failure.getKey().getName() + "\" fails test ------>");
				failure.getValue().printStackTrace();
			}
			throw firstFailure;
			// fail(name.getMethodName()+" fails");
		}
//		System.err.println(
//				"\n\n------------------------ " + name.getMethodName() + " TEST PASSED ---------------------\n\n");
	}

	/**
	 * @param agent         the agent to test
	 * @param expected
	 * @param gui
	 * @param launchTimeOut
	 * @param testTimeOut
	 */
	public void launchTestedAgent(Agent agent, ReturnCode expected, boolean gui, int launchTimeOut, int testTimeOut) {
		final ReturnCode returnCode = kernelAgent.launchAgent(agent, launchTimeOut);
		if (expected != null) {
			threadAssertEquals(expected, returnCode);
			resume();
		}
		try {
			await(testTimeOut);
		} catch (TimeoutException | InterruptedException e) {
			fail("test", e);
		}
	}

	public void testSimuAgentBehavior(Consumer<SimuAgent> behavior) {
		launchTestedAgent(new SimuAgent() {
			@Override
			protected void onActivation() {
				behavior.accept(this);
			}
		}, SUCCESS);
	}

	public void testThreadedBehavior(Consumer<Agent> behavior) {
		launchTestedAgent(new Agent() {
			@Override
			protected void onLive() {
				behavior.accept(this);
			}
		}, SUCCESS);
	}

	/**
	 * @param a
	 * @param expected
	 * @param gui
	 */
	public void launchTestedAgent(Agent a, ReturnCode expected, boolean gui) {
		launchTestedAgent(a, expected, gui, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * @param a
	 * @param expected
	 */
	public void launchTestedAgent(Agent a, ReturnCode expected) {
		launchTestedAgent(a, expected, false, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * @param a
	 */
	public void launchTestedAgent(Agent a) {
		launchTestedAgent(a, ReturnCode.SUCCESS, false, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	public void launchSimuAgentTest(SimuAgent sa) {
		launchTestedAgent(new EmptySimuLauncher() {
			@Override
			protected void onActivation() {
				super.onActivation();
				threadAssertEquals(SUCCESS, launchAgent(sa));
			}
		});
	}

	public void lineBreak() {
		System.err.println("---------------------------------");
	}

	public void noExceptionFailure() {
		threadFail("Exception not thrown");
	}

	public void everythingOK() {
		if (testFailed) {
			if (testException != null) {
				testException.printStackTrace();
			}
		}
	}

	/**
	 * Gets the bin test dir.
	 *
	 * @return the bin test dir
	 */
	public static String getBinTestDir() {
		return "bin";
	}

	static long time;

	/** The test failed. */
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

	protected void assertAgentIsTerminated(Agent a) {
		System.err.println(a);
		threadAssertEquals(kernelAgent.deadKernel, a.kernel);
	}

	static public void printMemoryUsage() {
		// System.gc();
		Long mem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
		System.err.println("\n----used memory: " + mem.toString().substring(0, 3) + " Mo\n");
	}

	public void checkTermination(Agent a) {
		assertFalse(a.alive.get());
		threadAssertEquals(KernelAgent.deadKernel, a.kernel);
	}

	public void awaitTermination(Agent a, long timeout) {
		synchronized (a.alive) {
			try {
				a.alive.wait(timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		checkTermination(a);
	}

//	/**
//	 * @param i
//	 */
//	public static void sleep(int millis) {
//		try {
//			Thread.sleep(millis);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//
//	}

	public static void printAllStacks() {
		for (Map.Entry<Thread, StackTraceElement[]> t : Thread.getAllStackTraces().entrySet()) {
			System.err.println("------------- " + t.getKey());
			for (StackTraceElement ste : t.getValue()) {
				System.err.println(ste);
			}
		}
	}

	public static void testFails(Throwable a) {
		testFailed = true;
		testException = a;
	}

}
