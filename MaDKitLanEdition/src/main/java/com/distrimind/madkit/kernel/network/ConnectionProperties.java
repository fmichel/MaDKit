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

import java.net.InetSocketAddress;
import java.util.ArrayList;

import com.distrimind.madkit.exceptions.NIOException;
import com.distrimind.madkit.kernel.MadkitProperties;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocol;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocolProperties;
import com.distrimind.ood.database.DatabaseWrapper;

/**
 * Represents properties of every connection
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class ConnectionProperties {

	/**
	 * The size of the RSA Key used for the login process (different from the
	 * connection process and the eventual global data encryption process)
	 */
	public short RSA_KEY_SIZE_FOR_LOGIN = 2048;
	/**
	 * The expiration of the RSA key used for the login process (different from the
	 * connection process and the eventual global data encryption process)
	 */
	public long RSA_KEY_EXPIRATION_FOR_LOGIN = 5184000000L;

	/**
	 * The maximum buffer size used to make packets. Notice that packet size are
	 * variable. This value cannot be greater than {@link Short#MAX_VALUE}.
	 */
	public short MAX_BUFFER_SIZE = Short.MAX_VALUE;

	/**
	 * The maximum data size (in bytes) for a message which is not sent with big
	 * data transfer functionality.
	 */
	public long MAX_SHORT_DATA_SIZE = 20971520L;

	/**
	 * When received data are incorrect or when an anomaly has been detected through
	 * the lan, and if the problem does not correspond to a security problem, the
	 * system can temporary expel the distant host temporary. This variable
	 * correspond to the duration in milliseconds of this expulsion.
	 */
	public long EXPULSION_DURATION = 600000;

	/**
	 * When received data are incorrect or when an anomaly has been detected through
	 * the lan, and if the problem does not correspond to a security problem, the
	 * system can temporary expel the distant host temporary. This variable
	 * corresponds to the number of detected anomalies accepted before triggering an
	 * expulsion.
	 */
	public short NB_MAX_ANOMALY_BEFORE_TRIGERING_EXPULSION = 10;

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
	public short NB_MAX_EXPULSIONS = 100;

	/**
	 * 
	 * 
	 * When a problem of security is detected, the system decide to ban temporary
	 * the distant concerned host. This variable correspond to the duration of this
	 * banishment.
	 */
	public long BANISHMENT_DURATION = 43200000;

	/**
	 * When a problem of security is detected, the system decide to ban temporary
	 * the distant concerned host. This variable corresponds to the number of
	 * detected anomalies accepted before triggering a banishment.
	 */
	public short NB_MAX_ANOMALY_BEFORE_TRIGERING_BANISHMENT = 4;

	/**
	 * When a problem of security is detected, the system decide to ban temporary
	 * the distant concerned host. But when too much banishment have been operated,
	 * the system ban definitively the concerned host. This variable corresponds to
	 * the maximum number of banishment before triggering the definitive ban.
	 */
	public short NB_MAX_BANISHMENT = 10;

	/**
	 * Duration of the statistics concerning the temporary expulsion of a computer.
	 * After this duration, the statistics are reseted.
	 */
	public long EXPULSION_STATISTIC_DURATION = 7200000;

	/**
	 * Duration of the statistics concerning the banishment of a computer. After
	 * this duration, the statistics are reseted.
	 */
	public long BANISHMENT_STATISTIC_DURATION = 1728000000;

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
	 * @see #GATEWAY_DEPTH
	 * @see #TIME_BETWEEN_EACH_UPDATE_OF_TRANSFERT_SPEED_FOR_GATEWAY_CONNECTION
	 */
	public boolean ENABLE_GATEWAY_CONNECTION = false;

	/**
	 * Define the number of gateway that are enabled between two computers. This
	 * value has no effect if {@link #ENABLE_GATEWAY_CONNECTION} is set to
	 * <code>false</code>
	 * 
	 * @see #ENABLE_GATEWAY_CONNECTION
	 */
	public int GATEWAY_DEPTH = 1;

	/**
	 * Define the duration between each update of the transfer speed between the two
	 * connected computers connected through the current computer. The transfer
	 * speed is automatically computed.
	 * 
	 * This value has no effect if {@link #ENABLE_GATEWAY_CONNECTION} is set to
	 * <code>false</code>
	 * 
	 * @see #ENABLE_GATEWAY_CONNECTION
	 */
	public long TIME_BETWEEN_EACH_UPDATE_OF_TRANSFERT_SPEED_FOR_GATEWAY_CONNECTION = 30000L;

	/**
	 * Define the number of cached blocks to transfer to another machine. If this
	 * number is reached, the current socket is blocked (so no information can be
	 * received and sent), until the cached blocks are sent to the other machine.
	 */
	public int NUMBER_OF_CACHED_BLOCKS_TO_TRANSFER_BEFORE_BLOCKING_SOCKET = 2;

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
	public KERNEL_ADDRESS_PRIORITY DISTANT_KERNEL_ADDRESS_PRIORITY = KERNEL_ADDRESS_PRIORITY.PRIORITY_IF_CERTIFIED_CONNECTION;

	public enum KERNEL_ADDRESS_PRIORITY {
		NON_PRIORITY, PRIORITY_IF_CERTIFIED_CONNECTION, PRIORITY
	}

	/**
	 * Define the maximum number of connections between two same kernels. Indeed,
	 * between two machines, it is possible to have several network interfaces, so
	 * severals connections. This limitation enables to limit the effect of a DoS
	 * attack.
	 */
	public short NUMBER_OF_MAXIMUM_CONNECTIONS_BETWEEN_TWO_SAME_KERNELS_AND_MACHINES = 3;

	/**
	 * Define the maximum number of connections from one IPV4. This limitation
	 * enables to limit the effect of a DoS attack.
	 */
	public int NUMBER_OF_MAXIMUM_CONNECTIONS_FROM_ONE_IPV4 = 256;

	/**
	 * Define the maximum number of connections from one IPV6. This limitation
	 * enables to limit the effect of a DoS attack.
	 */
	public int NUMBER_OF_MAXIMUM_CONNECTIONS_FROM_ONE_IPV6 = 8;

	/**
	 * Tells the maximum number of simultaneous transfers. Big data transfers are
	 * not concerned.
	 */
	public short NUMBER_OF_MAXIMUM_SIMULTANEOUS_NON_BIG_DATA_TRANSFERS = 8;

	private final ArrayList<ConnectionProtocolProperties<?>> connection_protocol_properties = new ArrayList<>();

	public final <CP extends ConnectionProtocol<CP>> void addConnectionProtocol(
			ConnectionProtocolProperties<CP> _properties) {
		connection_protocol_properties.add(_properties);
	}

	public ConnectionProtocol<?> getConnectionProtocolsInstances(InetSocketAddress _distant_inet_address,
			InetSocketAddress _local_interface_address, DatabaseWrapper sql_connection, MadkitProperties mkProperties, NetworkProperties _properties,
			boolean isServer, boolean needConnectionInitiationAbility) throws NIOException {
		for (ConnectionProtocolProperties<?> cpp : connection_protocol_properties) {
			if (cpp.isConcernedBy(_local_interface_address.getAddress(), _local_interface_address.getPort(),
					_distant_inet_address.getAddress(), isServer, needConnectionInitiationAbility))
				return cpp.getConnectionProtocolInstance(_distant_inet_address, _local_interface_address,
						sql_connection, mkProperties, _properties, isServer, needConnectionInitiationAbility);
		}
		return null;
	}

	public final boolean hasOneOrMoreConnectionsProtocols() {
		return connection_protocol_properties.size() > 0;
	}

}
