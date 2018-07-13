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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.kernel.MadkitClassLoader;
import com.distrimind.madkit.kernel.network.SystemMessage.Integrity;

/**
 * 
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.7
 */
public class FilteredObjectInputStream extends ObjectInputStream {
	private final NetworkProperties np;
	public FilteredObjectInputStream(InputStream _in, NetworkProperties np) throws IOException
	{
		super(_in);
		this.np=np;
	}
	private Class<?> nextAutorizedSubClass=null;
	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws ClassNotFoundException, IOException
	{
		if (nextAutorizedSubClass!=null)
		{
			Class<?> c=super.resolveClass(desc);
			if (c==nextAutorizedSubClass)
			{
				nextAutorizedSubClass=c.getSuperclass();
				if (nextAutorizedSubClass==Object.class)
					nextAutorizedSubClass=null;
				return c;
			}
			else
				throw new IOException("Illegal access error");
		}
		if (np.isAcceptedClassForSerializationUsingPatterns(desc.getName()))
		{
			Class<?> c=super.resolveClass(desc);
			if (c==null)
				return null;
			if (np.isAcceptedClassForSerializationUsingWhiteClassList(c))
			{
				nextAutorizedSubClass=c.getSuperclass();
				if (nextAutorizedSubClass==Object.class)
					nextAutorizedSubClass=null;
				return c;
			}
		}
		throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN, new ClassNotFoundException(desc.getName()));
	}
	
	public Class<?> resolveClass(String clazz) throws MessageSerializationException
	{
		try
		{
			if (np.isAcceptedClassForSerializationUsingPatterns(clazz))
			{
				Class<?> c=Class.forName(clazz, true, MadkitClassLoader.getSystemClassLoader());
				if (c==null)
					throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN, new ClassNotFoundException(clazz));
				if (np.isAcceptedClassForSerializationUsingWhiteClassList(c))
				{
					return c;
				}
			}
		}
		catch(Exception e)
		{
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN, new ClassNotFoundException(clazz));
		}
		throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN, new ClassNotFoundException(clazz));

	}
	
	@Override
    protected Class<?> resolveProxyClass(String[] interfaces)
            throws IOException, ClassNotFoundException{
		for (String s : interfaces)
		{
			if (np.isDeniedClassForSerializationUsingPatterns(s))
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN, new ClassNotFoundException(s));
		}
		Class<?> c=super.resolveProxyClass(interfaces);
		if (c==null)
			return null;
		if (np.isDeniedClassForSerializationUsingBlackClassList(c))
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN, new ClassNotFoundException(c.getName()));
		return c;
	}
}
