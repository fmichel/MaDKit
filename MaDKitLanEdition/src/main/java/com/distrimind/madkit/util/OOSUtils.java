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
package com.distrimind.madkit.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.kernel.network.SystemMessage.Integrity;
import com.distrimind.util.sizeof.ObjectSizer;

/**
 * 
 * @author Jason Mahdjoub
 * @since MaDKitLanEdition 1.7
 * @version 1.0
 * 
 */

public class OOSUtils {
	private static final int MAX_CHAR_BUFFER_SIZE=Short.MAX_VALUE*5;
	
	public static void writeString(final ObjectOutputStream oos, String s, int sizeMax, boolean supportNull) throws IOException
	{
		if (s==null)
		{
			if (!supportNull)
				throw new IOException();
			if (sizeMax>Short.MAX_VALUE)
				oos.writeInt(-1);
			else
				oos.writeShort(-1);
			return;
			
		}
			
		if (s.length()>sizeMax)
			throw new IOException();
		if (sizeMax>Short.MAX_VALUE)
			oos.writeInt(s.length());
		else
			oos.writeShort(s.length());
		oos.writeChars(s);
	}
	private static final Object stringLocker=new Object();
	
	private static char[] chars=null;
	
	public static String readString(final ObjectInputStream ois, int sizeMax, boolean supportNull) throws IOException
	{
		int size;
		if (sizeMax>Short.MAX_VALUE)
			size=ois.readInt();
		else
			size=ois.readShort();
		if (size==-1)
		{
			if (!supportNull)
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			return null;
		}
		if (size<0 || size>sizeMax)
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		if (sizeMax<MAX_CHAR_BUFFER_SIZE)
		{
			synchronized(stringLocker)
			{
				if (chars==null || chars.length<sizeMax)
					chars=new char[sizeMax];
				for (int i=0;i<size;i++)
					chars[i]=ois.readChar();
				return new String(chars, 0, size);
			}
		}
		else
		{
			char []chars=new char[sizeMax];
			for (int i=0;i<size;i++)
				chars[i]=ois.readChar();
			return new String(chars, 0, size);
			
		}
	}
	
	public static void writeBytes(final ObjectOutputStream oos, byte tab[], int sizeMax, boolean supportNull) throws IOException
	{
		writeBytes(oos, tab, 0, tab==null?0:tab.length, sizeMax, supportNull);
	}
	public static void writeBytes(final ObjectOutputStream oos, byte tab[], int off, int size, int sizeMax, boolean supportNull) throws IOException
	{
		if (tab==null)
		{
			if (!supportNull)
				throw new IOException();
			if (sizeMax>Short.MAX_VALUE)
				oos.writeInt(-1);
			else
				oos.writeShort(-1);
			return;
			
		}
		if (tab.length>sizeMax)
			throw new IOException();
		if (sizeMax>Short.MAX_VALUE)
			oos.writeInt(tab.length);
		else
			oos.writeShort(tab.length);
		oos.write(tab, off, size);
	}
	
	public static byte[] readBytes(final ObjectInputStream ois, int sizeMax, boolean supportNull) throws IOException
	{
		int size;
		if (sizeMax>Short.MAX_VALUE)
			size=ois.readInt();
		else
			size=ois.readShort();
		if (size==-1)
		{
			if (!supportNull)
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			return null;
		}
		if (size<0 || size>sizeMax)
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		
		byte []tab=new byte[size];
		if (ois.read(tab)!=size)
			throw new IOException();
		
		
		return tab;
		
	}
	
	public static void writeObjects(final ObjectOutputStream oos, Object tab[], int sizeMax, boolean supportNull) throws IOException
	{
		if (tab==null)
		{
			if (!supportNull)
				throw new IOException();
			if (sizeMax>Short.MAX_VALUE)
				oos.writeInt(-1);
			else
				oos.writeShort(-1);
			return;
			
		}
		if (tab.length>sizeMax)
			throw new IOException();
		if (sizeMax>Short.MAX_VALUE)
			oos.writeInt(tab.length);
		else
			oos.writeShort(tab.length);
		sizeMax-=tab.length;
		for (Object o : tab)
		{
			writeObject(oos, o, sizeMax, true);
		}
	}
	
	public static Object[] readObjects(final ObjectInputStream ois, int sizeMax, boolean supportNull) throws IOException, ClassNotFoundException
	{
		int size;
		if (sizeMax>Short.MAX_VALUE)
			size=ois.readInt();
		else
			size=ois.readShort();
		if (size==-1)
		{
			if (!supportNull)
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			return null;
		}
		if (size<0 || size>sizeMax)
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		
		Object []tab=new Object[size];
		sizeMax-=tab.length;
		for (int i=0;i<size;i++)
		{
			tab[i]=readObject(ois, sizeMax, true);
		}
		
		return tab;
		
	}
	public static int MAX_URL_LENGTH=8000;
	public static void writeInetAddress(final ObjectOutputStream oos, InetAddress inetAddress, boolean supportNull) throws IOException
	{
		if (inetAddress==null)
		{
			if (!supportNull)
				throw new IOException();
			oos.writeBoolean(false);
			return;
			
		}
		oos.writeBoolean(true);
		writeBytes(oos, inetAddress.getAddress(), 20, false);
	}
	
	public static InetAddress readInetAddress(final ObjectInputStream ois, boolean supportNull) throws IOException, ClassNotFoundException
	{
		if (ois.readBoolean())
		{
			byte[] address=readBytes(ois, 20, false);
			try
			{
				return InetAddress.getByAddress(address);
			}
			catch(Exception e)
			{
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN, e);
			}
		}
		else if (!supportNull)
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		else
			return null;
		
	}
	
	public static void writeInetSocketAddress(final ObjectOutputStream oos, InetSocketAddress inetSocketAddress, boolean supportNull) throws IOException
	{
		if (inetSocketAddress==null)
		{
			if (!supportNull)
				throw new IOException();
			oos.writeBoolean(false);
			return;
			
		}
		oos.writeBoolean(true);
		oos.writeInt(inetSocketAddress.getPort());
		writeInetAddress(oos, inetSocketAddress.getAddress(), false);
	}
	
	public static InetSocketAddress readInetSocketAddress(final ObjectInputStream ois, boolean supportNull) throws IOException, ClassNotFoundException
	{
		if (ois.readBoolean())
		{
			int port=ois.readInt();
			InetAddress ia=readInetAddress(ois, false);
			
			try
			{
				return new InetSocketAddress(ia, port);
			}
			catch(Exception e)
			{
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN, e);
			}
		}
		else if (!supportNull)
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		else
			return null;
		
	}
	
	public static void writeObject(final ObjectOutputStream oos, Object o, int sizeMax, boolean supportNull) throws IOException
	{
		if (o==null)
		{
			if (!supportNull)
				throw new IOException();
			if (sizeMax>Short.MAX_VALUE)
				oos.write(0);
			else
				oos.write(0);
			return;
			
		}
		if (o instanceof String)
		{
			oos.write(1);
			writeString(oos, (String)o, sizeMax, false);
		}
		else if (o instanceof byte[])
		{
			oos.write(2);
			writeBytes(oos, (byte[])o, sizeMax, false);
		}
		else if (o instanceof Object[])
		{
			oos.write(3);
			writeObjects(oos, (Object[])o, sizeMax, false);
		}
		else if (o instanceof InetSocketAddress)
		{
			oos.write(4);
			writeInetSocketAddress(oos, (InetSocketAddress)o, supportNull);
		}
		else if (o instanceof InetAddress)
		{
			oos.write(5);
			writeInetAddress(oos, (InetAddress)o, supportNull);
		}
		else
		{
			oos.write(Byte.MAX_VALUE);
			oos.writeObject(o);
		}
	}
	
	public static Object readObject(final ObjectInputStream ois, int sizeMax, boolean supportNull) throws IOException, ClassNotFoundException
	{
		byte type=ois.readByte();
		switch(type)
		{
		case 0:
			if (!supportNull)
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			return null;
		case 1:
			return readString(ois, sizeMax, false);
			
		case 2:
			return readBytes(ois, sizeMax, false);
		case 3:
			return readObjects(ois, sizeMax, false);
		case 4:
			return readInetSocketAddress(ois, false);
		case 5:
			return readInetAddress(ois, false);
		case Byte.MAX_VALUE:
			return ois.readObject();
		default:
			throw new MessageSerializationException(Integrity.FAIL);
		}
		
	}
	
	public static int getInternalSize(Object o, int sizeMax)
	{
		if (o ==null)
			return 0;
		if (o instanceof String)
		{
			return ((String)o).length()*2+sizeMax>Short.MAX_VALUE?4:2;
		}
		else if (o instanceof byte[])
		{
			return ((byte[])o).length+sizeMax>Short.MAX_VALUE?4:2;
		}
		else if (o instanceof SerializableAndSizable)
		{
			return ((SerializableAndSizable)o).getInternalSerializedSize();
		}
		else if (o instanceof Object[])
		{
			Object tab[]=(Object[])o;
			int size=sizeMax>Short.MAX_VALUE?4:2;
			for (Object so : tab)
			{
				size+=getInternalSize(so, sizeMax-tab.length);
			}
			return size;
		}
		else if (o instanceof InetAddress)
		{
			return ((InetAddress)o).getAddress().length+3;
		}
		else if (o instanceof InetSocketAddress)
		{
			return ((InetSocketAddress)o).getAddress().getAddress().length+7;
		}
		else
			return ObjectSizer.sizeOf(o);
	}
	
}
