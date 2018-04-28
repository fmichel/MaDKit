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
import java.util.Arrays;

import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.kernel.network.SystemMessage.Integrity;
import com.distrimind.madkit.util.SerializationTools;
import com.distrimind.madkit.util.SerializableAndSizable;

/**
 * This parameterizable class could be used to build a message tagged with an
 * enumeration and conveying any java objects using an array of Object.
 * 
 * @author Fabien Michel
 * @version 5.0
 * @since MaDKit 5.0.0.14
 *
 */
public class EnumMessage<E extends Enum<E>> extends ObjectMessage<Object[]> implements SerializableAndSizable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2129358510239154730L;
	private static final int MAX_PARAMETERS_LENGTH=1000;
	private E code;

	@Override
	public int getInternalSerializedSize() {
		return super.getInternalSerializedSizeImpl(MAX_PARAMETERS_LENGTH)+(code==null?1:(code.name().length()*2+5+code.getClass().getName().length()*2));
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	protected void readAndCheckObject(final ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		super.readAndCheckObjectImpl(in, MAX_PARAMETERS_LENGTH);
		try
		{
			code=(E)SerializationTools.readEnum(in, true);
		}
		catch(Exception e)
		{
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN, e);
		}
		
		
	}
	@Override
	protected void writeAndCheckObject(final ObjectOutputStream oos) throws IOException{
		super.writeAndCheckObjectImpl(oos, MAX_PARAMETERS_LENGTH);
		SerializationTools.writeEnum(oos, code, true);
		
	}
	
	
	/**
	 * Builds a message with the specified content
	 * 
	 * @param code
	 *            an enum constant of type E
	 * @param parameters
	 *            a list of objects
	 */
	public EnumMessage(E code, final Object... parameters) {
		super(parameters);
		this.code = code;
	}

	@Override
	public String toString() {
		String s = super.toString() + "\n" + (getClass().getSimpleName() + getConversationID()).replaceAll(".", " ");
		return s + "    command: " + code.name() + " {" + Arrays.deepToString(getContent()) + "}";
	}

	/**
	 * @return the enum constant which has been used to construct this message
	 */
	public E getCode() {
		return code;
	}
}