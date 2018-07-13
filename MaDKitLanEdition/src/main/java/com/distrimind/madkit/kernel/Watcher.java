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

package com.distrimind.madkit.kernel;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * This class defines a generic watcher agent. It holds a collection of probes
 * to explore agents' internal properties.
 * 
 * @author Fabien Michel
 * @author Olivier Gutknecht
 * @author Jason Mahdjoub
 * @since MaDKit 2.0
 * @since MadkitLanEdition 1.0
 * @version 5.1
 */
public class Watcher extends AbstractAgent {
	final private Set<Probe<? extends AbstractAgent>> probes = new LinkedHashSet<>();

	/**
	 * Adds the probe to the artificial organization so that it starts to probe the
	 * agents which are at the corresponding CGR location.
	 * 
	 * @param probe
	 *            the probe to add
	 */
	public void addProbe(final Probe<? extends AbstractAgent> probe) {
		if (kernel.addOverlooker(this, probe))
			probes.add(probe);
		if (logger != null && logger.isLoggable(Level.FINE))
			logger.fine("Probe added: " + probe);
	}

	/**
	 * Removes the probe from the artificial organization, thus stopping the probing
	 * activity.
	 * 
	 * @param probe
	 *            the probe to remove
	 */
	public void removeProbe(final Probe<? extends AbstractAgent> probe) {
		kernel.removeOverlooker(this, probe);
		probes.remove(probe);
	}

	@Override
	protected void activate() {
		if (logger != null)
			logger.talk(
					"\n\tHi human !\n\n I am an instance of the madkit.kernel.Watcher class.\n I am specialized in simulation probing.\n I use probes on the artificial society to analyze and\n visualize what is going on in a simulation.\n You can extend me to create your own\n simulation analyzing and visualizing tools !\n");
	}

	/**
	 * @see com.distrimind.madkit.kernel.AbstractAgent#terminate()
	 */
	@Override
	final void terminate() {
		removeAllProbes();
		super.terminate();
	}

	/**
	 * Remove all probes at once.
	 */
	public void removeAllProbes() {
		for (final Probe<? extends AbstractAgent> p : probes) {
			kernel.removeOverlooker(this, p);
		}
		probes.clear();
	}

	@SuppressWarnings("unchecked")
	public Probe<AbstractAgent>[] allProbes() {
		return probes.toArray(new Probe[0]);
		// return (Probe<AbstractAgent>[]) probes.toArray();
	}

	/**
	 * Returns the probes which have been successfully added
	 * 
	 * @return all the added probes
	 */
	public Set<Probe<? extends AbstractAgent>> getProbes() {
		return probes;
	}

	@Override
	public String toString() {
		return getName() + " " + Arrays.toString(allProbes());
	}

}