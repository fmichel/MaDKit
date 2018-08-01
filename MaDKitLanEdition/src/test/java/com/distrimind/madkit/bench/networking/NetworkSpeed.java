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

package com.distrimind.madkit.bench.networking;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.logging.Level;

import com.distrimind.madkit.message.StringMessage;
import org.junit.Test;

import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.AgentAddress;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.kernel.MadkitEventListener;
import com.distrimind.madkit.kernel.MadkitProperties;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.kernel.network.AbstractIP;
import com.distrimind.madkit.kernel.network.AccessDataMKEventListener;
import com.distrimind.madkit.kernel.network.AccessProtocolPropertiesMKEventListener;
import com.distrimind.madkit.kernel.network.ConnectionsProtocolsMKEventListener;
import com.distrimind.madkit.kernel.network.DoubleIP;
import com.distrimind.madkit.kernel.network.NetworkEventListener;
import com.distrimind.madkit.kernel.network.connection.access.AbstractAccessProtocolProperties;
import com.distrimind.madkit.kernel.network.connection.access.AccessProtocolWithP2PAgreementProperties;
import com.distrimind.madkit.kernel.network.connection.unsecured.UnsecuredConnectionProtocolProperties;
import com.distrimind.madkit.testing.util.agent.ForEverOnTheSameAASenderAgent;
import com.distrimind.madkit.testing.util.agent.NormalAgent;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.0.9
 * @since MadkitLanEdition 1.0
 * @version 1.0
 * 
 */

public class NetworkSpeed extends JunitMadkit {

	final MadkitEventListener eventListener1;
	final NetworkEventListener eventListener2;
	final ForEverOnTheSameAASenderAgent forEventAgent1;

	public NetworkSpeed() throws UnknownHostException {

		this.eventListener1 = new MadkitEventListener() {

			@Override
			public void onMadkitPropertiesLoaded(MadkitProperties _properties) {
				AbstractAccessProtocolProperties app = new AccessProtocolWithP2PAgreementProperties();

				try {
					new NetworkEventListener(true, false, false, null,
							new ConnectionsProtocolsMKEventListener(new UnsecuredConnectionProtocolProperties()),
							new AccessProtocolPropertiesMKEventListener(app),
							new AccessDataMKEventListener(AccessDataMKEventListener.getDefaultAccessData(GROUP)), 5000,
							null, InetAddress.getByName("0.0.0.0")).onMadkitPropertiesLoaded(_properties);
				} catch (Exception e) {
					e.printStackTrace();
				}
				_properties.networkProperties.networkLogLevel = Level.INFO;
			}
		};

		UnsecuredConnectionProtocolProperties u = new UnsecuredConnectionProtocolProperties();
		u.isServer = false;

		AbstractAccessProtocolProperties app = new AccessProtocolWithP2PAgreementProperties();

		this.eventListener2 = new NetworkEventListener(true, false, false, null,
				new ConnectionsProtocolsMKEventListener(u), new AccessProtocolPropertiesMKEventListener(app),
				new AccessDataMKEventListener(AccessDataMKEventListener.getDefaultAccessData(GROUP)), 5000,
                Collections.singletonList((AbstractIP) new DoubleIP(5000, (Inet4Address) InetAddress.getByName("127.0.0.1"),
                        (Inet6Address) InetAddress.getByName("::1"))),
				InetAddress.getByName("0.0.0.0"));
		forEventAgent1 = new ForEverOnTheSameAASenderAgent(1000, 1500);

	}

	@Test
	public void networkPingPong() {

		// addMadkitArgs("--kernelLogLevel",Level.INFO.toString(),"--networkLogLevel",Level.FINEST.toString());
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				setLogLevel(Level.OFF);
				requestRole(GROUP, ROLE);
				launchThreadedMKNetworkInstance(Level.INFO, AbstractAgent.class, forEventAgent1, eventListener2);

				// launchCustomNetworkInstance(Level.OFF, ForEverOnTheSameAASenderAgent.class);
				AgentAddress aa = waitNextMessage().getSender();
				Message m = null;
				for (int i = 0; i < 100; i++) {
					startTimer();
					sendMessage(aa, new StringMessage("test message"));
					m = waitNextMessage();
					stopTimer("");
				}
				System.err.println(m);
			}

			@Override
			protected void liveCycle() {
				this.killAgent(this);

			}
		}, eventListener1);
		cleanHelperMDKs();
	}

	@Test
	public void internal() {
		final int nbOfExchanges = 100000;
		addMadkitArgs("--network", "false", "--kernelLogLevel", Level.INFO.toString(), "--networkLogLevel",
				Level.INFO.toString());
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				setLogLevel(Level.OFF);
				requestRole(GROUP, ROLE);
				ForEverOnTheSameAASenderAgent a;
				launchAgent(a = new ForEverOnTheSameAASenderAgent(nbOfExchanges, 0));
				a.setLogLevel(Level.OFF);
				AgentAddress aa = waitNextMessage().getSender();
				Message m = null;
				startTimer();
				for (int i = 0; i < nbOfExchanges; i++) {
					sendMessage(aa, new Message());
					m = waitNextMessage();
				}
				stopTimer("for " + nbOfExchanges + " messages exchanged ");
				System.err.println(m);
			}

			@Override
			protected void liveCycle() {
				this.killAgent(this);
			}

		});
	}
	
	

}