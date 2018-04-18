/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
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
package com.distrimind.madkit.message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.kernel.network.NetworkProperties;
import com.distrimind.madkit.kernel.network.SystemMessage.Integrity;
import com.distrimind.madkit.util.OOSUtils;
import com.distrimind.madkit.util.SerializableAndSizable;


/**
 * This class describes a generic speech act message.
 * 
 * @author Ol. Gutknecht 10/03/98 original, revision 1.1 04/2002 J.Ferber
 * @author Jason Mahdjoub
 * @version 1.2
 * @since MaDKit 1.0
 */

public class ActMessage extends com.distrimind.madkit.kernel.Message implements SerializableAndSizable {

	private static final long serialVersionUID = -2556927686645807800L;
	static final int MAX_ACTION_LENGTH=Short.MAX_VALUE;
	static final int MAX_FIELD_LENGTH=8192;
	static final int MAX_STRING_VALUE_LENGTH=Short.MAX_VALUE;
	static final int MAX_STRING_CONTENT_LENGTH=Short.MAX_VALUE*10;
	
	protected String action;
	protected Hashtable<String, Object> fields;
	private boolean excludeFromEncryption;
	String content;

	@Override
	public int getInternalSerializedSize() {
		int res=super.getInternalSerializedSizeImpl()+OOSUtils.getInternalSize(action, MAX_ACTION_LENGTH)+5+OOSUtils.getInternalSize(content, MAX_STRING_CONTENT_LENGTH);
		for (Map.Entry<String, Object> e : fields.entrySet())
		{
			res+=OOSUtils.getInternalSize(e.getKey(), MAX_FIELD_LENGTH)+OOSUtils.getInternalSize(e.getValue(), MAX_STRING_VALUE_LENGTH);
		}
		return res;
	}
	
	@Override
	protected void readAndCheckObject(final ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		super.readAndCheckObjectImpl(in);
		int globalSize=NetworkProperties.GLOBAL_MAX_SHORT_DATA_SIZE;
		int totalSize=super.getInternalSerializedSizeImpl();
		if (totalSize>globalSize)
			throw new MessageSerializationException(Integrity.FAIL);
		action=OOSUtils.readString(in, MAX_ACTION_LENGTH, true);
		totalSize+=OOSUtils.getInternalSize(action, MAX_ACTION_LENGTH);
		if (totalSize>globalSize)
			throw new MessageSerializationException(Integrity.FAIL);
		int size=in.readInt();
		totalSize+=4;
		if (totalSize>globalSize)
			throw new MessageSerializationException(Integrity.FAIL);
		if (size<0)
			throw new MessageSerializationException(Integrity.FAIL);
		fields=new Hashtable<>();
		for (int i=0;i<size;i++)
		{
			String k=OOSUtils.readString(in, MAX_FIELD_LENGTH, true);
			Object v=OOSUtils.readObject(in, MAX_STRING_VALUE_LENGTH, true);
			totalSize+=OOSUtils.getInternalSize(k, MAX_FIELD_LENGTH)+OOSUtils.getInternalSize(v, MAX_STRING_VALUE_LENGTH);
			fields.put(k, v);
			if (totalSize>globalSize)
				throw new MessageSerializationException(Integrity.FAIL);
		}
		excludeFromEncryption=in.readBoolean();
		totalSize+=1;
		content=OOSUtils.readString(in, MAX_STRING_CONTENT_LENGTH, true);
		totalSize+=OOSUtils.getInternalSize(content, MAX_STRING_CONTENT_LENGTH);
		if (totalSize>globalSize)
			throw new MessageSerializationException(Integrity.FAIL);
	}
	@Override
	protected void writeAndCheckObject(final ObjectOutputStream oos) throws IOException{
		super.writeAndCheckObjectImpl(oos);
		OOSUtils.writeString(oos, action, MAX_ACTION_LENGTH, true);
		oos.writeInt(fields.size());
		for (Map.Entry<String, Object> e : fields.entrySet())
		{
			OOSUtils.writeString(oos, e.getKey(), MAX_FIELD_LENGTH, true);
			OOSUtils.writeObject(oos, e.getValue(), MAX_STRING_VALUE_LENGTH, true);
		}
		oos.writeBoolean(excludeFromEncryption);
		OOSUtils.writeString(oos, content, MAX_STRING_CONTENT_LENGTH, true);
	}
	
	
	/** Constructor for GenericMessage class
	 * @param actiontype the action type
	 *  */
	public ActMessage(String actiontype) {
		action = actiontype;
		fields = new Hashtable<>();
		excludeFromEncryption=false;
	}

	public ActMessage(String actiontype, String content) {
		this(actiontype);
		this.content = content;
	}

	public ActMessage(String actiontype, Object o) {
		this(actiontype);
		setObject(o);
	}

	public ActMessage(String actiontype, String content, Object o, boolean excludeFromEncryption) {
		this(actiontype);
		this.content = content;
		this.excludeFromEncryption=excludeFromEncryption;
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

	@Override
	public boolean excludedFromEncryption() {
		return excludeFromEncryption;
	}
	
	

}