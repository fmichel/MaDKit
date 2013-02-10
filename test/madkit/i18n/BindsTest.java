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
package madkit.i18n;

import static org.junit.Assert.fail;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;

import madkit.action.AgentAction;
import madkit.action.GUIManagerAction;
import madkit.action.GlobalAction;
import madkit.action.KernelAction;
import madkit.action.SchedulingAction;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 0.9
 * 
 */
public class BindsTest {

	static Map<Integer,String> keys = new HashMap<>();
	
	@Test
	public void KernelActionConflicts() {
		for (KernelAction ka : EnumSet.allOf(KernelAction.class)) {
			testKey(ka.getActionInfo().getKeyEvent(), ka.name());
		}
	}

	@Test
	public void AgentActionConflicts() {
		for (AgentAction ka : EnumSet.allOf(AgentAction.class)) {
			testKey(ka.getActionInfo().getKeyEvent(),ka.name());
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
						testKey((int) key, f.getName());
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
		if(i != KeyEvent.VK_DOLLAR){
			String e = keys.put(i, name);
			if(e != null){
				fail(name+" has same key as "+e);
			}
		}
	}

	@Test
	public void SchedulingActionConflicts() {
		for (SchedulingAction ka : EnumSet.allOf(SchedulingAction.class)) {
			testKey(ka.getActionInfo().getKeyEvent(),ka.name());
		}
	}

}