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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import madkit.action.KernelAction;
import madkit.messages.KernelMessage;

/**
 * 
 * 
 * 
 * @author Fabien Michel
 * @version 6.4
 * @since MaDKit 5
 *
 */
class MultiCastListener {

	static InetAddress ipAddress;

	private MulticastSocket multicastSocket;
	private DatagramSocket datagramSocket;
	private volatile boolean running = true;

	private Thread multicastThread;

	/**
	 */
	private MultiCastListener(MulticastSocket ms, DatagramSocket ds) {
		multicastSocket = ms;
		datagramSocket = ds;
	}

	static MultiCastListener getNewMultiCastListener(int localPort) throws IOException {
		MulticastSocket multicastSocket = null;
		DatagramSocket datagramSocket = null;
		int multiCastPort = 2009;
		if (ipAddress == null) {
			ipAddress = InetAddress.getByName("239.29.08.58");
		}
		multicastSocket = new MulticastSocket(multiCastPort);
		multicastSocket.joinGroup(ipAddress);
		datagramSocket = new DatagramSocket(localPort);
		return new MultiCastListener(multicastSocket, datagramSocket);
	}

	/**
	 * Activate the listener and broadcast existence
	 * 
	 * @param networkAgent
	 * @throws IOException
	 */
	void activate(NetworkAgent networkAgent) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		long kernelOnlineTimestamp = System.nanoTime();
		dos.writeLong(kernelOnlineTimestamp);
		dos.close();
		byte[] data = bos.toByteArray();
		datagramSocket.send(new DatagramPacket(data, 8, ipAddress, 2009));
		multicastThread = new Thread(() -> {
			while (running) {
				try {
					DatagramPacket peerRequest = new DatagramPacket(data, 8);
					multicastSocket.receive(peerRequest);
					DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
					if (kernelOnlineTimestamp < dis.readLong()) {
						networkAgent.receiveMessage(new NetworkStatusMessage(NetCode.NEW_PEER_DETECTED, peerRequest));
					}
				} catch (IOException e) {
					if (running) {
						networkAgent.receiveMessage(new KernelMessage(KernelAction.EXIT));
					}
					break;
				}
			}
			stop();
		});
		multicastThread.setName("Multicast Listener @ " + networkAgent.getName());
		multicastThread.start();
	}

	void stop() {
		running = false;
		multicastSocket.close();
		datagramSocket.close();
		multicastThread.interrupt();
		try {
			multicastThread.join(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
