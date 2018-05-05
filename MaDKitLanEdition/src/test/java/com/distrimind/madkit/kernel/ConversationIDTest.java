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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.distrimind.madkit.kernel.ConversationID.InterfacedIDs;
import com.distrimind.madkit.util.SerializationTools;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class ConversationIDTest extends JunitMadkit {
	static final Random rand = new Random(System.currentTimeMillis());

	@Test
	public void multipleTestConversationID() {
		for (int i = 0; i < 10; i++) {
			System.out.println("Test " + i);
			testConversationID();
		}
	}

	@Test
	public void testConversationID() {

		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				try {
					KernelAddress ka1 = getKernelAddress();
					KernelAddress ka2 = new KernelAddress(false);
					Map<KernelAddress, InterfacedIDs> globaInterfacedIDs1 = getMadkitKernel().getGlobalInterfacedIDs();
					Map<KernelAddress, InterfacedIDs> globaInterfacedIDs2 = new HashMap<>();
					int nb = rand.nextInt(100);
					ArrayList<ConversationID> ids = new ArrayList<>();
					for (int i = 0; i < nb; i++) {
						ConversationID id = new ConversationID();

						Assert.assertEquals(id, id);
						for (ConversationID other : ids)
							Assert.assertNotEquals(id, other);
						try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
							try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
								SerializationTools.writeExternalizableAndSizable(oos, id, false);
							}
							try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())) {
								try (ObjectInputStream ois = new ObjectInputStream(bais)) {
									ConversationID deserializedID = (ConversationID) SerializationTools.readExternalizableAndSizable(ois, false);
									Assert.assertEquals(id, deserializedID);
									for (ConversationID other : ids)
										Assert.assertNotEquals(deserializedID, other);
								}
							}
						}
						ids.add(id);
					}
					for (int i = 0; i < nb; i++) {
						ConversationID id = new ConversationID();
						// id.setOrigin(ka1);
						if (rand.nextInt(2) == 0) {
							id.setOrigin(ka1);
						} else {
							id.setOrigin(ka2);
						}

						Assert.assertEquals(id, id);
						for (ConversationID other : ids)
							Assert.assertNotEquals(id, other);
						try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
							try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
								SerializationTools.writeExternalizableAndSizable(oos, id, false);
								oos.flush();
							}
							
							try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())) {
								try (ObjectInputStream ois = new ObjectInputStream(bais)) {
									ConversationID deserializedID = (ConversationID) SerializationTools.readExternalizableAndSizable(ois, false);
									Assert.assertEquals(id, deserializedID);
									for (ConversationID other : ids)
										Assert.assertNotEquals(deserializedID, other);
								}
							}
						}
						ids.add(id);
					}

					ArrayList<ConversationID> interfacedToOtherIds = new ArrayList<>();

					for (ConversationID id : ids) {
						if (id.getOrigin() == null)
							continue;
						if (id.getOrigin().equals(ka2))
							continue;
						ConversationID interfacedToOther = id
								.getInterfacedConversationIDToDistantPeer(globaInterfacedIDs1, ka1, ka2);

						Assert.assertNotNull(interfacedToOther.getOrigin());
						Assert.assertEquals(interfacedToOther, interfacedToOther);
						for (ConversationID other : interfacedToOtherIds) {
							Assert.assertNotEquals(interfacedToOther, other);
						}
						try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
							try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
								SerializationTools.writeExternalizableAndSizable(oos, interfacedToOther, false);
							}
							try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())) {
								try (ObjectInputStream ois = new ObjectInputStream(bais)) {
									ConversationID deserializedID = (ConversationID) SerializationTools.readExternalizableAndSizable(ois, false);
									Assert.assertEquals(interfacedToOther, deserializedID);
									for (ConversationID other : interfacedToOtherIds)
										Assert.assertNotEquals(deserializedID, other);

								}
							}
						}
						interfacedToOtherIds.add(interfacedToOther);
					}
					ArrayList<ConversationID> interfacedByOtherIds = new ArrayList<>();
					for (ConversationID id : interfacedToOtherIds) {
						ConversationID interfacedByOther = id
								.getInterfacedConversationIDFromDistantPeer(globaInterfacedIDs2, ka2, ka1);

						Assert.assertNotNull("ka1?" + id.getOrigin().equals(ka1), interfacedByOther.getOrigin());
						Assert.assertEquals(interfacedByOther, interfacedByOther);
						for (ConversationID other : interfacedByOtherIds) {
							Assert.assertNotEquals(interfacedByOther, other);
						}
						try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
							try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
								SerializationTools.writeExternalizableAndSizable(oos, interfacedByOther, false);
							}
							try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())) {
								try (ObjectInputStream ois = new ObjectInputStream(bais)) {
									ConversationID deserializedID = (ConversationID) SerializationTools.readExternalizableAndSizable(ois, false);
									Assert.assertEquals(interfacedByOther, deserializedID);
									for (ConversationID other : interfacedByOtherIds)
										Assert.assertNotEquals(deserializedID, other);
								}
							}
						}
						interfacedByOtherIds.add(interfacedByOther);
					}
					ArrayList<ConversationID> interfacedFromOtherIds = new ArrayList<>();
					for (ConversationID id : interfacedByOtherIds) {
						ConversationID interfacedFromOther = id
								.getInterfacedConversationIDToDistantPeer(globaInterfacedIDs2, ka2, ka1);
						Assert.assertNotNull(interfacedFromOther.getOrigin());
						Assert.assertEquals(interfacedFromOther, interfacedFromOther);
						for (ConversationID other : interfacedFromOtherIds) {
							Assert.assertNotEquals(interfacedFromOther, other);
						}
						try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
							try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
								SerializationTools.writeExternalizableAndSizable(oos, interfacedFromOther, false);
							}
							try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())) {
								try (ObjectInputStream ois = new ObjectInputStream(bais)) {
									ConversationID deserializedID = (ConversationID) SerializationTools.readExternalizableAndSizable(ois, false);
									Assert.assertEquals(interfacedFromOther, deserializedID);
									for (ConversationID other : interfacedFromOtherIds)
										Assert.assertNotEquals(deserializedID, other);

								}
							}
						}
						interfacedFromOtherIds.add(interfacedFromOther);
					}
					ArrayList<ConversationID> interfacedByCurrentIds = new ArrayList<>();
					for (ConversationID id : interfacedFromOtherIds) {
						ConversationID interfacedByCurrent = id
								.getInterfacedConversationIDFromDistantPeer(globaInterfacedIDs1, ka1, ka2);

						Assert.assertNotNull(interfacedByCurrent.getOrigin());
						Assert.assertEquals(interfacedByCurrent, interfacedByCurrent);
						for (ConversationID other : interfacedByCurrentIds) {
							Assert.assertNotEquals(interfacedByCurrent, other);
						}
						try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
							try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
								SerializationTools.writeExternalizableAndSizable(oos, interfacedByCurrent, false);
							}
							try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())) {
								try (ObjectInputStream ois = new ObjectInputStream(bais)) {
									ConversationID deserializedID = (ConversationID) SerializationTools.readExternalizableAndSizable(ois, false);
									Assert.assertEquals(interfacedByCurrent, deserializedID);
									for (ConversationID other : interfacedByCurrentIds)
										Assert.assertNotEquals(deserializedID, other);
								}
							}
						}
						interfacedByCurrentIds.add(interfacedByCurrent);
					}

					ArrayList<BigDataTransferID> bigDataTransferID = new ArrayList<>();
					int i = 0;
					for (int index = 0; index < ids.size(); index++) {
						ConversationID idOriginal = ids.get(index);
						if (idOriginal.getOrigin() == null)
							continue;
						if (idOriginal.getOrigin().equals(ka2))
							continue;

						int nbFound = 0;
						Assert.assertEquals(interfacedByCurrentIds.get(i++), idOriginal);

						for (ConversationID id : interfacedByCurrentIds) {
							if (id.equals(idOriginal))
								nbFound++;
						}
						Assert.assertEquals(1, nbFound);
						BigDataTransferID bdtid = new BigDataTransferID(idOriginal, null);

						bigDataTransferID.add(bdtid);
						Assert.assertEquals(idOriginal, bdtid);
						Assert.assertEquals(bdtid, idOriginal);

					}

					Assert.assertFalse("size=" + globaInterfacedIDs1.size(), globaInterfacedIDs1.isEmpty());
					Assert.assertTrue("size=" + globaInterfacedIDs2.size(), globaInterfacedIDs2.isEmpty());

					ids = null;
					interfacedByCurrentIds = null;
					interfacedByOtherIds = null;
					interfacedFromOtherIds = null;
					interfacedToOtherIds = null;

					sleep(200);
					System.gc();
					System.gc();
					sleep(500);
					System.gc();
					sleep(200);
					System.gc();
					Assert.assertTrue("size=" + globaInterfacedIDs2.size(), globaInterfacedIDs2.isEmpty());

					bigDataTransferID = null;
					sleep(200);
					System.gc();
					System.gc();

					int cycles = 0;
					while ((!globaInterfacedIDs1.isEmpty() || !globaInterfacedIDs2.isEmpty()) && cycles++ < 30) {
						sleep(300);
						System.gc();
					}

					Assert.assertTrue("size=" + globaInterfacedIDs1.size(), globaInterfacedIDs1.isEmpty());
					Assert.assertTrue("size=" + globaInterfacedIDs2.size(), globaInterfacedIDs2.isEmpty());

				} catch (Exception e) {
					e.printStackTrace();
					Assert.fail();
				}
			}

		});

	}
}
