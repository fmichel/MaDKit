/*
 * Copyright 1997-2013 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.message;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import madkit.kernel.Message;

/**
 * This parameterizable class could be used to convey 
 * any Java Object between MaDKit agents.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.1
 * @version 0.9
 *
 */
public class ObjectMessage<T> extends Message {

	private static final long serialVersionUID = 2061462024105569662L;
	private final T content;
	
	/**
	 * Builds a message with the specified content
	 * @param content
	 */
	public ObjectMessage(final T content) {
		this.content = content;
	}

	/**
	 * Gets the content of this message
	 * @return the object of type T contained in the message
	 */
	public T getContent() {
		return content;
	}
	
   /**
    * Returns the message containing the maximum element of the given collection, according to the
    * <i>natural ordering</i> of the elements contained in the message.  All elements in the
    * messages must implement the <tt>Comparable</tt> interface.
    * Furthermore, all elements must be <i>mutually
    * comparable</i> (that is, <tt>e1.compareTo(e2)</tt> must not throw a
    * <tt>ClassCastException</tt> for any elements <tt>e1</tt> and
    * <tt>e2</tt> in the collection).<p>
    *
    * This method iterates over the entire collection, hence it requires
    * time proportional to the size of the collection.
    *
    * @param  messageCollection the collection of messages whose maximum element is to be determined.
    * @return the message containing the maximum element.
    * @throws ClassCastException if the content of the messages are
    *         not <i>mutually comparable</i>.
    * @throws NoSuchElementException if the collection is empty.
    * @see Comparable
    */
   public static <T extends Object & Comparable<? super T>, M extends ObjectMessage<T>> M max(Collection<M> messageCollection) {
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
    * Returns the message containing the minimum element of the given collection, according to the
    * <i>natural ordering</i> of the elements contained in the message.  All elements in the
    * messages must implement the <tt>Comparable</tt> interface.
    * Furthermore, all elements must be <i>mutually
    * comparable</i> (that is, <tt>e1.compareTo(e2)</tt> must not throw a
    * <tt>ClassCastException</tt> for any elements <tt>e1</tt> and
    * <tt>e2</tt> in the collection).<p>
    *
    * This method iterates over the entire collection, hence it requires
    * time proportional to the size of the collection.
    *
    * @param  messageCollection the collection of messages whose maximum element is to be determined.
    * @return the message containing the minimum element.
    * @throws ClassCastException if the content of the messages are
    *         not <i>mutually comparable</i>.
    * @throws NoSuchElementException if the collection is empty.
    * @see Comparable
    */
   public static <T extends Object & Comparable<? super T>, M extends ObjectMessage<T>> M min(Collection<M> messageCollection) {
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
    * {@linkplain Comparable natural ordering} of the content of each message.
    * All elements contained in the messages must implement the {@link Comparable}
    * interface.  Furthermore, all these elements must be
    * <i>mutually comparable</i> (that is, {@code e1.compareTo(e2)}
    * must not throw a {@code ClassCastException} for any elements
    * {@code e1} and {@code e2} in the list).
    *
    * <p>This sort is guaranteed to be <i>stable</i>:  equal elements will
    * not be reordered as a result of the sort.
    *
    * <p>The specified list must be modifiable, but need not be resizable.
    *
    * @param  list the list to be sorted.
    * @throws ClassCastException if the list contains elements that are not
    *         <i>mutually comparable</i> (for example, strings and integers).
    * @throws UnsupportedOperationException if the specified list's
    *         list-iterator does not support the {@code set} operation.
    * @throws IllegalArgumentException (optional) if the implementation
    *         detects that the natural ordering of the list elements is
    *         found to violate the {@link Comparable} contract
    */
  @SuppressWarnings("unchecked")
	public static <T extends Object & Comparable<? super T>, M extends ObjectMessage<T>> void sort(List<M> list) {
      Object[] a = list.toArray();
      Arrays.sort(a);
      ListIterator<M> i = list.listIterator();
      for (int j=0; j<a.length; j++) {
          i.next();
          i.set((M)a[j]);
      }
  }


   /**
	 * @see madkit.kernel.Message#toString()
	 */
	@Override
	public String toString() {
		String s = super.toString();
		s += "\n"+(getClass().getSimpleName()+getConversationID()).replaceAll(".", " ");
		return s+"    content: {"+content+"}";
	}
}
