/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
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
package com.distrimind.madkit.kernel.network.connection;


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.distrimind.madkit.exceptions.BlockParserException;
import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.kernel.network.SubBlock;
import com.distrimind.madkit.kernel.network.SubBlockInfo;
import com.distrimind.madkit.kernel.network.SystemMessage.Integrity;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocol.NullBlockChecker;
import com.distrimind.madkit.util.ExternalizableAndSizable;
import com.distrimind.madkit.util.SerializationTools;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public abstract class TransferedBlockChecker implements ExternalizableAndSizable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2143778142838474232L;

	private TransferedBlockChecker subChecker;
	protected TransferedBlockChecker()
	{
		
	}
	protected TransferedBlockChecker(TransferedBlockChecker subChecker, boolean supportSubBlock) {
		if (supportSubBlock)
			this.subChecker = subChecker;
		else
			this.subChecker = null;
	}

	public abstract SubBlockInfo checkSubBlock(SubBlock _block) throws BlockParserException;

	public SubBlockInfo recursiveCheckSubBlock(SubBlock _block) throws BlockParserException {
		SubBlockInfo res = checkSubBlock(_block);
		if (res.isValid() && subChecker != null) {
			{
				return subChecker.checkSubBlock(res.getSubBlock());
			}
		} else
		{
			return res;
		}
	}
	
	public boolean isCompletelyInoperant()
	{
		return (this instanceof NullBlockChecker) || (subChecker!=null && subChecker.isCompletelyInoperant()); 
	}

	//public abstract Integrity checkDataIntegrity();

	@Override
	public String toString() {
		return this.getClass().getName() + (subChecker == null ? "" : " with sub checker" + subChecker.toString());
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		Object o=SerializationTools.readExternalizableAndSizable(in, true);
		if (o!=null && !(o instanceof TransferedBlockChecker))
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		subChecker=(TransferedBlockChecker)o;
	}
	
	@Override
	public void writeExternal(ObjectOutput oos) throws IOException
	{
		SerializationTools.writeExternalizableAndSizable(oos, subChecker, true);
	}
	

}
