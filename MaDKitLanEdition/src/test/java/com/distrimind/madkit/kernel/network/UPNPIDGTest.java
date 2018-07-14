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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.fourthline.cling.support.model.PortMapping.Protocol;
import org.junit.Assert;
import org.junit.Test;

import com.distrimind.madkit.agr.LocalCommunity;
import com.distrimind.madkit.exceptions.SelfKillException;
import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.AgentFakeThread;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.kernel.MadkitEventListener;
import com.distrimind.madkit.kernel.MadkitProperties;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.kernel.network.UpnpIGDAgent.AskForConnectionStatusMessage;
import com.distrimind.madkit.kernel.network.UpnpIGDAgent.AskForExternalIPMessage;
import com.distrimind.madkit.kernel.network.UpnpIGDAgent.AskForPortMappingAddMessage;
import com.distrimind.madkit.kernel.network.UpnpIGDAgent.AskForPortMappingDeleteMessage;
import com.distrimind.madkit.kernel.network.UpnpIGDAgent.AskForRouterDetectionInformation;
import com.distrimind.madkit.kernel.network.UpnpIGDAgent.ConnexionStatusMessage;
import com.distrimind.madkit.kernel.network.UpnpIGDAgent.ExternalIPMessage;
import com.distrimind.madkit.kernel.network.UpnpIGDAgent.IGDRouterFoundMessage;
import com.distrimind.madkit.kernel.network.UpnpIGDAgent.MappingReturnCode;
import com.distrimind.madkit.kernel.network.UpnpIGDAgent.PortMappingAnswerMessage;

public class UPNPIDGTest extends JunitMadkit {
	static final int internalPort = 32110;
	static final int portStart = 55000;
	static final int portEnd = 55002;

	@Test
	public void testUPNPIGDAgent() {
		launchTest(new AbstractAgent() {
			final AtomicBoolean routerAlreadyFound = new AtomicBoolean(false);
			final AtomicBoolean externalIPReceived = new AtomicBoolean(false);
			final AtomicBoolean connectionStatus = new AtomicBoolean(false);
			final AtomicBoolean portMapped = new AtomicBoolean(false);
			final AtomicBoolean portUnmapped = new AtomicBoolean(false);

			@Override
			public void activate() {
				try {

					launchAgent(new AgentFakeThread() {

						@Override
						protected void liveByStep(Message _message) {
							try {
								if (_message instanceof IGDRouterFoundMessage) {
									System.out.println(_message);
									if (!routerAlreadyFound.getAndSet(true))
									{
										IGDRouterFoundMessage m = (IGDRouterFoundMessage) _message;
										this.sendMessageWithRole(LocalCommunity.Groups.NETWORK,
												LocalCommunity.Roles.LOCAL_NETWORK_EXPLORER_ROLE,
												new AskForConnectionStatusMessage(m.getConcernedRouter(),
														getMadkitConfig().networkProperties.delayBetweenEachRouterConnectionCheck),
												LocalCommunity.Roles.LOCAL_NETWORK_ROLE);
										this.sendMessageWithRole(LocalCommunity.Groups.NETWORK,
												LocalCommunity.Roles.LOCAL_NETWORK_EXPLORER_ROLE,
												new AskForExternalIPMessage(m.getConcernedRouter(),
														getMadkitConfig().networkProperties.delayBetweenEachExternalIPRouterCheck),
												LocalCommunity.Roles.LOCAL_NETWORK_ROLE);
									}
								} else if (_message instanceof ConnexionStatusMessage) {

									connectionStatus.set(true);
								} else if (_message instanceof ExternalIPMessage) {
									externalIPReceived.set(true);
									ExternalIPMessage m = (ExternalIPMessage) _message;
									if (m.getExternalIP() instanceof Inet4Address) {
										InetAddress ia = null;
										for (Enumeration<NetworkInterface> eni = NetworkInterface
												.getNetworkInterfaces(); eni.hasMoreElements();) {
											NetworkInterface ni = eni.nextElement();
											for (Enumeration<InetAddress> eia = ni.getInetAddresses(); eia
													.hasMoreElements();) {
												ia = eia.nextElement();
												if (!ia.isAnyLocalAddress() && !ia.isLoopbackAddress()
														&& (ia instanceof Inet4Address) /*&& !ni.getName().startsWith("wlan")*/) {
													break;
												} else
													ia = null;
											}
											if (ia != null)
												break;
										}

										if (ia != null) {
											ArrayList<Integer> list_ports = new ArrayList<>();
											for (int i = portStart; i <= portEnd; i++) {
												list_ports.add(i);
											}

											final AskForPortMappingAddMessage a = new AskForPortMappingAddMessage(
													m.getConcernedRouter(), ia, list_ports, internalPort, "",
													Protocol.TCP);
											//this.pause(2000);
											sendMessageWithRole(LocalCommunity.Groups.NETWORK,
													LocalCommunity.Roles.LOCAL_NETWORK_EXPLORER_ROLE, a,
													LocalCommunity.Roles.LOCAL_NETWORK_ROLE);
											
										}
										else
											Assert.fail();
									}

								} else if (_message.getClass() == PortMappingAnswerMessage.class) {
									if (portMapped.get())
									{
										PortMappingAnswerMessage m = (PortMappingAnswerMessage) _message;
										Assert.assertEquals(Protocol.TCP, m.getProtocol());
										Assert.assertEquals(internalPort, m.getInternalPort());
	
										Assert.assertEquals("message="+m.getMessage()+", description="+m.getDescription()+", external port="+m.getExternalPort(), MappingReturnCode.REMOVED, m.getReturnCode());
										Assert.assertTrue(
												m.getExternalPort() <= portEnd && m.getExternalPort() >= portStart);
	
										portUnmapped.set(true);
										JunitMadkit.pause(this, 1000);
										this.killAgent(this);
									}
									else
									{
										PortMappingAnswerMessage m = (PortMappingAnswerMessage) _message;
										Assert.assertEquals(Protocol.TCP, m.getProtocol());
										Assert.assertEquals(internalPort, m.getInternalPort());
	
										Assert.assertEquals("message="+m.getMessage()+", description="+m.getDescription()+", external port="+m.getExternalPort(), MappingReturnCode.SUCESS, m.getReturnCode());
										Assert.assertTrue(
												m.getExternalPort() <= portEnd && m.getExternalPort() >= portStart);
	
										portMapped.set(true);
										AskForPortMappingDeleteMessage m2 = new AskForPortMappingDeleteMessage(
												m.getConcernedRouter(), m.getExternalPort(), Protocol.TCP);
										this.sendMessageWithRole(LocalCommunity.Groups.NETWORK,
												LocalCommunity.Roles.LOCAL_NETWORK_EXPLORER_ROLE, m2,
												LocalCommunity.Roles.LOCAL_NETWORK_ROLE);
										
									}
								}
							} catch (SelfKillException e) {
								throw e;
							} catch (Exception e) {
								e.printStackTrace();
								Assert.fail();
							}

						}

						@Override
						public void activate() {
							try {
								requestRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.LOCAL_NETWORK_ROLE);
								launchAgent(UpnpIGDAgent.getInstance());
								this.sendMessageWithRole(LocalCommunity.Groups.NETWORK,
										LocalCommunity.Roles.LOCAL_NETWORK_EXPLORER_ROLE,
										new AskForRouterDetectionInformation(true),
										LocalCommunity.Roles.LOCAL_NETWORK_ROLE);
							} catch (Exception e) {
								e.printStackTrace();
								Assert.fail();
							}
						}
					});
					do {
						JunitMadkit.pause(this, 1000);
						System.out.println("-------------------");
						System.out.println("connection status : " + connectionStatus.get());
						System.out.println("external ip : " + externalIPReceived.get());
						System.out.println("port mapped : " + portMapped.get());
						System.out.println("port unmapped : " + portUnmapped.get());

					} while (!(externalIPReceived.get() && connectionStatus.get() && portMapped.get() && portUnmapped.get()));

					Assert.assertTrue(externalIPReceived.get());
					Assert.assertTrue(connectionStatus.get());
					Assert.assertTrue(portMapped.get());
					Assert.assertTrue(portUnmapped.get());
				} catch (Exception e) {
					e.printStackTrace();
					Assert.fail();
				}
			}
		}, new MadkitEventListener() {

			@Override
			public void onMadkitPropertiesLoaded(MadkitProperties _properties) {
				_properties.networkProperties.network = true;
				_properties.networkProperties.upnpIGDEnabled = true;
				_properties.madkitLogLevel = Level.INFO;
				_properties.agentLogLevel = Level.INFO;
				_properties.networkProperties.UpnpIGDLogLevel = Level.INFO;
				_properties.networkProperties.networkLogLevel = Level.INFO;
			}
		});
	}
}
