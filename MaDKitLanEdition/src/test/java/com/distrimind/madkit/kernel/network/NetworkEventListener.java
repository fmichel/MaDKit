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

import java.io.File;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.distrimind.madkit.kernel.MadkitEventListener;
import com.distrimind.madkit.kernel.MadkitProperties;
import com.distrimind.madkit.kernel.network.AbstractIP;
import com.distrimind.madkit.kernel.network.DoubleIP;
import com.distrimind.ood.database.EmbeddedHSQLDBDatabaseFactory;
import com.distrimind.ood.database.exceptions.DatabaseException;

import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class NetworkEventListener implements MadkitEventListener {
	final boolean network;
	final boolean upnpIGDEnabled;
	final boolean autoConnectWithLocalSitePeers;
	final boolean databaseEnabled;
	final File databaseFile;
	final AccessProtocolPropertiesMKEventListener madkitEventListenerForAccessProtocols;
	final ConnectionsProtocolsMKEventListener madkitEventListenerForConnectionProtocols;
	final AccessDataMKEventListener madkitEventListenerForAccessData;
	int localPortToBind;
	InetAddress[] inetAddressesToBind;
	List<AbstractIP> connectionsToAttempt;
	private Integer localDataAmountAcc = null;
	private Integer globalDataAmountAcc = null;
	private int gatewayDepth = 1;
	public long durationBeforeCancelingTransferConnection = 20000l;

	public void setGatewayDepth(int gatewayDepth) {
		this.gatewayDepth = gatewayDepth;
	}

	public NetworkEventListener(boolean network, boolean upnpIGDEnabled, boolean autoConnectWithLocalSitePeers,
			File databaseFile, ConnectionsProtocolsMKEventListener madkitEventListenerForConnectionProtocols,
			AccessProtocolPropertiesMKEventListener madkitEventListenerForAccessProtocols,
			AccessDataMKEventListener madkitEventListenerForAccessData, List<AbstractIP> connectionsToAttempt) {
		this(network, upnpIGDEnabled, autoConnectWithLocalSitePeers, databaseFile,
				madkitEventListenerForConnectionProtocols, madkitEventListenerForAccessProtocols,
				madkitEventListenerForAccessData, 5001, connectionsToAttempt);
	}

	public void addConnectionsToAttempt(AbstractIP ip) {
		if (connectionsToAttempt == null)
			connectionsToAttempt = new ArrayList<>();
		else if (!(connectionsToAttempt instanceof ArrayList)) {
			ArrayList<AbstractIP> al = new ArrayList<>();
			al.addAll(connectionsToAttempt);
			connectionsToAttempt = al;
		}
		connectionsToAttempt.add(ip);
	}

	public Integer getLocalDataAmountAcc() {
		return localDataAmountAcc;
	}

	public void setLocalDataAmountAcc(Integer _localDataAmountAcc) {
		localDataAmountAcc = _localDataAmountAcc;
	}

	public Integer getGlobalDataAmountAcc() {
		return globalDataAmountAcc;
	}

	public void setGlobalDataAmountAcc(Integer _globalDataAmountAcc) {
		globalDataAmountAcc = _globalDataAmountAcc;
	}

	public MadkitEventListener getConnectionProtocolsMKEventListener() {
		return madkitEventListenerForConnectionProtocols;
	}

	public void setLocalPortToBind(int localPortToBind) {
		this.localPortToBind = localPortToBind;
	}

	public void addInetAddressesToBind(InetAddress inetAddress) {
		if (inetAddressesToBind == null) {
			inetAddressesToBind = new InetAddress[1];
			inetAddressesToBind[0] = inetAddress;
		} else {
			InetAddress ias[] = new InetAddress[inetAddressesToBind.length + 1];
			System.arraycopy(inetAddressesToBind, 0, ias, 0, inetAddressesToBind.length);
			ias[inetAddressesToBind.length] = inetAddress;
			inetAddressesToBind = ias;
		}
	}

	public NetworkEventListener(boolean network, boolean upnpIGDEnabled, boolean autoConnectWithLocalSitePeers,
			File databaseFile, ConnectionsProtocolsMKEventListener madkitEventListenerForConnectionProtocols,
			AccessProtocolPropertiesMKEventListener madkitEventListenerForAccessProtocols,
			AccessDataMKEventListener madkitEventListenerForAccessData, int localPortToBind,
			List<AbstractIP> connectionsToAttempt, InetAddress... inetAddressesToBind) {
		this.network = network;
		this.upnpIGDEnabled = upnpIGDEnabled;
		this.autoConnectWithLocalSitePeers = autoConnectWithLocalSitePeers;
		this.databaseEnabled = databaseFile != null;
		this.databaseFile = databaseFile;
		this.madkitEventListenerForConnectionProtocols = madkitEventListenerForConnectionProtocols;
		this.madkitEventListenerForAccessProtocols = madkitEventListenerForAccessProtocols;
		this.madkitEventListenerForAccessData = madkitEventListenerForAccessData;
		this.localPortToBind = localPortToBind;
		this.inetAddressesToBind = inetAddressesToBind;
		this.connectionsToAttempt = connectionsToAttempt;
	}

	@Override
	public void onMadkitPropertiesLoaded(MadkitProperties _properties) {
		if (globalDataAmountAcc != null)
			_properties.networkProperties.maxSizeForUnreadShortDataFromAllConnections = globalDataAmountAcc.intValue();
		if (localDataAmountAcc != null)
			_properties.networkProperties.maxSizeForUnreadShortDataFromOneDistantKernel = localDataAmountAcc.intValue();

		_properties.networkProperties.durationBeforeCancelingTransferConnection = durationBeforeCancelingTransferConnection;
		_properties.networkProperties.connectionTimeOut = durationBeforeCancelingTransferConnection;
		_properties.networkProperties.network = network;
		_properties.networkProperties.upnpIGDEnabled = upnpIGDEnabled;
		_properties.networkProperties.autoConnectWithLocalSitePeers = autoConnectWithLocalSitePeers;
		try {
			if (databaseFile != null)
				_properties.setDatabaseFactory(new EmbeddedHSQLDBDatabaseFactory(databaseFile));
		} catch (DatabaseException e) {
			e.printStackTrace();
		}

		_properties.networkProperties.gatewayDepth = gatewayDepth;
		if (inetAddressesToBind != null && inetAddressesToBind.length > 0) {
			// _properties.networkProperties.portsToBindForManualDirectConnections=localPortToBind;
			_properties.networkProperties.portsToBindForAutomaticLocalConnections = localPortToBind;
			_properties.networkProperties.localInetAddressesToBind = new ArrayList<>();
			for (InetAddress ia : inetAddressesToBind)
				_properties.networkProperties.localInetAddressesToBind.add(ia);
		}
		if (connectionsToAttempt != null)
			_properties.networkProperties.connectionToAttempt = connectionsToAttempt;

		if (madkitEventListenerForConnectionProtocols != null)
			madkitEventListenerForConnectionProtocols.onMadkitPropertiesLoaded(_properties);
		if (this.madkitEventListenerForAccessProtocols != null)
			this.madkitEventListenerForAccessProtocols.onMadkitPropertiesLoaded(_properties);
		if (this.madkitEventListenerForAccessData != null)
			this.madkitEventListenerForAccessData.onMadkitPropertiesLoaded(_properties);
	}

	public static ArrayList<NetworkEventListener> getNetworkEventListenersForPeerToPeerConnectionsWithRandomProperties(
			boolean network, boolean upnpIGDEnabled, final boolean databaseEnabled,
			final boolean canTakeLoginInitiative, final boolean isServer, final Runnable invalidPassord,
			CustumHostIdentifier hostIdentifier, int... loginIndexes) throws UnknownHostException {
		ArrayList<NetworkEventListener> res = new ArrayList<>();
		for (ConnectionsProtocolsMKEventListener cp : ConnectionsProtocolsMKEventListener
				.getConnectionsProtocolsMKEventListenerForPeerToPeerConnections(isServer)) {
			for (AccessProtocolPropertiesMKEventListener app : AccessProtocolPropertiesMKEventListener
					.getConnectionsProtocolsMKEventListenerForPeerToPeerConnections()) {
				for (AccessDataMKEventListener ad : AccessDataMKEventListener
						.getAccessDataMKEventListenerForPeerToPeerConnections(canTakeLoginInitiative, invalidPassord,
								hostIdentifier, loginIndexes)) {
					res.add(new NetworkEventListener(network, upnpIGDEnabled, true,
							databaseEnabled ? new File(hostIdentifier + ".database") : null, cp, app, ad, 5000, null,
							InetAddress.getByName("0.0.0.0")));
					res.add(new NetworkEventListener(network, upnpIGDEnabled, false,
							databaseEnabled ? new File(hostIdentifier + ".database") : null, cp, app, ad, null));
				}
			}
		}
		return res;
	}

	public static ArrayList<Object[]> getNetworkEventListenersForLocalClientServerConnection(
			boolean bindDoubleInetAddress, boolean network, boolean upnpIGDEnabled, boolean databaseEnabled,
			final boolean canTakeLoginInitiative, boolean includeP2PConnectionPossibilityForClients,
			final Runnable invalidPassord, int clientNumber, int... loginIndexes)
			throws UnknownHostException, NoSuchAlgorithmException, NoSuchProviderException {
		ArrayList<Object[]> res = new ArrayList<>();

		for (ConnectionsProtocolsMKEventListener cp : ConnectionsProtocolsMKEventListener
				.getConnectionsProtocolsMKEventListenerForServerConnection(includeP2PConnectionPossibilityForClients)) {
			for (AccessProtocolPropertiesMKEventListener app : AccessProtocolPropertiesMKEventListener
					.getConnectionsProtocolsMKEventListenerForServerConnections()) {
				for (AccessDataMKEventListener ad : AccessDataMKEventListener
						.getAccessDataMKEventListenerForServerConnections(canTakeLoginInitiative, invalidPassord,
								AccessDataMKEventListener.getCustumHostIdentifier(0), loginIndexes)) {
					Object o[] = new Object[clientNumber + 1];
					if (bindDoubleInetAddress)
						o[0] = new NetworkEventListener(network, upnpIGDEnabled, false,
								databaseEnabled ? new File("tmpfortest0.database") : null, cp, app, ad, 5001, null,
								InetAddress.getByName("127.0.0.1"), InetAddress.getByName("::1"));
					else
						o[0] = new NetworkEventListener(network, upnpIGDEnabled, false,
								databaseEnabled ? new File("tmpfortest0.database") : null, cp, app, ad, 5001, null,
								InetAddress.getByName("0.0.0.0"));
					res.add(o);
				}
			}
		}

		for (int h = 1; h <= clientNumber; h++) {
			int index = 0;
			for (ConnectionsProtocolsMKEventListener cp : ConnectionsProtocolsMKEventListener
					.getConnectionsProtocolsMKEventListenerForClientConnection(
							includeP2PConnectionPossibilityForClients)) {
				for (AccessProtocolPropertiesMKEventListener app : AccessProtocolPropertiesMKEventListener
						.getConnectionsProtocolsMKEventListenerForClientConnections()) {
					for (AccessDataMKEventListener ad : AccessDataMKEventListener
							.getAccessDataMKEventListenerForClientConnections(canTakeLoginInitiative, invalidPassord,
									AccessDataMKEventListener.getCustumHostIdentifier(h), loginIndexes)) {
						Object o[] = res.get(index++);
						if (bindDoubleInetAddress)
							o[h] = new NetworkEventListener(network, upnpIGDEnabled, false,
									databaseEnabled ? new File("tmpfortest" + clientNumber + ".database") : null, cp,
									app, ad, 5001,
									Arrays.asList(
											(AbstractIP) new DoubleIP(5001,
													(Inet4Address) InetAddress.getByName("127.0.0.1")),
											(AbstractIP) new DoubleIP(5001,
													(Inet6Address) InetAddress.getByName("::1"))));
						else
							o[h] = new NetworkEventListener(network, upnpIGDEnabled, false,
									databaseEnabled ? new File("tmpfortest" + clientNumber + ".database") : null, cp,
									app, ad, 5001,
									Arrays.asList((AbstractIP) new DoubleIP(5001,
											(Inet4Address) InetAddress.getByName("127.0.0.1"),
											(Inet6Address) InetAddress.getByName("::1"))));

					}
				}
			}
		}
		return res;
	}

	public static ArrayList<Object[]> getNetworkEventListenersForPeerToPeerConnectionsWithRandomProperties(
			boolean network, boolean upnpIGDEnabled, boolean databaseEnabled, final boolean canTakeLoginInitiative,
			final Runnable invalidPassord, int hostNumber, int... loginIndexes) throws UnknownHostException {
		ArrayList<ArrayList<NetworkEventListener>> col = new ArrayList<>();
		col.add(NetworkEventListener.getNetworkEventListenersForPeerToPeerConnectionsWithRandomProperties(true, false,
				false, true, true, null, AccessDataMKEventListener.getCustumHostIdentifier(0), 1, 2, 3));
		for (int i = 1; i < hostNumber; i++) {
			col.add(NetworkEventListener.getNetworkEventListenersForPeerToPeerConnectionsWithRandomProperties(true,
					false, false, true, false, null, AccessDataMKEventListener.getCustumHostIdentifier(i), 1, 2, 3));
		}

		ArrayList<Object[]> res = new ArrayList<>();
		for (int i = 0; i < col.get(0).size(); i++) {
			Object params[] = new Object[hostNumber];
			for (int j = 0; j < hostNumber; j++)
				params[j] = col.get(j).get(i);
			res.add(params);
		}

		return res;
	}

	public static ArrayList<NetworkEventListener> getNetworkEventListenersForPeerToPeerConnectionsWithRandomProperties(
			boolean network, boolean upnpIGDEnabled, boolean databaseEnabled, final boolean canTakeLoginInitiative,
			boolean isServer, boolean autoConnectWithLocalSitePeers, final Runnable invalidPassord,
			CustumHostIdentifier hostIdentifier, int... loginIndexes) {
		ArrayList<NetworkEventListener> res = new ArrayList<>();
		for (ConnectionsProtocolsMKEventListener cp : ConnectionsProtocolsMKEventListener
				.getConnectionsProtocolsMKEventListenerForPeerToPeerConnections(isServer)) {
			for (AccessProtocolPropertiesMKEventListener app : AccessProtocolPropertiesMKEventListener
					.getConnectionsProtocolsMKEventListenerForPeerToPeerConnections()) {
				for (AccessDataMKEventListener ad : AccessDataMKEventListener
						.getAccessDataMKEventListenerForPeerToPeerConnections(canTakeLoginInitiative, invalidPassord,
								hostIdentifier, loginIndexes)) {
					res.add(new NetworkEventListener(network, upnpIGDEnabled, autoConnectWithLocalSitePeers,
							databaseEnabled ? new File(hostIdentifier + ".database") : null, cp, app, ad, null));
				}
			}
		}
		return res;
	}

	public static ArrayList<Object[]> getNetworkEventListenersForPeerToPeerConnectionsWithRandomProperties(
			boolean network, boolean upnpIGDEnabled, boolean databaseEnabled, final boolean canTakeLoginInitiative,
			boolean autoConnectWithLocalSitePeers, final Runnable invalidPassord, int hostNumber, int... loginIndexes) {
		ArrayList<ArrayList<NetworkEventListener>> col = new ArrayList<>();
		col.add(NetworkEventListener.getNetworkEventListenersForPeerToPeerConnectionsWithRandomProperties(true, false,
				false, true, true, autoConnectWithLocalSitePeers, null,
				AccessDataMKEventListener.getCustumHostIdentifier(0), 1, 2, 3, 4, 5));

		for (int i = 1; i < hostNumber; i++)
			col.add(NetworkEventListener.getNetworkEventListenersForPeerToPeerConnectionsWithRandomProperties(true,
					false, false, true, false, autoConnectWithLocalSitePeers, null,
					AccessDataMKEventListener.getCustumHostIdentifier(i), 1, 2, 3, 4, 5));

		ArrayList<Object[]> res = new ArrayList<>();
		for (int i = 0; i < col.get(0).size(); i++) {
			Object params[] = new Object[hostNumber];
			for (int j = 0; j < hostNumber; j++)
				params[j] = col.get(j).get(i);
			res.add(params);
		}

		return res;
	}
}
