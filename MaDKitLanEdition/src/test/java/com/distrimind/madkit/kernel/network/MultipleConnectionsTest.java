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
package com.distrimind.madkit.kernel.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.testng.Assert;

import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.kernel.Madkit;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocolProperties;
import com.distrimind.madkit.testing.util.agent.AgentBigTransfer;
import com.distrimind.madkit.testing.util.agent.NetworkPongAgent;
import com.distrimind.ood.database.EmbeddedHSQLDBWrapper;
import com.distrimind.util.Timer;

/**
 * 
 * @author Jason Mahdjoub
 * @since MadkitLanEdition 1.0
 * @version 1.1
 * 
 */

@RunWith(Parameterized.class)
public class MultipleConnectionsTest extends JunitMadkit {

	public static int HOST_NUMBERS = 5;

	@Parameters
	public static Collection<Object[]> data() {
		try {
			ArrayList<Object[]> res = new ArrayList<>();
			res.addAll(data(NetworkEventListener.getNetworkEventListenersForLocalClientServerConnection(true, true,
					false, true, true, false, null, HOST_NUMBERS - 1, 1, 2, 3, 4, 5), null, null));
			res.addAll(data(NetworkEventListener.getNetworkEventListenersForLocalClientServerConnection(true, true,
					false, true, true, false, null, HOST_NUMBERS - 1, 1, 2, 3, 4, 5), new Integer(100), null));
			res.addAll(data(NetworkEventListener.getNetworkEventListenersForLocalClientServerConnection(true, true,
					false, true, true, false, null, HOST_NUMBERS - 1, 1, 2, 3, 4, 5), null, new Integer(200)));
			res.addAll(data(NetworkEventListener.getNetworkEventListenersForLocalClientServerConnection(true, true,
					false, true, true, false, null, HOST_NUMBERS - 1, 1, 2, 3, 4, 5), new Integer(100),
					new Integer(200)));
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Collection<Object[]> data(Collection<Object[]> c, Integer localDataAmountAcc,
			Integer globalDataAmountAcc) {
		try {
			ArrayList<Object[]> res = new ArrayList<>(c.size());
			for (Iterator<Object[]> it = c.iterator(); it.hasNext();) {
				Object[] o = it.next();
				Object[] o2 = new Object[o.length + 2];
				o2[0] = localDataAmountAcc;
				o2[1] = globalDataAmountAcc;
				for (int i = 0; i < o.length; i++)
					o2[i + 2] = o[i];
				res.add(o2);
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	final NetworkEventListener eventListener1;
	final NetworkEventListener eventListener2;
	final NetworkEventListener eventListener3;
	final NetworkEventListener eventListener4;
	final NetworkEventListener eventListener5;
	final Integer localDataAmountAcc;
	final Integer globalDataAmountAcc;

	public MultipleConnectionsTest(Integer localDataAmountAcc, Integer globalDataAmountAcc,
			final NetworkEventListener eventListener1, final NetworkEventListener eventListener2,
			final NetworkEventListener eventListener3, final NetworkEventListener eventListener4,
			final NetworkEventListener eventListener5) {
		this.eventListener1 = eventListener1;
		this.eventListener2 = eventListener2;
		this.eventListener3 = eventListener3;
		this.eventListener4 = eventListener4;
		this.eventListener5 = eventListener5;
		this.localDataAmountAcc = localDataAmountAcc;
		this.globalDataAmountAcc = globalDataAmountAcc;
	}

	private static final long timeOut = 60000;

	@Test
	public void multipleAsynchroneConnectionTest() {
		eventListener1.setLocalDataAmountAcc(localDataAmountAcc);
		eventListener1.setGlobalDataAmountAcc(globalDataAmountAcc);
		eventListener2.setLocalDataAmountAcc(localDataAmountAcc);
		eventListener2.setGlobalDataAmountAcc(globalDataAmountAcc);
		eventListener3.setLocalDataAmountAcc(localDataAmountAcc);
		eventListener3.setGlobalDataAmountAcc(globalDataAmountAcc);
		eventListener4.setLocalDataAmountAcc(localDataAmountAcc);
		eventListener4.setGlobalDataAmountAcc(globalDataAmountAcc);
		eventListener5.setLocalDataAmountAcc(localDataAmountAcc);
		eventListener5.setGlobalDataAmountAcc(globalDataAmountAcc);
		for (ConnectionProtocolProperties<?> cpp : eventListener1.madkitEventListenerForConnectionProtocols.getConnectionProtocolProperties())
			System.out.println(cpp);
		cleanHelperMDKs();
		// addMadkitArgs(LevelOption.networkLogLevel.toString(),"FINER");
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() throws InterruptedException {
				try {
					sleep(1000);
					System.err.println("------------------------ Thread cound at start : " + Thread.activeCount());
					System.err.println("------------------------ localDataAmountAcc=" + localDataAmountAcc
							+ " --- globalDataAmountAcc=" + globalDataAmountAcc);
					removeDatabase();
					AgentsToLaunch agentsToLaunch1 = new AgentsToLaunch(1, 4, true);
					AgentsToLaunch agentsToLaunch2 = new AgentsToLaunch(2, 1, false);
					AgentsToLaunch agentsToLaunch3 = new AgentsToLaunch(3, 1, false);
					AgentsToLaunch agentsToLaunch4 = new AgentsToLaunch(4, 1, false);
					AgentsToLaunch agentsToLaunch5 = new AgentsToLaunch(5, 1, false);

					launchThreadedMKNetworkInstance(Level.INFO, AbstractAgent.class, agentsToLaunch1, eventListener1);
					sleep(2000);
					launchThreadedMKNetworkInstance(Level.INFO, AbstractAgent.class, agentsToLaunch2, eventListener2);
					launchThreadedMKNetworkInstance(Level.INFO, AbstractAgent.class, agentsToLaunch3, eventListener3);
					launchThreadedMKNetworkInstance(Level.INFO, AbstractAgent.class, agentsToLaunch4, eventListener4);
					launchThreadedMKNetworkInstance(Level.INFO, AbstractAgent.class, agentsToLaunch5, eventListener5);

					sleep(1400);
					int index = 0;
					for (Madkit m : getHelperInstances(5)) {
						if (index++ == 0) {
							checkConnectedKernelsNb(this, m, 4, timeOut);
							checkConnectedIntancesNb(this, m, 8, timeOut);
						} else {
							checkConnectedKernelsNb(this, m, 1, timeOut);
							checkConnectedIntancesNb(this, m, 2, timeOut);
						}
					}
					Timer t = new Timer(true);
					while (t.getMili() < 10000 && (!agentsToLaunch1.networkPingAgent.isOK()
							|| !agentsToLaunch2.networkPingAgent.isOK() || !agentsToLaunch3.networkPingAgent.isOK()
							|| !agentsToLaunch4.networkPingAgent.isOK() || !agentsToLaunch5.networkPingAgent.isOK())) {
						System.out.println("-------------------");
						agentsToLaunch1.networkPingAgent.printOK();
						agentsToLaunch2.networkPingAgent.printOK();
						agentsToLaunch3.networkPingAgent.printOK();
						agentsToLaunch4.networkPingAgent.printOK();
						agentsToLaunch5.networkPingAgent.printOK();
						sleep(1000);
					}

					Assert.assertTrue(agentsToLaunch1.networkPingAgent.isOK());
					Assert.assertTrue(agentsToLaunch2.networkPingAgent.isOK());
					Assert.assertTrue(agentsToLaunch3.networkPingAgent.isOK());
					Assert.assertTrue(agentsToLaunch4.networkPingAgent.isOK());
					Assert.assertTrue(agentsToLaunch5.networkPingAgent.isOK());

					Assert.assertEquals(ReturnCode.SUCCESS, agentsToLaunch1.launchBigDataTransferAgent());
					Assert.assertEquals(ReturnCode.SUCCESS, agentsToLaunch2.launchBigDataTransferAgent());
					Assert.assertEquals(ReturnCode.SUCCESS, agentsToLaunch3.launchBigDataTransferAgent());
					Assert.assertEquals(ReturnCode.SUCCESS, agentsToLaunch4.launchBigDataTransferAgent());
					Assert.assertEquals(ReturnCode.SUCCESS, agentsToLaunch5.launchBigDataTransferAgent());
					int nb = 0;
					while (nb++ < 200 && (!agentsToLaunch1.agentBigTransfer.isFinished()
							|| !agentsToLaunch2.agentBigTransfer.isFinished()
							|| !agentsToLaunch3.agentBigTransfer.isFinished()
							|| !agentsToLaunch4.agentBigTransfer.isFinished()
							|| !agentsToLaunch5.agentBigTransfer.isFinished())) {
						sleep(1000);
						System.err.println("------------------------ localDataAmountAcc=" + localDataAmountAcc
								+ " --- globalDataAmountAcc=" + globalDataAmountAcc);
						System.out.println(agentsToLaunch1.agentBigTransfer);
						System.out.println(agentsToLaunch2.agentBigTransfer);
						System.out.println(agentsToLaunch3.agentBigTransfer);
						System.out.println(agentsToLaunch4.agentBigTransfer);
						System.out.println(agentsToLaunch5.agentBigTransfer);
						Assert.assertTrue(agentsToLaunch1.agentBigTransfer.isOK());
						Assert.assertTrue(agentsToLaunch2.agentBigTransfer.isOK());
						Assert.assertTrue(agentsToLaunch3.agentBigTransfer.isOK());
						Assert.assertTrue(agentsToLaunch4.agentBigTransfer.isOK());
						Assert.assertTrue(agentsToLaunch5.agentBigTransfer.isOK());
					}

					Assert.assertTrue(agentsToLaunch1.agentBigTransfer.isOK());
					Assert.assertTrue(agentsToLaunch2.agentBigTransfer.isOK());
					Assert.assertTrue(agentsToLaunch3.agentBigTransfer.isOK());
					Assert.assertTrue(agentsToLaunch4.agentBigTransfer.isOK());
					Assert.assertTrue(agentsToLaunch5.agentBigTransfer.isOK());
					sleep(1000);
					if (logger != null)
						logger.info("stoping networks");
					// pause(1000);
					for (Madkit m : getHelperInstances(5))
						stopNetwork(m);
					for (Madkit m : getHelperInstances(5)) {
						checkConnectedKernelsNb(this, m, 0, timeOut);
						checkConnectedIntancesNb(this, m, 0, timeOut);
					}

					sleep(800);
					System.out.println("second round");
					// second round
					index = 0;
					for (Madkit m : getHelperInstances(5)) {
						startNetwork(m);
						if (index++ == 0)
							sleep(2000);
					}
					index = 0;
					for (Madkit m : getHelperInstances(5)) {
						if (index++ == 0) {
							checkConnectedKernelsNb(this, m, 4, timeOut);
							checkConnectedIntancesNb(this, m, 8, timeOut);
						} else {
							checkConnectedKernelsNb(this, m, 1, timeOut);
							checkConnectedIntancesNb(this, m, 2, timeOut);
						}
					}
					sleep(400);
					for (Madkit m : getHelperInstances(5))
						stopNetwork(m);
					for (Madkit m : getHelperInstances(5)) {
						checkConnectedKernelsNb(this, m, 0, timeOut);
						checkConnectedIntancesNb(this, m, 0, timeOut);
					}
					sleep(400);
					agentsToLaunch1.killAgent(agentsToLaunch1);
					agentsToLaunch2.killAgent(agentsToLaunch2);
					agentsToLaunch3.killAgent(agentsToLaunch3);
					agentsToLaunch4.killAgent(agentsToLaunch4);
					agentsToLaunch5.killAgent(agentsToLaunch5);

					agentsToLaunch1 = null;
					agentsToLaunch2 = null;
					agentsToLaunch3 = null;
					agentsToLaunch4 = null;
					agentsToLaunch5 = null;

					cleanHelperMDKs(this);
					Assert.assertEquals(getHelperInstances(0).size(), 0);
					/*
					 * for (Madkit m : getHelperInstances()) checkConnectedKernelsNb(this, m, 0,
					 * timeOut);
					 */
					/*
					 * for (Madkit m : getHelperInstances())
					 * Assert.assertTrue(checkMemoryLeakAfterNetworkStopped(m));
					 */
				} finally {
					removeDatabase();
				}
				sleep(400);
				System.err.println("------------------------ Thread cound at end : " + Thread.activeCount());
			}
		});
	}

	protected void removeDatabase() {
		if (eventListener1.databaseFile != null && eventListener1.databaseFile.exists())
			EmbeddedHSQLDBWrapper.deleteDatabaseFiles(eventListener1.databaseFile);
		if (eventListener2.databaseFile != null && eventListener2.databaseFile.exists())
			EmbeddedHSQLDBWrapper.deleteDatabaseFiles(eventListener2.databaseFile);
		if (eventListener3.databaseFile != null && eventListener3.databaseFile.exists())
			EmbeddedHSQLDBWrapper.deleteDatabaseFiles(eventListener3.databaseFile);
		if (eventListener4.databaseFile != null && eventListener4.databaseFile.exists())
			EmbeddedHSQLDBWrapper.deleteDatabaseFiles(eventListener4.databaseFile);
		if (eventListener5.databaseFile != null && eventListener5.databaseFile.exists())
			EmbeddedHSQLDBWrapper.deleteDatabaseFiles(eventListener5.databaseFile);
	}
}

class AgentsToLaunch extends AbstractAgent {

	public final NetworkPongAgent networkPingAgent;
	public final AgentBigTransfer agentBigTransfer;

	public AgentsToLaunch(int thisKernelNumber, int distantKernelNumber, boolean includeMoreThanOneBigDataTest) {
		this.networkPingAgent = new NetworkPongAgent(distantKernelNumber);
		agentBigTransfer = new AgentBigTransfer(0, true, true, false, true, true, distantKernelNumber, false,
				new AgentBigTransfer(1, true, false, false, includeMoreThanOneBigDataTest,
						!includeMoreThanOneBigDataTest, distantKernelNumber, false,
						new AgentBigTransfer(2, true, false, true, includeMoreThanOneBigDataTest,
								!includeMoreThanOneBigDataTest, distantKernelNumber, false,
								new AgentBigTransfer(3, false, false, false, includeMoreThanOneBigDataTest,
										!includeMoreThanOneBigDataTest, distantKernelNumber, false,
										new AgentBigTransfer(4, false, false, false, includeMoreThanOneBigDataTest,
												!includeMoreThanOneBigDataTest, distantKernelNumber, false,
												new AgentBigTransfer(5, false, false, true,
														includeMoreThanOneBigDataTest, !includeMoreThanOneBigDataTest,
														distantKernelNumber, false,
														new AgentBigTransfer(6, true, true, false, true, true,
																distantKernelNumber, true,
																new AgentBigTransfer(7, true, false, false,
																		includeMoreThanOneBigDataTest,
																		!includeMoreThanOneBigDataTest,
																		distantKernelNumber, true,
																		new AgentBigTransfer(8, true, false, true,
																				includeMoreThanOneBigDataTest,
																				!includeMoreThanOneBigDataTest,
																				distantKernelNumber, true,
																				new AgentBigTransfer(9, false, false,
																						false,
																						includeMoreThanOneBigDataTest,
																						!includeMoreThanOneBigDataTest,
																						distantKernelNumber, true,
																						new AgentBigTransfer(10, false,
																								false, false,
																								includeMoreThanOneBigDataTest,
																								!includeMoreThanOneBigDataTest,
																								distantKernelNumber,
																								true,
																								new AgentBigTransfer(11,
																										false, false,
																										true,
																										includeMoreThanOneBigDataTest,
																										!includeMoreThanOneBigDataTest,
																										distantKernelNumber,
																										true,
																										null))))))))))));
	}

	@Override
	public void activate() {
		launchAgent(networkPingAgent);
	}

	public ReturnCode launchBigDataTransferAgent() {
		return launchAgent(agentBigTransfer);
	}
}
