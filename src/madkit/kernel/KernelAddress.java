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



/**
 * This class represents a unique identifier for MaDKit kernel.
 * Uniqueness is guaranteed even when different kernels run on the same JVM.
 * 
 * @author Oliver Gutknecht
 * @author Fabien Michel
 * @version 5.2
 * @since MaDKit 1.0
 *
 */
public class KernelAddress implements java.io.Serializable{//TODO local kernel address

	private static final long serialVersionUID = 9180630068132973855L;

	
	private final int ID = Long.valueOf(System.nanoTime()).hashCode();
	private final String name = "@MK-"+(Integer.toString(ID,24).substring(0, 3));
	
	/**
	 * Avoid the default public visibility for denying usage.
	 */
	KernelAddress()
	{
	}
	
	@Override
	public boolean equals(Object obj) {
		//needed for networking mode
		return this == obj || obj.hashCode() == ID;
	}

	@Override
	public int hashCode() {
		return ID;
	}
	
	/** 
	 * Returns a string representation for this platform address 
	 * 
	 * @return a string representation for this platform address 
	 */
	@Override
	public String toString(){
		return name;
	}

}
