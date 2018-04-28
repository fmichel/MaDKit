
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
import java.io.Serializable;

import com.distrimind.madkit.kernel.network.NetworkProperties;
import com.distrimind.madkit.util.NetworkMessage;

/**
 * This parameterizable class could be used to convey any Java Object between
 * MaDKit agents and through the network.
 * 
 * @author Jason Mahdjoub
 * @since MaDKitLanEdition 1.7.0
 * @version 1.0
 *
 */
public class NetworkObjectMessage<T extends Serializable> extends ObjectMessage<T> implements NetworkMessage {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5376280521192149943L;
	
	
	

	@Override
	public int getInternalSerializedSize() {
		return super.getInternalSerializedSizeImpl(NetworkProperties.GLOBAL_MAX_SHORT_DATA_SIZE);
	}	
	
	@Override
	protected void readAndCheckObject(final ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		super.readAndCheckObjectImpl(in, NetworkProperties.GLOBAL_MAX_SHORT_DATA_SIZE);
	}
	@Override
	protected void writeAndCheckObject(final ObjectOutputStream oos) throws IOException{
		super.writeAndCheckObjectImpl(oos, NetworkProperties.GLOBAL_MAX_SHORT_DATA_SIZE);
	}
	
	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		readAndCheckObject(in);
	}
	private void writeObject(final ObjectOutputStream oos) throws IOException
	{
		writeAndCheckObject(oos);
	}
	
	/**
	 * Builds a message with the specified content
	 * 
	 * @param content the message content
	 * @param excludeFromEncryption tells if this message can be excluded from the lan encryption process
	 */
	public NetworkObjectMessage(final T content, boolean excludeFromEncryption) {
		super(content, excludeFromEncryption);
	}


	


	
}