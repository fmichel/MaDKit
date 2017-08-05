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
package com.distrimind.madkit.kernel;

import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static com.distrimind.madkit.kernel.AbstractAgent.State.TERMINATED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.testng.Assert;

import com.distrimind.madkit.action.KernelAction;
import com.distrimind.madkit.agr.LocalCommunity;
import com.distrimind.madkit.agr.Organization;
import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.Madkit;
import com.distrimind.madkit.kernel.network.Connection;
import com.distrimind.madkit.kernel.network.NetworkEventListener;
import com.distrimind.madkit.message.KernelMessage;
import com.distrimind.madkit.kernel.AbstractAgent.ReturnCode;
import com.distrimind.madkit.kernel.AbstractAgent.State;
import com.distrimind.madkit.testing.util.agent.ForEverAgent;
import com.distrimind.ood.database.DatabaseFactory;
import com.distrimind.ood.database.exceptions.DatabaseException;
import com.distrimind.util.Timer;

import gnu.vm.jgnu.security.NoSuchAlgorithmException;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * @version 1.0
 * 
 */
public class JunitMadkit {

	@Rule
	public TestName name = new TestName();

	/**
	 * 	 */
	public static String aString = "a";
	public static final String C = "Tcommunity";
	public static final String C2 = "Tcommunity2";
	public static final String G = "Tgroup";
	public static final String G2 = "Tgroup";
	public static final String NGDAccessData = "TgroupNGDAccessData";
	public static final String NGDLoginData = "TgroupNGDLoginData";
	public static final String NGLD = "TgroupNGLD";
	public static final Group GROUP = new Group(true, null, false, C, G);
	public static final Group GROUP2 = new Group(C2, G2);
	public static final Group DEFAULT_NETWORK_GROUP_FOR_ACCESS_DATA = new Group(true, null, false, C, NGDAccessData);

	public static final Group NETWORK_GROUP_FOR_LOGIN_DATA = new Group(true, null, false, C, NGLD);
	public static final String ROLE = "Trole";
	public static final String ROLE2 = "Trole2";

	public static String testTitle;
	protected Madkit madkit;
	private static ArrayList<Madkit> helperInstances = new ArrayList<Madkit>();

	public void addHelperInstance(Madkit m) {
		synchronized (helperInstances) {
			helperInstances.add(m);
			helperInstances.notifyAll();
		}
	}

	ArrayList<Madkit> getHelperInstances() {
		synchronized (helperInstances) {
			ArrayList<Madkit> res = new ArrayList<>();
			for (Iterator<Madkit> it = helperInstances.iterator(); it.hasNext();)
				res.add(it.next());
			return res;
		}
	}

	public ArrayList<Madkit> getHelperInstances(int nb) throws InterruptedException {
		return getHelperInstances(nb, 10000);
	}

	public ArrayList<Madkit> getHelperInstances(int nb, long delay) throws InterruptedException {
		Timer t = new Timer(true);
		synchronized (helperInstances) {
			while (helperInstances.size() != nb && t.getMili() < delay) {
				long timeOut = delay - t.getMili();
				if (timeOut > 0)
					helperInstances.wait(timeOut);
			}
			Assert.assertEquals(helperInstances.size(), nb);
			ArrayList<Madkit> res = new ArrayList<>();
			for (Iterator<Madkit> it = helperInstances.iterator(); it.hasNext();)
				res.add(it.next());
			return res;
		}
	}

	// static{
	// Runtime.getRuntime().addShutdownHook(new Thread(){
	// @Override
	// public void run() {
	// cleanHelperMDKs();
	// }
	// });
	// }

	protected List<String> mkArgs = new ArrayList<>(Arrays.asList(
			// "--"+Madkit.warningLogLevel,"INFO",
			"--desktop", "false", "--forceDesktop", "true", "--launchAgents",
			"{com.distrimind.madkit.kernel.AbstractAgent}", // to not have the desktop mode by
			// default
			"--logDirectory", getBinTestDir(), "--agentLogLevel", "ALL", "--madkitLogLevel", "INFO"));

	private static List<Process> externalProcesses = new ArrayList<>();

	public Madkit launchTest(AbstractAgent a, ReturnCode expected, boolean gui) {
		return launchTest(a, expected, gui, new MadkitEventListener() {

			@Override
			public void onMadkitPropertiesLoaded(MadkitProperties _properties) {

			}
		});
	}

	public Madkit launchTest(AbstractAgent a, ReturnCode expected, boolean gui, Runnable postTest) {
		return launchTest(a, expected, gui, new MadkitEventListener() {

			@Override
			public void onMadkitPropertiesLoaded(MadkitProperties _properties) {

			}
		}, postTest);
	}

	public static void setDatabaseFactory(MadkitProperties p, DatabaseFactory df) throws DatabaseException {
		p.setDatabaseFactory(df);
	}

	private static volatile boolean oneFailed = false;

	public static boolean isOneFailed() {
		return oneFailed;
	}

	public Madkit launchTest(AbstractAgent a, ReturnCode expected, boolean gui, MadkitEventListener eventListener) {
		return launchTest(a, expected, gui, eventListener, null);
	}

	public Madkit launchTest(AbstractAgent a, ReturnCode expected, boolean gui, MadkitEventListener eventListener,
			Runnable postTest) {
		System.err
				.println("\n\n------------------------ " + name.getMethodName() + " TEST START ---------------------");
		Madkit madkit = null;
		try {
			String[] args = null;
			if (mkArgs != null) {
				args = mkArgs.toArray(new String[mkArgs.size()]);
			}

			this.madkit = madkit = new Madkit(eventListener, args);
			AbstractAgent kernelAgent = madkit.getKernel()
					.getAgentWithRole(null, LocalCommunity.Groups.SYSTEM, Organization.GROUP_MANAGER_ROLE).getAgent();
			// kernelAgent.receiveMessage(new
			// KernelMessage(MadkitAction.LAUNCH_AGENT, a, false));
			a.setName(name.getMethodName());
			assertEquals(expected, kernelAgent.launchAgent(a, gui));
			if (postTest != null)
				postTest.run();
			if (testFailed) {
				if (testException != null) {
					testException.printStackTrace();
				}
				oneFailed = true;
				fail();
			}
		} catch (Throwable e) {
			System.err.println("\n\n\n------------------------------------");
			while (e.getCause() != null)
				e = e.getCause();
			e.printStackTrace();
			System.err.println("------------------------------------\n\n\n");
			oneFailed = true;
			Assert.fail(JunitMadkit.class.getSimpleName(), e);

		} finally {
			System.err.println("\n\n------------------------ " + name.getMethodName()
					+ " TEST FINISHED ---------------------\n\n");
			cleanHelperMDKs(a);
			closeMadkit(madkit);
			madkit = null;
		}
		/*
		 * if (madkit.getKernel().isAlive()) madkit.doAction(KernelAction.EXIT);
		 */

		return madkit;
	}

	public void lineBreak() {
		System.err.println("---------------------------------");
	}

	public void assertKernelIsAlive() {
		assertTrue(getKernel().isAlive());
	}

	public static void noExceptionFailure() {
		fail("Exception not thrown");
	}

	public Madkit launchTest(AbstractAgent a) {
		return launchTest(a, new MadkitEventListener() {

			@Override
			public void onMadkitPropertiesLoaded(MadkitProperties _properties) {
			}
		});
	}

	public static KernelAddress getKernelAddressInstance() throws NoSuchAlgorithmException {
		return new KernelAddress(false);
	}

	public Madkit launchTest(AbstractAgent a, MadkitEventListener eventListener) {
		return launchTest(a, SUCCESS, eventListener);
	}

	public Madkit launchTest(AbstractAgent a, ReturnCode expected) {
		return launchTest(a, expected, false, new MadkitEventListener() {

			@Override
			public void onMadkitPropertiesLoaded(MadkitProperties _properties) {
			}
		});
	}

	public Madkit launchTest(AbstractAgent a, ReturnCode expected, MadkitEventListener eventListener) {
		return launchTest(a, expected, false, eventListener);
	}

	public void launchDefaultAgent(AbstractAgent a) {
		a.launchAgent(new AbstractAgent() {
			@Override
			protected void activate() {
				createGroup(GROUP);
				requestRole(GROUP, ROLE);
			}
		});
	}

	public void everythingOK() {
		if (testFailed) {
			if (testException != null) {
				testException.printStackTrace();
			}
			fail();
		}
	}

	public Madkit launchTest(AbstractAgent a, boolean all) {
		if (all) {
			addMadkitArgs("--agentLogLevel", "ALL");
			addMadkitArgs("--kernelLogLevel", "FINEST");
		} else {
			addMadkitArgs("--agentLogLevel", "INFO");
			addMadkitArgs("--kernelLogLevel", "OFF");
		}
		return launchTest(a, SUCCESS);
	}

	public AbstractAgent getKernel() {
		return madkit.getKernel();
	}

	public AbstractAgent getKernel(Madkit m) {
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

	public void assertAgentIsTerminated(AbstractAgent a) {
		System.err.println(a);
		assertEquals(TERMINATED, a.getState());
		assertFalse(a.isAlive());
	}

	public void assertAgentIsZombie(AbstractAgent a) {
		System.err.println(a);
		assertEquals(State.ZOMBIE, a.getState());
		// assertFalse(a.isAlive());
	}

	static public void printMemoryUsage() {
		// System.gc();
		Long mem = new Long((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
		System.err.println("\n----used memory: " + mem.toString().substring(0, 3) + " Mo\n");
	}

	/**
	 * @param i
	 */
	public static void pause(AbstractAgent agent, int millis) {
		try {
			if (agent != null)
				agent.sleep(millis);
			else
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
		a.createGroup(GROUP, null);
		try {
			assertEquals(SUCCESS, a.requestRole(GROUP, ROLE, null));
		} catch (AssertionError e) {
			JunitMadkit.testFails(e);
		}
	}

	public static void testFails(Throwable a) {
		testFailed = true;
		testException = a;
	}

	public void launchThreadedMKNetworkInstance(final Level l, final NetworkEventListener networkEventListener) {
		launchThreadedMKNetworkInstance(l, ForEverAgent.class, null, networkEventListener);
	}

	public void launchThreadedMKNetworkInstance(final Level l, final Class<? extends AbstractAgent> agentClass,
			final AbstractAgent agentToLaunch, final NetworkEventListener networkEventListener) {
		this.launchThreadedMKNetworkInstance(l, agentClass, agentToLaunch, networkEventListener, null);
	}

	public void launchThreadedMKNetworkInstance(final Level l, final Class<? extends AbstractAgent> agentClass,
			final AbstractAgent agentToLaunch, final NetworkEventListener networkEventListener,
			final KernelAddress kernelAddress) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				launchCustomNetworkInstance(l, agentClass, agentToLaunch, networkEventListener, kernelAddress);
			}
		}).start();
	}

	public KernelAddress getKernelAddress(Madkit m) {
		return m.getKernel().getKernelAddress();
	}

	public void closeMadkit(Madkit m) {
		if (m == null)
			return;
		if (m.getKernel().isAlive())
			m.doAction(KernelAction.STOP_NETWORK);

		checkConnectedKernelsNb(null, m, 0, 20000);
		Assert.assertTrue(checkMemoryLeakAfterNetworkStopped(m));

		checkConnectedIntancesNb(null, m, 0, 20000);

		checkNumberOfNetworkAgents(null, m, 0, 20000);

		if (m.getKernel().isAlive())
			m.doAction(KernelAction.EXIT);

		checkKilledKernelsNb(null, m, 10000);

		checkEmptyConversationIDTraces(null, m, 10000);

		checkReleasedGroups(null, m);
	}

	public void cleanHelperMDKs(AbstractAgent agent) {
		synchronized (helperInstances) {
			if (!helperInstances.isEmpty() || !externalProcesses.isEmpty()) {
				try {
					for (Madkit m : helperInstances) {
						if (m.getKernel().isAlive())
							m.doAction(KernelAction.STOP_NETWORK);
					}
					for (Madkit m : helperInstances) {

						checkConnectedKernelsNb(agent, m, 0, 20000);
						Assert.assertTrue(checkMemoryLeakAfterNetworkStopped(m));
					}
					for (Madkit m : helperInstances) {
						checkConnectedIntancesNb(agent, m, 0, 20000);
					}

					for (Madkit m : helperInstances) {
						checkNumberOfNetworkAgents(agent, m, 0, 20000);
					}

					for (Madkit m : helperInstances) {
						if (m.getKernel().isAlive())
							m.doAction(KernelAction.EXIT);
					}
					for (Madkit m : helperInstances) {
						checkKilledKernelsNb(agent, m, 10000);
					}
					for (Madkit m : helperInstances) {
						checkEmptyConversationIDTraces(agent, m, 10000);
					}
					for (Madkit m : helperInstances) {
						checkReleasedGroups(agent, m);
					}

					for (Process p : externalProcesses) {
						p.destroy();
						try {
							p.waitFor();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				} finally {
					helperInstances.clear();
					externalProcesses.clear();
					helperInstances.notifyAll();
					// pause(agent, pauseTime);
					System.err.println("------------Cleaning help instances done ---------------------\n\n");
				}
			}
		}
	}

	/*
	 * public static void cleanHelperMDKs(AbstractAgent agent){
	 * cleanHelperMDKs(agent, 100); }
	 */
	public void cleanHelperMDKs(AbstractAgent agent, int pause) {
		cleanHelperMDKs(agent);
		pause(agent, pause);
	}

	public void cleanHelperMDKs() {
		cleanHelperMDKs(null);
	}

	public void launchThreadedMKNetworkInstance(final NetworkEventListener networkEventListener) {
		launchThreadedMKNetworkInstance(Level.OFF, networkEventListener);
	}

	public Madkit launchMKNetworkInstance(final NetworkEventListener networkEventListener) {
		return launchMKNetworkInstance(Level.INFO, networkEventListener);
	}

	public Madkit launchMKNetworkInstance(Level l, final NetworkEventListener networkEventListener) {
		return launchCustomNetworkInstance(l, ForEverAgent.class, null, networkEventListener, null);
	}

	public Madkit launchCustomNetworkInstance(final Level l, final Class<? extends AbstractAgent> agentTolaunch,
			final AbstractAgent agentToLaunch, final NetworkEventListener networkEventListener,
			KernelAddress kernelAddress) {
		Madkit m = new Madkit(kernelAddress, new MadkitEventListener() {

			@Override
			public void onMadkitPropertiesLoaded(MadkitProperties _properties) {
				_properties.networkProperties.network = true;
				_properties.networkProperties.networkLogLevel = l;
				_properties.launchAgents = new ArrayList<>();
				_properties.launchAgents.add(new AgentToLaunch(agentTolaunch, false, 1));
				_properties.kernelLogLevel = l;
				_properties.networkProperties.upnpIGDEnabled = false;
				networkEventListener.onMadkitPropertiesLoaded(_properties);

			}
		});

		if (agentToLaunch != null)
			m.getKernel().launchAgent(agentToLaunch);
		addHelperInstance(m);
		return m;
	}

	public void launchExternalNetworkInstance() {
		launchExternalNetworkInstance(ForEverAgent.class);
	}

	public void launchExternalNetworkInstance(Class<? extends AbstractAgent> agentTolaunch) {
		launchExternalMDKInstance("--createLogFiles", "--kernelLogLevel.network", "--launchAgents",
				"{" + agentTolaunch.getCanonicalName() + "}");
	}

	public void launchExternalMDKInstance(String... args) {
		String cmdLince = "java -Xms1024m -cp bin:build/test/classes:lib/junit-4.12.jar:lib/hamcrest-core-1.3.jar madkit.kernel.Madkit";
		for (String string : args) {
			cmdLince += " " + string;
		}
		try {
			Process p = Runtime.getRuntime().exec(cmdLince);
			externalProcesses.add(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stopNetwork(Madkit m) {
		m.getKernel().receiveMessage(new KernelMessage(KernelAction.STOP_NETWORK));
	}

	public void startNetwork(Madkit m) {
		m.getKernel().receiveMessage(new KernelMessage(KernelAction.LAUNCH_NETWORK));
	}

	public int getEffectiveConnections(Madkit m) {
		Set<Connection> l = m.getKernel().getEffectiveConnections(null);
		if (l == null)
			return 0;
		else
			return l.size();
	}

	public void checkConnectedIntancesNb(AbstractAgent agent, Madkit m, int nb, long timeout) {
		Set<Connection> l = null;
		Timer t = new Timer(true);

		do {
			l = m.getKernel().getEffectiveConnections(null);
			if (l != null) {
				System.out.println(m.getKernel() + " : connected instances =" + l.size() + " (expected=" + nb + ")");
				for (Madkit m2 : getHelperInstances()) {
					System.out.println("\t" + m2 + ". Effective connections : " + getEffectiveConnections(m2));
				}

			} else
				System.out.println("others =0");
			if (t.getMili() < timeout && (l == null || l.size() != nb))
				pause(agent, 1000);
		} while (t.getMili() < timeout && (l == null || l.size() != nb));
		assertEquals(nb, l.size());
	}

	public void checkConnectedKernelsNb(AbstractAgent agent, Madkit m, int nb, long timeout) {
		Set<KernelAddress> l = null;
		Timer t = new Timer(true);

		do {
			l = m.getKernel().getAvailableDistantKernels();
			if (l != null) {
				System.out.println(m.getKernel() + " : connected kernels=" + l.size() + " (expected=" + nb + ")");
			} else
				System.out.println("others =0");
			if (t.getMili() < timeout && (l == null || l.size() != nb))
				pause(agent, 1000);
		} while (t.getMili() < timeout && (l == null || l.size() != nb));
		assertEquals(nb, l.size());
	}

	public void checkNumberOfNetworkAgents(AbstractAgent agent, Madkit m, int nbExpected, long timeout) {
		int nb = 0;
		Timer t = new Timer(true);

		do {
			nb = getNumberOfNetworkAgents(m);
			if (nb != nbExpected) {
				System.out.println(
						m.getKernel() + " : connected network agents=" + nb + " (expected=" + nbExpected + ")");
			} else
				System.out.println("others =0");
			if (t.getMili() < timeout && nb != nbExpected)
				pause(agent, 1000);
		} while (t.getMili() < timeout && nb != nbExpected);
		assertEquals(nb, nbExpected);
	}

	private int getNumberOfNetworkAgents(Madkit m) {
		ArrayList<AbstractAgent> c = m.getKernel().getConnectedNetworkAgents();
		for (AbstractAgent aa : c) {
			System.out.println(m.getKernel() + "\t\t connected network agent : " + aa);
		}
		return c.size();
	}

	public void checkKilledKernelsNb(AbstractAgent agent, Madkit m, long timeout) {

		Timer t = new Timer(true);

		do {
			if (t.getMili() < timeout && m.getKernel().isAlive())
				pause(agent, 1000);
		} while (t.getMili() < timeout && m.getKernel().isAlive());
		assertFalse(m.getKernel().isAlive());
	}

	public void checkEmptyConversationIDTraces(AbstractAgent agent, Madkit m, long timeout) {

		Timer t = new Timer(true);
		System.gc();
		do {
			if (t.getMili() < timeout && !m.getKernel().isGlobalInterfacedIDsEmpty()) {
				System.out.println(m.getKernel() + " : global conversation ID interfaces empty = false");
				System.gc();
				pause(agent, 1000);
			}
		} while (t.getMili() < timeout && !m.getKernel().isGlobalInterfacedIDsEmpty());
		assertTrue(m.getKernel().getGlobalInterfacedIDs().isEmpty());
	}

	public void checkReleasedGroups(AbstractAgent agent, Madkit m) {
		Assert.assertFalse(LocalCommunity.Groups.AGENTS_SOCKET_GROUPS.hasMadKitTraces(m.kernelAddress));
		Assert.assertFalse(LocalCommunity.Groups.DISTANT_KERNEL_AGENTS_GROUPS.hasMadKitTraces(m.kernelAddress));
		Assert.assertFalse(LocalCommunity.Groups.GUI.hasMadKitTraces(m.kernelAddress));
		Assert.assertFalse(LocalCommunity.Groups.KERNELS.hasMadKitTraces(m.kernelAddress));
		Assert.assertFalse(LocalCommunity.Groups.LOCAL_NETWORKS.hasMadKitTraces(m.kernelAddress));
		Assert.assertFalse(LocalCommunity.Groups.NETWORK.hasMadKitTraces(m.kernelAddress));
		Assert.assertFalse(LocalCommunity.Groups.NETWORK_INTERFACES.hasMadKitTraces(m.kernelAddress));
		Assert.assertFalse(LocalCommunity.Groups.SYSTEM.hasMadKitTraces(m.kernelAddress));
		Assert.assertFalse(LocalCommunity.Groups.SYSTEM_ROOT.hasMadKitTraces(m.kernelAddress));
		Assert.assertFalse(LocalCommunity.Groups.TASK_AGENTS.hasMadKitTraces(m.kernelAddress));

		Assert.assertFalse(JunitMadkit.GROUP.hasMadKitTraces(m.kernelAddress));
		Assert.assertFalse(JunitMadkit.GROUP2.hasMadKitTraces(m.kernelAddress));
		Assert.assertFalse(JunitMadkit.DEFAULT_NETWORK_GROUP_FOR_ACCESS_DATA.hasMadKitTraces(m.kernelAddress));
		Assert.assertFalse(JunitMadkit.NETWORK_GROUP_FOR_LOGIN_DATA.hasMadKitTraces(m.kernelAddress));

	}

	public boolean checkMemoryLeakAfterNetworkStopped(Madkit m) {
		return m.getKernel().checkMemoryLeakAfterNetworkStopped();
	}

}
