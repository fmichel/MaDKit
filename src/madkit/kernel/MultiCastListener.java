/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
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
import java.net.UnknownHostException;

import madkit.action.KernelAction;
import madkit.message.KernelMessage;

/**
 * @author Fabien Michel
 * @version 0.9
 * @since MaDKit 5
 *
 */
final class MultiCastListener {


	static InetAddress ipAddress;

	final private MulticastSocket ms;
	final private DatagramSocket ds;
	private boolean running = true;

	/**
	 * @param ms2
	 */
	private MultiCastListener(MulticastSocket ms2, DatagramSocket ds2) {
		ms = ms2;
		ds = ds2;
	}

	static MultiCastListener getNewMultiCastListener(final int localPort) throws IOException, UnknownHostException{
		MulticastSocket ms = null;
		DatagramSocket ds = null;
		final int multiCastPort = 9999;
		if (ipAddress == null) {
			ipAddress = InetAddress.getByName("224.2.2.3");
		}
		ms = new MulticastSocket(multiCastPort);
		ms.joinGroup(ipAddress);
		ms.setLoopbackMode(true);
		ds = new DatagramSocket(localPort);
		return new MultiCastListener(ms,ds);
	}

	/**
	 * Activate the listener and broadcast existence
	 * 
	 * @param networkAgent
	 * @param localIP
	 * @param localPort
	 * @throws IOException 
	 */
	void activate(final NetworkAgent networkAgent) throws IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();  
		final DataOutputStream dos = new DataOutputStream(bos);  
		final long onlineTime = System.nanoTime();
		dos.writeLong(onlineTime);
		dos.close();  
		final byte[] data = bos.toByteArray();  		
		ds.send(new DatagramPacket(data, 8, ipAddress, 9999));
		final Thread t = new Thread(new Runnable() { //TODO problem if two arrive at the time
			@Override
			public void run() {
				while(running){
					try {
						final DatagramPacket peerRequest = new DatagramPacket(data, 8);
						ms.receive(peerRequest);
						final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
						if(onlineTime < dis.readLong())
							networkAgent.receiveMessage(new NetworkMessage(NetCode.NEW_PEER_DETECTED,peerRequest));
					} catch (IOException e) {
						if (running) {//socket failure
							networkAgent.receiveMessage(new KernelMessage(KernelAction.EXIT));
						}
						break;
					}
				}
				stop();
			}
		});
		t.setName("MCL "+networkAgent.getName());
		t.start();
	}

	void stop() {
		running = false;
		ms.close();
		ds.close();
	}

}
