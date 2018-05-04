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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.distrimind.madkit.util.SerializationTools;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class MultiGroupTest {
	@Test
	void testMultiGroup() {
		MultiGroup mg = new MultiGroup(new Group("C1", "G1", "G2"), new Group("C1", "G1", "G3"),
				new Group("C1", "G1", "G3", "G4"));
		Assert.assertTrue(mg.includes(new Group("C1", "G1", "G2")));
		Assert.assertFalse(mg.includes(new Group("C1", "G1", "G2", "G3")));
		Assert.assertFalse(mg.includes(new Group("C2", "G1", "G2")));
		Assert.assertTrue(mg.addGroup(new Group("C1", "G1", "G2", "G3")));
		Assert.assertTrue(mg.includes(new Group("C1", "G1", "G2", "G3")));
		mg.addGroup(new Group(true, "C1", "G1", "G3"));
		Assert.assertTrue(mg.includes(new Group("C1", "G1", "G3", "G5")));
		mg.addForbidenGroup(new Group("C1", "G1", "G3", "G5"));
		Assert.assertFalse(mg.includes(new Group("C1", "G1", "G3", "G5")));
		Assert.assertTrue(mg.includes(new Group("C1", "G1", "G3", "G6")));
		Assert.assertTrue(mg.getComplementary().includes(new Group("C1", "G1", "G3", "G5")));
		Assert.assertTrue(AbstractGroup.getUniverse().includes(mg.union(mg.getComplementary())));
		Assert.assertTrue(mg.union(mg.getComplementary()).includes(AbstractGroup.getUniverse()));
		Assert.assertEquals(mg.union(mg.getComplementary()), AbstractGroup.getUniverse());
		Assert.assertTrue(mg.union(new Group("C1", "G1", "G3", "G5")).includes(new Group("C1", "G1", "G3", "G5")));
		Assert.assertTrue(mg.intersect(new Group(true, "C1", "G1", "G3")).includes(new Group("C1", "G1", "G3", "G10")));
		Assert.assertTrue(mg.intersect(new Group(true, "C1", "G1", "G3")).includes(new Group("C1", "G1", "G3")));
		Assert.assertTrue(mg.includes(new Group("C1", "G1", "G2")));
		Assert.assertFalse(mg.intersect(new Group(true, "C1", "G1", "G3")).includes(new Group("C1", "G1", "G2")));
		// Assert.assertTrue(mg.intersect(mg.getComplementary()).isEmpty());//TODO
		new MultiGroup();
	}

	@Test(dataProvider = "getGroups", dependsOnMethods = "testMultiGroup")
	public void testCloneGroup(AbstractGroup... groups) {
		MultiGroup g = new MultiGroup(groups);
		MultiGroup g2 = g.clone();
		Assert.assertEquals(g, g2);
	}

	@Test(dataProvider = "getGroups", dependsOnMethods = "testMultiGroup")
	public void testGroupSerialization(AbstractGroup... groups) throws IOException, ClassNotFoundException {
		MultiGroup g = new MultiGroup(groups);

		byte[] array = null;

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
				SerializationTools.writeExternalizableAndSizable(oos, g, false);
			}
			array = baos.toByteArray();
		}

		MultiGroup g2 = null;
		try (ByteArrayInputStream bais = new ByteArrayInputStream(array)) {
			try (ObjectInputStream ois = new ObjectInputStream(bais)) {
				g2 = (MultiGroup) ois.readObject();
			}
		}

		Assert.assertEquals(g, g2);
	}

	@DataProvider(name = "getGroups", parallel = true)
	AbstractGroup[][] getGroups() {
		return new AbstractGroup[][] {
				{ new Group("C1", "G1", "G2"), new Group("C1", "G1", "G3"), new Group("C1", "G1", "G3", "G4"), },
				{ new Group("C1", "G1", "G2"), new Group("C1", "G1", "G3"), new Group("C2", "G1", "G3", "G4"), },
				{ new Group("C1", "G1", "G2"), new MultiGroup(new Group("C1", "G1", "G3"), new Group("C1", "G5", "G3")),
						new Group("C2", "G1", "G3", "G4"), } };
	}

}
