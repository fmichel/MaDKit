/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */
package madkit.kernel;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * This class represents the conversation ID to which a message belongs.
 * 
 * When a message is created, it is given an ID that will
 * be used to tag all the messages that will be created
 * for answering this message using {@link AbstractAgent#sendReply(Message, Message)} like methods.
 * Especially, if the answer is again used for replying, the ID
 * will be used again to tag this new answer, and so on.
 * 
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.4
 */
final public class ConversationID implements Serializable{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 4280603137316237711L;
	final static private AtomicInteger ID_COUNTER = new AtomicInteger();//TODO if many many ??
	final private int id ;
	private KernelAddress origin;

	ConversationID() {
		id = ID_COUNTER.getAndIncrement();
	}
	
	@Override
	public String toString() {
		return id+(origin == null ? "" : "-"+origin);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(origin != null){//obj necessarily comes from the network or is different, so origin should have been set priorly if there is a chance of equality
			final ConversationID ci = (ConversationID) obj;//no check is intentional
			return this.id == ci.id && origin.equals(ci.origin);
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
}