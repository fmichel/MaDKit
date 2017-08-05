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
package com.distrimind.madkit.boot.process;

import static org.junit.Assert.assertEquals;

import java.util.logging.Level;

import org.junit.Test;

import com.distrimind.madkit.action.GUIManagerAction;
import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.JunitMadkit;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.0.10
 * @since MadkitLanEdition 1.0
 * @version 1.0
 * 
 */

public class WrongArgTest extends JunitMadkit {

	@Test
	public void agentDoesNotExist() {
		addMadkitArgs("--launchAgents", "{fake.fake}", "--kernelLogLevel", Level.ALL.toString());
		launchTest(new AbstractAgent());
	}

	@Test
	public void wrongLaunchParameters() {
		addMadkitArgs("--launchAgents", "{" + AbstractAgent.class.getName() + "}" + ",fd,h", "kernelLogLevel",
				Level.OFF.toString());
		launchTest(new AbstractAgent());
	}

	@Test
	public void wrongParameters() {
		mkArgs.clear();
		addMadkitArgs("azdadad");
		launchTest(new AbstractAgent());
	}

	@Test
	public void notAnAgentClass() {
		addMadkitArgs("--launchAgents", "{" + Object.class.getName() + "}", "--kernelLogLevel", Level.OFF.toString());
		launchTest(new AbstractAgent());
	}

	@Test
	public void agentCannotBeInitialized() {
		addMadkitArgs("--launchAgents", "{" + GUIManagerAction.class.getName() + "}", "--kernelLogLevel",
				Level.OFF.toString());
		launchTest(new AbstractAgent());
	}

	@Test
	public void defaultLogLevels() {
		mkArgs = null;
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(Level.OFF, getMadkitConfig().kernelLogLevel);
				assertEquals(Level.OFF, getMadkitConfig().guiLogLevel);
				assertEquals(Level.INFO, getMadkitConfig().networkProperties.networkLogLevel);
				assertEquals(Level.INFO, getMadkitConfig().madkitLogLevel);
				assertEquals(Level.INFO, getMadkitConfig().agentLogLevel);
				assertEquals(Level.FINE, getMadkitConfig().warningLogLevel);
				assertEquals(Level.INFO, getMadkitConfig().madkitLogLevel);
			}
		});
	}
}
