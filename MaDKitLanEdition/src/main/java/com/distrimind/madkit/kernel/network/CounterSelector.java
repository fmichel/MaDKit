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

import java.util.ArrayList;
import java.util.Iterator;

import com.distrimind.madkit.exceptions.PacketException;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocol;

/**
 * 
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.7
 *
 */
public class CounterSelector {
	private byte actualCounterID=0;
	private byte nextCounterID=1;
	private boolean goToNextCounter=false;
	private int lockedActualCounterID=0;
	private int lockedNextCounterID=0;
	private boolean counterIDJustChanged=false;
	private final ArrayList<PacketCounter> counters;
	private volatile boolean activated=false;
	CounterSelector(ConnectionProtocol<?> connectionProtocols)
	{
		counters=new ArrayList<>(connectionProtocols.sizeOfSubConnectionProtocols()+1);
		for (Iterator<ConnectionProtocol<?>> it = connectionProtocols.iterator();it.hasNext();)
			counters.add(it.next().getPacketCounter());
	}
	
	private void incrementCounters()
	{
		for (PacketCounter pc : counters)
			pc.incrementOtherCounters();
	}
	
	public boolean isActivated()
	{
		return activated;
	}
	
	public void setActivated()
	{
		synchronized(this)
		{
			activated=true;
		}
	}
	
	public byte getNewCounterID()
	{
		if (!activated)
			return -1;
		synchronized(this)
		{
			if (goToNextCounter)
			{
				++lockedNextCounterID;
				return nextCounterID;
			}
			else
			{

				incrementCounters();
				goToNextCounter=true;
				
				++lockedActualCounterID;
				return actualCounterID;
			}
		}
	}
	
	public void releaseCounterID(byte counterID) throws PacketException
	{
		synchronized(this)
		{
			if (counterID==actualCounterID)
			{
				if (lockedActualCounterID<=0)
					throw new PacketException();
				if (--lockedActualCounterID==0)
				{
					counterIDJustChanged=true;
					actualCounterID=nextCounterID++;
					if (actualCounterID==-1)
					{
						actualCounterID++;
						nextCounterID++;
					}
					lockedActualCounterID=lockedNextCounterID;
					lockedNextCounterID=0;
					goToNextCounter=false;
				}
			}
			else if (counterID==nextCounterID)
			{
				if (lockedNextCounterID<=0)
					throw new PacketException();
				if (!goToNextCounter)
					throw new PacketException();
				--lockedNextCounterID;
			}
			else if (counterID!=-1)
				throw new PacketException();
		}
	}
	
	public State getState(byte counterID) throws PacketException
	{
		synchronized(this)
		{
			if (!activated)
				return State.NOT_ACTIVATED;
			if (actualCounterID==counterID)
			{
				if (counterIDJustChanged)
				{
					counterIDJustChanged=false;
					return State.VALIDATE_NEXT_COUNTER;
				}
				else
					return State.KEEP_ACTUAL;
			}
			else if (nextCounterID==counterID)
				return State.TAKE_NEXT_COUNTER;
			else
				throw new PacketException();
		}
	}
	
	public enum State
	{
		NOT_ACTIVATED((byte)0),
		KEEP_ACTUAL((byte)1),
		TAKE_NEXT_COUNTER((byte)2),
		VALIDATE_NEXT_COUNTER((byte)3);
		
		private byte code;
		private State(byte code)
		{
			this.code=code;
		}
		
		byte getCode()
		{
			return code;
		}
		
		
	}
	
	public static State getCounterState(byte code)
	{
		for (State cs : State.values())
			if (cs.getCode()==code)
				return cs;
		return null;
	}
}
