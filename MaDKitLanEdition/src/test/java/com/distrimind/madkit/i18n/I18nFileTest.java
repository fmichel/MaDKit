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
package com.distrimind.madkit.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.Properties;

import org.junit.Test;

import com.distrimind.madkit.action.AgentAction;
import com.distrimind.madkit.action.GUIManagerAction;
import com.distrimind.madkit.action.KernelAction;
import com.distrimind.madkit.action.SchedulingAction;
import com.distrimind.madkit.kernel.AbstractAgent.ReturnCode;

public class I18nFileTest {

	@Test
	public final void testReturnCode() throws IOException {
		testFilePresenceAndContent(ReturnCode.class, "fr_FR");
	}

	@Test
	public final void testAgentAction() throws IOException {
		testFilePresenceAndContent(AgentAction.class, "fr_FR");
	}

	@Test
	public final void testKernelAction() throws IOException {
		testFilePresenceAndContent(KernelAction.class, "fr_FR");
	}

	@Test
	public final void testGUIManagerAction() throws IOException {
		testFilePresenceAndContent(GUIManagerAction.class, "fr_FR");
	}

	@Test
	public final void testSchedulingAction() throws IOException {
		testFilePresenceAndContent(SchedulingAction.class, "fr_FR");
	}

	public <E extends Enum<E>> void testFilePresenceAndContent(Class<E> e, String... languages) throws IOException {
		EnumSet<E> set = EnumSet.allOf(e);
		testKeys(e, set, "");
		for (String lang : languages) {
			testKeys(e, set, "_" + lang);
		}
	}

	/**
	 * @param e
	 * @param set
	 * @throws IOException
	 */
	private <E extends Enum<E>> void testKeys(Class<E> e, EnumSet<E> set, String lang) throws IOException {
		System.err.println("\n----------------testing " + e + lang);
		Properties defaultConfig = new Properties();

		InputStream is = Words.class.getResourceAsStream(e.getSimpleName() + lang + ".properties");
		if (is == null)
			throw new NullPointerException("is");
		System.out.println(e.getSimpleName());
		defaultConfig.load(is);
		assertNotNull(defaultConfig);
		assertEquals(set.size(), defaultConfig.size());
		for (E enum1 : set) {
			System.err.println(enum1.name());
			assertNotNull(defaultConfig.getProperty(enum1.name()));
		}
	}
}
