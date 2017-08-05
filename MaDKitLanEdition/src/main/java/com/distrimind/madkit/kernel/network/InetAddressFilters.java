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
package com.distrimind.madkit.kernel.network;

import java.net.InetAddress;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.distrimind.madkit.util.XMLObjectParser;
import com.distrimind.madkit.util.XMLUtilities;
import com.distrimind.util.properties.XMLProperties;

/**
 * Represents allowed and forbidden lan
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 * @see InetAddressFilter
 *
 */
public class InetAddressFilters extends XMLProperties {
	public InetAddressFilters() {
		super(new XMLObjectParser());
	}

	public InetAddressFilters(ArrayList<InetAddressFilter> allowFilters, ArrayList<InetAddressFilter> denyFilters) {
		this();
		this.allowFilters = allowFilters;
		this.denyFilters = denyFilters;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3686912008334236319L;

	/**
	 * Filters representing sub networks, and for which this connection protocol is
	 * valid
	 */
	protected ArrayList<InetAddressFilter> allowFilters = null;

	/**
	 * Filters representing sub networks, and for which this connection protocol
	 * cannot be valid
	 */
	protected ArrayList<InetAddressFilter> denyFilters = null;

	public void setAllowFilters(ArrayList<InetAddressFilter> allowFilters) {
		this.allowFilters = allowFilters;
	}

	public void setDenyFilters(ArrayList<InetAddressFilter> denyFilters) {
		this.denyFilters = denyFilters;
	}

	public void setAllowFilters(InetAddressFilter... allowFilters) {
		this.allowFilters = new ArrayList<>();
		for (InetAddressFilter iaf : allowFilters)
			this.allowFilters.add(iaf);
	}

	public void setDenyFilters(InetAddressFilter... denyFilters) {
		this.denyFilters = new ArrayList<>();
		for (InetAddressFilter iaf : denyFilters)
			this.denyFilters.add(iaf);
	}

	public void setAllowFilters(AbstractIP... allowFilters) {
		this.allowFilters = new ArrayList<>();
		for (AbstractIP aip : allowFilters)
			for (InetAddressFilter iaf : aip.getInetAddressFilters())
				this.allowFilters.add(iaf);
	}

	public void setDenyFilters(AbstractIP... denyFilters) {
		this.denyFilters = new ArrayList<>();
		for (AbstractIP aip : denyFilters)
			for (InetAddressFilter iaf : aip.getInetAddressFilters())
				this.denyFilters.add(iaf);
	}

	public ArrayList<InetAddressFilter> getAllowFilters() {
		return allowFilters;
	}

	public ArrayList<InetAddressFilter> getDenyFilters() {
		return denyFilters;
	}

	/**
	 * Tells if the filter accept the connection with the given parameters
	 * 
	 * @param _distant_inet_address
	 *            the distant inet address
	 * @param _local_port
	 *            the local port
	 * @return true if the filter accept the connection with the given parameters
	 */
	public boolean isConcernedBy(InetAddress _distant_inet_address, int _local_port) {
		if (denyFilters != null) {
			for (InetAddressFilter cpf : denyFilters) {
				if (cpf.isConcernedBy(_distant_inet_address, _local_port))
					return false;
			}
		}
		if (allowFilters == null)
			return true;
		for (InetAddressFilter cpf : allowFilters) {
			if (cpf.isConcernedBy(_distant_inet_address, _local_port))
				return true;
		}
		return false;
	}

	@Override
	public Node getRootNode(Document _document) {
		for (int i = 0; i < _document.getChildNodes().getLength(); i++) {
			Node n = _document.getChildNodes().item(i);
			if (n.getNodeName().equals(XMLUtilities.MDK))
				return n;
		}
		return null;
	}

	@Override
	public Node createOrGetRootNode(Document _document) {
		Node res = getRootNode(_document);
		if (res == null) {
			res = _document.createElement(XMLUtilities.MDK);
			_document.appendChild(res);
		}
		return res;
	}

}
