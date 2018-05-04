/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.madkit.kernel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.madkit.kernel.network.KernelAddressInterfaced;
import com.distrimind.madkit.util.SerializationTools;

import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.0.9
 * @since MadkitLanEdition 1.0
 * @version 1.0
 * 
 */
public class KernelAddressTest {

	protected static List<KernelAddress> kas;
	private static List<KernelAddress> simultaneous;

	public static KernelAddress getKernelAddressInstance() throws NoSuchAlgorithmException, NoSuchProviderException {
		return new KernelAddress(false);
	}

	@BeforeClass
	public static void createNewAddresses() throws NoSuchAlgorithmException, NoSuchProviderException {
		kas = new ArrayList<>();
		simultaneous = new ArrayList<>();
		for (int i = 0; i < 2000; i++) {
			try {
				Thread.sleep((long) (Math.random() * 2));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			kas.add(new KernelAddress(true));
		}
		for (int i = 0; i < 2000; i++) {
			simultaneous.add(new KernelAddress(true));
		}
	}

	/*
	 * @Test public void testHashCode() { for (KernelAddress ka : kas) { for
	 * (KernelAddress other : kas) { if (ka != other && other.hashCode() ==
	 * ka.hashCode()) { fail("two addresses with identical hashCode"); } } } for
	 * (KernelAddress ka : simultaneous) { for (KernelAddress other : simultaneous)
	 * { if (ka != other && other.hashCode() == ka.hashCode()) {
	 * fail("two addresses with identical hashCode"); } } } }
	 */

	public void createKASimultaneously() throws InterruptedException {
		List<Thread> ts = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						for (int j = 0; j < 1000; j++) {
							synchronized (kas) {
								kas.add(new KernelAddress(true));
							}
						}
					} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
						e.printStackTrace();
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
	public void testUniqueness() throws InterruptedException, NoSuchAlgorithmException, NoSuchProviderException {
		for (int i = 0; i < 1000; i++) {
			assertFalse(new KernelAddress(true).equals(new KernelAddress(true)));
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
	public void testEqualsObject() throws NoSuchAlgorithmException, IOException, ClassNotFoundException, NoSuchProviderException {
		for (KernelAddress ka : kas) {
			for (KernelAddress other : kas) {
				if (ka != other && other.equals(ka)) {
					fail("two addresses equals");
				}
			}
		}
		for (KernelAddress ka : kas) {
			KernelAddress kas = null;
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
					SerializationTools.writeExternalizableAndSizable(oos, ka, false);
				}
				try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())) {
					try (ObjectInputStream ois = new ObjectInputStream(bais)) {
						kas = (KernelAddress) ois.readObject();
					}

				}
			}
			Assert.assertEquals(ka, ka);
			Assert.assertEquals(ka, kas);
			KernelAddressInterfaced kai = new KernelAddressInterfaced(ka, true);
			KernelAddressInterfaced kais = null;
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
					SerializationTools.writeExternalizableAndSizable(oos, kai, false);
				}
				try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())) {
					try (ObjectInputStream ois = new ObjectInputStream(bais)) {
						kais = (KernelAddressInterfaced) ois.readObject();
					}

				}
			}
			Assert.assertEquals(kai, kai);
			Assert.assertEquals(kai, kais);
			Assert.assertEquals(kai.getOriginalKernelAddress(), kai.getOriginalKernelAddress());
			Assert.assertEquals(kai.getOriginalKernelAddress(), kais.getOriginalKernelAddress());
			Assert.assertEquals(kais.getOriginalKernelAddress(), kai.getOriginalKernelAddress());
			Assert.assertEquals(kais, kai);
			Assert.assertEquals(kai, ka);
			Assert.assertEquals(ka, kai);
			Assert.assertEquals(kai, kas);
			Assert.assertEquals(kas, kai);
			Assert.assertEquals(kais, kas);
			Assert.assertEquals(kas, kais);
			Assert.assertEquals(kais, ka);
			Assert.assertEquals(ka, kais);
			kai = new KernelAddressInterfaced(ka, false);
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
					SerializationTools.writeExternalizableAndSizable(oos, kai, false);
				}
				try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())) {
					try (ObjectInputStream ois = new ObjectInputStream(bais)) {
						kais = (KernelAddressInterfaced) ois.readObject();
					}

				}
			}
			Assert.assertEquals(kai, kai);
			Assert.assertEquals(kai.getOriginalKernelAddress(), kai.getOriginalKernelAddress());
			Assert.assertEquals(kai.getOriginalKernelAddress(), kais.getOriginalKernelAddress());
			Assert.assertEquals(kais.getOriginalKernelAddress(), kai.getOriginalKernelAddress());
			Assert.assertEquals(kai, kais);
			Assert.assertEquals(kais, kai);
			Assert.assertNotEquals(kai, ka);
			Assert.assertNotEquals(ka, kai);
			Assert.assertNotEquals(kais, ka);
			Assert.assertNotEquals(ka, kais);
			Assert.assertNotEquals(kais, kas);
			Assert.assertNotEquals(kas, kais);
			Assert.assertNotEquals(kai, kas);
			Assert.assertNotEquals(kas, kai);
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

	// @Test
	// public void testToString() {
	// for (KernelAddress ka : simultaneous) {
	// System.err.println(ka);
	// }
	// }

}
