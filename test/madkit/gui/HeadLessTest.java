/*
 * Copyright 2013 Fabien Michel
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.gui;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.1
 * @version 0.9
 * 
 */
import javax.swing.JFrame;

import madkit.kernel.AbstractAgent;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.JunitMadkit;
import madkit.testing.util.agent.NormalAgent;

import org.junit.Test;



public class HeadLessTest extends JunitMadkit {

	@Test
	public void testAA(){
		System.setProperty("java.awt.headless", "true");
		launchTest(new AbstractAgent(){
			@Override
			protected void activate() {
				super.activate();
			}
			@Override
			public void setupFrame(JFrame frame) {
				super.setupFrame(frame);
			}
		},ReturnCode.SUCCESS,true);
		pause(100);
		testAgent();
	}

	/**
	 * 
	 */
	@Test
	public void testAgent() {
		System.setProperty("java.awt.headless", "true");
		launchTest(new NormalAgent(){
			@Override
			protected void activate() {
				super.activate();
			}
			@Override
			public void setupFrame(JFrame frame) {
				super.setupFrame(frame);
			}
		},ReturnCode.SUCCESS,true);
		pause(100);
	}
}
