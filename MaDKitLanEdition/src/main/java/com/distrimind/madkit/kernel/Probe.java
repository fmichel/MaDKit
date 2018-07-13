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

import java.lang.reflect.Field;

import com.distrimind.madkit.simulation.probe.PropertyProbe;

/**
 * This class defines a watcher's generic probe. A probe is configured according
 * to a community, a group and a role.
 * 
 * @author Fabien Michel
 * @author Olivier Gutknecht
 * @author Jason Mahdjoub
 * @since MaDKitLanEdition 1.0
 * @version 6.0
 * @see Watcher
 * @see PropertyProbe
 * 
 */
public class Probe<A extends AbstractAgent> extends Overlooker<A> {

	/**
	 * Builds a new Probe on the given CGR location of the artificial society.
	 * Once created, it has to be added by a {@link Watcher} agent using the
	 * {@link Watcher#addProbe(Probe)} method.
	 * 
	 * This has the same effect than <code>#Probe(groups, roleName, true)</code>
	 * 
	 * @param groups the groups
	 * @param roleName the role name
	 * @see Watcher
	 */
	public Probe(final AbstractGroup groups, final String roleName) {
		super(groups, roleName);
	}

	/**
	 * Builds a new Probe on the given CGR location of the artificial society.
	 * Once created, it has to be added by a {@link Watcher} agent using the
	 * {@link Watcher#addProbe(Probe)} method.
	 * 
	 * @param groups the groups
	 * @param roleName the role name
	 * @param unique
	 *            Tells if the function {@link Overlooker#getCurrentAgentsList()}
	 *            must returns unique references.
	 * @see Watcher
	 */
	public Probe(final AbstractGroup groups, final String roleName, final boolean unique) {
		super(groups, roleName, unique);
	}

	/**
	 * Returns the agent's field named <code>fieldName</code>. This also works on
	 * <code>private</code> fields, even inherited ones.
	 * 
	 * @param agentClass
	 *            the targeted agent
	 * @param fieldName
	 *            the name of the field
	 * @return the agent's field named <code>fieldName</code>
	 * @throws NoSuchFieldException if the field was not found
	 */
	@SuppressWarnings("unchecked")
	public static Field findFieldOn(Class<? extends AbstractAgent> agentClass, final String fieldName)
			throws NoSuchFieldException {
		Field f;
		while (true) {
			try {
				f = agentClass.getDeclaredField(fieldName);
				if (f != null) {
					if (!f.isAccessible()) {// TODO seems to be always the case the first time
						f.setAccessible(true);
					}
					return f;
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				agentClass = (Class<? extends AbstractAgent>) agentClass.getSuperclass();// TODO not go further than A ?
				if (agentClass == AbstractAgent.class) {// TODO bench vs local variable or Object ?
					throw e;
				}
			}
		}
	}

}
