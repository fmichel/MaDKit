/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
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
import java.util.logging.Level;

import madkit.agr.OrganizationSnapshot;

/**
 * @author Fabien Michel
 * @version 0.9
 * @since MaDKit 5.0
 *
 */
final class KernelConnection extends Thread{

	private final Socket distantKernelSocket;
	private boolean activated = false;
	private final NetworkAgent myNetAgent;
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

	public KernelConnection(NetworkAgent netAgent, InetAddress address, int port) throws IOException {
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
	OrganizationSnapshot waitForDistantOrg() throws IOException, ClassNotFoundException {
			return (OrganizationSnapshot) ois.readObject();
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
	void sendConnectionInfo(KernelAddress myKA, OrganizationSnapshot map) throws IOException {
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
