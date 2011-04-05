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


	final static int multiCastPort = 9999;
	final static String MC_IP = "224.2.2.3";

	final private MulticastSocket ms;

	static MultiCastListener getNewMultiCastListener(){
		MulticastSocket ms = null;
		try {
			ms = new MulticastSocket(multiCastPort);
			ms.joinGroup(InetAddress.getByName(MC_IP));
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return new MultiCastListener(ms);
	}

	void activate(final NetworkAgent networkAgent, final InetAddress localIP,final int localPort) {
		new Thread(new Runnable() { //TODO problem if two arrive at the time
			@Override
			public void run() {
				while(true){
					try {
						final DatagramPacket packet = new DatagramPacket(new byte[0], 0);
						ms.receive(packet);
//						final KernelServer netConfig = networkAgent.getNetConfig();
						//						if(packet.getAddress().getCanonicalHostName().contains(".local")) //TODO find another way for the ips this is dirty
						if(localIP.equals(packet.getAddress()) && localPort == packet.getPort()){
							continue;
						}
						networkAgent.receiveMessage(new NetworkMessage<DatagramPacket>(NetCode.NEW_PEER_DETECTED,packet));
					} catch (IOException e) {
						networkAgent.receiveMessage(new Message());//Means Shutdown
					}
				}
			}
		}).start();

		try {
			DatagramPacket packet = new DatagramPacket(new byte[0], 0, InetAddress.getByName(MultiCastListener.MC_IP), MultiCastListener.multiCastPort);
			new DatagramSocket(localPort).send(packet);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
}
	/**
	 * @param ms2
	 */
	private MultiCastListener(MulticastSocket ms2) {
		ms = ms2;
	}

	public void stop() {
		ms.close();
	}

}
