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
package com.distrimind.madkit.util;

import java.util.Map;
import java.util.Properties;

import com.distrimind.madkit.kernel.AgentToLaunch;
import com.distrimind.madkit.kernel.MadkitClassLoader;
import com.distrimind.madkit.kernel.network.InetAddressFilter;
import com.distrimind.madkit.kernel.network.KernelAddressPriority;
import com.distrimind.util.properties.AbstractXMLObjectParser;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class XMLObjectParser extends AbstractXMLObjectParser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6159286637228970729L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object convertXMLToObject(Class<?> field_type, String nodeValue) throws Exception {
		if (nodeValue == null)
			return null;
		nodeValue = nodeValue.trim();
		if (field_type == Class.class) {
			return MadkitClassLoader.getLoader().loadClass(nodeValue);
		} else if (field_type == AgentToLaunch.class) {
			return AgentToLaunch.parse(nodeValue);
		} else if (field_type == KernelAddressPriority.class) {
			return KernelAddressPriority.valueOf(nodeValue);
		} else if (field_type == InetAddressFilter.class) {
			return InetAddressFilter.parse(nodeValue);
		} else if (field_type == Properties.class) {
			Properties p = new Properties();
			String entries[] = nodeValue.split(";");
			for (String e : entries) {
				String keyvalue[] = e.split(" ");
				if (keyvalue.length == 2) {
					p.put(keyvalue[0], keyvalue[1]);
				}
			}
			return p;
		}
		return Void.TYPE;
	}

	@Override
	public String convertObjectToXML(Class<?> field_type, Object object) throws Exception {
		if (field_type == AgentToLaunch.class) {
			return object.toString();
		} else if (field_type == KernelAddressPriority.class) {
			return object.toString();
		} else if (field_type == Class.class) {
			return ((Class<?>) object).getCanonicalName();
		} else if (field_type == InetAddressFilter.class)
			return object.toString();
		else if (field_type == Properties.class) {
			if (object == null)
				return "";
			Properties p = (Properties) object;

			StringBuffer res = new StringBuffer();
			for (Map.Entry<Object, Object> e : p.entrySet()) {
				res.append(e.getKey().toString());
				res.append(" ");
				res.append(e.getValue());
				res.append(";");
			}
			return res.toString();

		} else
			return null;
	}

	@Override
	public boolean isValid(Class<?> _field_type) {
		return _field_type == AgentToLaunch.class || _field_type == KernelAddressPriority.class
				|| _field_type == InetAddressFilter.class || _field_type == Class.class
				|| _field_type == Properties.class;
	}

}
