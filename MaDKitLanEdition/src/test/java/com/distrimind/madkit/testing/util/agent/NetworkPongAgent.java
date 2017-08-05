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
import com.distrimind.madkit.message.ObjectMessage;
import com.distrimind.madkit.message.hook.DistantKernelAgentEventMessage;
import com.distrimind.madkit.message.hook.HookMessage.AgentActionEvent;
import com.distrimind.madkit.message.hook.OrganizationEvent;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class NetworkPongAgent extends AgentAddressAgentTester {

	private final int destinationNumber;
	private volatile int numberOfConnectedKernels = 0;
	private volatile int numberOfDistantPongAgents = 0;
	private volatile int numberOfLocalPongAgents = 0;
	private volatile int numberOfDistantPingAgents = 0;
	private volatile int numberOfLocalPingAgents = 0;
	public volatile boolean pingPongTestLaunched = false;
	private volatile boolean testReceiver = true;
	private volatile boolean testSender = true;
	private volatile boolean testTraveledAgentAddress = true;

	protected NetworkPingAgent networkPingAgent;

	public NetworkPongAgent(int destinationNumber) {
		networkPingAgent = new NetworkPingAgent(destinationNumber);
		this.destinationNumber = destinationNumber;
	}

	@Override
	public void activate() {

		Assert.assertEquals(ReturnCode.SUCCESS, requestHookEvents(AgentActionEvent.REQUEST_ROLE));
		Assert.assertEquals(ReturnCode.SUCCESS, requestHookEvents(AgentActionEvent.DISTANT_KERNEL_CONNECTED));

		Assert.assertEquals(ReturnCode.SUCCESS,
				this.requestRole(JunitMadkit.DEFAULT_NETWORK_GROUP_FOR_ACCESS_DATA, NetworkPingAgent.pongRole));
		System.out.println("Pong activated : " + getKernelAddress());
	}

	@Override
	protected void liveByStep(Message _message) {
		if (_message instanceof OrganizationEvent) {
			OrganizationEvent m = ((OrganizationEvent) _message);
			if (m.getContent().equals(AgentActionEvent.REQUEST_ROLE)) {
				if (m.getSourceAgent().getRole().equals(NetworkPingAgent.pongRole)) {
					if (m.getSourceAgent().isFrom(getKernelAddress()))
						++numberOfLocalPongAgents;
					else
						++numberOfDistantPongAgents;
					launchPingAgent();
				}
				if (m.getSourceAgent().getRole().equals(NetworkPingAgent.pingRole)) {
					if (m.getSourceAgent().isFrom(getKernelAddress()))
						++numberOfLocalPingAgents;
					else
						++numberOfDistantPingAgents;
					launchPingAgent();
				}
			}
		} else if (_message instanceof DistantKernelAgentEventMessage) {
			DistantKernelAgentEventMessage m = (DistantKernelAgentEventMessage) _message;
			if (m.getContent().equals(AgentActionEvent.DISTANT_KERNEL_CONNECTED)) {
				++numberOfConnectedKernels;
				launchPingAgent();
			} else if (m.getContent().equals(AgentActionEvent.DISTANT_KERNEL_DISCONNECTED)) {
				--numberOfConnectedKernels;
			}

		} else if (_message instanceof ObjectMessage) {
			@SuppressWarnings("unchecked")
			ObjectMessage<Object[]> om = (ObjectMessage<Object[]>) _message;
			String message = (String) om.getContent()[0];
			AgentAddress aa = (AgentAddress) om.getContent()[1];

			Assert.assertEquals(NetworkPingAgent.messagePing, message);
			if (_message.getSender().isFrom(getKernelAddress())) {
				System.out.println("Receiving and replied to local ping message : " + getKernelAddress());
			} else {

				System.out.println("Receiving and replied to network ping message : " + getKernelAddress());
				testReceiver &= testAgentAddressReceiver(_message, JunitMadkit.DEFAULT_NETWORK_GROUP_FOR_ACCESS_DATA,
						NetworkPingAgent.pongRole);
				testSender &= testAgentAddressSender(_message, JunitMadkit.DEFAULT_NETWORK_GROUP_FOR_ACCESS_DATA,
						NetworkPingAgent.pingRole);
				testTraveledAgentAddress &= testTraveledAgentAddress(aa, false,
						JunitMadkit.DEFAULT_NETWORK_GROUP_FOR_ACCESS_DATA, NetworkPingAgent.pingRole);

			}
			Assert.assertEquals(ReturnCode.SUCCESS, sendReply(_message,
					new ObjectMessage<Object[]>(new Object[] { NetworkPingAgent.messagePong, aa })));
		} else {
			System.out.println("incomprehensible message  : " + getKernelAddress());
			Assert.assertFalse(true);
		}

	}

	public void launchPingAgent() {
		if (!pingPongTestLaunched && numberOfLocalPongAgents == 1 && numberOfDistantPongAgents == destinationNumber
				&& numberOfConnectedKernels == destinationNumber) {
			launchAgent(networkPingAgent);
			pingPongTestLaunched = true;
		}
	}

	public boolean isOK() {
		return numberOfLocalPongAgents == 1 && numberOfLocalPingAgents == 1
				&& numberOfDistantPongAgents == destinationNumber && numberOfDistantPingAgents == destinationNumber
				&& numberOfConnectedKernels == destinationNumber && testReceiver && testSender
				&& testTraveledAgentAddress && networkPingAgent.isOK();
	}

	public void printOK() {
		System.out.print("numberOfLocalPingAgents=" + numberOfLocalPingAgents + " ; " + "numberOfDistantPingAgents="
				+ numberOfDistantPingAgents + " ; " + "numberOfLocalPongAgents=" + numberOfLocalPongAgents + " ; "
				+ "numberOfDistantPongAgents=" + numberOfDistantPongAgents + " ; " + "numberOfConnectedKernels="
				+ numberOfConnectedKernels + " ; " + " ; testReceiver=" + testReceiver + " ; testSender=" + testSender);
		networkPingAgent.printOK();
		System.out.println();
	}

}
