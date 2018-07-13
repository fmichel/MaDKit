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
package com.distrimind.madkit.kernel.network.connection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.distrimind.madkit.exceptions.NIOException;
import com.distrimind.madkit.kernel.MadkitProperties;
import com.distrimind.madkit.kernel.network.InetAddressFilters;
import com.distrimind.madkit.kernel.network.NetworkProperties;
import com.distrimind.madkit.util.MultiFormatPropertiesObjectParser;
import com.distrimind.madkit.util.XMLUtilities;
import com.distrimind.ood.database.DatabaseWrapper;
import com.distrimind.util.properties.MultiFormatProperties;

/**
 * Represents properties of a specific connection protocol
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition
 *
 */
public abstract class ConnectionProtocolProperties<CP extends ConnectionProtocol<CP>> extends MultiFormatProperties {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5967436161679203461L;

	/**
	 * Allowed and forbidden distant peers
	 */
	public InetAddressFilters filtersForDistantPeers = null;

	/**
	 * Allowed and forbidden local network interfaces
	 */
	public InetAddressFilters filtersForLocalNetworkInterfaces = null;

	/**
	 * The corresponding connection protocol class
	 */
	public Class<CP> connectionProtocolClass;

	/**
	 * Duration in millisecond after what secured random generator are
	 * reinitialized.
	 */
	public long renewRandomInterval = 600000L;

	/**
	 * The sub protocol to instantiate when this one is terminated
	 */
	public ConnectionProtocolProperties<?> subProtocolProperties = null;

	protected ConnectionProtocolProperties(Class<CP> _connectionProtocolClass) {
		super(new MultiFormatPropertiesObjectParser());
		connectionProtocolClass = _connectionProtocolClass;
	}

	public int getNumberOfSubConnectionProtocols() {
		if (subProtocolProperties == null)
			return 0;
		else
			return subProtocolProperties.getNumberOfSubConnectionProtocols() + 1;
	}

	/**
	 * Tells if the filter accept the connection with the given parameters
	 * corresponding to a distant peer
	 * 
	 * @param _distant_inet_address
	 *            the distant inet address
	 * @param _local_port
	 *            the local port
	 * @return true if the filter accept the connection with the given parameters
	 */
	public boolean isConcernedByDistantPeer(InetAddress _distant_inet_address, int _local_port) {
		if (filtersForDistantPeers == null)
			return true;
		else
			return filtersForDistantPeers.isConcernedBy(_distant_inet_address, _local_port);
	}

	/**
	 * Tells if the filter accept the connection with the given parameters
	 * corresponding to a local network interface
	 * 
	 * @param _local_inet_address
	 *            the local inet address
	 * @param _local_port
	 *            the local port
	 * @return true if the filter accept the connection with the given parameters
	 */
	public boolean isConcernedByLocalNetworkInterface(InetAddress _local_inet_address, int _local_port) {
		if (filtersForLocalNetworkInterfaces == null)
			return true;
		else
			return filtersForLocalNetworkInterfaces.isConcernedBy(_local_inet_address, _local_port);
	}

	/**
	 * Tells if the filter accept the connection with the given parameters
	 * corresponding to a local network interface and a distant peer
	 * 
	 * @param _local_inet_address
	 *            the local inet address
	 * @param _local_port
	 *            the local port
	 * @param _distant_inet_address
	 *            the distant inet address
	 * @param isServer true if the current host is a server
	 * @param needBiDirectionnalConnectionInitiationAbility true if the two peers must be able to initiate the connection
	 * @return true if the filter accept the connection with the given parameters
	 */
	public boolean isConcernedBy(InetAddress _local_inet_address, int _local_port, InetAddress _distant_inet_address,
			boolean isServer, boolean needBiDirectionnalConnectionInitiationAbility) {
		return isConcernedByLocalNetworkInterface(_local_inet_address, _local_port)
				&& isConcernedByDistantPeer(_distant_inet_address, _local_port)
				&& ((needBiDirectionnalConnectionInitiationAbility && this.supportBidirectionnalConnectionInitiative())
						|| !needBiDirectionnalConnectionInitiationAbility
								&& (!isServer || this.canBeServer()));
	}

	public ConnectionProtocol<CP> getConnectionProtocolInstance(InetSocketAddress _distant_inet_address,
			InetSocketAddress _local_interface_address, DatabaseWrapper sql_connection, MadkitProperties mkProperties, NetworkProperties _properties,
			boolean isServer, boolean needBiDirectionnalConnectionInitiationAbility) throws NIOException {
		return getConnectionProtocolInstance(_distant_inet_address, _local_interface_address, sql_connection,
				mkProperties, _properties, 0, isServer, needBiDirectionnalConnectionInitiationAbility);
	}

	@SuppressWarnings("unused")
	private ConnectionProtocol<CP> getConnectionProtocolInstance(InetSocketAddress _distant_inet_address,
																 InetSocketAddress _local_interface_address, DatabaseWrapper sql_connection, MadkitProperties mkProperties, NetworkProperties _properties,
																 int subProtocolLevel, boolean isServer, boolean needBiDirectionnalConnectionInitiationAbility)
			throws NIOException {
		try {
			Constructor<CP> c = connectionProtocolClass.getDeclaredConstructor(InetSocketAddress.class,
					InetSocketAddress.class, ConnectionProtocol.class, DatabaseWrapper.class, MadkitProperties.class, NetworkProperties.class,
					int.class, boolean.class, boolean.class);
			c.setAccessible(true);
			ConnectionProtocol<?> sub = null;
			if (subProtocolProperties != null) {
				sub = subProtocolProperties.getConnectionProtocolInstance(_distant_inet_address,
						_local_interface_address, sql_connection, mkProperties, _properties, subProtocolLevel + 1, isServer,
						needBiDirectionnalConnectionInitiationAbility);
				if (sub == null)
					return null;
			}
			return c.newInstance(_distant_inet_address, _distant_inet_address, sub, sql_connection, mkProperties, _properties,
					subProtocolLevel, isServer,
					needBiDirectionnalConnectionInitiationAbility);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new NIOException("Impossible to instantiate class " + connectionProtocolClass.getCanonicalName(), e);
		}

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

	public final boolean needsServerSocket(InetAddress _local_inet_address, int _local_port) {
		if (isConcernedByLocalNetworkInterface(_local_inet_address, _local_port))
			return needsServerSocketImpl() || (subProtocolProperties != null
					&& subProtocolProperties.needsServerSocket(_local_inet_address, _local_port));
		else
			return false;
	}

	protected abstract boolean needsServerSocketImpl();

	public final boolean canTakeConnectionInitiative() {
		if (canTakeConnectionInitiativeImpl()) {
			if (subProtocolProperties != null)
				return subProtocolProperties.canTakeConnectionInitiative();
			else
				return true;
		} else
			return false;

	}

	public final boolean supportBidirectionnalConnectionInitiative() {
		if (supportBidirectionnalConnectionInitiativeImpl()) {
			if (subProtocolProperties != null)
				return subProtocolProperties.supportBidirectionnalConnectionInitiative();
			else
				return true;
		} else
			return false;
	}

	protected abstract boolean supportBidirectionnalConnectionInitiativeImpl();

	protected abstract boolean canTakeConnectionInitiativeImpl();

	protected abstract boolean canBeServer();

}
