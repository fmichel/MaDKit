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
package com.distrimind.madkit.testing.util.agent;

import org.junit.Assert;

import com.distrimind.madkit.kernel.AgentAddress;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.message.NetworkObjectMessage;
import com.distrimind.madkit.message.ObjectMessage;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class NetworkPingAgent extends AgentAddressAgentTester {

	private final int destinationNumber;
	public static final String messagePing = "message ping";
	public static final String messagePong = "message pong";
	public static final String pingRole = "ping role";
	public static final String pongRole = "pong role";
	private volatile int localPongReceived = 0;
	private int distantPongReceived = 0;
	private volatile boolean testReceiver = true;
	private volatile boolean testSender = true;
	private volatile boolean testTraveledAgentAddress = true;

	NetworkPingAgent(int destinationNumber) {
		this.destinationNumber = destinationNumber;

	}

	@Override
	public void activate() {
		System.out.println("Ping activating : " + getKernelAddress());
		Assert.assertEquals(ReturnCode.SUCCESS,
				this.requestRole(JunitMadkit.DEFAULT_NETWORK_GROUP_FOR_ACCESS_DATA, pingRole));

		ReturnCode rc = broadcastMessage(JunitMadkit.DEFAULT_NETWORK_GROUP_FOR_ACCESS_DATA, pongRole,
				new NetworkObjectMessage<>(new Object[]{messagePing,
						getAgentAddressIn(JunitMadkit.DEFAULT_NETWORK_GROUP_FOR_ACCESS_DATA, pingRole)}));
		Assert.assertTrue(rc.toString(), ReturnCode.SUCCESS == rc || ReturnCode.TRANSFER_IN_PROGRESS == rc);
		System.out.println("Ping activated : " + getKernelAddress());
		/*
		 * scheduleTask(new Task<>(new Callable<Void>() {
		 * 
		 * @Override public Void call() throws Exception {
		 * Assert.assertEquals(ReturnCode.SUCCESS,
		 * broadcastMessage(JunitMadkit.DEFAULT_NETWORK_GROUP_FOR_ACCESS_DATA, pongRole,
		 * new StringMessage(messagePing)));
		 * System.out.println("Ping message sent : "+getKernelAddress()); return null; }
		 * }, System.currentTimeMillis()+2500));
		 */
	}

	@Override
	protected void liveByStep(Message _message) {
		if (_message instanceof ObjectMessage) {
			@SuppressWarnings("unchecked")
			ObjectMessage<Object[]> om = (ObjectMessage<Object[]>) _message;
			String message = (String) om.getContent()[0];
			AgentAddress aa = (AgentAddress) om.getContent()[1];

			Assert.assertEquals(message, messagePong);
			if (_message.getSender().isFrom(getKernelAddress())) {
				System.out.println("Receiving local pong message : " + getKernelAddress());
				++localPongReceived;
			} else {
				System.out.println("Receiving distant pong message : " + getKernelAddress());
				testReceiver &= testAgentAddressReceiver(_message, JunitMadkit.DEFAULT_NETWORK_GROUP_FOR_ACCESS_DATA,
						pingRole);
				testSender &= testAgentAddressSender(_message, JunitMadkit.DEFAULT_NETWORK_GROUP_FOR_ACCESS_DATA,
						pongRole);
				testTraveledAgentAddress &= testTraveledAgentAddress(aa, true,
						JunitMadkit.DEFAULT_NETWORK_GROUP_FOR_ACCESS_DATA, pingRole);
				++distantPongReceived;
			}

		} else {
			System.out.println("Uncomprehensible message !");
			Assert.fail();
		}
	}

	public boolean isOK() {
		return destinationNumber == distantPongReceived && localPongReceived == 1 && testReceiver
				&& testTraveledAgentAddress && testSender;
	}

	public void printOK() {
		System.out.print("distantPongReceived=" + distantPongReceived + " ; localPongReceived=" + localPongReceived
				+ " ; testReceiver=" + testReceiver + " ; testSender=" + testSender);
	}

}
