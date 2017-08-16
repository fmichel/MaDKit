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

import madkit.action.KernelAction;
import madkit.message.KernelMessage;

/**
 * The kernel server class. Create P2P connections with other kernels.
 * 
 * @author Fabien Michel
 * @version 0.91
 * @since MaDKit 5.0.0.2
 *
 */
final class KernelServer {

    private static final int startingPort = 4444;

    private final ServerSocket serverSocket;

    private boolean running = true;

    private static final String EXTERNAL_IP;

    static {
	String s = null;
	try {
	    BufferedReader in = new BufferedReader(new InputStreamReader(new URL(Madkit.WEB + "/whatismyip.php").openStream()));
	    s = in.readLine();
	    in.close();
	}
	catch(IOException e) {
	}
	EXTERNAL_IP = s == null ? "" : " -- WAN : " + s;
    }

    /**
     * @param serverSocket2
     */
    private KernelServer(ServerSocket serverSocket2) {
	serverSocket = serverSocket2;
    }

    void activate(final NetworkAgent netAgent) {
	final Thread t = new Thread(new Runnable() {

	    @Override
	    public void run() {
		while (running) {
		    try {
			netAgent.receiveMessage(new NetworkMessage(NetCode.NEW_PEER_REQUEST, serverSocket.accept()));
		    }
		    catch(IOException e) {
			if (running) {// socket failure
			    netAgent.receiveMessage(new KernelMessage(KernelAction.EXIT));
			}
			break;
		    }
		}
		stop();
	    }
	});
	t.setName("MK Server " + netAgent.getName());
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

    void stop() {
	running = false;
	try {
	    serverSocket.close();
	}
	catch(IOException e) {
	    e.printStackTrace();
	}
    }

    @SuppressWarnings("resource")
    static final KernelServer getNewKernelServer() {
	InetAddress ip = findInetAddress();
	if (ip == null) {
	    try {
		ip = InetAddress.getLocalHost();
	    }
	    catch(UnknownHostException e1) {
		e1.printStackTrace();
		return null;
	    }
	}
	// ip.getHostName();
	ServerSocket serverSocket = null;
	int port = startingPort;
	while (serverSocket == null) {
	    try {
		serverSocket = new ServerSocket(port, 50, ip);
	    }
	    catch(IOException e) {
		port++;
	    }
	}
	return new KernelServer(serverSocket);
    }

    @Override
    public String toString() {
	return getIp() + ":" + getPort() + EXTERNAL_IP;
    }

    static private InetAddress findInetAddress() {
	try {
	    Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
	    // find:
	    while (en.hasMoreElements()) {
		NetworkInterface ni = en.nextElement();
		if (!ni.isLoopback()) {
		    final Enumeration<InetAddress> e = ni.getInetAddresses();
		    while (e.hasMoreElements()) {
			InetAddress ia = e.nextElement();
			if (!ia.isLoopbackAddress() && ia instanceof Inet4Address) {
			    return ia;
			}
		    }
		}
	    }
	}
	catch(SocketException e1) {
	    e1.printStackTrace();
	}
	return null;
    }

}
