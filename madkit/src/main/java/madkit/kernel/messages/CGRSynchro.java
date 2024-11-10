
package madkit.kernel.messages;

import madkit.kernel.AgentAddress;
import madkit.messages.ObjectMessage;

/**
 * @author Fabien Michel
 * @version 0.9
 * @since MaDKit 5.0
 *
 */
public class CGRSynchro extends ObjectMessage<AgentAddress> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1125125814563126121L;

	public enum Code {
		CREATE_GROUP,
		REQUEST_ROLE,
		LEAVE_ROLE,
		LEAVE_GROUP
//		LEAVE_ORG		
		}
	
	private final Code code;

	/**
	 * @param code
	 * @param aa
	 */
	public CGRSynchro(final Code code, final AgentAddress aa) {
		super(aa);
		this.code = code;
	}

	/**
	 * @return the code
	 */
	public Code getCode() {
		return code;
	}

	@Override
	public String toString() {
		return super.toString()+"\n\t"+getCode()+" on "+getContent();
	}


}

class RequestRoleSecure extends ObjectMessage<Object>{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1661974372588706717L;
	private final AgentAddress	requester;
	private final String	roleName;

	public RequestRoleSecure(AgentAddress requester, String roleName, Object key) {
		super(key);
		this.requester = requester;
		this.roleName = roleName;
	}

	/**
	 * @return the requester
	 */
	AgentAddress getRequester() {
		return requester;
	}

	/**
	 * @return the roleName
	 */
	public String getRoleName() {
		return roleName;
	}
}