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
package com.distrimind.madkit.kernel.network;

import com.distrimind.madkit.exceptions.BlockParserException;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public abstract class SubBlockParser {
	
	
	public abstract SubBlockInfo getSubBlock(SubBlock _block) throws BlockParserException;

	public abstract SubBlock getParentBlock(SubBlock _block) throws BlockParserException;

	protected final SubBlock getParentBlockWithNoTreatments(SubBlock _block) throws BlockParserException {
		return new SubBlock(_block.getBytes(), _block.getOffset() - getSizeHead(),
				getBodyOutputSizeForEncryption(_block.getSize()) + getSizeHead());
		/*
		 * int outputSize=getBodyOutputSize(_block.getSize()); int
		 * additionalOffset=outputSize-_block.getSize(); if (additionalOffset==0) return
		 * new SubBlock(_block.getBytes(), _block.getOffset()-getSizeHead(),
		 * outputSize+getSizeHead()); else { byte[] bytes=new
		 * byte[_block.getBytes().length]; System.arraycopy(_block.getBytes(),
		 * _block.getOffset(), bytes, _block.getOffset()-additionalOffset,
		 * _block.getSize());
		 * 
		 * return new SubBlock(bytes, _block.getOffset()-getSizeHead()-additionalOffset,
		 * outputSize+getSizeHead()); }
		 */
	}

	public abstract int getSizeHead() throws BlockParserException;

	/**
	 * cannot change during process
	 * 
	 * @return the maximum size head
	 * @throws BlockParserException
	 */
	public abstract int getMaximumSizeHead() throws BlockParserException;

	// public int getSizeBlockModulus() throws BlockParserExcetion;
	public abstract int getBodyOutputSizeForEncryption(int size) throws BlockParserException;

	public abstract int getBodyOutputSizeForDecryption(int size) throws BlockParserException;
	
	public abstract SubBlockInfo checkEntrantPointToPointTransferedBlock(SubBlock _block) throws BlockParserException;
	
	public abstract SubBlock signIfPossibleSortantPointToPointTransferedBlock(SubBlock _block) throws BlockParserException;
}
