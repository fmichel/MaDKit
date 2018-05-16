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
package com.distrimind.madkit.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.kernel.MadkitProperties;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MadkitLanEdition 1.0
 * @version 1.0
 * 
 */
public class MadkitPropertiesTest {

	@Test
	public void testLoadPropertiesFromMaDKitXML() {
		try {
			final MadkitProperties madkitProperties = new MadkitProperties();
			madkitProperties.loadXML(new File("com/distrimind/madkit/xml/success.xml"));
			System.err.println(madkitProperties);
			assertEquals(madkitProperties.getFreeStringProperties().getProperty("test"), "yes");
			assertEquals(madkitProperties.getFreeStringProperties().getProperty("test2"), "good");
			assertEquals(Boolean.valueOf(madkitProperties.desktop), Boolean.valueOf(true));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testLoadPropertiesFromXMLPropertiesFile() {
		try {
			final MadkitProperties madkitProperties = new MadkitProperties();
			madkitProperties.loadXML(new File("com/distrimind/madkit/boot/process/madkit2.xml"));
			System.err.println(madkitProperties);
			assertEquals(new URL("http://madkitlanedition.test.com"), madkitProperties.madkitWeb);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testLoadPropertiesFromYAMLPropertiesFile() {
		try {
			final MadkitProperties madkitProperties = new MadkitProperties();
			madkitProperties.loadYAML(new File("com/distrimind/madkit/boot/process/madkit2.yaml"));
			System.err.println(madkitProperties);
			assertEquals(new URL("http://madkitlanedition.test.com"), madkitProperties.madkitWeb);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testLoadPropertiesFromFile() {
		try {
			new MadkitProperties().loadXML(new File("notExist"));
			JunitMadkit.noExceptionFailure();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The parsing is OK but the values are not
	 */
	@Test
	public void testLoadPropertiesFromPropertiesFileFailFromWrongFileContent() {
		try {
			final MadkitProperties madkitProperties = new MadkitProperties();
			madkitProperties.loadXML(new File("com/distrimind/madkit/xml/bench.xml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
