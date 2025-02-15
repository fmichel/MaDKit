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

import static org.testng.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeMethod;

import madkit.simulation.SimuAgent;
import madkit.test.utils.EmptySimuLauncher;
import net.jodah.concurrentunit.ConcurrentTestCase;

/**
 *
 * @version 6.0.2
 * 
 */
public abstract class MadkitConcurrentTestCase extends ConcurrentTestCase {

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
		System.err.println("\n\n*************\n*****TEST**** " + testName + " *****************\n*************");
	}

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
	 * Launches an agent and return without timeout
	 * 
	 * @param a the agent
	 */
	public void launchAgent(Agent a) {
		kernelAgent.launchAgent(a, 0);
	}

	/**
	 * Run test with a given agent
	 * 
	 * @param initialAgent the agent to run the test with
	 */
	public void runTest(Agent initialAgent) {
		launchAgent(initialAgent);
		try {
			await(20000);
		} catch (TimeoutException | InterruptedException e) {
			fail("TimeoutException / InterruptedException", e);
		}
	}

	public void runSimuTest(SimuAgent sa) {
		runTest(new EmptySimuLauncher() {
			@Override
			protected void onActivation() {
				super.onActivation();
				launchAgent(sa);
			}
		});
	}

	public void lineBreak() {
		System.err.println("---------------------------------");
	}

	public void noExceptionFailure() {
		threadFail("Exception not thrown");
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
		threadAssertFalse(a.alive.get());
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

	public static void printAllStacks() {
		for (Map.Entry<Thread, StackTraceElement[]> t : Thread.getAllStackTraces().entrySet()) {
			System.err.println("------------- " + t.getKey());
			for (StackTraceElement ste : t.getValue()) {
				System.err.println(ste);
			}
		}
	}

}
