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
package madkit.message;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class describes a generic speech act message.
 * 
 * @author Ol. Gutknecht 10/03/98 original, revision 1.1 04/2002 J.Ferber
 * @version 1.1
 * @since MaDKit 1.0
 */

public class ActMessage extends madkit.kernel.Message {

    private static final long serialVersionUID = -2556927686645807800L;
    protected String action;
    protected final Hashtable<String, Object> fields;
    String content;

    /** Constructor for GenericMessage class */
    public ActMessage(String actiontype) {
	action = actiontype;
	fields = new Hashtable<>();
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

    public Enumeration<String> getKeys() {
	return fields.keys();
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