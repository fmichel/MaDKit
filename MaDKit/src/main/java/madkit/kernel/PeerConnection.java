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

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;

/**
 * @author Fabien Michel
 * @version 6.4
 *
 */
final class PeerConnection {

	private final Socket peerSocket;
	private boolean activated = false;
	private final NetworkAgent myNetAgent;
	private KernelAddress peerKernelAddress;
	private final ObjectOutputStream outputStream;
	private final ObjectInputStream inputStream;

	boolean isActivated() {
		return activated;
	}

	/**
	 * @return the peerKernelAddress
	 */
	KernelAddress getPeerKernelAddress() {
		return peerKernelAddress;
	}

	public PeerConnection(NetworkAgent netAgent, InetAddress address, int port) throws IOException {
		this(netAgent, new Socket(address, port));
	}

	public PeerConnection(NetworkAgent netAgent, Socket kernelClient) throws IOException {
		myNetAgent = netAgent;
		peerSocket = kernelClient;
		outputStream = new ObjectOutputStream(peerSocket.getOutputStream());
		inputStream = new ObjectInputStream(peerSocket.getInputStream());
	}

	synchronized void activate() {
		if (!activated) {
			activated = true;
			Thread.ofPlatform().start(() -> {
				while (peerSocket.isConnected()) {
					try {
						myNetAgent.receiveMessage((Message) inputStream.readObject());
					} catch (ClassNotFoundException e) {
						myNetAgent.getLogger().log(Level.SEVERE, "Unable to deserialize object", e);
					} catch (IOException e) {
						logIOException(e);
						break;
					}
				}
				myNetAgent.receiveMessage(new NetworkStatusMessage(NetCode.PEER_DECONNECTED, peerKernelAddress));
				closeConnection();
			});
		}
	}

	/**
	 * @param e
	 */
	private void logIOException(final IOException e) {
		if (e instanceof SocketException || e instanceof EOFException) {
			myNetAgent.getLogger().log(Level.FINEST, " socket closed on " + peerKernelAddress, e);
		} else {
			myNetAgent.getLogger().severe(() -> "io problem " + e.getMessage() + " on " + peerKernelAddress);
		}
	}

	/**
	 * @param m
	 */
	synchronized void sendMessage(final Message m) {
		try {
			outputStream.writeObject(m);
		} catch (IOException e) {
			logIOException(e);
			closeConnection();
		}

	}

	/**
	 * close the connection by closing the socket and the streams
	 */
	synchronized void closeConnection() {
		myNetAgent.getLogger().finer(() -> "Closing socket and stream for peer " + peerKernelAddress);
		try {
			outputStream.close();
			inputStream.close();
			peerSocket.close();
		} catch (IOException e) {
			myNetAgent.getLogger().log(Level.FINE, "", e);
		}

	}

	/**
	 * @param netAgent
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	OrganizationSnapshot waitForDistantOrg() throws IOException, ClassNotFoundException {
		return (OrganizationSnapshot) inputStream.readObject();
	}

	/**
	 * Reads the kernel address from the input stream, sets it and returns it.
	 * 
	 * @return the kernel address of the foreign kernel.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	KernelAddress waitForDistantKernelAddress() throws IOException, ClassNotFoundException {
		peerKernelAddress = (KernelAddress) inputStream.readObject();
		return peerKernelAddress;
	}

	@Override
	public String toString() {
		return getInetAddress().getHostAddress() + " dka = " + (peerKernelAddress == null ? "NA" : peerKernelAddress);
	}

	/**
	 * Gets the port.
	 *
	 * @return the port
	 */
	public int getPort() {
		return peerSocket.getPort();
	}

	/**
	 * Gets the inet address.
	 *
	 * @return the inet address
	 */
	public InetAddress getInetAddress() {
		return peerSocket.getInetAddress();
	}

	/**
	 * @return the peerSocket
	 */
	Socket getPeerSocket() {
		return peerSocket;
	}

	/**
	 * @return the outputStream
	 */
	ObjectOutputStream getOutputStream() {
		return outputStream;
	}

}
