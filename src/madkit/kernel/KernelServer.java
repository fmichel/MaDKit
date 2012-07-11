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

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import madkit.action.KernelAction;
import madkit.message.KernelMessage;

/**
 * @author Fabien Michel
 * @version 0.9
 * @since MaDKit 5.0.0.2
 *
 */
final class KernelServer {

	final static private int startingPort = 4444;

	final private ServerSocket serverSocket;

	private boolean running = true;

	/**
	 * @param serverSocket2
	 */
	private KernelServer(ServerSocket serverSocket2) {
		serverSocket = serverSocket2;
	}



	void activate(final NetworkAgent netAgent){
		final Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while(running){
					try {
						netAgent.receiveMessage(new NetworkMessage(NetCode.NEW_PEER_REQUEST, serverSocket.accept()));
					} catch (IOException e) {
						if (running) {//socket failure
							netAgent.receiveMessage(new KernelMessage(KernelAction.EXIT));
						}
						break;
					}
				}
				stop();
			}
		});
		t.setName("MK Server "+netAgent.getName());
		t.start();
	}



	int getPort() {
		return serverSocket.getLocalPort();
	}

	/**
	 * @return the ip
	 */
	InetAddress getIp() {
		return serverSocket.getInetAddress();
	}

	void stop(){
		running = false;
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	final static KernelServer getNewKernelServer() {
		InetAddress ip = findInetAddress();
		if(ip == null){
			try {
				ip = InetAddress.getLocalHost();
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
				return null;
			}
		}
		ip.getHostName();
		boolean inUse = true;
		ServerSocket serverSocket = null;
		int port = startingPort;
		while (inUse) {
			try {
				serverSocket = new ServerSocket(port,50,ip);
				inUse = false;
			} catch (IOException e) {
				port++;
			}
		}
		return new KernelServer(serverSocket);
	}


	static private InetAddress findInetAddress(){
		try {
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			//		    find:
			while (en.hasMoreElements()) {
				NetworkInterface ni = en.nextElement();
				//				printParameter(ni);
				if (! ni.isLoopback()) {
					final Enumeration<InetAddress> e = ni.getInetAddresses();
					while (e.hasMoreElements()) {
						InetAddress ia = e.nextElement();
						if (!ia.isLoopbackAddress() && ia instanceof Inet4Address) {
							return ia;
						}
					}
				}
			}
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		return null;	
	}


}
