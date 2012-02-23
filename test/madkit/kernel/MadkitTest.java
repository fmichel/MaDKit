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
package madkit.kernel;

import static org.junit.Assert.*;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.10
 * @version 0.9
 * 
 */
public class MadkitTest {

	static Madkit mk;

	// @Before
	// public void before() {
	// mk = new Madkit(null);
	// }

	/**
	 * 
	 */
	private void testMKlogLevelBoot(String MKLogLevel) {
		System.err.println("\n\n\n\n\n--------------------MK log level = " + MKLogLevel + "-------------------");
		String[] args = { LevelOption.madkitLogLevel.toString(), MKLogLevel };
		new Madkit(args);
		System.err.println("\n\n--------------------MK log level = " + MKLogLevel + "-------------------\n\n\n\n\n");
	}

	@Test
	public void nullArgs() {
		Madkit.main(null);
	}

	@Test
	public void buildConfigFromArgsTest() {
		Madkit m = new Madkit(null);
		String[] args = new String[1];
		args[0]="";
		assertTrue(m.buildConfigFromArgs(args).isEmpty());
		assertTrue(m.buildConfigFromArgs(null).isEmpty());
		args = new String[2];
		args[0]="";
		args[1]="";
		assertTrue(m.buildConfigFromArgs(args).isEmpty());
	}

	@Test
	public void mkLogLevelALL() {
		testMKlogLevelBoot("ALL");
	}

	@Test
	public void mkLogLevelFINEST() {
		testMKlogLevelBoot("FINEST");
	}

	@Test
	public void mkLogLevelOFF() {
		testMKlogLevelBoot("OFF");
	}

	@Test
	public void testOptionAutoLogDir() {
		// TODO
	}

	@Test
	public void defaultAgentLogging() throws InterruptedException {
		Madkit m = new Madkit(null);
		Thread.sleep(100);
		assertNull(m.getKernel().logger);
		String[] args = { BooleanOption.desktop.toString(), "false" };
		m = new Madkit(args);
		Thread.sleep(100);
		assertNull(m.getKernel().logger);
		String[] argss = { Madkit.Option.launchAgents.toString(), "madkit.kernel.Agent" };
		m = new Madkit(argss);
		Thread.sleep(100);
		assertNull(m.getKernel().logger);
	}

	@Test
	public void buildSessionTest() {
		String[] args = { BooleanOption.desktop.toString(), "false", "--kernelLogLevel", "ALL" };
		new Madkit(args);
	}

}
