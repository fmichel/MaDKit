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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Enumeration;

/**
 * This class represents a unique identifier for MaDKit kernel. Uniqueness is guaranteed even when different kernels run
 * on the same JVM or over the network.
 * 
 * @author Oliver Gutknecht
 * @author Fabien Michel
 * @version 5.31
 * @since MaDKit 1.0
 *
 */
public class KernelAddress implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3191926058535092533L;

    private static final transient long LOCAL_MAC;
    static {
	long result = 0;
	try {
	    final Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
	    if (e != null) {
		while (e.hasMoreElements()) {
		    final NetworkInterface ni = e.nextElement();
		    if (!ni.isLoopback()) {
			final byte[] hardwareAddress = ni.getHardwareAddress();
			if (hardwareAddress != null) {
			    for (final byte value : hardwareAddress) {
				result <<= 8;
				result |= value & 255;
			    }
			}
			break;
		    }
		}
	    }
	}
	catch(SocketException e1) {
	    e1.printStackTrace();
	}
	LOCAL_MAC = result;
    }

    private final long networkID; // for distant identification
    private transient String name;

    private final short localID;

    /**
     * Avoid the default public visibility for denying usage.
     */
    KernelAddress() {
	networkID = LOCAL_MAC;
	short tmp = 0;
	synchronized (Madkit.VERSION) {
	    try (final FileChannel channel = new RandomAccessFile(new File(System.getProperty("java.io.tmpdir"), "KA_MDK"), "rw").getChannel();
		    final FileLock lock = channel.lock();) {
		final ByteBuffer b = ByteBuffer.allocate(2);
		channel.read(b, 0);
		tmp = (short) (b.getShort(0) + 1);
		b.putShort(0, tmp).rewind();
		channel.write(b, 0);
	    }
	    catch(IOException e) {
		e.printStackTrace();
		tmp = (short) System.nanoTime();
	    }
	}
	localID = tmp;
    }

    /**
     * Tells if another kernel address is the same. If <code>true</code>, this means that both addresses refer to the same
     * kernel, i.e. same MaDKit instance.
     * 
     * @throws ClassCastException
     *             On purpose, if the address is compared to an object with another type which is considered as a
     *             programming error.
     * @throws NullPointerException
     *             On purpose, if the address is compared to <code>null</code> which is considered as a programming error.
     */
    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	final KernelAddress other = (KernelAddress) obj;
	return other.localID == localID && networkID == other.networkID;
    }

    @Override
    public int hashCode() {
	return localID;
    }

    public String getNetworkID() {
	return localID + "-" + networkID;
    }

    /**
     * Returns a simplified string representation for this platform address
     * 
     * @return a string representation for this platform address
     */
    @Override
    public String toString() {
	if (name == null)
	    name = "@MK-" + (localID < 0 ? localID + 65536 : localID);
	return name;
    }

}
