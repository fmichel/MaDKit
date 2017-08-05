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

import com.distrimind.madkit.kernel.AgentFakeThread;
import com.distrimind.madkit.kernel.Message;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class DoItDuringLifeCycleAgentFakeThread extends AgentFakeThread {
	private boolean inActivate = false, inLive = false, inEnd = false;

	public DoItDuringLifeCycleAgentFakeThread(boolean inActivate, boolean inLive, boolean inEnd) {
		this.inActivate = inActivate;
		this.inLive = inLive;
		this.inEnd = inEnd;
	}

	@SuppressWarnings("unused")
	private void privateMethod() {
	}

	public DoItDuringLifeCycleAgentFakeThread(boolean inActivate, boolean inLive) {
		this(inActivate, inLive, false);
	}

	public DoItDuringLifeCycleAgentFakeThread(boolean inActivate) {
		this(inActivate, false, false);
	}

	public DoItDuringLifeCycleAgentFakeThread() {
		this(false, true, false);
	}

	@Override
	public String getName() {
		return super.getName() + (inActivate ? "-inActivate-" : "") + (inLive ? "-inLive-" : "")
				+ (inEnd ? "-inEnd-" : "");
	}

	@Override
	public void activate() throws InterruptedException {
		super.activate();
		if (inActivate) {
			if (logger != null)
				logger.info("Doing It in activate!!");
			doIt();
		}
	}

	@Override
	public void liveByStep(Message m) throws InterruptedException {
		if (inLive) {
			if (logger != null)
				logger.info("Doing It in live!!");
			doIt();
		}
		this.killAgent(this);
	}

	@Override
	public void end() throws InterruptedException {
		super.end();
		if (inEnd) {
			if (logger != null)
				logger.info("Doing It in end!!");
			doIt();
		}
	}

	@SuppressWarnings("unused")
	public void doIt() throws InterruptedException {
		if (logger != null)
			logger.info("\n\n\tDo it JOB DONE !!!!!! in " + getState() + "\n\n");
	}

}
