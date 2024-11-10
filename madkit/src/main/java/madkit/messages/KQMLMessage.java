
package madkit.messages;

/**
 * This class describes a KQML message. It provides accessors for all
 * reserved fields defined in the KQML Specification.
 * Note that the :receiver and :sender are automatically mapped to the
 * MaDKit AgentAddress.
 * 
 * @author Olivier Gutknecht
 * @version MaDKit 1.0
 */

public class KQMLMessage extends ActMessage {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -3534390128191295863L;

	/** Constructor for KQMLMessage class */
	public KQMLMessage(String performative) {
		super(performative);
	}

	public String getPerformative() {
		return action;
	}

	public String getForce() {
		return (String) getFieldValue("force");
	}

	public void setForce(String s) {
		fields.put("force", s);
	}

	public String getReplyWith() {
		return (String) getFieldValue("reply-with");
	}

	public void setReplyWith(String s) {
		fields.put("reply-with", s);
	}

	@Override
	public String getInReplyTo() {
		return (String) getFieldValue("in-reply-to");
	}

	@Override
	public void setInReplyTo(String s) {
		fields.put("in-reply-to", s);
	}

	public String getLanguage() {
		return (String) getFieldValue("language");
	}

	public void setLanguage(String s) {
		fields.put("language", s);
	}

	public String getOntology() {
		return (String) getFieldValue("ontology");
	}

	public void setOntology(String s) {
		fields.put("ontology", s);
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("(" + getPerformative());
		for (String field : fields.keySet()){
			buffer.append(" :" + field + " ");
			buffer.append(getFieldValue(field));
		}
		if (getSender() != null)
			buffer.append(" :sender \"" + getSender() + "\"");
		if (getReceiver() != null)
			buffer.append(" :receiver \"" + getReceiver() + "\"");

		buffer.append(")");
		return new String(buffer);
	}

}
