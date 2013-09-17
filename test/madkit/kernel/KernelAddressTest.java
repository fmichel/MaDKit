/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MaDKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.9
 * @version 0.9
 * 
 */
public class KernelAddressTest {

	private static List<KernelAddress> kas;
	private static List<KernelAddress> simultaneous;

	@BeforeClass
	public static void createNewAddresses() {
		kas = new ArrayList<>();
		simultaneous = new ArrayList<>();
		for (int i = 0; i < 2000; i++) {
			try {
				Thread.sleep((long) (Math.random() * 2));
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
				if (ka != other && other.hashCode() == ka.hashCode()) {
					fail("two addresses with identical hashCode");
				}
			}
		}
		for (KernelAddress ka : simultaneous) {
			for (KernelAddress other : simultaneous) {
				if (ka != other && other.hashCode() == ka.hashCode()) {
					fail("two addresses with identical hashCode");
				}
			}
		}
	}

	public void createKASimultaneously() throws InterruptedException {
		List<Thread> ts = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					for (int j = 0; j < 1000; j++) {
						synchronized (kas) {
							kas.add(new KernelAddress());
						}
					}
				}
			});
			ts.add(t);
			t.start();
		}
		for (Thread thread : ts) {
			thread.join();
		}
	}

	@Test
	public void testUniqueness() throws InterruptedException {
		for (int i = 0; i < 1000; i++) {
			assertFalse(new KernelAddress().hashCode() == new KernelAddress().hashCode());
		}
		for (KernelAddress ka : kas) {
			for (KernelAddress other : simultaneous) {
				if (other.hashCode() == ka.hashCode()) {
					fail("two addresses with identical hashCode");
				}
			}
		}
		createKASimultaneously();
		ArrayList<KernelAddress> all = new ArrayList<>(kas);
		for (Iterator<KernelAddress> iterator = all.iterator(); iterator.hasNext();) {
			ArrayList<KernelAddress> l = new ArrayList<>(all);
			KernelAddress ka = iterator.next();
			l.remove(ka);
			for (KernelAddress other : l) {
				if (other.hashCode() == ka.hashCode()) {
					fail("two addresses with identical hashCode");
				}
			}
			iterator.remove();
		}
	}
	
	@Test
	public void testUniqueness2() throws IOException, InterruptedException {
		Process p = Runtime.getRuntime().exec("java -cp bin:build/test/classes madkit.kernel.Madkit --desktop --launchAgents madkit.kernel.AbstractAgent");
		Thread.sleep(10000);
		p.destroy();
	}

	// @Test
	// public void testLocalKernelAddress() {
	// KernelAddress ka = new KernelAddress();
	// System.err.println(ka);
	// KernelAddress lka = new LocalKernelAddress();
	// System.err.println(lka);
	// }

	@Test
	public void testEqualsObject() {
		for (KernelAddress ka : kas) {
			for (KernelAddress other : kas) {
				if (ka != other && other.equals(ka)) {
					fail("two addresses equals");
				}
			}
		}
		for (KernelAddress ka : simultaneous) {
			for (KernelAddress other : simultaneous) {
				if (ka != other && other.equals(ka)) {
					fail("two addresses equals");
				}
			}
		}
		for (KernelAddress ka : kas) {
			for (KernelAddress other : simultaneous) {
				if (ka != other && other.equals(ka)) {
					fail("two addresses equals");
				}
			}
		}
	}

//	@Test
//	public void testToString() {
//		for (KernelAddress ka : simultaneous) {
//			System.err.println(ka);
//		}
//	}

}
