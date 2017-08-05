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

/**
 * Represent an agent to launch
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since version 1.0
 */
public class AgentToLaunch {
	private final Class<? extends AbstractAgent> classAgent;
	private final boolean withGUI;
	private final int number;

	public AgentToLaunch(Class<? extends AbstractAgent> _class_agent, boolean _withGUI, int _number) {
		classAgent = _class_agent;
		withGUI = _withGUI;
		number = _number;
	}

	public Class<? extends AbstractAgent> getClassAgent() {
		return classAgent;
	}

	public boolean isWithGUI() {
		return withGUI;
	}

	public int getNumber() {
		return number;
	}

	public static AgentToLaunch parse(String s) throws ClassNotFoundException {
		if (s.equals("null"))
			return null;
		String split[] = s.split(",");
		@SuppressWarnings("unchecked")
		Class<? extends AbstractAgent> classAgent = (Class<? extends AbstractAgent>) MadkitClassLoader.getLoader()
				.loadClass(split[0].trim());
		int number = 1;
		boolean withGUI = false;
		if (split.length > 1) {
			withGUI = Boolean.parseBoolean(split[1].trim());
			if (split.length > 2)
				number = Integer.parseInt(split[2].trim());
		}
		return new AgentToLaunch(classAgent, withGUI, number);
	}

	@Override
	public String toString() {
		return classAgent.getCanonicalName() + "," + withGUI + "," + number;
	}
}
