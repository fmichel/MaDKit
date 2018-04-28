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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.kernel.network.SystemMessage.Integrity;
import com.distrimind.madkit.util.SerializationTools;

/**
 * This parameterizable class could be used to convey any Java Object between
 * MaDKit agents.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.1
 * @version 1.0
 *
 */
public class ObjectMessage<T> extends Message {

	
	private T content;
	private boolean excludeFromEncryption;

	/**
	 * Builds a message with the specified content
	 * 
	 * @param content the message content
	 */
	public ObjectMessage(final T content) {
		this(content, false);
	}
	
	
	public int getInternalSerializedSizeImpl(int maxContentLength) {
		return super.getInternalSerializedSizeImpl()+1+SerializationTools.getInternalSize(content, maxContentLength);
	}	
	
	@SuppressWarnings("unchecked")
	
	protected void readAndCheckObjectImpl(final ObjectInputStream in, int maxContentLength) throws IOException, ClassNotFoundException
	{
		super.readAndCheckObjectImpl(in);
		try
		{
			content=(T)SerializationTools.readObject(in, maxContentLength, true);
		}
		catch(Exception e)
		{
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN, e);
			
		}
		excludeFromEncryption=in.readBoolean();
		
		
	}
	
	protected void writeAndCheckObjectImpl(final ObjectOutputStream oos, int maxContentLength) throws IOException{
		super.writeAndCheckObjectImpl(oos);
		SerializationTools.writeObject(oos, content, maxContentLength, true);
		oos.writeBoolean(excludeFromEncryption);
	}
	
	/**
	 * Builds a message with the specified content
	 * 
	 * @param content the message content
	 * @param excludeFromEncryption tells if this message can be excluded from the lan encryption process
	 */
	public ObjectMessage(final T content, boolean excludeFromEncryption) {
		this.content = content;
		this.excludeFromEncryption=excludeFromEncryption;
	}

	/**
	 * Gets the content of this message
	 * 
	 * @return the object of type T contained in the message
	 */
	public T getContent() {
		return content;
	}

	/**
	 * Returns the message containing the maximum element of the given collection,
	 * according to the <i>natural ordering</i> of the elements contained in the
	 * message. All elements in the messages must implement the <tt>Comparable</tt>
	 * interface. Furthermore, all elements must be <i>mutually comparable</i> (that
	 * is, <tt>e1.compareTo(e2)</tt> must not throw a <tt>ClassCastException</tt>
	 * for any elements <tt>e1</tt> and <tt>e2</tt> in the collection).
	 * <p>
	 *
	 * This method iterates over the entire collection, hence it requires time
	 * proportional to the size of the collection.
	 *
	 * @param messageCollection
	 *            the collection of messages whose maximum element is to be
	 *            determined.
	 * @return the message containing the maximum element.
	 * @param <T> the object type to compare. Must implement Comparable
	 * @param <M> the object message type
	 * @throws ClassCastException
	 *             if the content of the messages are not <i>mutually
	 *             comparable</i>.
	 * @throws NoSuchElementException
	 *             if the collection is empty.
	 * @see Comparable
	 */
	public static <T extends Object & Comparable<? super T>, M extends ObjectMessage<T>> M max(
			Collection<M> messageCollection) {
		Iterator<M> i = messageCollection.iterator();
		M candidate = i.next();
		while (i.hasNext()) {
			M next = i.next();
			if (next.getContent().compareTo(candidate.getContent()) > 0)
				candidate = next;
		}
		return candidate;
	}

	/**
	 * Returns the message containing the minimum element of the given collection,
	 * according to the <i>natural ordering</i> of the elements contained in the
	 * message. All elements in the messages must implement the <tt>Comparable</tt>
	 * interface. Furthermore, all elements must be <i>mutually comparable</i> (that
	 * is, <tt>e1.compareTo(e2)</tt> must not throw a <tt>ClassCastException</tt>
	 * for any elements <tt>e1</tt> and <tt>e2</tt> in the collection).
	 * <p>
	 *
	 * This method iterates over the entire collection, hence it requires time
	 * proportional to the size of the collection.
	 *
	 * @param messageCollection
	 *            the collection of messages whose maximum element is to be
	 *            determined.
	 * @return the message containing the minimum element.
	 * @param <T> the object type to compare. Must implement Comparable
	 * @param <M> the object message type
	 * 
	 * @throws ClassCastException
	 *             if the content of the messages are not <i>mutually
	 *             comparable</i>.
	 * @throws NoSuchElementException
	 *             if the collection is empty.
	 * @see Comparable
	 */
	public static <T extends Object & Comparable<? super T>, M extends ObjectMessage<T>> M min(
			Collection<M> messageCollection) {
		Iterator<M> i = messageCollection.iterator();
		M candidate = i.next();
		while (i.hasNext()) {
			M next = i.next();
			if (next.getContent().compareTo(candidate.getContent()) < 0)
				candidate = next;
		}
		return candidate;
	}

	/**
	 * Sorts the specified list of messages into ascending order, according to the
	 * {@linkplain Comparable natural ordering} of the content of each message. All
	 * elements contained in the messages must implement the {@link Comparable}
	 * interface. Furthermore, all these elements must be <i>mutually comparable</i>
	 * (that is, {@code e1.compareTo(e2)} must not throw a
	 * {@code ClassCastException} for any elements {@code e1} and {@code e2} in the
	 * list).
	 *
	 * <p>
	 * This sort is guaranteed to be <i>stable</i>: equal elements will not be
	 * reordered as a result of the sort.
	 *
	 * <p>
	 * The specified list must be modifiable, but need not be resizable.
	 *
	 * @param list
	 *            the list to be sorted.
	 *            
	 * @param <T> the object type to compare. Must implement Comparable
	 * @param <M> the object message type
	 * @throws ClassCastException
	 *             if the list contains elements that are not <i>mutually
	 *             comparable</i> (for example, strings and integers).
	 * @throws UnsupportedOperationException
	 *             if the specified list's list-iterator does not support the
	 *             {@code set} operation.
	 * @throws IllegalArgumentException
	 *             (optional) if the implementation detects that the natural
	 *             ordering of the list elements is found to violate the
	 *             {@link Comparable} contract
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Object & Comparable<? super T>, M extends ObjectMessage<T>> void sort(List<M> list) {
		Object[] a = list.toArray();
		Arrays.sort(a);
		ListIterator<M> i = list.listIterator();
		for (int j = 0; j < a.length; j++) {
			i.next();
			i.set((M) a[j]);
		}
	}

	/**
	 * @see com.distrimind.madkit.kernel.Message#toString()
	 */
	@Override
	public String toString() {
		String s = super.toString();
		s += "\n" + (getClass().getSimpleName() + getConversationID()).replaceAll(".", " ");
		return s + "    content: {" + content + "}";
	}

	@Override
	public boolean excludedFromEncryption() {
		return excludeFromEncryption;
	}
}
