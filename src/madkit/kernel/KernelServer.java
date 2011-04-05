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
import java.net.BindException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import madkit.kernel.NetworkMessage.NetCode;

/**
 * @author Fabien Michel
 * @version 0.9
 * @since MadKit 5.0.0.2
 *
 */
class KernelServer {

	final static private int startingPort = 4444;

	final private ServerSocket serverSocket;

	/**
	 * @param serverSocket2
	 */
	private KernelServer(ServerSocket serverSocket2) {
		serverSocket = serverSocket2;
	}



	void activate(final NetworkAgent netAgent){
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					try {
						netAgent.receiveMessage(new NetworkMessage<Socket>(NetCode.NEW_PEER_REQUEST, serverSocket.accept()));
					} catch (IOException e) {
//						NetworkMessage<String> stopMessage = new NetworkMessage<String>(e.getMessage());
//						stopMessage.setCode(STOP_NETWORK);
						netAgent.receiveMessage(new Message());//shutdown !
					}
				}
			}
		}).start();
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
		try {
			serverSocket.close();
		} catch (IOException e) {
		}
	}

	final static KernelServer getNewKernelServer(){
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
			} catch (BindException e) {
				port++;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
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



	//TODO remove after test
//	private static void printParameter(NetworkInterface ni) throws SocketException {
//		System.out.println(" Name = " + ni.getName());
//		System.out.println(" Display Name = " + ni.getDisplayName());
//		System.out.println(" Is up = " + ni.isUp());
//		System.out.println(" Support multicast = " + ni.supportsMulticast());
//		System.out.println(" Is loopback = " + ni.isLoopback());
//		System.out.println(" Is virtual = " + ni.isVirtual());
//		System.out.println(" Is point to point = " + ni.isPointToPoint());
//		System.out.println(" Hardware address = " + ni.getHardwareAddress());
//		System.out.println(" MTU = " + ni.getMTU());
//
//		System.out.println("\nList of Interface Addresses:");
//		List<InterfaceAddress> list = ni.getInterfaceAddresses();
//		Iterator<InterfaceAddress> it = list.iterator();
//
//		while (it.hasNext()) {
//			InterfaceAddress ia = it.next();
//			System.out.println(" Address = " + ia.getAddress());
//			System.out.println(" Broadcast = " + ia.getBroadcast());
//			System.out.println(" Network prefix length = " + ia.getNetworkPrefixLength());
//			InetAddress ineta = ia.getAddress();
//			System.err.println(ineta.getCanonicalHostName());
//			System.out.println("");
//		}
//	}

}
