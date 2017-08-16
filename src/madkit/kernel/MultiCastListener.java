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

    private final MulticastSocket ms;
    private final DatagramSocket ds;
    private boolean running = true;

    /**
     * @param ms2
     */
    private MultiCastListener(MulticastSocket ms2, DatagramSocket ds2) {
	ms = ms2;
	ds = ds2;
    }

    @SuppressWarnings("resource")
    static MultiCastListener getNewMultiCastListener(final int localPort) throws IOException, UnknownHostException {
	MulticastSocket ms = null;
	DatagramSocket ds = null;
	final int multiCastPort = 2009;
	if (ipAddress == null) {
	    ipAddress = InetAddress.getByName("239.29.08.58");
	}
	ms = new MulticastSocket(multiCastPort);
	ms.joinGroup(ipAddress);
	ds = new DatagramSocket(localPort);
	return new MultiCastListener(ms, ds);
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
	ds.send(new DatagramPacket(data, 8, ipAddress, 2009));
	final Thread t = new Thread(new Runnable() { // TODO problem if two arrive at the time

	    @Override
	    public void run() {
		while (running) {
		    try {
			final DatagramPacket peerRequest = new DatagramPacket(data, 8);
			ms.receive(peerRequest);
			final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
			if (onlineTime < dis.readLong())
			    networkAgent.receiveMessage(new NetworkMessage(NetCode.NEW_PEER_DETECTED, peerRequest));
		    }
		    catch(IOException e) {
			if (running) {// socket failure
			    networkAgent.receiveMessage(new KernelMessage(KernelAction.EXIT));
			}
			break;
		    }
		}
		stop();
	    }
	});
	t.setName("MCL " + networkAgent.getName());
	t.start();
    }

    void stop() {
	running = false;
	ms.close();
	ds.close();
    }

}
