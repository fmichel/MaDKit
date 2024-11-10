
package madkit.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class describes a generic speech act message.
 * 
 * @author Ol. Gutknecht 10/03/98 original, revision 1.1 04/2002 J.Ferber
 * @version 1.2
 * @since MaDKit 1.0
 */

public class ActMessage extends madkit.kernel.Message {

    private static final long serialVersionUID = -2556927686645807800L;
    protected String action;
    protected final Map<String, Object> fields;
    String content;

    /** Constructor for GenericMessage class */
    public ActMessage(String actiontype) {
	action = actiontype;
	fields = new HashMap<>();
    }

    public ActMessage(String actiontype, String content) {
	this(actiontype);
	this.content = content;
    }

    public ActMessage(String actiontype, Object o) {
	this(actiontype);
	setObject(o);
    }

    public ActMessage(String actiontype, String content, Object o) {
	this(actiontype);
	this.content = content;
	setObject(o);
    }

    public String getAction() {
	return action;
    }

    public String getContent() {
	return content;
    }

    public void setContent(String s) {
	content = s;
    }

    public Object getObject() {
	return fields.get("object");
    }

    public void setObject(Object o) {
	fields.put("object", o);
    }

    public Set<String> getKeys() {
	return fields.keySet();
    }

    public void setField(String key, Object value) {
	fields.put(key, value);
    }

    public Object getFieldValue(String key) {
	return fields.get(key);
    }

    public String getInReplyTo() {
	return (String) getFieldValue(":in-reply-to");
    }

    public void setInReplyTo(String s) {
	setField(":in-reply-to", s);
    }

}