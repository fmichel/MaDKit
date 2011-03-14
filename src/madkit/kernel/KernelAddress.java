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

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * @author Oliver Gutknecht
 * @author Fabien Michel since v.3
 * @version 5.1
 * @since MadKit 1.0
 *
 */
final class KernelAddress implements java.io.Serializable{

	private static final long serialVersionUID = 6895837581447431330L;
	private String host;
	private final int ID;
	
	KernelAddress()
	{
		ID = Integer.parseInt(Long.toString(System.currentTimeMillis()).substring(9));//TODO use hashcode of Long
		host = null;
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			host = "local";
		}
	}

//	one kerneladdress by kernel so need to overwrite this
	@Override
	public boolean equals(Object obj) {
//		KernelAddress other = (KernelAddress) obj;
//		return host.equals(other.host) && ID == other.ID;//TODO Maybe compare only IDs
		return obj.hashCode() == ID;
	}
	
	public int hashCode() {
		return ID;
	}

	/** Returns the canonical string representation for this platform address */
	@Override
	public String toString()
	{
		return ("@"+host+":MK"+ID);
	}


}
