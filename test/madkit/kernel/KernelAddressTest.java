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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

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
