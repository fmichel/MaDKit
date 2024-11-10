
package madkit.kernel;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class represents the conversation ID to which a message belongs. When a
 * message is created, it is given an ID that will be used to tag all the
 * messages that will be created for answering this message using
 * {@link Agent#reply(Message, Message)} like methods. Especially, if the answer
 * is again used for replying, the ID will be used again to tag this new answer,
 * and so on.
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.4
 */
public final class ConversationID implements Serializable {

	private static final long serialVersionUID = 4280603137316237711L;
	private static final AtomicInteger ID_COUNTER = new AtomicInteger();
	private final int id;
	private KernelAddress origin;

	ConversationID() {
		id = ID_COUNTER.getAndIncrement();
	}

	@Override
	public String toString() {
		return id + (origin == null ? "" : "-" + origin);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (origin != null) {// obj necessarily comes from the network or is different, so origin should have
								// been set priorly if there is a chance
			// of equality
			final ConversationID ci = (ConversationID) obj;// no check is intentional
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