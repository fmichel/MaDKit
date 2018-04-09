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
package com.distrimind.madkit.kernel.network.connection.secured;

import org.bouncycastle.util.Arrays;

import com.distrimind.madkit.kernel.network.CounterSelector.State;
import com.distrimind.madkit.kernel.network.PacketCounter;
import com.distrimind.util.Bits;
import com.distrimind.util.crypto.AbstractSecureRandom;

/**
 * 
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.7
 *
 */
class PacketCounterForEncryptionAndSignature implements PacketCounter {
	private final byte[] myEncryptionCounter;
	private final byte[] mySignatureCounter;
	private byte[] otherEncryptionCounter;
	private byte[] otherSignatureCounter;
	private byte[] myNextEncryptionCounter;
	private byte[] myNextSignatureCounter;
	private static short ENCRYPTION_COUNTER_SIZE_BYTES=2;
	private static short SIGNATURE_COUNTER_SIZE_BYTES=16;
	private boolean nextMyCounterSelected=false;
	PacketCounterForEncryptionAndSignature(AbstractSecureRandom random, boolean encryptionEnabled, boolean signatureEnabled)
	{
		if (encryptionEnabled)
		{
			myEncryptionCounter=new byte[ENCRYPTION_COUNTER_SIZE_BYTES];
			random.nextBytes(myEncryptionCounter);
			this.myNextEncryptionCounter=Arrays.clone(this.myEncryptionCounter);
			incrementCounter(this.myNextEncryptionCounter);
		}
		else 
		{
			myEncryptionCounter=null;
			this.myNextEncryptionCounter=null;
		}
		if (signatureEnabled)
		{
			mySignatureCounter=new byte[SIGNATURE_COUNTER_SIZE_BYTES];
			random.nextBytes(mySignatureCounter);
			this.myNextSignatureCounter=Arrays.clone(this.mySignatureCounter);
			incrementCounter(this.myNextSignatureCounter);
		}
		else
		{
			mySignatureCounter=null;
			myNextSignatureCounter=null;
		}
		
		
		
		
		this.otherEncryptionCounter=null;
		this.otherSignatureCounter=null;
	}
	
	byte[] getMyEncodedCounters()
	{
		if (myEncryptionCounter==null && mySignatureCounter==null)
			return new byte[0];
		if (myEncryptionCounter==null)
			return mySignatureCounter;
		if (mySignatureCounter==null)
			return myEncryptionCounter;
		
		return Bits.concateEncodingWithShortSizedTabs(myEncryptionCounter, mySignatureCounter);
	}
	
	boolean setDistantCounters(byte[] counters)
	{
		if (counters==null)
			return false;
		try
		{
			if (myEncryptionCounter==null && mySignatureCounter==null)
				return true;
			if (myEncryptionCounter==null)
			{
				if (counters==null || counters.length!=SIGNATURE_COUNTER_SIZE_BYTES)
					return false;
				otherSignatureCounter=counters;
			}
			else if (mySignatureCounter==null)
			{
				if (counters==null || counters.length!=ENCRYPTION_COUNTER_SIZE_BYTES)
					return false;
				otherEncryptionCounter=counters;
			}
			else
			{
				byte tab[][]=Bits.separateEncodingsWithShortSizedTabs(counters);
				if (tab==null || tab.length!=2 || tab[0]==null || tab[1]==null || tab[0].length!=ENCRYPTION_COUNTER_SIZE_BYTES || tab[1].length!=SIGNATURE_COUNTER_SIZE_BYTES)
					return false;
				this.otherEncryptionCounter=tab[0];
				this.otherSignatureCounter=tab[1];
			}
			
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	@Override
	public void incrementMyCounters() {
		if (myEncryptionCounter!=null)
		{
			incrementCounter(myEncryptionCounter);
			incrementCounter(myNextEncryptionCounter);
		}
		if (mySignatureCounter!=null)
		{
			incrementCounter(mySignatureCounter);
			incrementCounter(myNextSignatureCounter);
		}
	}
	
	private void incrementCounter(byte[] counter)
	{
		for (int i=0;i<counter.length;i++)
		{
			if (++counter[i]!=0)
				return;
		}
	}

	@Override
	public void incrementOtherCounters() {
		if (myEncryptionCounter!=null)
			incrementCounter(otherEncryptionCounter);
		if (mySignatureCounter!=null)
			incrementCounter(otherSignatureCounter);
		
	}

	public byte[] getMyEncryptionCounter() {
		if (nextMyCounterSelected)
			return myNextEncryptionCounter;
		else
			return myEncryptionCounter;
		
	}

	public byte[] getMySignatureCounter() {
		if (nextMyCounterSelected)
			return myNextSignatureCounter;
		else 
			return mySignatureCounter;
		
	}

	public byte[] getOtherEncryptionCounter() {
		return otherEncryptionCounter;
	
	}

	public byte[] getOtherSignatureCounter() {
		return otherSignatureCounter;
	}

	@Override
	public void selectMyCounters(State state) {
		if (state==State.KEEP_ACTUAL)
			nextMyCounterSelected=false;
		else if (state==State.TAKE_NEXT_COUNTER)
			nextMyCounterSelected=true;
		else if (state==State.VALIDATE_NEXT_COUNTER_AND_TAKE_ACTUAL)
		{
			nextMyCounterSelected=false;
			incrementMyCounters();
		}
		else if (state==State.VALIDATE_NEXT_COUNTER_AND_TAKE_NEXT)
		{
			nextMyCounterSelected=true;
			incrementMyCounters();
		}
		
		
	}
	
	
}
