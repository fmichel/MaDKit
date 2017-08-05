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

import static com.distrimind.madkit.kernel.JunitMadkit.GROUP;
import static com.distrimind.madkit.kernel.JunitMadkit.ROLE;

import java.util.ArrayList;
import java.util.List;

import com.distrimind.madkit.api.abstractAgent.LaunchAgentBucketWithRolesWithListTest;
import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.Role;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.13
 * @version 0.9
 * 
 */
public class SimulatedAgent extends AbstractAgent {

	private int privatePrimitiveField = 1;
	public double publicPrimitiveField = 2;
	@SuppressWarnings("unused")
	private Object objectField = new Object();
	private boolean activated = false;

	@Override
	protected void activate() {
		// setLogLevel(Level.ALL);
		bucketModeRequestRole(GROUP, ROLE, null);
		activated = true;
	}

	@SuppressWarnings("unused")
	private void launchAgentBucketWithRoles() {// used by reflection
		List<AbstractAgent> l = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			l.add(new SimulatedAgent());
		}
		launchAgentBucket(l, new Role(GROUP, ROLE));
		LaunchAgentBucketWithRolesWithListTest.testAgents(l);
	}

	@Override
	protected void end() {
		System.err.println("ening");
	}

	public void doIt() {
		if (logger != null)
			logger.info("doing it");
	}

	public boolean goneThroughActivate() {
		return activated;
	}

	/**
	 * @return the privatePrimitiveField
	 */
	public int getPrivatePrimitiveField() {
		return privatePrimitiveField;
	}

	/**
	 * @param privatePrimitiveField
	 *            the privatePrimitiveField to set
	 */
	public void setPrivatePrimitiveField(int privatePrimitiveField) {
		this.privatePrimitiveField = privatePrimitiveField;
	}
}
