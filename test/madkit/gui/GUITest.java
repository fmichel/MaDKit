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
package madkit.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.swing.JFrame;

import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.9
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class GUITest extends JunitMadkit {

	@Test
	public void hasGUITest() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				AbstractAgent a = new AbstractAgent();
				assertEquals(ReturnCode.SUCCESS, launchAgent(a, true));
				assertTrue(a.hasGUI());
			}
		});
	}

	@Test
	public void setupFrameTest() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(ReturnCode.SUCCESS, launchAgent(new AbstractAgent(){
					private boolean ok = false;
					private JFrame f;
					@Override
					public void setupFrame(JFrame frame) {
						ok = true;
						f = frame;
					}
					@Override
					protected void activate() {
						assertTrue(ok);
						f.dispose();
					}
				}, true));
			}
		});
	}

}