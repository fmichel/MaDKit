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
 * This class represents a unique identifier for MaDKit kernel.
 * Uniqueness is guaranteed even when different kernels run on the same JVM or over the network.
 * 
 * @author Oliver Gutknecht
 * @author Fabien Michel
 * @version 5.3
 * @since MaDKit 1.0
 *
 */
public class KernelAddress implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 3191926058535092533L;
	
	private final static transient long	LOCAL_MAC;
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
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		LOCAL_MAC = result;
	}

	final private long networkID; //for distant identification
	private transient String	 name;

	final private short localID;
	
	/**
	 * Avoid the default public visibility for denying usage.
	 */
	KernelAddress(){
		networkID = LOCAL_MAC;
		short tmp = 0;
		synchronized (Madkit.VERSION) {
			try (final FileChannel channel = new RandomAccessFile(new File(System.getProperty("java.io.tmpdir"), "KA_MDK"), "rw").getChannel();final FileLock lock = channel.lock();) {
				final ByteBuffer b = ByteBuffer.allocate(2);
				channel.read(b, 0);
				tmp = (short) (b.getShort(0) + 1);
				b.putShort(0, tmp).rewind();
				channel.write(b, 0);
			} catch (IOException e) {
				e.printStackTrace();
				tmp = (short) System.nanoTime();
			}
		}
		localID = tmp;
	}
	
	/**
	 * Tells if another kernel address is the same.
	 * If <code>true</code>, this means that both addresses refer to
	 * the same kernel, i.e. same MaDKit instance.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 * 
	 * @throws ClassCastException On purpose, 
	 * if the address is compared to an object with another type 
	 * which is considered as a programming error.
	 * @throws NullPointerException On purpose, 
	 * if the address is compared to <code>null</code>
	 * which is considered as a programming error.
	 */
	@Override
	public boolean equals(Object obj) {
		final KernelAddress other = (KernelAddress) obj;
		return this == obj || (other.localID == localID && networkID == other.networkID);
	}

	@Override
	public int hashCode() {
		return localID;
	}
	
	/** 
	 * Returns a string representation for this platform address 
	 * 
	 * @return a string representation for this platform address 
	 */
	@Override
	public String toString(){
		if(name == null)
			name = "@MK-"+(localID < 0 ? localID + 65536 : localID);
		return name;
	}

}
