
package madkit.messages;

import madkit.kernel.AgentAddress;
import madkit.kernel.Message;


/**
 * A filter that accepts message based on the sender's role.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.4
 * @version 0.9
 *
 */
public class SenderRoleFilter implements MessageFilter {
	
	
	private final String community, group, role;

	/**
	 * a new filter that acts according to the sender's CGR location.
	 * 
	 * @param community 
	 * @param group 
	 * @param role the role that the sender must have
	 */
	public SenderRoleFilter(final String community, final String group, final String role) {
		this.community = community;
		this.group = group;
		this.role = role;
	}

	@Override
	public boolean accept(final Message m) {
		final AgentAddress sender = m.getSender();
		return sender.getCommunity().equals(community) && sender.getGroup().equals(group) && sender.getRole().equals(role);
	}

}
