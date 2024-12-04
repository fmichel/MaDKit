package madkit.messages;

import madkit.kernel.AgentAddress;
import madkit.kernel.Message;

/**
 * A filter that accepts message based on the sender's role.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.4
 * @version 6.0
 *
 */
public class SenderRoleFilter implements MessageFilter {

	private final String community;
	private final String group;
	private final String role;

	/**
	 * a new filter that acts according to the sender's CGR location.
	 * 
	 * @param community
	 * @param group
	 * @param role      the role that the sender must have
	 */
	public SenderRoleFilter(String community, String group, String role) {
		this.community = community;
		this.group = group;
		this.role = role;
	}

	/**
	 * Tests whether or not the specified message should be accepted. The message is
	 * accepted if the sender's community, group, and role match the specified
	 * values.
	 *
	 * @param m the message to be tested
	 * @return true if the message is accepted, false otherwise
	 */
	@Override
	public boolean accept(Message m) {
		AgentAddress sender = m.getSender();
		return sender.getCommunity().equals(community) && sender.getGroup().equals(group)
				&& sender.getRole().equals(role);
	}

}