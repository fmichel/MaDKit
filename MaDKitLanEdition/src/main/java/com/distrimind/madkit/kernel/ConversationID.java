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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.kernel.network.SystemMessage.Integrity;
import com.distrimind.madkit.util.ExternalizableAndSizable;
import com.distrimind.madkit.util.SerializationTools;

/**
 * 
 * This class represents the conversation ID to which a message belongs.
 * 
 * When a message is created, it is given an ID that will be used to tag all the
 * messages that will be created for answering this message using
 * {@link AbstractAgent#sendReply(Message, Message)} like methods. Especially,
 * if the answer is again used for replying, the ID will be used again to tag
 * this new answer, and so on.
 * 
 * 
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @version 2.0
 * @since MadKitLanEdition 1.0
 */
@SuppressWarnings("ExternalizableWithoutPublicNoArgConstructor")
public class ConversationID implements ExternalizableAndSizable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4280603137316237711L;
	final static private AtomicInteger ID_COUNTER = new AtomicInteger(
			(int) (Math.random() * (double) Integer.MAX_VALUE));// TODO if many many ??

	private int id;
	private volatile KernelAddress origin;

	ConversationID() {
		id = ID_COUNTER.getAndIncrement();
		origin = null;
	}

	protected int getID() {
		return id;
	}

	ConversationID(int id, KernelAddress origin) {
		this.id = id;
		this.origin = origin;
	}

	@SuppressWarnings("MethodDoesntCallSuperMethod")
	@Override
	public ConversationID clone() {
		return this;
	}

	@Override
	public String toString() {
		return id + (origin == null ? "" : origin.toString());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj instanceof ConversationID)) {// obj necessarily comes from the network or is different,
																// so origin should have been set priorly if there is a
																// chance of equality
			final ConversationID ci = (ConversationID) obj;// no check is intentional

			return this.getID() == ci.getID()
					&& ((getOrigin() == ci.getOrigin()) || (getOrigin() != null && getOrigin().equals(ci.getOrigin())));
		}

		return false;
	}

	void setOrigin(KernelAddress origin) {
		if (this.origin == null) {
			this.origin = origin;
		}
	}

	@Override
	public int hashCode() {
		return id;
	}

	public KernelAddress getOrigin() {
		return origin;
	}

	

	private static class OriginalID {
		final int originalID;
		private final AtomicInteger nbPointers;

		OriginalID(int originalID) {
			this(originalID, new AtomicInteger(0));
		}

		OriginalID(int originalID, AtomicInteger nbPointers) {
			this.originalID = originalID;
			this.nbPointers = nbPointers;
		}

		public void incrementPointerToThisOriginalID() {
			nbPointers.incrementAndGet();
		}

		public int getOriginalID() {
			// nbPointers.incrementAndGet();
			return originalID;
		}

		public AtomicInteger getNbPointers() {
			return nbPointers;
		}

		public boolean remove() {
			int val = nbPointers.decrementAndGet();
			if (val < 0)
				new IllegalAccessError().printStackTrace();
			return val <= 0;
		}
	}

	static class InterfacedIDs {
		private int id_counter;
		private final HashMap<Integer, OriginalID> original_ids = new HashMap<>();
		private final HashMap<Integer, OriginalID> distant_ids = new HashMap<>();

		InterfacedIDs() {
			id_counter = (int) (Math.random() * (double) Integer.MAX_VALUE);
		}

		private int getAndIncrementIDCounter() {
			if (id_counter++ == -1)
				return id_counter++;
			else
				return id_counter;
		}

		OriginalID getNewID(Integer original) {
			OriginalID res = distant_ids.get(original);
			if (res == null) {
				res = new OriginalID(getAndIncrementIDCounter());
				original_ids.put(res.originalID, new OriginalID(original, res.getNbPointers()));
				distant_ids.put(original, res);
			}
			res.incrementPointerToThisOriginalID();
			return res;
		}

		OriginalID getOriginalID(int distant_id) {
			return original_ids.get(distant_id);
		}

		void removeDistantID(Integer distantid) {
			OriginalID oi = original_ids.get(distantid);
			if (oi.remove())
				distant_ids.remove(original_ids.remove(distantid).originalID);
		}

		boolean isEmpty() {
			return original_ids.isEmpty();
		}

		/*int getOriginalIDsNumber() {
			return original_ids.size();
		}*/
	}

	private transient volatile Map<KernelAddress, InterfacedIDs> global_interfaced_ids = null;
	protected transient Map<KernelAddress, OriginalID> myInterfacedIDs = null;

	Map<KernelAddress, InterfacedIDs> getGlobalInterfacedIDs() {
		return this.global_interfaced_ids;
	}

	@SuppressWarnings({"SynchronizeOnNonFinalField", "deprecation"})
	@Override
	protected void finalize() {
		if (myInterfacedIDs != null) {
			if (global_interfaced_ids != null) {
				synchronized (global_interfaced_ids) {
					try {
						for (Map.Entry<KernelAddress, OriginalID> kpi : myInterfacedIDs.entrySet()) {
							InterfacedIDs i2 = global_interfaced_ids.get(kpi.getKey());
							i2.removeDistantID(kpi.getValue().originalID);
							if (i2.isEmpty()) {
								global_interfaced_ids.remove(kpi.getKey());
							}
						}
					} catch (Throwable e) {
						e.printStackTrace();
					}
					global_interfaced_ids = null;
				}
				myInterfacedIDs = null;
			}
		}
	}

	ConversationID getInterfacedConversationIDToDistantPeer(Map<KernelAddress, InterfacedIDs> global_interfaced_ids,
			KernelAddress currentKernelAddress, KernelAddress distantKernelAddress) {

		if (origin.equals(distantKernelAddress))
			return this;
		else if (origin.equals(currentKernelAddress)) {
			OriginalID distantid = null;
			if (myInterfacedIDs != null) {
				distantid = myInterfacedIDs.get(distantKernelAddress);
			}
			if (distantid == null) {
				if (myInterfacedIDs == null)
					myInterfacedIDs = Collections.synchronizedMap(new HashMap<KernelAddress, OriginalID>());
				//noinspection SynchronizationOnLocalVariableOrMethodParameter
				synchronized (global_interfaced_ids) {
					this.global_interfaced_ids = global_interfaced_ids;
					InterfacedIDs i = global_interfaced_ids.get(distantKernelAddress);
					if (i == null) {
						i = new InterfacedIDs();
						global_interfaced_ids.put(distantKernelAddress, i);
					}
					distantid = i.getNewID(this.id);
				}
				myInterfacedIDs.put(distantKernelAddress, distantid);
			}
			/*
			 * else { myInterfacedIDs.put(distantKernelAddress, distantid); }
			 */

			return new ConversationID(distantid.getOriginalID(), origin);
			/*
			 * ConversationID cid=new ConversationID(distantid.getOriginalID(), origin);
			 * cid.myInterfacedIDs=new HashMap<KernelAddress, ConversationID.OriginalID>();
			 * cid.myInterfacedIDs.put(distantKernelAddress, distantid);
			 * distantid.incrementPointerToThisOriginalID(); return cid;
			 */
		} else {
			return new ConversationID(0, null);

		}
	}

	@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
	ConversationID getInterfacedConversationIDFromDistantPeer(Map<KernelAddress, InterfacedIDs> global_interfaced_ids,
															  KernelAddress currentKernelAddress, KernelAddress distantKernelAddress) {
		if (origin == null) {
			return new ConversationID();
		} else if (origin.equals(distantKernelAddress)) {
			return this;
		} else if (origin.equals(currentKernelAddress)) {
			synchronized (global_interfaced_ids) {
				InterfacedIDs i = global_interfaced_ids.get(distantKernelAddress);
				if (i == null) {
					ConversationID c = new ConversationID();
					c.setOrigin(currentKernelAddress);
					return c;
				} else {
					OriginalID o = i.getOriginalID(id);
					if (o == null) {
						ConversationID c = new ConversationID();
						c.setOrigin(currentKernelAddress);
						return c;
					} else {
						// return new ConversationID(o.originalID, origin);
						ConversationID cid = new ConversationID(o.getOriginalID(), origin);
						cid.global_interfaced_ids = global_interfaced_ids;
						cid.myInterfacedIDs = Collections
								.synchronizedMap(new HashMap<KernelAddress, ConversationID.OriginalID>());
						cid.myInterfacedIDs.put(distantKernelAddress, i.getNewID(o.getOriginalID()));
						/*
						 * if (myInterfacedIDs==null) myInterfacedIDs=new HashMap<>();
						 * myInterfacedIDs.put(distantKernelAddress, i.getNewID(new
						 * Integer(o.getOriginalID())));
						 */
						return cid;
					}
				}
			}
		} else {
			return new ConversationID();
		}
	}

	@Override
	public int getInternalSerializedSize() {
		
		return 4+this.origin.getInternalSerializedSize();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(this.id);
		SerializationTools.writeExternalizableAndSizable(out, this.origin, true);
		
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.id=in.readInt();
		Object o=SerializationTools.readExternalizableAndSizable(in, true);
		if (o!=null && !(o instanceof KernelAddress))
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		this.origin=(KernelAddress)o;
	}
}
