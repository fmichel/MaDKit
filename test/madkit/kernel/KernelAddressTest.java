/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MadKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
public class KernelAddressTest {

	private static List<KernelAddress> kas;
	private static List<KernelAddress> simultaneous;

	@BeforeClass 
	public static void createNewAddresses(){
		kas = new ArrayList<KernelAddress>();
		simultaneous = new ArrayList<KernelAddress>();
		for (int i = 0; i < 2000; i++) {
			try {
				Thread.sleep((long) (Math.random()*2));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			kas.add(new KernelAddress());
		}
		for (int i = 0; i < 2000; i++) {
			simultaneous.add(new KernelAddress());
		}
	}
	
	@Test
	public void testHashCode() {
		for (KernelAddress ka : kas) {
			for (KernelAddress other : kas) {
				if(ka != other && other.hashCode() == ka.hashCode()){
					fail("two addresses with identical hashCode");
				}
			}
		}
		for (KernelAddress ka : simultaneous) {
			for (KernelAddress other : simultaneous) {
				if(ka != other && other.hashCode() == ka.hashCode()){
					fail("two addresses with identical hashCode");
				}
			}
		}
	}

	@Test
	public void testUniqueness() {
		for (KernelAddress ka : kas) {
			for (KernelAddress other : simultaneous) {
				if(other.hashCode() == ka.hashCode()){
					fail("two addresses with identical hashCode");
				}
			}
		}
	}

	@Test
	public void testEqualsObject() {
		for (KernelAddress ka : kas) {
			for (KernelAddress other : kas) {
				if(ka != other && other.equals(ka)){
					fail("two addresses equals");
				}
			}
		}
		for (KernelAddress ka : simultaneous) {
			for (KernelAddress other : simultaneous) {
				if(ka != other && other.equals(ka)){
					fail("two addresses equals");
				}
			}
		}
		for (KernelAddress ka : kas) {
			for (KernelAddress other : simultaneous) {
				if(ka != other && other.equals(ka)){
					fail("two addresses equals");
				}
			}
		}
	}

	@Test
	public void testToString() {
		for (KernelAddress ka : simultaneous) {
			System.err.println(ka);
		}
	}

}
