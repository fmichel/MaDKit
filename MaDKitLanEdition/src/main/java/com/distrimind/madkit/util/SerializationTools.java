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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Objects;

import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.kernel.MadkitClassLoader;
import com.distrimind.madkit.kernel.network.FilteredObjectInputStream;
import com.distrimind.madkit.kernel.network.SystemMessage;
import com.distrimind.madkit.kernel.network.SystemMessage.Integrity;
import com.distrimind.util.AbstractDecentralizedID;
import com.distrimind.util.crypto.ASymmetricKeyPair;
import com.distrimind.util.crypto.Key;
import com.distrimind.util.sizeof.ObjectSizer;
import static com.distrimind.madkit.util.ReflectionTools.*;

/**
 * 
 * @author Jason Mahdjoub
 * @since MaDKitLanEdition 1.7
 * @version 1.0
 * 
 */

public class SerializationTools {
	private static final int MAX_CHAR_BUFFER_SIZE=Short.MAX_VALUE*5;
	
	public static void writeString(final DataOutput oos, String s, int sizeMax, boolean supportNull) throws IOException
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
	
	public static String readString(final DataInput ois, int sizeMax, boolean supportNull) throws IOException
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
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN, "size="+size+", sizeMax="+sizeMax);
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
	
	public static void writeBytes(final DataOutput oos, byte tab[], int sizeMax, boolean supportNull) throws IOException
	{
		writeBytes(oos, tab, 0, tab==null?0:tab.length, sizeMax, supportNull);
	}
	public static void writeBytes(final DataOutput oos, byte tab[], int off, int size, int sizeMax, boolean supportNull) throws IOException
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
	public static void writeBytes2D(final DataOutput oos, byte tab[][], int sizeMax1,int sizeMax2, boolean supportNull1, boolean supportNull2) throws IOException
	{
		writeBytes2D(oos, tab, 0, tab==null?0:tab.length, sizeMax1, sizeMax2, supportNull1, supportNull2);
	}
	public static void writeBytes2D(final DataOutput oos, byte tab[][], int off, int size, int sizeMax1, int sizeMax2,  boolean supportNull1, boolean supportNull2) throws IOException
	{
		if (tab==null)
		{
			if (!supportNull1)
				throw new IOException();
			if (sizeMax1>Short.MAX_VALUE)
				oos.writeInt(-1);
			else
				oos.writeShort(-1);
			return;
			
		}
		if (tab.length>sizeMax1)
			throw new IOException();
		if (sizeMax1>Short.MAX_VALUE)
			oos.writeInt(tab.length);
		else
			oos.writeShort(tab.length);
		for (byte[] b : tab)
			SerializationTools.writeBytes(oos, b, sizeMax2, supportNull2);
	}
	public static byte[][] readBytes2D(final DataInput ois, int sizeMax1, int sizeMax2,  boolean supportNull1, boolean supportNull2) throws IOException
	{
		int size;
		if (sizeMax1>Short.MAX_VALUE)
			size=ois.readInt();
		else
			size=ois.readShort();
		if (size==-1)
		{
			if (!supportNull1)
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			return null;
		}
		if (size<0 || size>sizeMax1)
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		
		byte [][]tab=new byte[size][];
		for (int i=0;i<size;i++)
			tab[i]=readBytes(ois, sizeMax2, supportNull2);
		
		
		return tab;
		
	}
	public static byte[] readBytes(final DataInput ois, int sizeMax, boolean supportNull) throws IOException
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
		ois.readFully(tab);
		
		return tab;
		
	}
	
	public static final int MAX_KEY_SIZE=Short.MAX_VALUE;
	public static void writeKey(final DataOutput oos, Key key, boolean supportNull) throws IOException
	{
		
		if (key==null)
		{
			if (!supportNull)
				throw new IOException();
			oos.writeBoolean(false);
			return;
			
		}
		oos.writeBoolean(true);
		writeBytes(oos, key.encode(), MAX_KEY_SIZE, false);
	}

	public static Key readKey(final DataInput in, boolean supportNull) throws IOException
	{
		if (in.readBoolean())
		{
			byte[] k=readBytes(in, MAX_KEY_SIZE, false);
			try
			{
				assert k != null;
				return Key.decode(k);
			}
			catch(Exception e)
			{
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			}
		}
		else
		{
			if (!supportNull)
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			return null;
		}
	}
	
	
	public static void writeKeyPair(final DataOutput oos, ASymmetricKeyPair keyPair, boolean supportNull) throws IOException
	{
		
		if (keyPair==null)
		{
			if (!supportNull)
				throw new IOException();
			oos.writeBoolean(false);
			return;
			
		}
		oos.writeBoolean(true);
		
		writeBytes(oos, keyPair.encode(), MAX_KEY_SIZE*2, false);
	}

	public static ASymmetricKeyPair readKeyPair(final DataInput in, boolean supportNull) throws IOException
	{
		if (in.readBoolean())
		{
			byte[] k=readBytes(in, MAX_KEY_SIZE*2, false);
			try
			{
				assert k != null;
				return ASymmetricKeyPair.decode(k);
			}
			catch(Exception e)
			{
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			}
		}
		else
		{
			if (!supportNull)
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			return null;
		}
	}
	public static void writeObjects(final ObjectOutput oos, Object tab[], int sizeMax, boolean supportNull) throws IOException
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
	
	public static Object[] readObjects(final ObjectInput ois, int sizeMax, boolean supportNull) throws IOException, ClassNotFoundException
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
	public static void writeExternalizableAndSizables(final ObjectOutput oos, ExternalizableAndSizable tab[], int sizeMaxBytes, boolean supportNull) throws IOException
	{
		if (tab==null)
		{
			if (!supportNull)
				throw new IOException();
			oos.writeInt(-1);
			return;
			
		}
		if (tab.length*4>sizeMaxBytes)
			throw new IOException();
		oos.writeInt(tab.length);
		int total=4;
		
		for (ExternalizableAndSizable o : tab)
		{
			writeExternalizableAndSizable(oos, o, true);
			total+=o.getInternalSerializedSize();
			
			if (total>=sizeMaxBytes)
				throw new IOException();
		}
	}
	
	public static ExternalizableAndSizable[] readExternalizableAndSizables(final ObjectInput ois, int sizeMaxBytes, boolean supportNull) throws IOException, ClassNotFoundException
	{
		int size=ois.readInt();
		if (size==-1)
		{
			if (!supportNull)
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			return null;
		}
		if (size<0 || size*4>sizeMaxBytes)
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		
		ExternalizableAndSizable []tab=new ExternalizableAndSizable[size];
		sizeMaxBytes-=4;
		for (int i=0;i<size;i++)
		{
			Externalizable o=readExternalizableAndSizable(ois, true);
			if (!(o instanceof ExternalizableAndSizable))
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			ExternalizableAndSizable s=(ExternalizableAndSizable)o;
			sizeMaxBytes-=s.getInternalSerializedSize();
			if (sizeMaxBytes<0)
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			tab[i]=s;
		}
		
		return tab;
		
	}
	public static int MAX_URL_LENGTH=8000;
	public static void writeInetAddress(final DataOutput oos, InetAddress inetAddress, boolean supportNull) throws IOException
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
	
	public static void writeDecentralizedID(final DataOutput oos, AbstractDecentralizedID id, boolean supportNull) throws IOException
	{
		if (id==null)
		{
			if (!supportNull)
				throw new IOException();
			oos.writeBoolean(false);
			return;
			
		}
		oos.writeBoolean(true);
		writeBytes(oos, id.getBytes(), 513, false);
	}
	public static AbstractDecentralizedID readDecentralizedID(final DataInput in, boolean supportNull) throws IOException
	{
		if (in.readBoolean())
		{
			try
			{
				return AbstractDecentralizedID.instanceOf(Objects.requireNonNull(readBytes(in, 513, false)));
			}
			catch(Exception e)
			{
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			}
		}
		if (!supportNull)
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		return null;
	}
	
	public static InetAddress readInetAddress(final DataInput ois, boolean supportNull) throws IOException {
		if (ois.readBoolean())
		{
			byte[] address=readBytes(ois, 20, false);
			try
			{
				assert address != null;
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
	
	public static void writeInetSocketAddress(final DataOutput oos, InetSocketAddress inetSocketAddress, boolean supportNull) throws IOException
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
	
	
	public static InetSocketAddress readInetSocketAddress(final DataInput ois, boolean supportNull) throws IOException {
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
	public static void writeEnum(final DataOutput oos, Enum<?> e, boolean supportNull) throws IOException
	{
		if (e==null)
		{
			if (!supportNull)
				throw new IOException();
			oos.writeBoolean(false);
			return;
			
		}
		oos.writeBoolean(true);
		SerializationTools.writeString(oos, e.getClass().getName(), MAX_CLASS_LENGTH, false);
		SerializationTools.writeString(oos, e.name(), 1000, false);
	}
	public final static int MAX_CLASS_LENGTH=16396;
	@SuppressWarnings("unchecked")
	public static Enum<?> readEnum(final DataInput ois, boolean supportNull) throws IOException, ClassNotFoundException
	{
		if (ois.readBoolean())
		{
			String clazz=SerializationTools.readString(ois, MAX_CLASS_LENGTH, false);
			String value=SerializationTools.readString(ois, 1000, false);
			@SuppressWarnings("rawtypes")
			Class c=Class.forName(clazz, false, MadkitClassLoader.getSystemClassLoader());
			if (!c.isEnum())
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			try
			{
				assert value != null;
				return Enum.valueOf(c, value);
			}
			catch(ClassCastException e)
			{
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			}
			
		}
		else if (!supportNull)
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		else
			return null;
		
	}
	public static void writeExternalizableAndSizable(final ObjectOutput oos, Externalizable e, boolean supportNull) throws IOException
	{
		if (e==null)
		{
			if (!supportNull)
				throw new IOException();
			oos.writeBoolean(false);
			return;
			
		}
		Class<?> clazz=e.getClass();
		if (!ExternalizableAndSizable.class.isAssignableFrom(clazz) && !SystemMessage.class.isAssignableFrom(clazz))
			throw new IOException();
		
		if (oos.getClass()==oosClazz)
		{
			try
			{
				e=(Externalizable)invoke(replaceObject, oos, e);
				if (e!=null)
					clazz=e.getClass();
			}
			catch(Exception e2)
			{
				throw new IOException(e2);
			}
		}
		oos.writeBoolean(true);
		SerializationTools.writeString(oos, clazz.getName(), MAX_CLASS_LENGTH, false);
		assert e != null;
		e.writeExternal(oos);
		
	}
	private static final HashMap<Class<?>, Constructor<?>> constructors=new HashMap<>();
	
	private static Constructor<?> getDefaultConstructor(final Class<?> clazz) throws NoSuchMethodException, SecurityException
	{
		synchronized(constructors)
		{
			Constructor<?> c=constructors.get(clazz);
			if (c==null)
			{
				final Constructor<?> cons=clazz.getDeclaredConstructor();
				c=AccessController.doPrivileged(new PrivilegedAction<Constructor<?>>() {

					@Override
					public Constructor<?> run() {
						
						cons.setAccessible(true);
						return cons;
					}
				});
				constructors.put(clazz, c);
			}
			return c;
		}
	}
	private final static Class<?> oosClazz;
	private final static Class<?> oisClazz;
	private final static Method replaceObject;
	private final static Method resolveObject;
	
	
	
	
	static
	{
	
		oosClazz=loadClass("com.distrimind.madkit.kernel.network.DistantKernelAgent$OOS");
		oisClazz=loadClass("com.distrimind.madkit.kernel.network.DistantKernelAgent$OIS");
		resolveObject=getMethod(oisClazz, "resolveObject", Object.class);
		replaceObject=getMethod(oosClazz, "replaceObject", Object.class);
		
	}
	
	
	
	
	
	public static Externalizable readExternalizableAndSizable(final ObjectInput ois, boolean supportNull) throws IOException, ClassNotFoundException
	{
		if (ois.readBoolean())
		{
			String clazz=SerializationTools.readString(ois, MAX_CLASS_LENGTH, false);
			
			
			
			
			try
			{
				Class<?> c;
				boolean isOIS=ois.getClass()==oisClazz;
				if (isOIS)
					c= ((FilteredObjectInputStream)ois).resolveClass(clazz);
				else
					c= Class.forName(clazz, false, MadkitClassLoader.getSystemClassLoader());
				if (!ExternalizableAndSizable.class.isAssignableFrom(c) && !SystemMessage.class.isAssignableFrom(c))
					throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
				Constructor<?> cons=getDefaultConstructor(c);
				Externalizable res=(Externalizable)cons.newInstance();
				
				res.readExternal(ois);
				if (isOIS)
				{
					res=(Externalizable)invoke(resolveObject, ois, res);
				}
				return res;
			}
			catch(InvocationTargetException | NoSuchMethodException | IllegalAccessException | InstantiationException e)
			{
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN, e);
			}
			
		}
		else if (!supportNull)
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		else
			return null;
		
	}
	public static void writeObject(final ObjectOutput oos, Object o, int sizeMax, boolean supportNull) throws IOException
	{
		if (o==null)
		{
			if (!supportNull)
				throw new IOException();
			
			oos.write(0);
		}
		else if (o instanceof ExternalizableAndSizable || o instanceof SystemMessage)
		{
			oos.write(1);
			writeExternalizableAndSizable(oos, (Externalizable)o, supportNull);
		}
		else if (o instanceof String)
		{
			oos.write(2);
			writeString(oos, (String)o, sizeMax, false);
		}
		else if (o instanceof byte[])
		{
			oos.write(3);
			writeBytes(oos, (byte[])o, sizeMax, false);
		}
		else if (o instanceof byte[][])
		{
			oos.write(4);
			writeBytes2D(oos, (byte[][])o, sizeMax, sizeMax, false, false);
		}
		else if (o instanceof ExternalizableAndSizable[])
		{
			oos.write(5);
			writeExternalizableAndSizables(oos, (ExternalizableAndSizable[])o, sizeMax, supportNull);
		}
		else if (o instanceof Object[])
		{
			oos.write(6);
			writeObjects(oos, (Object[])o, sizeMax, false);
		}
		else if (o instanceof InetSocketAddress)
		{
			oos.write(7);
			writeInetSocketAddress(oos, (InetSocketAddress)o, supportNull);
		}
		else if (o instanceof InetAddress)
		{
			oos.write(8);
			writeInetAddress(oos, (InetAddress)o, supportNull);
		}
		else if (o instanceof AbstractDecentralizedID)
		{
			oos.write(9);
			writeDecentralizedID(oos, (AbstractDecentralizedID)o, supportNull);
		}
		else if (o instanceof Key)
		{
			oos.write(10);
			writeKey(oos, (Key)o, supportNull);
		}
		else if (o instanceof ASymmetricKeyPair)
		{
			oos.write(11);
			writeKeyPair(oos, (ASymmetricKeyPair)o, supportNull);
		}
		else if (o instanceof Enum<?>)
		{
			oos.write(12);
			writeEnum(oos, (Enum<?>)o, supportNull);
		}
		else 
		{
			throw new IOException();
			/*oos.write(Byte.MAX_VALUE);
			oos.writeObject(o);*/
		}
	}
	
	public static Object readObject(final ObjectInput ois, int sizeMax, boolean supportNull) throws IOException, ClassNotFoundException
	{
		byte type=ois.readByte();
		switch(type)
		{
		case 0:
			if (!supportNull)
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			return null;
		case 1:
			return readExternalizableAndSizable(ois, supportNull);
		case 2:
			return readString(ois, sizeMax, false);
		case 3:
			return readBytes(ois, sizeMax, false);
		case 4:
			return readBytes2D(ois, sizeMax, sizeMax, false, false);
		case 5:
			return readExternalizableAndSizables(ois, sizeMax, false);
		case 6:
			return readObjects(ois, sizeMax, false);
		case 7:
			return readInetSocketAddress(ois, false);
		case 8:
			return readInetAddress(ois, false);
		case 9:
			return readDecentralizedID(ois, false);
		case 10:
			return readKey(ois, false);
		case 11:
			return readKeyPair(ois, false);
		case 12:
			return readEnum(ois, false);
		/*case Byte.MAX_VALUE:
			return ois.readObject();*/
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
		else if (o instanceof Key)
		{
			return 3+((Key)o).encode().length;
		}
		else if (o instanceof ASymmetricKeyPair)
		{
			return 3+((ASymmetricKeyPair)o).encode().length;
		}
		else if (o instanceof byte[][])
		{
			byte tab[][]=((byte[][])o);
			int res=sizeMax>Short.MAX_VALUE?4:2;
			for (byte[] b : tab)
				res+=2+(b==null?0:b.length);
			return res;
		}
		else if (o instanceof ExternalizableAndSizable)
		{
			return ((ExternalizableAndSizable)o).getInternalSerializedSize();
		}
		else if (o instanceof ExternalizableAndSizable[])
		{
			int size=4;
			for (ExternalizableAndSizable s : (ExternalizableAndSizable[])o)
				size+=s.getInternalSerializedSize();
			return size;
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
		else if (o instanceof AbstractDecentralizedID)
		{
			return ((AbstractDecentralizedID) o).getBytes().length+2;
		}
		else if (o instanceof Enum<?>)
		{
			return 5+((Enum<?>)o).name().length()*2+o.getClass().getName().length()*2;
		}
		else
			return ObjectSizer.sizeOf(o);
	}
	
}
