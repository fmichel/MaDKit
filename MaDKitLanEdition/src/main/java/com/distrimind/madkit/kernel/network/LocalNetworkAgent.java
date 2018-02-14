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
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import org.fourthline.cling.support.model.Connection.Status;
import org.fourthline.cling.support.model.Connection.StatusInfo;
import org.fourthline.cling.support.model.PortMapping.Protocol;

import com.distrimind.madkit.agr.LocalCommunity;
import com.distrimind.madkit.kernel.AgentFakeThread;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.kernel.NetworkAgent;
import com.distrimind.madkit.kernel.Task;
import com.distrimind.madkit.kernel.network.UpnpIGDAgent.AskForRouterDetectionInformation;
import com.distrimind.madkit.kernel.network.UpnpIGDAgent.ConnexionStatusMessage;
import com.distrimind.madkit.kernel.network.UpnpIGDAgent.ExternalIPMessage;
import com.distrimind.madkit.kernel.network.BindInetSocketAddressMessage.Type;
import com.distrimind.madkit.kernel.network.UpnpIGDAgent.AskForConnectionStatusMessage;
import com.distrimind.madkit.kernel.network.UpnpIGDAgent.AskForExternalIPMessage;
import com.distrimind.madkit.kernel.network.UpnpIGDAgent.AskForPortMappingAddMessage;
import com.distrimind.madkit.kernel.network.UpnpIGDAgent.IGDRouterFoundMessage;
import com.distrimind.madkit.kernel.network.UpnpIGDAgent.IGDRouterLostMessage;
import com.distrimind.madkit.kernel.network.UpnpIGDAgent.MappingReturnCode;
import com.distrimind.madkit.kernel.network.UpnpIGDAgent.PortMappingAnswerMessage;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocol.ConnectionClosedReason;
import com.distrimind.util.SystemFunctions;

/**
 * This agent represents a local network. He aims to select which network
 * interface, according it speed, has to be used to make connections.
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
class LocalNetworkAgent extends AgentFakeThread {
	private final ArrayList<NetworkInterface> network_interfaces = new ArrayList<>();
	private final InterfaceAddress network_interface_ipv6, network_interface_ipv4;
	private final ArrayList<BindInetSocketAddressMessage> socket_binds = new ArrayList<>();
	protected volatile ArrayList<BindInetSocketAddressMessage> effective_socket_binds = new ArrayList<>();
	private boolean upnpIGDEnabled = false;
	private HashMap<InetAddress, Router> routers = new HashMap<>();
	private final Stack<AskForConnectionMessage> standby_connections = new Stack<>();
	private final ArrayList<ConnectionStatusMessage> effective_connections = new ArrayList<>();
	private boolean askConnectionActivated = false;

	@Override
	public String toString() {
		return super.toString() + "[network_interfaces=" + network_interfaces + "]";
	}

	boolean isSameAs(LocalNetworkAgent lna) throws SocketException {
		if (network_interface_ipv6 != null && lna.network_interface_ipv6 != null) {
			return InetAddressFilter.isSameLocalNetwork(network_interface_ipv6.getAddress(),
					lna.network_interface_ipv6.getAddress());
		} else if (network_interface_ipv4 != null && lna.network_interface_ipv4 != null) {
			return InetAddressFilter.isSameLocalNetwork(network_interface_ipv4.getAddress(),
					lna.network_interface_ipv4.getAddress());
		} else
			return false;

	}

	private LocalNetworkAgent(NetworkInterface ni, InterfaceAddress ia) {
		if (ia.getAddress() instanceof Inet4Address) {
			network_interface_ipv4 = ia;
			network_interface_ipv6 = null;
		} else if (ia.getAddress() instanceof Inet6Address) {
			network_interface_ipv4 = null;
			network_interface_ipv6 = ia;
		} else
			throw new IllegalArgumentException();
		network_interfaces.add(ni);
	}

	private LocalNetworkAgent(NetworkInterface ni, InterfaceAddress ia1, InterfaceAddress ia2) {
		if ((ia1.getAddress() instanceof Inet4Address) && (ia2.getAddress() instanceof Inet6Address)) {
			network_interface_ipv4 = ia1;
			network_interface_ipv6 = ia2;
		} else if ((ia1.getAddress() instanceof Inet6Address) && (ia2.getAddress() instanceof Inet4Address)) {
			network_interface_ipv4 = ia2;
			network_interface_ipv6 = ia1;
		} else
			throw new IllegalArgumentException();
		network_interfaces.add(ni);
	}

	static ArrayList<LocalNetworkAgent> extractLocalNetworkAgents(NetworkInterface ni, ArrayList<InterfaceAddress> list) {
		ArrayList<LocalNetworkAgent> res = new ArrayList<>();
		if (list.size() == 1) {
			res.add(new LocalNetworkAgent(ni, list.iterator().next()));
		} else if (list.size() == 2) {
			try {
				Iterator<InterfaceAddress> it = list.iterator();
				res.add(new LocalNetworkAgent(ni, it.next(), it.next()));
			} catch (Exception e) {
				res.clear();
				Iterator<InterfaceAddress> it = list.iterator();
				res.add(new LocalNetworkAgent(ni, it.next()));
				res.add(new LocalNetworkAgent(ni, it.next()));
			}
		} else {
			for (Iterator<InterfaceAddress> it = list.iterator(); it.hasNext();)
				res.add(new LocalNetworkAgent(ni, it.next()));
		}
		return res;
	}

	static ArrayList<LocalNetworkAgent> putNetworkInterfaces(List<LocalNetworkAgent> _local_network_agents,
			Collection<NetworkInterface> nis) {
		ArrayList<LocalNetworkAgent> res = new ArrayList<>();
		ArrayList<LocalNetworkAgent> local_network_agents = new ArrayList<LocalNetworkAgent>(
				_local_network_agents.size());
		local_network_agents.addAll(_local_network_agents);
		for (NetworkInterface ni : nis) {
			ArrayList<LocalNetworkAgent> l = putNetworkInterface(local_network_agents, ni);
			res.addAll(l);
			local_network_agents.addAll(l);
		}
		return res;
	}

	/**
	 * 
	 * @param _local_network_agents
	 * @param nis_to_add
	 * @param nis_to_remove
	 * @return agents which has to be launch (if they are not) and the agents that
	 *         must be killed (if they are launched)
	 */
	static ArrayList<LocalNetworkAgent> putAndRemoveNetworkInterfaces(
			ArrayList<LocalNetworkAgent> _local_network_agents, Collection<NetworkInterface> nis_to_add,
			Collection<NetworkInterface> nis_to_remove) {
		synchronized (LocalNetworkAgent.class) {
			ArrayList<LocalNetworkAgent> ladd = putNetworkInterfaces(_local_network_agents, nis_to_add);
			ladd.removeAll(removeNetworkInterfaces(ladd, nis_to_remove));
			ladd.addAll(removeNetworkInterfaces(_local_network_agents, nis_to_remove));
			return ladd;
		}
	}

	private static ArrayList<LocalNetworkAgent> putNetworkInterface(List<LocalNetworkAgent> local_network_agents,
			NetworkInterface ni) {
		if (ni == null)
			throw new NullPointerException("ni");
		if (local_network_agents == null)
			throw new NullPointerException("local_network_agents");

		ArrayList<LocalNetworkAgent> res = new ArrayList<>();
		ArrayList<InterfaceAddress> not_found_addresses = new ArrayList<InterfaceAddress>();
		ArrayList<LocalNetworkAgent> found_lna_match = new ArrayList<LocalNetworkAgent>();
		int niValidAddressesNumber=0;
		for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
			if (isValid(ia)) {
				++niValidAddressesNumber;
				boolean found = false;
				for (LocalNetworkAgent lna : local_network_agents) {
					if (lna.isConcernedBy(ia)) {
						boolean add = true;
						for (LocalNetworkAgent lna2 : found_lna_match) {
							if (lna == lna2)
								add = false;
						}
						if (add) {
							found_lna_match.add(lna);

						}
						found = true;
					}
				}
				if (!found)
					not_found_addresses.add(ia);
			}
		}
		if (found_lna_match.size() > 0) {
			for (LocalNetworkAgent lna : found_lna_match)
				lna.receiveMessage(new NetworkInterfaceAddedMessage(ni));
		}
		if (not_found_addresses.size() > 0) {
			if (not_found_addresses.size() == niValidAddressesNumber) {
				res.addAll(extractLocalNetworkAgents(ni, not_found_addresses));
			} else {
				for (InterfaceAddress ia : not_found_addresses) {
					res.add(new LocalNetworkAgent(ni, ia));
				}
			}
		}
		return res;
	}

	private static ArrayList<LocalNetworkAgent> removeNetworkInterfaces(
			ArrayList<LocalNetworkAgent> _local_network_agents, Collection<NetworkInterface> nis) {

		ArrayList<LocalNetworkAgent> res = new ArrayList<>();
		int toRemove[] = new int[_local_network_agents.size()];
		for (int i = 0; i < toRemove.length; i++)
			toRemove[i] = 0;
		for (NetworkInterface ni : nis) {

			for (int i = 0; i < toRemove.length; i++) {
				LocalNetworkAgent lna = _local_network_agents.get(i);
				if (lna.isConcernedBy(ni)) {
					lna.receiveMessage(new NetworkInterfaceRemovedMessage(ni));
					toRemove[i]++;
				}
			}
		}
		for (int i = 0; i < toRemove.length; i++) {
			LocalNetworkAgent lna = _local_network_agents.get(i);
			if (lna.network_interfaces.size() == toRemove[i])
				res.add(lna);
		}
		return res;
	}

	static class ActivateAskConnection extends Message {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7127554650681077601L;

		final boolean askConnectionActivated;

		ActivateAskConnection(boolean activateAskConnection) {
			this.askConnectionActivated = activateAskConnection;
		}
	}

	/*
	 * static ArrayList<LocalNetworkAgent>
	 * removeNetworkInterface(List<LocalNetworkAgent> local_network_agents,
	 * NetworkInterface ni) { if (ni==null) throw new NullPointerException("ni"); if
	 * (local_network_agents==null) throw new
	 * NullPointerException("local_network_agents");
	 * 
	 * ArrayList<LocalNetworkAgent> res=new ArrayList<>();
	 * 
	 * for (LocalNetworkAgent lna : local_network_agents) { if
	 * (lna.isConcernedBy(ni)) { lna.receiveMessage(new
	 * NetworkInterfaceRemovedMessage(ni));
	 * 
	 * } } }
	 */
	private boolean isConcernedBy(NetworkInterface ni) {
		for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
			if (isConcernedBy(ia))
				return true;
		}
		return false;
	}

	private boolean isConcernedBy(InterfaceAddress ia) {
		if (ia == null) {
			return false;
		} else
			return isConcernedBy(ia.getAddress());
	}

	protected boolean isConcernedBy(InetAddress ia) {
		if (ia == null) {
			return false;
		} else if (!isValid(ia)) {
			return false;
		} else if (network_interface_ipv4 != null && ia instanceof Inet4Address) {
			return InetAddressFilter.isSameLocalNetwork(network_interface_ipv4.getAddress().getAddress(),
					ia.getAddress(), network_interface_ipv4.getNetworkPrefixLength());
		} else if (network_interface_ipv6 != null && ia instanceof Inet6Address) {
			return InetAddressFilter.isSameLocalNetwork(network_interface_ipv6.getAddress().getAddress(),
					ia.getAddress(), network_interface_ipv6.getNetworkPrefixLength());
		} else
			return false;
	}

	@Override
	protected void activate() {
		setLogLevel(getMadkitConfig().networkProperties.networkLogLevel);
		if (logger != null && logger.isLoggable(Level.FINE))
			logger.fine("Launching LocalNetworkAgent ...");
		upnpIGDEnabled = getMadkitConfig().networkProperties.upnpIGDEnabled;

		/*
		 * this.requestRole(LocalCommunity.Groups.NETWORK,
		 * LocalCommunity.Roles.LOCAL_NETWORK_AFFECTATION_ROLE);
		 */
		this.requestRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.LOCAL_NETWORK_ROLE);
		this.requestRole(LocalCommunity.Groups.LOCAL_NETWORKS, LocalCommunity.Roles.LOCAL_NETWORK_AFFECTATION_ROLE);
		this.requestRole(LocalCommunity.Groups.LOCAL_NETWORKS, LocalCommunity.Roles.LOCAL_NETWORK_ROLE);

		// this.requestRole(LocalCommunity.Groups.getLocalLanGroup(inet_address),
		// LocalCommunity.Roles.LOCAL_NETWORK_AFFECTATION_ROLE);
		if (upnpIGDEnabled) {
			this.sendMessageWithRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.LOCAL_NETWORK_EXPLORER_ROLE,
					new AskForRouterDetectionInformation(true), LocalCommunity.Roles.LOCAL_NETWORK_ROLE);
		}

		if (logger != null && logger.isLoggable(Level.FINE))
			logger.fine("LocalNetworkAgent LAUNCHED !");

	}

	@Override
	protected void end() {
		if (upnpIGDEnabled) {
			this.sendMessageWithRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.LOCAL_NETWORK_EXPLORER_ROLE,
					new AskForRouterDetectionInformation(false), LocalCommunity.Roles.LOCAL_NETWORK_ROLE);
		}

		if (logger != null && logger.isLoggable(Level.FINE))
			logger.fine("LocalNetworkAgent  KILLED !");

	}

	private boolean isConcernedBy(BindInetSocketAddressMessage m) {
		InetAddress addr = m.getInetSocketAddress().getAddress();
		if (addr == null)
			return false;
		if (addr.isAnyLocalAddress())
			return true;
		else {
			return isConcernedBy(addr);
		}

	}

	private boolean insertBind(BindInetSocketAddressMessage bind_message) {

		for (Iterator<BindInetSocketAddressMessage> it = socket_binds.iterator(); it.hasNext();) {
			BindInetSocketAddressMessage m = it.next();
			if (m.include(bind_message))
				return false;
		}
		for (Iterator<BindInetSocketAddressMessage> it = socket_binds.iterator(); it.hasNext();) {
			BindInetSocketAddressMessage m = it.next();
			if (bind_message.include(m)) {
				it.remove();
			}
		}
		socket_binds.add(bind_message);
		if (logger != null && logger.isLoggable(Level.FINER))
			logger.finer("Bind added : " + bind_message);

		return true;
	}

	private boolean removeBind(BindInetSocketAddressMessage bind_message) {
		boolean changed = false;
		for (Iterator<BindInetSocketAddressMessage> it = socket_binds.iterator(); it.hasNext();) {
			BindInetSocketAddressMessage m = it.next();
			if (bind_message.include(m)) {
				it.remove();
				changed = true;
				if (logger != null && logger.isLoggable(Level.FINER))
					logger.finer("Bind removed : " + m);

			}
		}
		return changed;
	}

	@Override
	protected void liveByStep(Message _message) {
		if (_message instanceof IGDRouterFoundMessage) {
			IGDRouterFoundMessage m = (IGDRouterFoundMessage) _message;

			if (isConcernedBy(m.getConcernedRouter())) {
				if (logger != null && logger.isLoggable(Level.FINER))
					logger.finer("IGD router found : " + m);

				routers.put(m.getConcernedRouter(), new Router(m.getConcernedRouter()));

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
		} else if (_message.getClass() == PortMappingAnswerMessage.class) {
			PortMappingAnswerMessage m = (PortMappingAnswerMessage) _message;
			if (getMadkitConfig().networkProperties.portsToBindForAutomaticLocalConnections == m.getInternalPort()
					&& m.getReturnCode().equals(MappingReturnCode.SUCESS)) {
				Router r = routers.get(m.getConcernedRouter());
				if (r != null) {
					if (logger != null && logger.isLoggable(Level.FINEST))
						logger.finest("Port mapping " + m.getInternalPort() + " added : " + r);

					r.setExternalPort(m.getExternalPort());
				}
			}
		} else if (_message instanceof IGDRouterLostMessage) {
			Router r = routers.remove(((IGDRouterLostMessage) _message).getConcernedRouter());
			if (r != null) {
				if (logger != null && logger.isLoggable(Level.FINER))
					logger.finer("IGD router lost : " + r);

				checkAndUpdateMadkitRoute();
			}
		} else if (_message instanceof ConnexionStatusMessage) {
			ConnexionStatusMessage m = (ConnexionStatusMessage) _message;
			Router r = routers.get(m.getConcernedRouter());
			if (r != null) {
				if (logger != null && logger.isLoggable(Level.FINER))
					logger.finer("Received IGD router connection status : " + m);

				r.changeStatus(m.getStatus());
				if (r.canInferGlobalMadkitRoutePolicy()) {
					checkAndUpdateMadkitRoute();
				}
			}
		} else if (_message instanceof ExternalIPMessage) {
			ExternalIPMessage m = (ExternalIPMessage) _message;
			Router r = routers.get(m.getConcernedRouter());
			if (r != null) {
				if (logger != null && logger.isLoggable(Level.FINER))
					logger.finer("Received IGD router's external IP: " + m);

				r.changeExternalIp(m.getExternalIP());
				if (m.getExternalIP() instanceof Inet4Address && r.getExternalPort() < 0
						&& this.network_interface_ipv4 != null) {
					AskForPortMappingAddMessage a = new AskForPortMappingAddMessage(m.getConcernedRouter(),
							network_interface_ipv4.getAddress(),
							getMadkitConfig().networkProperties.externalRouterPortsToMap,
							getMadkitConfig().networkProperties.portsToBindForAutomaticLocalConnections, "",
							Protocol.TCP);
					this.sendMessageWithRole(LocalCommunity.Groups.NETWORK,
							LocalCommunity.Roles.LOCAL_NETWORK_EXPLORER_ROLE, a,
							LocalCommunity.Roles.LOCAL_NETWORK_ROLE);
				}
				if (r.canInferGlobalMadkitRoutePolicy())
					checkAndUpdateMadkitRoute();
			}
		} else if (_message instanceof BindInetSocketAddressMessage) {
			BindInetSocketAddressMessage bind = (BindInetSocketAddressMessage) _message;
			if (isConcernedBy(bind)) {
				boolean changed = false;
				if (bind.getType().equals(Type.BIND))
					changed = insertBind(bind);
				else
					changed = removeBind(bind);
				if (changed)
					checkAndUpdateMadkitRoute();
			}
		} else if (_message instanceof NetworkInterfaceAddedMessage) {
			boolean add = false;
			synchronized (LocalNetworkAgent.class) {
				NetworkInterfaceAddedMessage m = (NetworkInterfaceAddedMessage) _message;
				if (isConcernedBy(m.getNetworkInterface())) {
					add = !network_interfaces.contains(m.getNetworkInterface());

					if (add) {
						if (logger != null && logger.isLoggable(Level.FINER))
							logger.finer("Network interface connected : " + m.getNetworkInterface());
						network_interfaces.add(m.getNetworkInterface());
					}
				}
			}
			if (add)
				checkAndUpdateMadkitRoute();
		} else if (_message instanceof NetworkInterfaceRemovedMessage) {
			synchronized (LocalNetworkAgent.class) {
				NetworkInterfaceRemovedMessage m = (NetworkInterfaceRemovedMessage) _message;
				if (network_interfaces.remove(m.getNetworkInterface())) {
					if (logger != null && logger.isLoggable(Level.FINER))
						logger.finer("Network interface removed : " + m.getNetworkInterface());

					checkAndUpdateMadkitRoute();
					if (network_interfaces.isEmpty())
						this.killAgent(this);
				}
			}
		} else if (_message.getClass() == ActivateAskConnection.class) {
			ActivateAskConnection a = (ActivateAskConnection) _message;
			this.askConnectionActivated = a.askConnectionActivated;
			checkStandbyConnections();

		} else if (_message.getClass() == AskForConnectionMessage.class) {
			AskForConnectionMessage con = (AskForConnectionMessage) _message;
			if (logger != null && logger.isLoggable(Level.FINER))
				logger.finer("Receiving : " + con);

			if (con.type.equals(ConnectionStatusMessage.Type.CONNECT)) {
				boolean connection_not_already_established = true;
				if (askConnectionActivated) {
					for (ConnectionStatusMessage csm : effective_connections) {
						if (csm.getIP().equals(con.getIP())) {
							connection_not_already_established = false;
							break;
						}
					}
					if (connection_not_already_established) {
						if (logger != null && logger.isLoggable(Level.FINER))
							logger.finer("Ask for connection : " + con);

						boolean found = false;
						con.chooseIP(isIPV6ConnectionPossible());
						if (con.choosenIP != null) {
							boolean local = isConcernedBy(con.getChoosenIP().getAddress());
							for (BindInetSocketAddressMessage b : effective_socket_binds) {
								found = isConcernedBy(b, con, local);
								if (found) {
									con = con.clone();

									con.interface_address = b.getInetSocketAddress();
									if (this.broadcastMessageWithRole(LocalCommunity.Groups.LOCAL_NETWORKS,
											LocalCommunity.Roles.NIO_ROLE, con, LocalCommunity.Roles.LOCAL_NETWORK_ROLE)
											.equals(ReturnCode.NO_RECIPIENT_FOUND) && logger != null) {
										logger.severe("No NIO agent found !");

									}
									break;
								}
							}
						}
						if (!found) {
							addStandbyAskedConnection(con);
						}
					}
				} else {
					addStandbyAskedConnection(con);
				}
			} else if (con.type.equals(ConnectionStatusMessage.Type.DISCONNECT)) {
				/*
				 * for (Iterator<ConnectionStatusMessage> it=
				 * effective_connections.iterator();it.hasNext();) { ConnectionStatusMessage
				 * cs=it.next(); if (cs.address.equals(con.address)) { it.remove();
				 * //found=true; break; } }
				 */
				if (logger != null && logger.isLoggable(Level.FINER))
					logger.finer("Ask for deconnection : " + con);

				this.broadcastMessageWithRole(LocalCommunity.Groups.LOCAL_NETWORKS, LocalCommunity.Roles.NIO_ROLE, con,
						LocalCommunity.Roles.LOCAL_NETWORK_ROLE);
			}
		} else if (_message.getClass() == ConnectionStatusMessage.class) {
			ConnectionStatusMessage cs = (ConnectionStatusMessage) _message;

			if (cs.type.equals(ConnectionStatusMessage.Type.CONNECT)
					&& isConcernedBy(cs.interface_address.getAddress())) {
				boolean found = false;
				for (ConnectionStatusMessage cs2 : effective_connections) {
					if (cs2.getIP().equals(cs.getIP())) {
						found = true;
						break;
					}
				}
				if (!found)
					effective_connections.add(cs);
				if (logger != null && logger.isLoggable(Level.FINER))
					logger.finer("Receving : " + cs);

			} else if (cs.type.equals(ConnectionStatusMessage.Type.DISCONNECT)) {
				for (Iterator<ConnectionStatusMessage> it = effective_connections.iterator(); it.hasNext();) {
					ConnectionStatusMessage cs2 = it.next();
					if (cs2.getIP().equals(cs.getIP()) && cs2.interface_address.equals(cs.interface_address)) {
						it.remove();
						break;
					}
				}
				if (cs.connection_closed_reason.equals(ConnectionClosedReason.CONNECTION_LOST)) {
					receiveMessage(new AskForConnectionMessage(ConnectionStatusMessage.Type.CONNECT, cs.getIP()));
				} else if (cs.connection_closed_reason.equals(ConnectionClosedReason.CONNECTION_ANOMALY)) {
					for (Iterator<AskForConnectionMessage> it = standby_connections.iterator(); it.hasNext();) {
						if (it.next().getIP().equals(cs.getIP()))
							it.remove();
					}
				}
				if (logger != null && logger.isLoggable(Level.FINER))
					logger.finer("Receving : " + cs);

			}
		} else if (_message instanceof NetworkAgent.StopNetworkMessage) {
			this.killAgent(this);
		}

	}

	private boolean isIPV6ConnectionPossible() {
		for (BindInetSocketAddressMessage bind : this.effective_socket_binds)
			if (bind.getInetSocketAddress().getAddress() instanceof Inet6Address)
				return true;
		return false;
	}

	/*
	 * private boolean isConcernedBy(ConnectionStatusMessage con, boolean local) {
	 * if ((this.network_interface_ipv6!=null && (con.getChoosenIP().getAddress()
	 * instanceof Inet6Address)) || (this.network_interface_ipv4!=null &&
	 * con.getChoosenIP().getAddress() instanceof Inet4Address)) { InetSocketAddress
	 * choosenIP=con.getChoosenIP(); if (choosenIP.getAddress().isAnyLocalAddress()
	 * || choosenIP.getAddress().isMulticastAddress()) { return false; } else if
	 * (local) { return true; } else if (choosenIP.getAddress().isLinkLocalAddress()
	 * || choosenIP.getAddress().isSiteLocalAddress() ||
	 * con.getChoosenIP().getAddress().isLoopbackAddress()) { return false; } else
	 * if (upnpIGDEnabled) { for (Router r : routers.values()) { if
	 * (r.getStatus().equals(Status.Connected)) { return true; } } } else { return
	 * true; } } return false; }
	 */

	private boolean isConcernedBy(BindInetSocketAddressMessage b, ConnectionStatusMessage con, boolean local) {
		InetSocketAddress choosenIP = con.getChoosenIP();
		if (choosenIP == null)
			return false;
		if (/*
			 * b.getInetSocketAddress().getPort()==con.getChoosenIP().getPort() &&
			 */((b.getInetSocketAddress().getAddress() instanceof Inet4Address)
				&& (choosenIP.getAddress() instanceof Inet4Address)
				|| (b.getInetSocketAddress().getAddress() instanceof Inet6Address)
						&& (choosenIP.getAddress() instanceof Inet6Address))) {

			if (choosenIP.getAddress().isAnyLocalAddress() || choosenIP.getAddress().isMulticastAddress()) {
				return false;
			} else if (b.getInetSocketAddress().getAddress().isLoopbackAddress()
					&& !choosenIP.getAddress().isLoopbackAddress()) {
				return false;
			} else if (local) {
				return true;
			} else if (choosenIP.getAddress().isLinkLocalAddress() || choosenIP.getAddress().isSiteLocalAddress()
					|| con.getChoosenIP().getAddress().isLoopbackAddress()) {
				return false;
			} else if (upnpIGDEnabled) {
				for (Router r : routers.values()) {
					if (r.getStatus().equals(Status.Connected)) {
						return true;
					}
				}
			} else {
				return true;
			}
		}
		return false;
	}

	void checkStandbyConnections() {
		if (askConnectionActivated) {
			if (logger != null && logger.isLoggable(Level.FINEST))
				logger.finest("Checking standby connections...");

			while (!standby_connections.isEmpty()) {
				receiveMessage(standby_connections.pop());
			}
		}
	}

	private void addStandbyAskedConnection(AskForConnectionMessage m) {

		for (Iterator<AskForConnectionMessage> it = standby_connections.iterator(); it.hasNext();) {
			AskForConnectionMessage afcm = it.next();
			if (afcm.getIP().equals(m.getIP())) {
				it.remove();
			}
		}
		if (m.getType().equals(ConnectionStatusMessage.Type.CONNECT))
			standby_connections.add(m);
		if (logger != null && logger.isLoggable(Level.FINER))
			logger.finer("Standby connection added : " + m);

	}

	void checkConnectionsToAdd(ArrayList<BindInetSocketAddressMessage> added_binds,
			ArrayList<BindInetSocketAddressMessage> removed_binds) {
		for (BindInetSocketAddressMessage newBind : added_binds) {
			for (ConnectionStatusMessage ask : effective_connections) {
				if (isConcernedBy(newBind, ask, isConcernedBy(ask.getChoosenIP().getAddress()))) {
					BindInetSocketAddressMessage bestBind = newBind;
					for (BindInetSocketAddressMessage b : effective_socket_binds) {
						if (isConcernedBy(b, ask, isConcernedBy(ask.getChoosenIP().getAddress()))) {
							if (!removed_binds.contains(b)) {
								if (b.networkInterfaceSpeed >= bestBind.networkInterfaceSpeed) {
									bestBind = b;
								}
							}
						}
					}
					if (newBind == bestBind) {
						if (logger != null && logger.isLoggable(Level.FINER))
							logger.finer("New better connection to bind + " + newBind);

						this.broadcastMessageWithRole(LocalCommunity.Groups.LOCAL_NETWORKS,
								LocalCommunity.Roles.NIO_ROLE,
								new AskForConnectionMessage(
										com.distrimind.madkit.kernel.network.ConnectionStatusMessage.Type.CONNECT,
										ask.getIP(), ask.getChoosenIP(), newBind.getInetSocketAddress()),
								LocalCommunity.Roles.LOCAL_NETWORK_ROLE);
					}

				}
			}

		}
	}

	private void checkAndUpdateMadkitRoute() {
		ArrayList<BindInetSocketAddressMessage> binds = new ArrayList<>();
		synchronized (LocalNetworkAgent.class) {
			for (BindInetSocketAddressMessage b : socket_binds) {
				binds.addAll(selectNetworkInterfaces(b, network_interfaces));
			}
		}
		ArrayList<BindInetSocketAddressMessage> added_binds = new ArrayList<>(binds.size());
		final ArrayList<BindInetSocketAddressMessage> removed_binds = new ArrayList<>(binds.size());

		// check added binds
		for (BindInetSocketAddressMessage b : binds) {
			boolean found = false;
			for (BindInetSocketAddressMessage o : this.effective_socket_binds) {

				if (b.getInetSocketAddress().equals(o.getInetSocketAddress())) {
					found = true;
					break;
				}
			}
			if (!found) {
				added_binds.add(b);
				this.broadcastMessageWithRole(LocalCommunity.Groups.LOCAL_NETWORKS, LocalCommunity.Roles.NIO_ROLE, b,
						LocalCommunity.Roles.LOCAL_NETWORK_ROLE);
			}
		}
		// check removed binds
		for (BindInetSocketAddressMessage o : effective_socket_binds) {
			boolean found = false;
			for (BindInetSocketAddressMessage b : binds) {
				if (b.getInetSocketAddress().equals(o.getInetSocketAddress())) {
					found = true;
					break;
				}
			}
			if (!found) {
				removed_binds.add(o);

			}
		}
		synchronized (LocalNetworkAgent.class) {
			effective_socket_binds = binds;
		}
		if (added_binds.size() > 0) {
			checkConnectionsToAdd(added_binds, removed_binds);
			checkStandbyConnections();
		}
		scheduleTask(new Task<>(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				synchronized (LocalNetworkAgent.class) {
					if (isAlive()) {
						for (BindInetSocketAddressMessage o : removed_binds) {
							if (!effective_socket_binds.contains(o))
								broadcastMessage(LocalCommunity.Groups.LOCAL_NETWORKS, LocalCommunity.Roles.NIO_ROLE,
										new BindInetSocketAddressMessage(Type.DISCONNECT, o.getInetSocketAddress()),
										false);
						}
					}
					return null;
				}
			}
		}, getMadkitConfig().networkProperties.maxDurationBeforeClosingObsoleteNetworkInterfaces
				+ System.currentTimeMillis()));
		if (logger != null && logger.isLoggable(Level.FINER))
			logger.finer("Madkit route updated");

		/*
		 * if (removed_binds) checkConnectionsToStandby();
		 */
	}

	private static boolean isValid(InterfaceAddress ia) {
		return ia != null && isValid(ia.getAddress());
	}

	private static boolean isValid(InetAddress ia) {
		try {
			return !ia.isLinkLocalAddress() && !ia.isAnyLocalAddress() && !ia.isMulticastAddress()
					&& ia.isReachable(500);
		} catch (Exception e) {
			return false;
		}
	}
	
	public static void main(String args[]) throws SocketException
	{
		Enumeration<NetworkInterface> nis=NetworkInterface.getNetworkInterfaces();
		for (;nis.hasMoreElements();)
		{
			NetworkInterface ni=nis.nextElement();
			System.out.println(ni.getDisplayName()+", index="+ni.getIndex()+", loopback="+ni.isLoopback()+", pointtopoint="+ni.isPointToPoint()+", up="+ni.isUp()+", virtual="+ni.isVirtual()+", support multicast="+ni.supportsMulticast());
			for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
				if (isValid(ia))
				{
					System.out.println("\t"+ia.getAddress()+" anylocaladdress="+ia.getAddress().isAnyLocalAddress()+", sitelocaladdress="+ia.getAddress().isSiteLocalAddress()+", linklocaladdress"+ia.getAddress().isLinkLocalAddress()+", loopback="+ia.getAddress().isLoopbackAddress()+
							", multicast="+ia.getAddress().isMulticastAddress()+", mgcglobal="+ia.getAddress().isMCGlobal()+", mcorglocal"+ia.getAddress().isMCOrgLocal()+", mcnodelocal="+ia.getAddress().isMCNodeLocal()+", mcsitelocal="+ia.getAddress().isMCSiteLocal()+", mclinklocal="+ia.getAddress().isMCLinkLocal());
				}
			}
		}
	}

	private ArrayList<BindInetSocketAddressMessage> selectNetworkInterfaces(BindInetSocketAddressMessage bind,
			ArrayList<NetworkInterface> nis) {
		ArrayList<BindInetSocketAddressMessage> res = new ArrayList<>();
		if (bind.getInetSocketAddress().getAddress().isAnyLocalAddress()) {
			ArrayList<NetworkInterface> selected_nis = new ArrayList<>();
			long speeds[] = new long[nis.size()];
			long max = Long.MIN_VALUE;
			for (int i = 0; i < nis.size(); i++) {
				NetworkInterface ni = nis.get(i);
				speeds[i] = SystemFunctions.getNetworkInterfaceSpeed(ni);
				if (max < speeds[i])
					max = speeds[i];
			}
			for (int i = 0; i < nis.size(); i++)
				if (speeds[i] == max)
					selected_nis.add(nis.get(i));
			for (NetworkInterface ni : selected_nis) {
				for (Enumeration<InetAddress> ias = ni.getInetAddresses(); ias.hasMoreElements();) {
					InetAddress ia = ias.nextElement();
					if (isConcernedBy(ia)) {
						res.add(new BindInetSocketAddressMessage(Type.BIND,
								new InetSocketAddress(ia, bind.getInetSocketAddress().getPort()), max));
					}
				}
			}
		} else {
			for (NetworkInterface ni : nis) {
				long speed = SystemFunctions.getNetworkInterfaceSpeed(ni);
				for (Enumeration<InetAddress> ias = ni.getInetAddresses(); ias.hasMoreElements();) {
					InetAddress ia = ias.nextElement();
					if (isConcernedBy(ia) && ia.equals(bind.getInetSocketAddress().getAddress()))
						res.add(new BindInetSocketAddressMessage(Type.BIND,
								new InetSocketAddress(ia, bind.getInetSocketAddress().getPort()), speed));
				}
			}
		}
		return res;
	}

	public class PossibleAddressForDirectConnnection {
		private final AbstractIP IP;

		protected PossibleAddressForDirectConnnection(AbstractIP isa) {
			if (isa == null)
				throw new NullPointerException("isa");
			IP = isa;
		}

		public boolean isConcernedBy(InetAddress local_address) {
			return LocalNetworkAgent.this.isConcernedBy(local_address);
		}

		public AbstractIP getIP() {
			return IP;
		}

		@Override
		public int hashCode() {
			return IP.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (o == null)
				return false;
			if (o.getClass() == PossibleAddressForDirectConnnection.class)
				return ((PossibleAddressForDirectConnnection) o).IP.equals(IP);

			return false;
		}
	}

	class PossibleInetAddressesUsedForDirectConnectionChanged extends Message {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3247059035995491429L;

		protected PossibleInetAddressesUsedForDirectConnectionChanged(InetSocketAddress oldIP,
				InetSocketAddress newIP) {
			if (oldIP != null)
				getMadkitConfig().networkProperties.removePossibleAddressForDirectConnection(
						new PossibleAddressForDirectConnnection(new DoubleIP(oldIP)));
			if (newIP != null)
				getMadkitConfig().networkProperties.addPossibleAddressForDirectConnection(
						new PossibleAddressForDirectConnnection(new DoubleIP(newIP)));
		}

		@Override
		public PossibleInetAddressesUsedForDirectConnectionChanged clone() {
			return this;
		}

		boolean isConcernedBy(InetAddress local_address) {
			return LocalNetworkAgent.this.isConcernedBy(local_address);
		}

	}

	class Router {
		final InetAddress address;
		private Status status = null;
		private boolean statusChanged = false;
		private InetAddress external_ip = null;
		private boolean externalIPChanged = false;
		private int externalPort = -1;

		Router(InetAddress _address) {
			address = _address;
		}

		InetAddress getAddress() {
			return address;
		}

		protected void changeStatus(StatusInfo status_info) {
			if (status == null || !status.equals(status_info.getStatus())) {
				InetSocketAddress old_isa = getInetSocketAddressForDirectConnection();

				status = status_info.getStatus();
				statusChanged = true;
				InetSocketAddress new_isa = getInetSocketAddressForDirectConnection();
				if (old_isa != new_isa) {
					broadcastMessageWithRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.SOCKET_AGENT_ROLE,
							new PossibleInetAddressesUsedForDirectConnectionChanged(old_isa, new_isa),
							LocalCommunity.Roles.LOCAL_NETWORK_ROLE);
				}
			}
		}

		InetSocketAddress getInetSocketAddressForDirectConnection() {
			if (external_ip != null && status.equals(Status.Connected)) {
				if (external_ip instanceof Inet6Address)
					return new InetSocketAddress(external_ip,
							getMadkitConfig().networkProperties.portsToBindForAutomaticLocalConnections);
				else if (external_ip instanceof Inet6Address && getExternalPort() >= 0)
					return new InetSocketAddress(external_ip, getExternalPort());
				else
					return null;
			}
			return null;
		}

		protected void changeExternalIp(InetAddress address) {
			if (external_ip == null || address == null || !external_ip.equals(address)) {
				if ((external_ip == null && address != null) || (external_ip != null && address == null))
					externalIPChanged = true;
				InetSocketAddress old_isa = getInetSocketAddressForDirectConnection();
				external_ip = address;
				if (externalIPChanged) {
					InetSocketAddress new_isa = getInetSocketAddressForDirectConnection();
					if (old_isa != new_isa) {
						broadcastMessageWithRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.SOCKET_AGENT_ROLE,
								new PossibleInetAddressesUsedForDirectConnectionChanged(old_isa, new_isa),
								LocalCommunity.Roles.LOCAL_NETWORK_ROLE);
					}
				}
			}
		}

		protected boolean canInferGlobalMadkitRoutePolicy() {
			boolean r = statusChanged || externalIPChanged;
			statusChanged = false;
			externalIPChanged = false;

			return r;
		}

		InetAddress getExternalIP() {
			return external_ip;
		}

		Status getStatus() {
			return status;
		}

		protected void setExternalPort(int port) {
			InetSocketAddress old_isa = getInetSocketAddressForDirectConnection();
			externalPort = port;
			InetSocketAddress new_isa = getInetSocketAddressForDirectConnection();
			if (old_isa != new_isa) {
				broadcastMessageWithRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.SOCKET_AGENT_ROLE,
						new PossibleInetAddressesUsedForDirectConnectionChanged(old_isa, new_isa),
						LocalCommunity.Roles.LOCAL_NETWORK_ROLE);
			}
		}

		boolean hasExternalPort() {
			return externalPort > 0;
		}

		int getExternalPort() {
			return externalPort;
		}
	}

	static abstract class NetworkInterfaceChangedMessage extends Message {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4064954141729288644L;

		private final NetworkInterface network_interface_changed;

		NetworkInterfaceChangedMessage(NetworkInterface network_interface_changed) {
			this.network_interface_changed = network_interface_changed;
		}

		NetworkInterface getNetworkInterface() {
			return network_interface_changed;
		}
	}

	static class NetworkInterfaceAddedMessage extends NetworkInterfaceChangedMessage {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3064424111658577003L;

		NetworkInterfaceAddedMessage(NetworkInterface network_interface_changed) {
			super(network_interface_changed);
		}

	}

	static class NetworkInterfaceRemovedMessage extends NetworkInterfaceChangedMessage {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1740120758614753232L;

		NetworkInterfaceRemovedMessage(NetworkInterface network_interface_changed) {
			super(network_interface_changed);
		}

	}

}
