/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import madkit.kernel.NetworkMessage.NetCode;

/**
 * @author Fabien Michel
 * @version 0.9
 * @since MadKit 5
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
			ds = new DatagramSocket(localPort);
			ds.send(new DatagramPacket(new byte[0], 0, ipAddress, 9999));
			return new MultiCastListener(ms,ds);
	}

	void activate(final NetworkAgent networkAgent, final InetAddress localIP,final int localPort) {
		final Thread t = new Thread(new Runnable() { //TODO problem if two arrive at the time
			@Override
			public void run() {
				while(running){
					try {
						final DatagramPacket peerRequest = new DatagramPacket(new byte[0], 0);
						ms.receive(peerRequest);
						if(localIP.equals(peerRequest.getAddress()) && localPort == peerRequest.getPort()){
							continue;
						}
						networkAgent.receiveMessage(new NetworkMessage<DatagramPacket>(NetCode.NEW_PEER_DETECTED,peerRequest));
					} catch (IOException e) {
						if (running) {//socket failure
							networkAgent.receiveMessage(new NetworkMessage<Object>(NetCode.FAILURE, null));
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
