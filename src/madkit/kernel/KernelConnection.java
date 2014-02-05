/*
 * Copyright 1997-2013 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author Fabien Michel
 * @version 0.9
 * @since MaDKit 5.0
 *
 */
final class KernelConnection extends Thread{

	final private Socket distantKernelSocket;
	private boolean activated = false;
	final private NetworkAgent myNetAgent;
	private KernelAddress distantKernelAddress;

	boolean isActivated() {
		return activated;
	}

	/**
	 * @return the distantKernelSocket
	 */
	Socket getDistantKernelSocket() {
		return distantKernelSocket;
	}

	private final ObjectOutputStream oos;
	private final ObjectInputStream ois;

	/**
	 * @return the distantKernelAddress
	 */
	KernelAddress getKernelAddress() {
		return distantKernelAddress;
	}

	@SuppressWarnings("resource")
	public KernelConnection(NetworkAgent netAgent, InetAddress address, int port) throws UnknownHostException,IOException {
		this(netAgent,new Socket(address, port));
	}

	public KernelConnection(NetworkAgent netAgent, Socket kernelClient) throws IOException{
		myNetAgent = netAgent;
		distantKernelSocket = kernelClient;
		oos = new ObjectOutputStream(distantKernelSocket.getOutputStream());
		ois = new ObjectInputStream(distantKernelSocket.getInputStream());
	}

	/**
	 * @param netAgent
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	Map<String, Map<String, Map<String, Set<AgentAddress>>>> waitForDistantOrg() throws IOException, ClassNotFoundException {
			return (Map<String, Map<String, Map<String, Set<AgentAddress>>>>) ois.readObject();
	}
	
	/**
	 * Reads the kernel address from the input stream, sets it and returns it.
	 * @return the kernel address of the foreign kernel.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	KernelAddress waitForDistantKernelAddress() throws IOException, ClassNotFoundException{
		return distantKernelAddress = (KernelAddress) ois.readObject();
	}


	/**
	 * @param map
	 * @throws IOException 
	 */
	void sendConnectionInfo(KernelAddress myKA, Map<String, Map<String, Map<String, Set<AgentAddress>>>> map) throws IOException {
			oos.writeObject(myKA);
			oos.writeObject(map);
	}

	@Override
	public void run() {
		activated = true;
		while(distantKernelSocket.isConnected()){
			try {
				myNetAgent.receiveMessage((Message) ois.readObject());
			} catch (ClassNotFoundException e) {
				myNetAgent.getLogger().log(Level.SEVERE,"Unable to deserialize object", e);
			} catch (IOException e) {
				logIOException(e);
				break;
			}
		}
		myNetAgent.receiveMessage(new NetworkMessage(NetCode.PEER_DECONNECTED, distantKernelAddress));
		closeConnection();
	}

	/**
	 * @param e
	 */
	private void logIOException(final IOException e) {
		if (e instanceof SocketException || e instanceof EOFException) {
			myNetAgent.getLogger().log(Level.FINEST," socket closed on "+distantKernelAddress, e);
		}
		else{
			myNetAgent.getLogger().severeLog("io problem", e);
		}
	}

	/**
	 * @param m
	 */
	synchronized void sendMessage(final Message m) {
		try {
			oos.writeObject(m);
		} catch (IOException e) {
			logIOException(e);
		}
		
	}

	/**
	 * close the connection by closing the socket and the streams
	 */
	synchronized void closeConnection() {
		try {
			oos.close();
			ois.close();
			distantKernelSocket.close();
		} catch (IOException e) {
			myNetAgent.getLogger().log(Level.FINE, "", e);
		}
		
	}
	
	@Override
	public String toString() {
		return getInetAddress().getHostAddress()+" dka = "+(distantKernelAddress == null ? "NA" : distantKernelAddress);
	}

	public int getPort(){
		return distantKernelSocket.getPort();
	}
	
	public InetAddress getInetAddress(){
		return distantKernelSocket.getInetAddress();
	}

}
