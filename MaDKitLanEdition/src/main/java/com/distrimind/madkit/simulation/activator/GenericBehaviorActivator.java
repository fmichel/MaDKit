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
package com.distrimind.madkit.simulation.activator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.AbstractGroup;
import com.distrimind.madkit.kernel.Activator;
import com.distrimind.madkit.kernel.Scheduler;
import com.distrimind.madkit.simulation.SimulationException;

/**
 * An activator that invokes a single method with no parameters on a group of
 * agents. This class encapsulates behavior invocation on MaDKit agents for
 * scheduler agents. This activator allows to call a particular Java method on
 * agents regardless of their actual class type as long as they extend
 * {@link AbstractAgent}. This has to be used by {@link Scheduler} subclasses to
 * create simulation applications.
 * 
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKitLanEdition 1.0
 * @version 2.0
 * 
 */
public class GenericBehaviorActivator<A extends AbstractAgent> extends Activator<A> {
	/**
	 * methods maps an agent class to its corresponding Method object for runtime
	 * invocation
	 */
	private final Map<Class<? extends A>, Method> methods;
	private final String methodName;
	// private Class<? extends A> cachedClass = null;

	/**
	 * Builds a new GenericBehaviorActivator on the given CGR location of the
	 * artificial society. Once created, it has to be added by a {@link Scheduler}
	 * agent using the {@link Scheduler#addActivator(Activator)} method. Once added,
	 * it could be used to trigger the behavior on all the agents which are at this
	 * CGR location, regardless of their class type as long as they extend
	 * {@link AbstractAgent}
	 * 
	 * This function has the same effect than
	 * <code>#GenericBehaviorActivator(groups, role, theBehaviorToActivate, true)</code>
	 * 
	 * 
	 * @param groups the groups
	 * @param role the role
	 * @param theBehaviorToActivate
	 *            name of the Java method which will be invoked
	 */
	public GenericBehaviorActivator(AbstractGroup groups, final String role, final String theBehaviorToActivate) {
		this(groups, role, theBehaviorToActivate, true);
	}

	/**
	 * Builds a new GenericBehaviorActivator on the given CGR location of the
	 * artificial society. Once created, it has to be added by a {@link Scheduler}
	 * agent using the {@link Scheduler#addActivator(Activator)} method. Once added,
	 * it could be used to trigger the behavior on all the agents which are at this
	 * CGR location, regardless of their class type as long as they extend
	 * {@link AbstractAgent}
	 * 
	 * 
	 * @param groups the groups
	 * @param role the role
	 * @param theBehaviorToActivate
	 *            name of the Java method which will be invoked
	 * @param unique
	 *            Tells if the function {@link #getCurrentAgentsList()}
	 *            must returns unique references.
	 */
	public GenericBehaviorActivator(AbstractGroup groups, final String role, final String theBehaviorToActivate,
			final boolean unique) {
		super(groups, role, unique);
		methods = new HashMap<>();
		methodName = theBehaviorToActivate;
	}

	public String getBehaviorName() {
		return methodName;
	}

	/**
	 * Triggers the corresponding behavior on all the agents which are at the CGR
	 * location defined by this activator.
	 * @param agents the agents
	 * @param args the arguments
	 * @see com.distrimind.madkit.kernel.Activator#execute(List, Object...)
	 */
	@Override
	public void execute(final List<A> agents, Object... args) {
		// local cache for multicore execute and avoiding adding collision
		Method cachedM = null;
		Class<? extends A> cachedC = null;
		for (final A a : agents) {
			if (a.isAlive()) {
				@SuppressWarnings("unchecked")
				final Class<? extends A> agentClass = (Class<? extends A>) a.getClass();
				if (agentClass != cachedC) {
					cachedC = agentClass;
					cachedM = methods.get(agentClass);
					if (cachedM == null) {
						try {
							cachedM = findMethodOn(agentClass, methodName);
						} catch (NoSuchMethodException e) {
							throw new SimulationException(toString(), e);
						}
						synchronized (methods) {
							methods.put(agentClass, cachedM);
						}
					}
				}
				try {
					Thread.currentThread().setName(a.getAgentThreadName(a.getState()));
					cachedM.invoke(a);
				} catch (IllegalAccessException e) {
					throw new SimulationException(toString(), e);
				} catch (InvocationTargetException e) {
					handleException(e.getCause(),
							new SimulationException(toString() + " on " + cachedM + " " + a, e.getCause()));
				}
			}
		}
	}
}
