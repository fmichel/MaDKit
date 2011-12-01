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
package madkit.gui;

import static org.junit.Assert.assertTrue;

import java.awt.event.KeyEvent;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import madkit.action.AgentAction;
import madkit.action.GUIManagerAction;
import madkit.action.KernelAction;
import madkit.action.SchedulingAction;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
public class BindsTest {

	static Set<Integer> keys = new HashSet<Integer>();;
	
	@Test
	public void KernelActionConflicts() {
		for (KernelAction ka : EnumSet.allOf(KernelAction.class)) {
			int i = ka.getActionInfo().getKeyEvent();
			if(i != KeyEvent.VK_DOLLAR){
				assertTrue(keys.add(i));
			}
		}
	}

	@Test
	public void AgentActionConflicts() {
		for (AgentAction ka : EnumSet.allOf(AgentAction.class)) {
			int i = ka.getActionInfo().getKeyEvent();
			if(i != KeyEvent.VK_DOLLAR){
				assertTrue(keys.add(i));
			}
		}
	}

	@Test
	public void GUIManagerActionConflicts() {
		for (GUIManagerAction ka : EnumSet.allOf(GUIManagerAction.class)) {
			int i = ka.getActionInfo().getKeyEvent();
			if(i != KeyEvent.VK_DOLLAR){
				assertTrue(keys.add(i));
			}
		}
	}

	@Test
	public void SchedulingActionConflicts() {
		for (SchedulingAction ka : EnumSet.allOf(SchedulingAction.class)) {
			int i = ka.getActionInfo().getKeyEvent();
			if(i != KeyEvent.VK_DOLLAR){
				assertTrue(keys.add(i));
			}
		}
	}

}