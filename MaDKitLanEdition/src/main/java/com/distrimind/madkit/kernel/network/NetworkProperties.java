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

import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.distrimind.madkit.exceptions.NIOException;
import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.AgentLogger;
import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.madkit.kernel.MadkitProperties;
import com.distrimind.madkit.kernel.NetworkAgent;
import com.distrimind.madkit.kernel.network.LocalNetworkAgent.PossibleAddressForDirectConnnection;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocol;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocolProperties;
import com.distrimind.madkit.kernel.network.connection.access.AccessData;
import com.distrimind.madkit.kernel.network.connection.access.AbstractAccessProtocolProperties;
import com.distrimind.madkit.util.XMLObjectParser;
import com.distrimind.madkit.util.XMLUtilities;
import com.distrimind.ood.database.DatabaseWrapper;
import com.distrimind.util.properties.XMLProperties;

/**
 * MaDKit network options which are valued with a long, an int, or a short
 * representing parameters. These options could be used from the command line or
 * using the main method of MaDKit.
 * 
 * @author Jason Mahdjoub
 * @since MaDKitLanEdition 1.0
 * @version 1.1
 * 
 */
public class NetworkProperties extends XMLProperties {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4437074500457055696L;

	public NetworkProperties() {
		super(new XMLObjectParser());
		globalStatBandwith = new StatsBandwidth();

		globalStatBandwith.putBytesDownloadedInRealTime(DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_30_SECONDS_SEGMENTS,
				globalRealTimeTransferStatPer30SecondsForDownload);
		globalStatBandwith.putBytesDownloadedInRealTime(DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_5_MINUTES_SEGMENTS,
				globalRealTimeTransferStatPer5MinutesForDownload);
		globalStatBandwith.putBytesUploadedInRealTime(DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_30_SECONDS_SEGMENTS,
				globalRealTimeTransferStatPer30SecondsForUpload);
		globalStatBandwith.putBytesUploadedInRealTime(DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_5_MINUTES_SEGMENTS,
				globalRealTimeTransferStatPer5MinutesForUpload);
		globalStatBandwith.putBytesUploadedInRealBytes(DEFAULT_STAT_PER_512KB_SEGMENTS,
				globalTransferSpeedStatPer512SegmentsForUpload);
		globalStatBandwith.putBytesDownloadedInRealBytes(DEFAULT_STAT_PER_512KB_SEGMENTS,
				globalTransferSpeedStatPer512SegmentsForDownload);
		try {
			whiteInetAddressesList = new ArrayList<>();
			whiteInetAddressesList.add(InetAddress.getByName("0.0.0.0"));
			whiteInetAddressesList.add(InetAddress.getByName("127.0.0.1"));
			whiteInetAddressesList.add(InetAddress.getByName("::1"));
			whiteInetAddressesList.add(InetAddress.getByName("localhost"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Class<? extends AbstractAgent> networkAgent = NetworkAgent.class;

	/**
	 * Option defining the default warning log level for newly launched agents.
	 * Default value is "FINE". This value could be changed individually by the
	 * agents using {@link AgentLogger#setWarningLogLevel(Level)} on their personal
	 * logger.
	 * <p>
	 * Example:
	 * <ul>
	 * <li>--warningLogLevel OFF</li>
	 * <li>--warningLogLevel ALL</li>
	 * <li>--warningLogLevel FINE</li>
	 * </ul>
	 * 
	 * @see AbstractAgent#logger
	 * @see java.util.logging.Logger
	 * @see AbstractAgent#getMadkitConfig()
	 * @since MaDKit 5
	 */
	public Level networkLogLevel = Level.INFO;

	/**
	 * Starts the network on startup. Default value is "false".
	 */
	public boolean network = false;

	/**
	 * The maximum buffer size used to make packets. Notice that packet size are
	 * variable. This value cannot be greater than {@link Block#BLOCK_SIZE_LIMIT}.
	 */
	public int maxBufferSize = Short.MAX_VALUE;
	

	/**
	 * The maximum number of random bits introduced into each packet in order to
	 * increase security. This value cannot be greater than
	 * <code>maxBufferSize/2</code>.
	 */
	public short maxRandomPacketValues = 0;

	/**
	 * The maximum data size (in bytes) for a message which is not sent with big
	 * data transfer functionality.
	 */
	public int maxShortDataSize = 20971520;

	/**
	 * The maximum unread data size (in bytes) for messages considered as short data
	 * (not big data). This threshold concerns only one distant kernel.
	 */
	public long maxSizeForUnreadShortDataFromOneDistantKernel = maxShortDataSize * 2;

	/**
	 * The maximum global unread data size (in bytes) for messages considered as
	 * short data (not big data). This threshold concerns all connected distants
	 * kernels.
	 */
	public long maxSizeForUnreadShortDataFromAllConnections = 419430400l;

	/**
	 * A file transfer is computed in bytes per second according the average of data
	 * transfered during the last specified milliseconds (this variable).
	 */
	public long bigDataStatDurationMean = 1000l;

	/**
	 * When received data are incorrect or when an anomaly has been detected through
	 * the lan, and if the problem does not correspond to a security problem, the
	 * system can temporary expel the distant host temporary. This variable
	 * correspond to the duration in milliseconds of this expulsion.
	 */
	public long expulsionDuration = 600000l;

	/**
	 * When received data are incorrect or when an anomaly has been detected through
	 * the lan, and if the problem does not correspond to a security problem, the
	 * system can temporary expel the distant host temporary. This variable
	 * corresponds to the number of detected anomalies accepted before triggering an
	 * expulsion.
	 */
	public short nbMaxAnomaliesBeforeTrigeringExpulsion = 7;

	/**
	 * When received data are incorrect or when an anomaly has been detected through
	 * the lan, and if the problem does not correspond to a security problem, the
	 * system can temporary expel the distant host temporary.
	 * 
	 * This variable corresponds to the maximum number of expulsion accepted by the
	 * system. If this maximum is reached, then the system consider next expulsion
	 * as banishment related to security problems.
	 *
	 */
	public short nbMaxExpulsions = 20;

	/**
	 * 
	 * 
	 * When a problem of security is detected, the system decide to ban temporary
	 * the distant concerned host. This variable correspond to the duration of this
	 * banishment.
	 */
	public long banishmentDuration = 43200000l;

	/**
	 * When a problem of security is detected, the system decide to ban temporary
	 * the distant concerned host. This variable corresponds to the number of
	 * detected anomalies accepted before triggering a banishment.
	 */
	public short nbMaxAnomaliesBeforeTrigeringBanishment = 1;

	/**
	 * When a problem of security is detected, the system decide to ban temporary
	 * the distant concerned host. But when too much banishment have been operated,
	 * the system ban definitively the concerned host. This variable corresponds to
	 * the maximum number of banishment before triggering the definitive ban.
	 */
	public short nbMaxBanishments = 100;

	/**
	 * Duration of the statistics concerning the temporary expulsion of a computer.
	 * After this duration, the statistics are reseted.
	 */
	public long expulsionStatisticDuration = 7200000l;

	/**
	 * Duration of the statistics concerning the banishment of a computer. After
	 * this duration, the statistics are reseted.
	 */
	public long banishmentStatisticDuration = 1728000000l;

	/**
	 * If set to true, this connection can enable to make a gateway between two
	 * computers. If the two computers are able to connect themselves between them,
	 * the current connection is just useful to inform the two computers that they
	 * can connect between them. Else the current connection will be used to be a
	 * gateway between the two computers. Every data between the two computers will
	 * be transfered into the current computer/connection. However, if data is
	 * encrypted, it cannot be comprehensible for the current computer. So the data
	 * security is maintained.
	 * 
	 * @see #gatewayDepth
	 * @see #timeBetweenEachUpdateOfTransfertSpeedForGatewayConnection
	 */
	public boolean gatewayConnection = false;

	/**
	 * Define the number of gateway that are enabled between two computers. This
	 * value has no effect if {@link #gatewayConnection} is set to
	 * <code>false</code>
	 * 
	 * @see #gatewayConnection
	 */
	public int gatewayDepth = 1;

	/**
	 * Define the duration between each update of the transfer speed between the two
	 * connected computers connected through the current computer. The transfer
	 * speed is automatically computed.
	 * 
	 * This value has no effect if {@link #gatewayConnection} is set to
	 * <code>false</code>
	 * 
	 * @see #gatewayConnection
	 */
	public long timeBetweenEachUpdateOfTransfertSpeedForGatewayConnection = 30000l;

	/**
	 * Define the number of cached blocks in bytes to transfer to another machine.
	 * If this number is reached, the current socket is blocked (so no information
	 * can be received and sent), until the cached blocks are sent to the other
	 * machine.
	 */
	public long numberOfCachedBytesToTransferBeforeBlockingSocket = this.maxShortDataSize * 2;

	/**
	 * Tells, in the case of several identical kernel addresses which can come only
	 * under a security intrusion, that the connection using this property to
	 * 'PRIORITY' will consider the distant kernel address as priority with another,
	 * and the connection using this property to 'PRIORITY_IF_CERTIFIED_CONNECTION',
	 * do the same if the current connection is certified through a certificate. If
	 * two connections allows a priority with the same kernel address, this variable
	 * will not have any effect. So, in this case, the system should interface the
	 * two kernel addresses with two local different addresses. Intrusion is then
	 * not possible, whatever the value of this variable, but if the kernel address
	 * is interfaced, it will not be the same into the entire network.
	 */
	public KernelAddressPriority distantKernelAddressPriority = KernelAddressPriority.priorityIfCertifiedConnection;

	/**
	 * Define the maximum number of connections between two same kernels. Indeed,
	 * between two machines, it is possible to have several network interfaces, so
	 * severals connections. This limitation enables to limit the effect of a DoS
	 * attack.
	 */
	public short numberOfMaximumConnectionsBetweenTwoSameKernelsAndMachines = 3;// TODO check if rejected peer needs to
																				// not try a reconnection

	/**
	 * Define the maximum number of connections from one IPV4. This limitation
	 * enables to limit the effect of a DoS attack.
	 */
	public int numberOfMaximumConnectionsFromOneIPV4 = 256;

	/**
	 * Define the maximum number of connections from one IPV6. This limitation
	 * enables to limit the effect of a DoS attack.
	 */
	public int numberOfMaximumConnectionsFromOneIPV6 = 8;

	/**
	 * Gives the duration after what the system will consider as obsolete a transfer
	 * connection between two peers with the current peer as intermediate peer.
	 */
	public long durationBeforeCancelingTransferConnection = 120000l;

	/**
	 * Distant Ip addresses are divided into IP range. Each IP range is associated
	 * to a specific public and private Key used for cryptography. This variable
	 * defines the maximum number of IP ranges, than the maximum number of key pairs
	 * to memories into the database.
	 */
	public short maximumNumberOfCryptoKeysForIpsSpectrum = (short) 1000;

	/**
	 * Delay after a channel selector returns 0 network event and gives hand to the
	 * system for eventual data refresh
	 */
	public long selectorTimeOut = 30000l;

	/**
	 * Delay after a channel selector returns 0 network event and gives hand to the
	 * system for eventual data refresh
	 */
	public long selectorTimeOutWhenWaitingPendingConnections = 300l;

	/**
	 * Delay after a connection is considered obsolete if no data was transfered.
	 */
	public long connectionTimeOut = 30000l;

	/**
	 * Tells if the UPNP IGD protocol is activated.
	 * 
	 * This protocol enables to open port into the local router, in order to have
	 * access to internet.
	 * 
	 * @see #timeBetweenEachUpdateOfTransfertSpeedForGatewayConnection
	 */
	public boolean upnpIGDEnabled = false;
	
	/**
	 * UPNP IGD Stream Port listener
	 */
	public int upnpStreamIDGPort = 0;

	/**
	 * UPNP IGD Stream Port listener
	 */
	public int upnpMulticastIDGPort = 1901;
	
	/**
	 * Log level for UpnpIGD agent
	 */
	public Level UpnpIGDLogLevel = Level.INFO;

	/**
	 * This value represents the time between each local router scan, this in order
	 * to update data about local router.
	 * 
	 * @see #upnpIGDEnabled
	 */
	public long delayBetweenEachRouterConnectionCheck = 10000l;

	/**
	 * This value represents the time between each scan of external ip of each local
	 * router, this in order to update data about available internet connection.
	 * 
	 * @see #upnpIGDEnabled
	 */
	public long delayBetweenEachExternalIPRouterCheck = 3600000l;

	/**
	 * Tells if the network interfaces availability can be scanned (does not work if
	 * {@link NetworkProperties#upnpIGDEnabled} is set to <code>false</code>.
	 * 
	 * @see #timeBetweenEachUpdateOfTransfertSpeedForGatewayConnection
	 */
	public boolean networkInterfaceScan = true;

	/**
	 * This value represents the time between each local network interface scan,
	 * this in order to update data about available and connected network interface.
	 */
	public long delayBetweenEachNetworkInterfaceScan = 30000l;

	/**
	 * Delay before routing kernel optimize the choice of agents sockets for packets
	 * sending.
	 */
	public long delayBetweenEachAgentSocketOptimization = 10000l;

	/**
	 * Delay before distant kernel address checking becomes obsolete.
	 */
	public long maxDurationOfDistantKernelAddressCheck = 60000l;

	/**
	 * Delay before closing connections using a network interface slowest than
	 * others.
	 */
	public long maxDurationBeforeClosingObsoleteNetworkInterfaces = 60000l;

	/**
	 * for internal use
	 */
	public static final byte connectionProtocolDatabaseUsingCodeForEncryption = 1;

	/**
	 * for internal use
	 */
	public static final byte connectionProtocolDatabaseUsingCodeForSignature = 2;
	/**
	 * for internal use
	 */
	public static final byte accessProtocolDatabaseUsingCode = 3;

	/**
	 * Default IPV4 used to join several peers into the local network, and enable
	 * them to detect themselves.
	 */
	public static final InetAddress defaultIPV4MulticastGroupForPeerDiscovery;

	/**
	 * Default IPV6 used to join several peers into the local network, and enable
	 * them to detect themselves.
	 */
	public static final InetAddress defaultIPV6MulticastGroupForPeerDiscovery;

	static {
		InetAddress ipv4 = null, ipv6 = null;
		try {
			ipv4 = InetAddress.getByName("239.54.08.89");

			ipv6 = InetAddress.getByName("ff05:5d6e:89fa:5fbc:589f:fc57:c459:ab79");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		defaultIPV4MulticastGroupForPeerDiscovery = ipv4;
		defaultIPV6MulticastGroupForPeerDiscovery = ipv6;

	}

	/**
	 * IPV4 used to join several peers into the local network, and enable them to
	 * detect themselves.
	 */
	public InetAddress IPV4MulticastGroupForPeerDiscovery = defaultIPV4MulticastGroupForPeerDiscovery;

	/**
	 * IPV6 used to join several peers into the local network, and enable them to
	 * detect themselves.
	 */
	public InetAddress IPV6MulticastGroupForPeerDiscovery = defaultIPV6MulticastGroupForPeerDiscovery;

	/**
	 * Port in which a message is diffused through the network to inform other
	 * computers, that this one is ready to receive connections.
	 */
	public int portForMulticastDiffusionMessage = 5000;

	/**
	 * If set to true, the system will be connected with all local peers
	 * automatically.
	 */
	public boolean autoConnectWithLocalSitePeers = true;

	/**
	 * Ports and ips to bind, in order to listen to client connections.
	 */
	public ArrayList<Integer> externalRouterPortsToMap = null;

	/**
	 * Concerns local inet interface addresses to bind. If set to null, all local
	 * interfaces will be binded.
	 */
	public ArrayList<InetAddress> localInetAddressesToBind = null;

	/**
	 * local port to bind
	 */
	public int portsToBindForAutomaticLocalConnections = 5001;

	/**
	 * manual port to bind
	 */
	public int portsToBindForManualDirectConnections = -1;

	private List<PossibleAddressForDirectConnnection> addressesToConnect = Collections
			.synchronizedList(new ArrayList<PossibleAddressForDirectConnnection>());

	void addPossibleAddressForDirectConnection(PossibleAddressForDirectConnnection isa) {
		if (isa == null)
			throw new NullPointerException("isa");
		addressesToConnect.add(isa);
	}

	void removePossibleAddressForDirectConnection(PossibleAddressForDirectConnnection isa) {
		addressesToConnect.remove(isa);
	}

	public List<PossibleAddressForDirectConnnection> getPossibleAddressesForDirectConnection() {
		return addressesToConnect;
	}

	public List<AbstractIP> connectionToAttempt = null;

	/**
	 * Represents properties of each used connection protocol and each sub network
	 * 
	 * @see InetAddressFilter
	 * @see InetAddressFilters
	 * @see ConnectionProtocolProperties
	 */
	private final ArrayList<ConnectionProtocolProperties<?>> connectionProtocolProperties = new ArrayList<>();

	public final <CP extends ConnectionProtocol<CP>> void addConnectionProtocol(
			ConnectionProtocolProperties<CP> _properties) {
		connectionProtocolProperties.add(_properties);
	}

	/**
	 * 
	 * @return the connection protocols list.
	 */
	public ArrayList<ConnectionProtocolProperties<?>> getConnectionProtocolList() {
		return connectionProtocolProperties;
	}

	/**
	 * Returns a connection protocol chain according the distant peer ip, and the
	 * local used port.
	 * 
	 * @param _distant_inet_address
	 *            the distant peer ip
	 * @param _local_interface_address
	 *            the local interface address
	 * @param sql_connection
	 * 			the sql database wrapper
	 * @param mkProperties the madkit properties
	 * @param isServer
	 * 			true if this peer can receive connection ask from other peer
	 * @param needBiDirectionnalConnectionInitiationAbility
	 * 			true if the two concerned peers can be interpreted as servers
	 * @return a connection protocol chain according the distant peer ip, and the
	 *         local used port. Returns null if no connection protocol was found.
	 * @throws NIOException
	 *             if a problem occurs
	 */
	public ConnectionProtocol<?> getConnectionProtocolInstance(InetSocketAddress _distant_inet_address,
			InetSocketAddress _local_interface_address, DatabaseWrapper sql_connection, MadkitProperties mkProperties, boolean isServer,
			boolean needBiDirectionnalConnectionInitiationAbility) throws NIOException {
		for (ConnectionProtocolProperties<?> cpp : getConnectionProtocolList()) {
			if (cpp.isConcernedBy(_local_interface_address.getAddress(), _local_interface_address.getPort(),
					_distant_inet_address.getAddress(), isServer, needBiDirectionnalConnectionInitiationAbility)) {
				return cpp.getConnectionProtocolInstance(_distant_inet_address, _local_interface_address,
						sql_connection, mkProperties, this, isServer, needBiDirectionnalConnectionInitiationAbility);
			}
		}
		return null;
	}

	/**
	 * Returns a connection protocol properties according the distant peer ip, and
	 * the local used port.
	 * 
	 * @param _distant_inet_address
	 *            the distant peer ip
	 * @param _local_interface_address
	 *            the local interface address
	 * @param isServer
	 * 			true if this peer can receive connection ask from other peer
	 * @param mustSupportBidirectionnalConnectionInitiative
	 * 			true if the two concerned peers can be interpreted as servers
	 * @return a connection protocol chain according the distant peer ip, and the
	 *         local used port. Returns null if no connection protocol was found.
	 * @throws NIOException
	 *             if a problem occurs
	 */
	public ConnectionProtocolProperties<?> getConnectionProtocolProperties(InetSocketAddress _distant_inet_address,
			InetSocketAddress _local_interface_address, boolean isServer,
			boolean mustSupportBidirectionnalConnectionInitiative) throws NIOException {
		return this.getConnectionProtocolProperties(_distant_inet_address, _local_interface_address, 0, isServer,
				mustSupportBidirectionnalConnectionInitiative);
	}

	/**
	 * Returns a connection protocol properties according the distant peer ip, and
	 * the local used port.
	 * 
	 * @param _distant_inet_address
	 *            the distant peer ip
	 * @param _local_interface_address
	 *            the local interface address
	 * @param subProtocolLevel
	 * 			the sub protocol properties level (the root protocol start with 0)
	 * @param isServer
	 * 			true if this peer can receive connection ask from other peer
	 * @param mustSupportBidirectionnalConnectionInitiative
	 * 			true if the two concerned peers can be interpreted as servers
	 * @return a connection protocol properties according the distant peer ip, and
	 *         the local used port. Returns null if no connection protocol was
	 *         found.
	 * @throws NIOException
	 *             if a problem occurs
	 */
	public ConnectionProtocolProperties<?> getConnectionProtocolProperties(InetSocketAddress _distant_inet_address,
			InetSocketAddress _local_interface_address, int subProtocolLevel, boolean isServer,
			boolean mustSupportBidirectionnalConnectionInitiative) throws NIOException {
		for (ConnectionProtocolProperties<?> cpp : getConnectionProtocolList()) {
			int l = subProtocolLevel;
			while (l > 0 && cpp.subProtocolProperties != null) {
				cpp = cpp.subProtocolProperties;
				--l;
			}

			if (l == 0 && cpp.isConcernedBy(_local_interface_address.getAddress(), _local_interface_address.getPort(),
					_distant_inet_address.getAddress(), isServer, mustSupportBidirectionnalConnectionInitiative)) {
				return cpp;
			}
		}
		return null;
	}

	/**
	 * Returns true if the local interface address can be associated with a
	 * connection protocol that needs a server socket.
	 * 
	 * @param _local_interface_address
	 *            the local interface address
	 * @return true if the local interface address can be associated with a
	 *         connection protocol that needs a server socket.
	 */
	public boolean needsServerSocket(InetSocketAddress _local_interface_address) {
		for (ConnectionProtocolProperties<?> cpp : getConnectionProtocolList()) {
			if (cpp.needsServerSocket(_local_interface_address.getAddress(), _local_interface_address.getPort()))
				return true;
		}
		return false;
	}

	/**
	 * 
	 * @return true if one or more connections protocols exists. Default connection
	 *         protocol is not counted.
	 */
	public final boolean hasOneOrMoreConnectionsProtocols() {
		return connectionProtocolProperties.size() > 0;
	}

	private final ArrayList<AccessData> accessDataList = new ArrayList<AccessData>();

	/**
	 * Add an AccessData protocol according its InetAddress filters
	 * 
	 * @param accessData
	 *            the access data
	 * @see AccessData
	 */
	public void addAccessData(AccessData accessData) {
		this.accessDataList.add(accessData);
	}

	/**
	 * 
	 * @return the access data list
	 */
	public ArrayList<AccessData> getAccessDataList() {

		return accessDataList;
	}

	/**
	 * Gets an access data according the given parameters
	 * 
	 * @param _distant_inet_address
	 *            the distant inet address used
	 * @param _local_interface_address
	 *            the local inet address used
	 * @return the corresponding access data, or null if no one was found
	 * @see AccessData
	 */
	public AccessData getAccessData(InetSocketAddress _distant_inet_address,
			InetSocketAddress _local_interface_address) {
		for (AccessData ad : accessDataList) {
			if (ad.isConcernedBy(_distant_inet_address.getAddress(), _local_interface_address.getPort()))
				return ad;
		}
		return null;
	}

	protected ArrayList<AbstractAccessProtocolProperties> accessProtocolProperties = new ArrayList<>();

	/**
	 * Add an AccessProtocolProperties according its InetAddress filters
	 * 
	 * @param _access_protocol_properties
	 *            the access protocol properties
	 * @see AbstractAccessProtocolProperties
	 */
	public void addAccessProtocolProperties(AbstractAccessProtocolProperties _access_protocol_properties) {
		accessProtocolProperties.add(_access_protocol_properties);
	}

	/**
	 * 
	 * @return the access protocol properties list
	 */
	public ArrayList<AbstractAccessProtocolProperties> getAccessProtocolProperties() {
		return accessProtocolProperties;
	}

	/**
	 * Gets an {@link AbstractAccessProtocolProperties} instance according the given
	 * parameters
	 * 
	 * @param _distant_inet_address
	 *            the distant inet address used
	 * @param _local_interface_address
	 *            the local inet address used
	 * @return the corresponding {@link AbstractAccessProtocolProperties}, or null if no one
	 *         was found
	 * @see AbstractAccessProtocolProperties
	 */
	public AbstractAccessProtocolProperties getAccessProtocolProperties(InetSocketAddress _distant_inet_address,
			InetSocketAddress _local_interface_address) {
		for (AbstractAccessProtocolProperties ad : accessProtocolProperties) {
			if (ad.isConcernedBy(_distant_inet_address.getAddress(), _local_interface_address.getPort()))
				return ad;
		}
		return null;
	}

	/**
	 * Returns true if a connection is possible with the given parameters and the
	 * current properties values
	 * 
	 * @param _distant_inet_address
	 *            the distant inet address
	 * @param isServer true if the current host is a server 
	 * @param _local_interface_address
	 *            the local inet address
	 * @param takeConnectionInitiative
	 *            tells if the current peer will take connection initiative
	 * @param mustSupportBidirectionnalConnectionInitiative
	 *            tells if the connection support bi-directional connection
	 *            initiative
	 * @return true if a connection is possible with the given parameters and the
	 *         current properties values
	 * @see #addAccessData(AccessData)
	 * @see #addConnectionProtocol(ConnectionProtocolProperties)
	 * @see #addAccessProtocolProperties(AbstractAccessProtocolProperties)
	 */
	public boolean isConnectionPossible(InetSocketAddress _distant_inet_address,
			InetSocketAddress _local_interface_address, boolean takeConnectionInitiative, boolean isServer,
			boolean mustSupportBidirectionnalConnectionInitiative) {
		boolean found = false;
		for (AccessData ad : this.accessDataList) {
			if (ad.isConcernedBy(_distant_inet_address.getAddress(), _local_interface_address.getPort())) {
				found = true;
				break;
			}
		}
		if (!found)
			return false;
		found = false;
		for (AbstractAccessProtocolProperties app : accessProtocolProperties) {
			if (app.isConcernedBy(_distant_inet_address.getAddress(), _local_interface_address.getPort())) {
				found = true;
				break;
			}
		}
		if (!found)
			return false;
		found = false;
		for (ConnectionProtocolProperties<?> cpp : getConnectionProtocolList()) {
			if (cpp.isConcernedBy(_local_interface_address.getAddress(), _local_interface_address.getPort(),
					_distant_inet_address.getAddress(), isServer, mustSupportBidirectionnalConnectionInitiative)) {
				if (((takeConnectionInitiative && cpp.canTakeConnectionInitiative()) || !takeConnectionInitiative))
					return true;
				else
					return false;
			}
		}
		return false;
	}

	@Override
	public Node getRootNode(Document _document) {
		for (int i = 0; i < _document.getChildNodes().getLength(); i++) {
			Node n = _document.getChildNodes().item(i);
			if (n.getNodeName().equals(XMLUtilities.MDK))
				return n;
		}
		return null;
	}

	@Override
	public Node createOrGetRootNode(Document _document) {
		Node res = getRootNode(_document);
		if (res == null) {
			res = _document.createElement(XMLUtilities.MDK);
			_document.appendChild(res);
		}
		return res;
	}

	private final Map<ConnectionIdentifier, StatsBandwidth> transfer_stat_per_network_interface = new HashMap<>();
	private final Map<Integer, StatsBandwidth> transfer_stat_per_id_transfer = new HashMap<>();
	private final Map<KernelAddress, StatsBandwidth> transfer_stat_per_kernel_address = new HashMap<>();

	/**
	 * Tag/key corresponding to LAN statistics per segments of 512Kb
	 * 
	 * @see StatsBandwidth#getBytesDownloadedInRealBytes(String)
	 * @see StatsBandwidth#getBytesUploadedInRealBytes(String)
	 */
	public static final String DEFAULT_STAT_PER_512KB_SEGMENTS = "~~DEFAULT_UPLOAD_STAT_PER_BYTES_SEGMENTS";

	/**
	 * Tag/key corresponding to real time LAN statistics per segments of 30 seconds
	 * 
	 * @see StatsBandwidth#getBytesDownloadedInRealTime(String)
	 * @see StatsBandwidth#getBytesUploadedInRealTime(String)
	 */
	public static final String DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_30_SECONDS_SEGMENTS = "~~DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_30_SECONDS_SEGMENTS";

	/**
	 * Tag/key corresponding to real time LAN statistics per segments of 5 minutes
	 * 
	 * @see StatsBandwidth#getBytesDownloadedInRealTime(String)
	 * @see StatsBandwidth#getBytesUploadedInRealTime(String)
	 */
	public static final String DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_5_MINUTES_SEGMENTS = "~~DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_5_MINUTES_SEGMENTS";

	private final TransferSpeedStat globalTransferSpeedStatPer512SegmentsForDownload = new TransferSpeedStat(524288l,
			32768l, 300000l);
	private final TransferSpeedStat globalTransferSpeedStatPer512SegmentsForUpload = new TransferSpeedStat(524288l,
			32768l, 300000l);
	private final RealTimeTransfertStat globalRealTimeTransferStatPer30SecondsForDownload = new RealTimeTransfertStat(
			30000l, 3000l);
	private final RealTimeTransfertStat globalRealTimeTransferStatPer30SecondsForUpload = new RealTimeTransfertStat(
			30000l, 3000l);
	private final RealTimeTransfertStat globalRealTimeTransferStatPer5MinutesForDownload = new RealTimeTransfertStat(
			300000l, 3000l);
	private final RealTimeTransfertStat globalRealTimeTransferStatPer5MinutesForUpload = new RealTimeTransfertStat(
			300000l, 3000l);
	private final StatsBandwidth globalStatBandwith;

	private void initializeStatsBandwitdh(StatsBandwidth stats) {
		stats.putBytesDownloadedInRealTime(DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_30_SECONDS_SEGMENTS,
				new RealTimeTransfertStat(30000l, 3000l));
		stats.putBytesDownloadedInRealTime(DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_5_MINUTES_SEGMENTS,
				new RealTimeTransfertStat(300000l, 3000l));
		stats.putBytesUploadedInRealTime(DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_30_SECONDS_SEGMENTS,
				new RealTimeTransfertStat(30000l, 3000l));
		stats.putBytesUploadedInRealTime(DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_5_MINUTES_SEGMENTS,
				new RealTimeTransfertStat(300000l, 3000l));
		stats.putBytesUploadedInRealBytes(DEFAULT_STAT_PER_512KB_SEGMENTS,
				new TransferSpeedStat(524288l, 32768l, 300000l));
		stats.putBytesDownloadedInRealBytes(DEFAULT_STAT_PER_512KB_SEGMENTS,
				new TransferSpeedStat(524288l, 32768l, 300000l));

		String globalStats = "(For global statistics)";
		stats.putBytesDownloadedInRealTime(DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_30_SECONDS_SEGMENTS + globalStats,
				globalRealTimeTransferStatPer30SecondsForDownload);
		stats.putBytesDownloadedInRealTime(DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_5_MINUTES_SEGMENTS + globalStats,
				globalRealTimeTransferStatPer5MinutesForDownload);
		stats.putBytesUploadedInRealTime(DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_30_SECONDS_SEGMENTS + globalStats,
				globalRealTimeTransferStatPer30SecondsForUpload);
		stats.putBytesUploadedInRealTime(DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_5_MINUTES_SEGMENTS + globalStats,
				globalRealTimeTransferStatPer5MinutesForUpload);
		stats.putBytesUploadedInRealBytes(DEFAULT_STAT_PER_512KB_SEGMENTS + globalStats,
				globalTransferSpeedStatPer512SegmentsForUpload);
		stats.putBytesDownloadedInRealBytes(DEFAULT_STAT_PER_512KB_SEGMENTS + globalStats,
				globalTransferSpeedStatPer512SegmentsForDownload);

	}

	/**
	 * Add if necessary and gets LAN statistics related to one connection.
	 * 
	 * @param connectionIdentifier
	 *            the connection identifier
	 * @return the corresponding statistics or null if no statistics were found.
	 */
	StatsBandwidth addIfNecessaryAndGetStatsBandwitdh(ConnectionIdentifier connectionIdentifier) {
		synchronized (transfer_stat_per_network_interface) {
			if (connectionIdentifier == null)
				throw new NullPointerException("connectionIdentifier");

			StatsBandwidth sb = transfer_stat_per_network_interface.get(connectionIdentifier);
			if (sb != null)
				return sb;
			transfer_stat_per_network_interface.put(connectionIdentifier, sb = new StatsBandwidth());
			initializeStatsBandwitdh(sb);

			return sb;
		}
	}

	/**
	 * Add if necessary and gets LAN statistics related to one connection.
	 * 
	 * @param transferIdentifier
	 *            the transfer identifier
	 * @return the corresponding statistics or null if no statistics were found.
	 */
	StatsBandwidth addIfNecessaryAndGetStatsBandwitdh(int transferIdentifier) {
		synchronized (transfer_stat_per_id_transfer) {
			if (transferIdentifier == TransferAgent.NullIDTransfer.getID())
				throw new NullPointerException("transferIdentifier is a NullIDTransfer");
			Integer i = new Integer(transferIdentifier);

			StatsBandwidth sb = transfer_stat_per_id_transfer.get(i);
			if (sb != null)
				return sb;
			transfer_stat_per_id_transfer.put(i, sb = new StatsBandwidth());
			initializeStatsBandwitdh(sb);

			return sb;
		}
	}

	/**
	 * Gets LAN statistics related to one connection
	 * 
	 * @param transferIdentifier
	 *            the transfer identifier
	 * @return the corresponding statistics or null if no statistics were found.
	 */
	public StatsBandwidth getStatsBandwith(int transferIdentifier) {
		synchronized (transfer_stat_per_id_transfer) {
			return transfer_stat_per_id_transfer.get(new Integer(transferIdentifier));
		}
	}

	/**
	 * Remove LAN statistics related to one connection corresponding to one local
	 * inet address and one distant inet address
	 * 
	 * @param transferIdentifier
	 *            the transfer identifier
	 * @return the corresponding statistics or null if no statistics were found.
	 */
	StatsBandwidth removeStatsBandwitdh(int transferIdentifier) {
		synchronized (transfer_stat_per_id_transfer) {
			Integer i = new Integer(transferIdentifier);
			return transfer_stat_per_id_transfer.remove(i);
		}
	}

	/**
	 * Remove LAN statistics related to one connection corresponding to one local
	 * inet address and one distant inet address
	 * 
	 * @param connectionIdentifier
	 *            the connection identifier
	 * @return the corresponding statistics or null if no statistics were found.
	 */
	StatsBandwidth removeStatsBandwitdh(ConnectionIdentifier connectionIdentifier) {
		synchronized (transfer_stat_per_network_interface) {
			if (connectionIdentifier == null)
				throw new NullPointerException("connectionIdentifier");

			return transfer_stat_per_network_interface.remove(connectionIdentifier);
		}
	}

	/**
	 * Add if necessary and gets LAN statistics corresponding to one distant Madkit
	 * kernel
	 * 
	 * @param kernel_address
	 *            the Madkit kernel
	 * @return the corresponding statistics or null if no statistics were found.
	 */
	StatsBandwidth addIfNecessaryAndGetStatsBandwitdh(KernelAddress kernel_address) {
		synchronized (transfer_stat_per_kernel_address) {
			if (kernel_address == null)
				throw new NullPointerException("kernel_address");

			StatsBandwidth sb = transfer_stat_per_kernel_address.get(kernel_address);
			if (sb != null)
				return sb;
			transfer_stat_per_kernel_address.put(kernel_address, sb = new StatsBandwidth());
			initializeStatsBandwitdh(sb);

			return sb;
		}
	}

	/**
	 * Remove LAN statistics corresponding to one distant Madkit kernel
	 * 
	 * @param kernel_address
	 *            the Madkit kernel
	 * @return the corresponding statistics or null if no statistics were found.
	 */
	StatsBandwidth removeStatsBandwitdh(KernelAddress kernel_address) {
		synchronized (transfer_stat_per_kernel_address) {
			if (kernel_address == null)
				throw new NullPointerException("kernel_address");

			return transfer_stat_per_kernel_address.remove(kernel_address);
		}
	}

	/**
	 * Gets LAN statistics related to one connection
	 * 
	 * @param connectionIdentifier
	 *            the connection identifier
	 * @return the corresponding statistics or null if no statistics were found.
	 */
	public StatsBandwidth getStatsBandwith(ConnectionIdentifier connectionIdentifier) {
		synchronized (transfer_stat_per_network_interface) {
			return transfer_stat_per_network_interface.get(connectionIdentifier);
		}
	}

	/**
	 * Gets LAN statistics corresponding to one distant Madkit kernel
	 * 
	 * @param kernel_address
	 *            the Madkit kernel
	 * @return the corresponding statistics or null if no statistics were found.
	 */
	public StatsBandwidth getStatsBandwith(KernelAddress kernel_address) {
		synchronized (transfer_stat_per_kernel_address) {
			return transfer_stat_per_kernel_address.get(kernel_address);
		}
	}

	/**
	 * Gets global LAN statistics corresponding to all established connections
	 * 
	 * @return the corresponding global statistics
	 */
	public StatsBandwidth getGlobalStatsBandwith() {
		return globalStatBandwith;
	}

	private TransferFilter transferTriggers = null;

	/**
	 * Set a transfer trigger used to filter indirect connections
	 * 
	 * @param transferTriggers
	 *            the transfer filter
	 * @see TransferFilter
	 */
	public void setTransferTriggers(TransferFilter transferTriggers) {
		this.transferTriggers = transferTriggers;
	}

	/**
	 * Gets the transfer trigger used to filter indirect connections
	 * 
	 * @return the transfer filter (null by default)
	 * @see TransferFilter
	 */
	public TransferFilter getTransferTriggers() {
		return this.transferTriggers;
	}

	private List<InetAddress> whiteInetAddressesList;

	/**
	 * Gets addresses that cannot be blacklisted.
	 * 
	 * @return addresses that cannot be blacklisted.
	 */
	public List<InetAddress> getWhiteInetAddressesList() {
		return whiteInetAddressesList;
	}

	/**
	 * Add an address that cannot be blacklisted
	 * 
	 * @param ia
	 *            the address
	 */
	public void addWhiteInetAddress(InetAddress ia) {
		if (ia == null)
			throw new NullPointerException("ia");
		whiteInetAddressesList.add(ia);
	}

	/**
	 * Add addresses that cannot be blacklisted
	 * 
	 * @param ias
	 *            the addresses
	 */
	public void addWhiteInetAddresses(Collection<InetAddress> ias) {
		if (ias == null)
			throw new NullPointerException("ias");
		for (InetAddress ia : ias)
			addWhiteInetAddress(ia);
	}

	/**
	 * Remove address that cannot be blacklisted
	 * 
	 * @param ia
	 *            the address
	 */
	public void removeWhiteInetAddress(InetAddress ia) {
		whiteInetAddressesList.remove(ia);
	}

	/**
	 * Remove addresses that cannot be blacklisted
	 * 
	 * @param ias
	 *            the addresses
	 */
	public void removeWhiteInetAddresses(Collection<InetAddress> ias) {
		whiteInetAddressesList.removeAll(ias);
	}

	/**
	 * Clean addresses that cannot be blacklisted
	 * 
	 */
	public void cleanWhiteInetAddresses() {
		whiteInetAddressesList.clear();
	}
	
	/**
	 * True if the data transfered through several peers can be checked (if necessary) by intermediate peers with their own security process. 
	 * End to end message checking is also included into this case, regarding the used connection protocols between the end peers. 
	 * False if only end to end message check are accepted
	 */
	public boolean canUsePointToPointTransferedBlockChecker=true;
	
	static volatile int GLOBAL_MAX_SHORT_DATA_SIZE=20971520;
	
	private static final HashMap<Class<?>, Boolean> checkedSystemMessageClasses=new HashMap<>();
	
	static boolean checkSystemMessageCompatibility(SystemMessage sm)
	{
		if (sm==null)
			throw new NullPointerException();
		synchronized(checkedSystemMessageClasses)
		{
			Boolean valid=checkedSystemMessageClasses.get(sm.getClass());
			if (valid==null)
			{
				try
				{
					Method m=sm.getClass().getDeclaredMethod("readObject", ObjectInputStream.class);
					valid=Boolean.valueOf(m!=null);
				}
				catch(Exception e)
				{
					valid=Boolean.valueOf(false);
				}
				checkedSystemMessageClasses.put(sm.getClass(), valid);
			}
			return valid.booleanValue();
		}			
	}
	
	
}
