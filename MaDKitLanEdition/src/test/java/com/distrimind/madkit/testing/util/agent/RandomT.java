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
package com.distrimind.madkit.testing.util.agent;

import java.util.ArrayList;
import java.util.logging.Level;

import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.Agent;
import com.distrimind.madkit.kernel.KernelException;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.6
 * @version 0.9
 * 
 */
public class RandomT extends Agent {

	public static ArrayList<AbstractAgent> agents;
	public static boolean killingOn = false;

	public void activate() throws InterruptedException {
		super.activate();
		setLogLevel(Level.OFF);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see madkit.kernel.Agent#live()
	 */
	@Override
	public void liveCycle() throws InterruptedException {

		getLogger().setWarningLogLevel(Level.FINE);
		for (int i = 0; i < 100; i++) {
			if (logger != null) {
				logger.info("living");
			}
			pause((int) (Math.random() * 100));
			AbstractAgent a = agents.get((int) (Math.random() * agents.size()));
			if (a.getState().equals(State.NOT_LAUNCHED)) {
				ReturnCode res = launchAgent(a, Math.random() > .5 ? 0 : 1);
				// launchAgent(agents.get((int)
				// (Math.random()*agents.size())),Math.random()>.5?0:1);
				if (logger != null)
					logger.info("launching result is : " + res);
			}
			killSomebody();
		}
		this.killAgent(this);
	}

	private void killSomebody() {
		ReturnCode res;
		if (killingOn) {
			AbstractAgent a = agents.get((int) (Math.random() * agents.size()));
			if (a.getState().equals(State.LIVING)) {
				res = killAgent(a, Math.random() > .5 ? 0 : 1);
				if (logger != null)
					logger.info("kill on " + a.getName() + " result is : " + res);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.madkit.agentLifeCycle.LifeCycleTestAgent#end()
	 */
	@Override
	public void end() throws InterruptedException {
		super.end();
		for (int i = 0; i < 10; i++) {
			if (logger != null) {
				logger.info("dying");
			}
			try {
				pause((int) (Math.random() * 100));
				AbstractAgent a = agents.get((int) (Math.random() * agents.size()));
				if (a.getState().equals(State.NOT_LAUNCHED)) {
					ReturnCode res = launchAgent(a, Math.random() > .5 ? 0 : 1);
					// launchAgent(agents.get((int)
					// (Math.random()*agents.size())),Math.random()>.5?0:1);
					if (logger != null)
						logger.info("launching result is : " + res);
				}
				killSomebody();
			} catch (KernelException e) {
				System.err.println("kernel ex : " + getState() + " alive " + isAlive());
				e.printStackTrace();
			}
		}
	}
}
