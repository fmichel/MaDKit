
package madkit.kernel;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;
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
import java.util.logging.Level;

import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeMethod;

import madkit.kernel.Agent.ReturnCode;
import madkit.simulation.SimulationEngine;
import net.jodah.concurrentunit.ConcurrentTestCase;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */
public class JunitMadkit extends ConcurrentTestCase {

	public static String aString = "dontExist";
	public static final String COMMUNITY = "Tcommunity";
	public static final String COMMUNITY2 = "Tcommunity2";
	public static final String GROUP = "Tgroup";
	public static final String GROUP2 = "Tgroup2";
	public static final String ROLE = "Trole";
	public static final String ROLE2 = "Trole2";

	protected Madkit madkit;
	private Lock lock;
	private Condition testDone;

	KernelAgent kernelAgent;

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
    public void handleTestMethodName(Method method)
    {
        String testName = method.getName(); 
        System.err.println("\n\n\n************* "+testName+ " *****************");
    }


	private static AssertionError firstFailure;
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
				System.err
						.println("\n<--------------- Agent \"" + failure.getKey().getName() + "\" fails test ------>");
				failure.getValue().printStackTrace();
			}
			throw firstFailure;
			// fail(name.getMethodName()+" fails");
		}
//		System.err.println(
//				"\n\n------------------------ " + name.getMethodName() + " TEST PASSED ---------------------\n\n");
	}

	/**
	 * @param a
	 * @param expected
	 * @param gui
	 */
	public void launchTestedAgent(Agent a, ReturnCode expected, boolean gui, int launchTimeOut, int testTimeOut) {
		final ReturnCode returnCode = kernelAgent.launchAgent(a, launchTimeOut);
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

	public void testBehavior(Consumer<Agent> behavior) {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				getLogger().setLevel(Level.ALL);
				behavior.accept(this);
			}
		},SUCCESS);
	}

	/**
	 * @param a
	 * @param expected
	 * @param gui
	 */
	public void launchTestedAgent(Agent a, ReturnCode expected, boolean gui) {
		launchTestedAgent(a, expected, gui, Integer.MAX_VALUE,  Integer.MAX_VALUE);
	}

	/**
	 * @param a
	 * @param expected
	 */
	public void launchTestedAgent(Agent a, ReturnCode expected) {
		launchTestedAgent(a, expected, false, Integer.MAX_VALUE,  Integer.MAX_VALUE);
	}


	/**
	 * @param a
	 * @param expected
	 */
	public void launchTestedAgent(Agent a) {
		launchTestedAgent(a, ReturnCode.SUCCESS, false, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}
	
	public ReturnCode launchTestScheduler(AbstractScheduler<?> sch) {
		SimulationEngine s = new SimulationEngine();
		launchTestedAgent(s);
		return s.launchAgent(sch);
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
//			fail();
		}
	}

	public static String getBinTestDir() {
		return "bin";
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
