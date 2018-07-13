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
package com.distrimind.madkit.kernel.network.connection.access;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


import com.distrimind.madkit.kernel.AbstractGroup;
import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.madkit.kernel.MadkitProperties;
import com.distrimind.madkit.kernel.MultiGroup;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
@SuppressWarnings({"SameParameterValue", "BooleanMethodIsAlwaysInverted"})
public abstract class AbstractAccessProtocol {
	protected final AccessData access_data;
	protected final MadkitProperties properties;


	private AtomicReference<MultiGroup> groups_access = new AtomicReference<>();
	private boolean other_can_takes_initiative;

	private List<Identifier> identifiers = null;
	private ArrayList<PairOfIdentifiers> acceptedIdentifiers = null;
	private ArrayList<PairOfIdentifiers> deniedIdentifiers = null;
	protected final InetSocketAddress distant_inet_address;
	// private final InetSocketAddress local_interface_address;
	private KernelAddress kernel_address = null;
	
	private LinkedList<AccessMessage> differedAccessMessages = new LinkedList<>();
	private boolean accessFinalizedMessageReceived = false;

	public AbstractAccessProtocol(InetSocketAddress _distant_inet_address, InetSocketAddress _local_interface_address,
			LoginEventsTrigger loginTrigger,
			MadkitProperties _properties) throws AccessException {
		if (_distant_inet_address == null)
			throw new NullPointerException("_distant_inet_address");
		if (_local_interface_address == null)
			throw new NullPointerException("_local_interface_address");
		if (_properties == null)
			throw new NullPointerException("_properties");
		properties = _properties;
		access_data = properties.networkProperties.getAccessData(_distant_inet_address, _local_interface_address);
		if (access_data == null)
			throw new NullPointerException("No Access data was found into the MadkitProperties !");

		AbstractAccessProtocolProperties access_protocol_properties = properties.networkProperties.getAccessProtocolProperties(_distant_inet_address,_local_interface_address);
		if (access_protocol_properties == null)
			throw new NullPointerException("No AccessProtocolProperties was found into the MadkitProperties !");

		// maximum_rsa_message_size=access_protocol_properties.RSALoginKeySize/8-11;
		distant_inet_address = _distant_inet_address;
		access_protocol_properties.checkProperties();
		// this.accessGroupsNotifier=accessGroupsNotifier;
		if (access_data instanceof LoginData) {
			((LoginData) access_data).addTrigger(loginTrigger);
		}
	}
	protected void setGroupAccess(MultiGroup gp)
	{
		groups_access.set(gp);
	}
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (o.getClass()==this.getClass())
			return ((AbstractAccessProtocol) o).access_data.equals(access_data);
		return false;
	}

	public void setKernelAddress(KernelAddress ka) {
		kernel_address = ka;
	}
	
	protected KernelAddress getKernelAddress()
	{
		return kernel_address;
	}
	
	protected void reset()
	{
		identifiers=null;
		acceptedIdentifiers=null;
		deniedIdentifiers=null;
		groups_access.set(null);
	}


	public AccessMessage setAndGetNextMessage(AccessMessage _m) throws AccessException {
		if (_m instanceof AccessFinalizedMessage)
			accessFinalizedMessageReceived = true;
		
		return subSetAndGetNextMessage(_m);
		
	}

	public abstract AccessMessage subSetAndGetNextMessage(AccessMessage _m) throws AccessException;

	protected boolean differrAccessMessage(AccessMessage m) {
		if (((m instanceof NewLocalLoginAddedMessage) || (m instanceof NewLocalLoginRemovedMessage))) {

			differedAccessMessages.offer(m);
			return true;
		}
		return false;
	}

	public AccessMessage manageDifferedAccessMessage()
			throws AccessException {
		
			while (differedAccessMessages.size() != 0) {
				AccessMessage res = manageDifferableAccessMessage(differedAccessMessages.poll());
				if (res != null)
					return res;
			}

			return null;

	}

	protected abstract AccessMessage manageDifferableAccessMessage(AccessMessage _m) throws AccessException;

	/*
	 * private final LinkedList<LocalLogingAccessMessage>
	 * new_login_events_waiting=new LinkedList<>();
	 * 
	 * private void addNewLocalLoginEvent(LocalLogingAccessMessage loginEvent) {
	 * new_login_events_waiting.add(loginEvent); }
	 * 
	 * private LocalLogingAccessMessage popLocalLogingEvent() { if
	 * (new_login_events_waiting.size()==0) return null;
	 * 
	 * return new_login_events_waiting.removeFirst(); }
	 */

	//private KernelAddress distant_kernel_address;
	private ArrayList<PairOfIdentifiers> last_accepted_identifiers = new ArrayList<>();
	private ArrayList<PairOfIdentifiers> all_accepted_identifiers = new ArrayList<>();
	private ArrayList<PairOfIdentifiers> last_denied_identifiers_from_other = new ArrayList<>();
	private ArrayList<PairOfIdentifiers> last_unlogged_identifiers = new ArrayList<>();

	/*public KernelAddress getDistantKernelAddress() {
		return distant_kernel_address;
	}*/

	/*protected void setDistantKernelAddress(KernelAddress ka)
	{
		distant_kernel_address=ka;
	}*/
	
	protected void addLastAcceptedAndDeniedIdentifiers(ArrayList<PairOfIdentifiers> _accepted_identifiers,
			ArrayList<PairOfIdentifiers> _denied_identifiers) {
		last_accepted_identifiers.addAll(_accepted_identifiers);
		all_accepted_identifiers.addAll(_accepted_identifiers);
		last_denied_identifiers_from_other.addAll(_denied_identifiers);
	}

	protected UnlogMessage removeAcceptedIdentifiers(ArrayList<Identifier> _identifiers) {

		ArrayList<PairOfIdentifiers> toRemove = new ArrayList<>(_identifiers.size());
		ArrayList<Identifier> res = new ArrayList<>(_identifiers.size());
		for (Identifier id : _identifiers) {
			for (PairOfIdentifiers id2 : all_accepted_identifiers) {
				if (id.equals(id2.getLocalIdentifier())) {
					toRemove.add(id2);
					res.add(id2.getDistantIdentifier());
					break;
				}
			}
		}
		all_accepted_identifiers.removeAll(toRemove);
		last_accepted_identifiers.removeAll(toRemove);
		last_unlogged_identifiers.addAll(toRemove);
		return new UnlogMessage(res);
	}

	protected void updateGroupAccess() throws AccessException {

		AbstractGroup defaultGroup = access_data.getDefaultGroupsAccess();
		MultiGroup mg;
		if (defaultGroup == null)
			mg = new MultiGroup();
		else
			mg = new MultiGroup(defaultGroup);

		if (access_data instanceof LoginData) {
			LoginData lp = (LoginData) access_data;

			for (PairOfIdentifiers id : all_accepted_identifiers)
				mg.addGroup(lp.getGroupsAccess(id.getLocalIdentifier()));

		}
		groups_access.set(mg);

		notifyAccessGroupChangements = true;
	}

	public MultiGroup getGroupsAccess() {
		return groups_access.get();
	}

	// private final AccessGroupsNotifier accessGroupsNotifier;
	private boolean notifyAccessGroupChangements = false;

	public boolean isNotifyAccessGroupChangements() {
		boolean res = notifyAccessGroupChangements;
		notifyAccessGroupChangements = false;
		return res;
	}

	public ArrayList<PairOfIdentifiers> getLastDeniedIdentifiers() {
		ArrayList<PairOfIdentifiers> res = last_denied_identifiers_from_other;
		last_denied_identifiers_from_other = new ArrayList<>();
		return res;
	}

	public ArrayList<PairOfIdentifiers> getLastUnloggedIdentifiers() {
		ArrayList<PairOfIdentifiers> res = last_unlogged_identifiers;
		last_unlogged_identifiers = new ArrayList<>();
		return res;
	}

	public ArrayList<PairOfIdentifiers> getLastAcceptedIdentifiers() {
		ArrayList<PairOfIdentifiers> res = last_accepted_identifiers;
		last_accepted_identifiers = new ArrayList<>();
		return res;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<PairOfIdentifiers> getAllAcceptedIdentifiers() {
		return (ArrayList<PairOfIdentifiers>) all_accepted_identifiers.clone();
	}

	public abstract boolean isAccessFinalized();
	
	protected void setIdentifiers(List<Identifier> identifiers)
	{
		this.identifiers=identifiers;
	}
	protected List<Identifier> getIdentifiers()
	{
		return this.identifiers;
	}
	
	protected boolean isAccessFinalizedMessage()
	{
		return accessFinalizedMessageReceived;
	}

	protected boolean isOtherCanTakesInitiative() {
		return other_can_takes_initiative;
	}

	protected void setOtherCanTakesInitiative(boolean other_can_takes_initiative) {
		this.other_can_takes_initiative = other_can_takes_initiative;
	}

	protected ArrayList<PairOfIdentifiers> getAcceptedIdentifiers() {
		return acceptedIdentifiers;
	}

	protected void setAcceptedIdentifiers(ArrayList<PairOfIdentifiers> acceptedIdentifiers) {
		this.acceptedIdentifiers = acceptedIdentifiers;
	}

	protected ArrayList<PairOfIdentifiers> getDeniedIdentifiers() {
		return deniedIdentifiers;
	}

	protected void setDeniedIdentifiers(ArrayList<PairOfIdentifiers> deniedIdentifiers) {
		this.deniedIdentifiers = deniedIdentifiers;
	}
	
}
