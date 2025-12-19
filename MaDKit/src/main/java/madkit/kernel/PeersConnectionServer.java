/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.kernel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * The kernel server class creates a server waiting for incoming connection requests from
 * other kernels.
 * 
 * @author Fabien Michel
 * @version 6.4
 * @since MaDKit 5.0.0.2
 *
 */
final class PeersConnectionServer {

	private static final int STARTING_PORT = 4444;

	private final ServerSocket mySocket;

	private NetworkAgent myAgent;

	private boolean running = false;

	private static final String EXTERNAL_IP;

	static {
		String s = null;
		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(new URL("https://www.madkit.net/madkit/whatismyip.php").openStream()));
			s = in.readLine();
			in.close();
		} catch (IOException e) {
		}
		EXTERNAL_IP = s == null ? "" : " -- WAN : " + s;
	}

	/**
	 * @param serverSocket2
	 */
	private PeersConnectionServer(ServerSocket serverSocket2, NetworkAgent agent) {
		myAgent = agent;
		mySocket = serverSocket2;
	}

	static final PeersConnectionServer getNewServer(NetworkAgent na) throws SocketException, UnknownHostException {
		InetAddress ip = findInetAddress();
		if (ip == null) {
			ip = InetAddress.getLocalHost();
		}
		ServerSocket serverSocket = null;
		int port = STARTING_PORT;
		while (serverSocket == null) {
			try {
				serverSocket = new ServerSocket(port, 50, ip);
			} catch (IOException e) {
				port++;
			}
		}
		return new PeersConnectionServer(serverSocket, na);
	}

	void activate() {
		final Thread t = new Thread(() -> {
			running = true;
			while (running) {
				try {
					myAgent.receiveMessage(new NetworkStatusMessage(NetCode.NEW_PEER_REQUEST, mySocket.accept()));
				} catch (IOException e) {
					running = false;
				}
			}
		}, "MK Server " + myAgent.getName());
		t.start();
	}

	void stop() {
		running = false;
		try {
			mySocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	int getPort() {
		return mySocket.getLocalPort();
	}

	/**
	 * @return the ip
	 */
	InetAddress getIp() {
		return mySocket.getInetAddress();
	}

	@Override
	public String toString() {
		return getIp() + ":" + getPort() + EXTERNAL_IP;
	}

	private static InetAddress findInetAddress() throws SocketException {
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface networkInterface = interfaces.nextElement();
			if (!networkInterface.isLoopback()) {
				Enumeration<InetAddress> e = networkInterface.getInetAddresses();
				while (e.hasMoreElements()) {
					InetAddress inetAddress = e.nextElement();
					if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
						return inetAddress;
					}
				}
			}
		}
		return null;
	}

}
