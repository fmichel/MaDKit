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

import static org.junit.Assert.fail;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;

import org.junit.Test;

import com.distrimind.madkit.action.AgentAction;
import com.distrimind.madkit.action.GUIManagerAction;
import com.distrimind.madkit.action.GlobalAction;
import com.distrimind.madkit.action.KernelAction;
import com.distrimind.madkit.action.SchedulingAction;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 0.9
 * 
 */
public class BindsTest {

	static Map<Integer, String> keys = new HashMap<>();

	@Test
	public void KernelActionConflicts() {
		for (KernelAction ka : EnumSet.allOf(KernelAction.class)) {
			testKey(ka.getActionInfo().getKeyEvent(), ka.name());
		}
	}

	@Test
	public void AgentActionConflicts() {
		for (AgentAction ka : EnumSet.allOf(AgentAction.class)) {
			testKey(ka.getActionInfo().getKeyEvent(), ka.name());
		}
	}

	@Test
	public void GUIManagerActionConflicts() {
		for (GUIManagerAction ka : EnumSet.allOf(GUIManagerAction.class)) {
			testKey(ka.getActionInfo().getKeyEvent(), ka.name());
		}
	}

	@Test
	public void GlobalActionConflicts() throws IllegalArgumentException {
		for (Field f : GlobalAction.class.getDeclaredFields()) {
			try {
				final Object object = f.get(null);
				final Class<? extends Object> cl = object.getClass();
				if (Action.class.isAssignableFrom(cl)) {
					final Object key = ((Action) object).getValue(Action.MNEMONIC_KEY);
					if (key != null) {
						testKey(((Integer) key).intValue(), f.getName());
					}
				}
			} catch (IllegalAccessException e) {
			}
		}
	}

	/**
	 * @param i
	 * @param name
	 */
	private void testKey(int i, String name) {
		if (i != KeyEvent.VK_DOLLAR) {
			String e = keys.put(Integer.valueOf(i), name);
			if (e != null) {
				fail(name + " has same key (" + i + ") as " + e);
			}
		}
	}

	@Test
	public void SchedulingActionConflicts() {
		for (SchedulingAction ka : EnumSet.allOf(SchedulingAction.class)) {
			testKey(ka.getActionInfo().getKeyEvent(), ka.name());
		}
	}

}