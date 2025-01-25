/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import madkit.messages.ConversationFilter;
import madkit.messages.StringMessage;

/**
 * 
 * This class represents the mailbox of an Agent
 * 
 *
 * @version 6.0
 * @since MaDKit 6.0
 *
 */
public final class Mailbox {

	final BlockingDeque<Message> messageBox;

	Mailbox() {
		messageBox = new LinkedBlockingDeque<>();
	}

	@Override
	public String toString() {
		return messageBox.toString();
	}

	boolean add(Message m) {
		return messageBox.offer(m);
	}

	/**
	 * Retrieves and removes the oldest received message contained in the mailbox.
	 * Beware that if T is not the type of the next message, a
	 * {@link ClassCastException} will be thrown. For instance, if the next message
	 * is a {@link StringMessage} and you call this method with T being
	 * {@link StringMessage}, it will work. But if you call it with T being
	 * {@link Message}, a {@link ClassCastException} will be thrown.
	 * <p>
	 * However, if you are seeking for a message of type T in the mailbox, you can
	 * use {@link #next(Predicate)} with a specific Predicate such as in
	 * <p>
	 * <code>StringMessage sm = next(StringMessage.class::isInstance)</code>
	 *
	 * @param <M> the expected type of the message to get
	 * @return The next message or <code>null</code> if the message box is empty.
	 */
	@SuppressWarnings("unchecked")
	public synchronized <M extends Message> M next() {
		return (M) messageBox.poll();
	}

	/**
	 * Retrieves and removes the first oldest message of the mailbox that matches
	 * the filter.
	 * 
	 * @param filter the filter to use
	 * @param <M>    the expected type of the message to get
	 *
	 * @return The next acceptable message or <code>null</code> if such message has
	 *         not been found.
	 */
	public synchronized <M extends Message> M next(Predicate<M> filter) {
		Objects.requireNonNull(filter);
		for (Iterator<Message> iterator = messageBox.iterator(); iterator.hasNext();) {
			M t = checkTypeFilterConvert(iterator.next(), filter);
			if (t != null) {
				iterator.remove();
				return t;
			}
		}
		return null;
	}

	/**
	 * Retrieves all the messages of the mailbox, without taking those that match
	 * the filter, Messages are listed in the order they were received.
	 *
	 * @param filter if <code>null</code> all the messages are returned and removed
	 *               from the mailbox.
	 * @return the ordered list of matching messages, or an empty list if none has
	 *         been found.
	 */
	@SuppressWarnings("unchecked")
	public synchronized <M extends Message> List<M> nextMatches(Predicate<M> filter) {
		List<M> match = new ArrayList<>();
		if (filter == null) {
			messageBox.drainTo((Collection<? super Message>) match);
		} else {
			populateFilteredMessageListInAscendingOrder(match, filter);
		}
		return match;
	}

	/**
	 * populates the match list with the messages that match the filter and removes
	 * them from the mailbox.
	 * 
	 * @param <T>    the type of the message to get
	 * @param match  the list of messages that match the filter
	 * @param filter the filter
	 */
	private <T extends Message> void populateFilteredMessageListInAscendingOrder(List<T> match, Predicate<T> filter) {
		for (Iterator<Message> iterator = messageBox.iterator(); iterator.hasNext();) {
			T t = checkTypeFilterConvert(iterator.next(), filter);
			if (t != null) {
				iterator.remove();
				match.add(t);
			}
		}
	}

	/**
	 * Retrieves and removes all the messages from the mailbox.
	 * 
	 * @return the list of messages in the order they were received
	 */
	public synchronized List<Message> removeAll() {
		List<Message> messages = new ArrayList<>();
		messageBox.drainTo(messages);
		return messages;
	}

	/**
	 * Retrieves and removes all the messages from the message box.
	 * 
	 * @param <M>    the type of the message to get
	 * @param filter the filter to use to select the messages to remove
	 * @return the list of messages in the order they were received
	 */
	public synchronized <M extends Message> List<M> getAll(Predicate<M> filter) {
		List<M> match = new ArrayList<>();
		populateFilteredMessageListInAscendingOrder(match, filter);
		return match;
	}

	/**
	 * Gets the last received message. Beware that if <code>T</code> is not the type
	 * of the next message, a {@link ClassCastException} will be thrown. For
	 * instance, if the newest message is a {@link StringMessage} and you call this
	 * method with T being {@link StringMessage}, it will work. But if you call it
	 * with T being {@link Message}, a {@link ClassCastException} will be thrown.
	 * <p>
	 * However, if you are seeking for a message of type T, you can use
	 * {@link #getNewest(Predicate)} with a specific Predicate such as in
	 * <p>
	 * <code>StringMessage sm = getNewest(StringMessage.class::isInstance)</code>
	 *
	 * @param <T> the expected type of the message
	 *
	 * @return the last received message or <code>null</code> if the mailbox is
	 *         empty.
	 */
	@SuppressWarnings("unchecked")
	public synchronized <T extends Message> T getNewest() {
		return (T) messageBox.pollLast();
	}

	/**
	 * Gets the last received message according to a filter.
	 *
	 * @param filter the message filter to use
	 * @return the last received message that matches the filter or
	 *         <code>null</code> if such message has not been found.
	 */
	public synchronized <M extends Message> M getNewest(Predicate<M> filter) {
		for (Iterator<Message> iterator = messageBox.descendingIterator(); iterator.hasNext();) {
			M m = checkTypeFilterConvert(iterator.next(), filter);
			if (m != null) {
				iterator.remove();
				return m;
			}
		}
		return null;
	}

	/**
	 * Checks if the message is of the expected type and matches the filter. If it
	 * does, it returns the message, otherwise it returns null.
	 * 
	 * @param <M>     the expected type of the message
	 * @param message the message to check
	 * @param filter  the filter to use
	 * @return the message if it is of the expected type and matches the filter,
	 *         otherwise null
	 */
	private <M extends Message> M checkTypeFilterConvert(Message message, Predicate<M> filter) {
		try {
			M m = (M) message;
			if (filter.test(m)) {
				return m;
			}
		} catch (ClassCastException e) {
			// nothing to do
		}
		return null;
	}

	/**
	 * Purges the mailbox and returns the most recent received message at that time.
	 *
	 * Beware that if <code>T</code> is not the type of the next message, a
	 * {@link ClassCastException} will be thrown.
	 * 
	 * However, if you are seeking for a message of type T, you can use
	 * {@link #purge(Predicate)} with a specific Predicate such as in
	 * <p>
	 * <code>StringMessage sm = purge(StringMessage.class::isInstance)</code>
	 *
	 * @param <M> the expected type of the message
	 *
	 * @return the most recent received message or <code>null</code> if the mailbox
	 *         is already empty.
	 */
	@SuppressWarnings("unchecked")
	public synchronized <M extends Message> M purge() {
		Message m = messageBox.pollLast();
		messageBox.clear();
		return (M) m;
	}

	/**
	 * Purges the mailbox and returns the most recent received message at that time
	 * that matches the filter.
	 *
	 * @param <M>    the expected type of the message
	 * @param filter the filter to use
	 * @return the m most recent received message that matches the filter or null if
	 *         no such message has been found.
	 */
	public synchronized <M extends Message> M purge(Predicate<M> filter) {
		M m = getNewest(filter);
		messageBox.clear();
		return m;
	}

	/**
	 * /** Gets the next message which is a reply to the <i>originalMessage</i>.
	 *
	 * @param originalMessage the message to which a reply is searched.
	 * @return a reply to the <i>originalMessage</i> or <code>null</code> if no
	 *         reply to this message has been received.
	 */
	@SuppressWarnings("unchecked")
	public synchronized <M extends Message> M getReply(Message originalMessage) {
		return (M) next(new ConversationFilter(originalMessage));
	}

	/**
	 * Tells if there is a message in the mailbox
	 *
	 * @return <code>true</code> if there is no message in the mailbox.
	 */
	public synchronized boolean isEmpty() {
		return messageBox.isEmpty();
	}

	/**
	 * This method is the blocking version of nextMessage(). If there is no message
	 * in the mailbox, it suspends the agent life until a message is received.
	 * 
	 * <p>
	 * Beware that if T is not the type of the received message, a
	 * {@link ClassCastException} will be thrown. For instance, if the next message
	 * is a {@link StringMessage} and you call this method with T being
	 * {@link StringMessage}, it will work. But if you call it with T being
	 * {@link Message}, a {@link ClassCastException} will be thrown.
	 * 
	 * @param <M> the expected type of the message
	 * @see #waitNext(long)
	 * @return the first received message
	 */
	@SuppressWarnings("unchecked")
	public synchronized <M extends Message> M waitNext() {
		try {
			return (M) messageBox.take();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AgentInterruptedException();
		}
	}

	/**
	 * Wait next message. This is equivalent to calling
	 * waitNext(timeoutMilliSeconds, TimeUnit.MILLISECONDS)
	 * 
	 *
	 * @param <T>                 the expected type
	 * @param timeoutMilliSeconds the timeout in milliseconds
	 * @return the t
	 */
	public synchronized <T extends Message> T waitNext(long timeoutMilliSeconds) {
		return waitNext(timeoutMilliSeconds, TimeUnit.MILLISECONDS);
	}

	@SuppressWarnings("unchecked")
	private <T extends Message> T waitNext(long timeout, TimeUnit timeUnit) {
		try {
			return (T) messageBox.poll(timeout, timeUnit);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AgentInterruptedException();
		}
	}

	/**
	 * Retrieves and removes the next message that complies with the filter, waiting
	 * for ever if necessary until a matching message becomes available.
	 * 
	 * @param filter the filter to use
	 * @param <M>    the expected type of the message
	 * 
	 * @return the first received message that matches the filter
	 */
	public synchronized <M extends Message> M waitNext(Predicate<M> filter) {
		M t = null;
		List<Message> receptions = new ArrayList<>();
		try {
			while (t == null) {
				Message m = messageBox.take();
				t = checkTypeFilterConvert(m, filter);
				if (t == null) {
					receptions.add(0, m);
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AgentInterruptedException();
		} finally {
			receptions.forEach(messageBox::offerFirst);
		}
		return t;
	}

	/**
	 * This method gets the next message of the mailbox or waits for a new incoming
	 * acceptable message up to a certain delay.
	 * 
	 * @param timeOutMilliseconds the maximum time to wait, in milliseconds.
	 * @param filter              the filter to use
	 * @param <M>                 the expected type of the message
	 * 
	 * @return a message that matches or <code>null</code> otherwise.
	 */
	public synchronized <M extends Message> M waitNext(Integer timeOutMilliseconds, Predicate<M> filter) {
		long timeOutNanos = TimeUnit.MILLISECONDS
				.toNanos(timeOutMilliseconds != null ? timeOutMilliseconds : Integer.MAX_VALUE);
		long endTime = System.nanoTime() + timeOutNanos;
		List<Message> receptions = new ArrayList<>();
		M answer = null;
		while (timeOutNanos > 0) {
			Message incoming = waitNext(timeOutNanos, TimeUnit.NANOSECONDS);
			if (incoming == null) {
				break;
			}
			answer = checkTypeFilterConvert(incoming, filter);
			if (answer != null) {
				break;
			}
			receptions.add(0, incoming);
			timeOutNanos = endTime - System.nanoTime();
		}
		receptions.forEach(messageBox::offerFirst);
		return answer;
	}

	/**
	 * Returns the next <i>howMany</i> messages that match the filter, waiting for
	 * ever if necessary until <i>howMany</i> matching messages become available.
	 * 
	 * @param <M>                 the expected type of the message
	 * @param timeOutMilliseconds the maximum time to wait, in milliseconds.
	 * @param howMany             the number of messages to wait for
	 * @param filter              the filter to use
	 * @return the list of messages that match the filter
	 */
	public synchronized <M extends Message> List<M> waitMessages(Integer timeOutMilliseconds, int howMany,
			Predicate<M> filter) {
		List<M> answers = new ArrayList<>(howMany);
		List<Message> receptions = new ArrayList<>();
		long timeOutNanos = TimeUnit.MILLISECONDS
				.toNanos(timeOutMilliseconds != null ? timeOutMilliseconds : Integer.MAX_VALUE);
		long endTime = System.nanoTime() + timeOutNanos;
		while (answers.size() < howMany && timeOutNanos > 0) {
			Message incoming = waitNext(timeOutNanos, TimeUnit.NANOSECONDS);
			if (incoming == null) {
				break;
			}
			M answer = checkTypeFilterConvert(incoming, filter);
			if (answer != null) {
				answers.add(answer);
			} else {
				receptions.add(0, incoming);
			}
			timeOutNanos = endTime - System.nanoTime();
		}
		receptions.forEach(messageBox::offerFirst);
		return answers;
	}

	/**
	 * Returns the next <i>howMany</i> messages that are replies to the
	 * <i>origin</i> message, waiting for ever if necessary until <i>howMany</i>
	 * replies become available.
	 * 
	 * Beware that if T is not the type of the answers, a {@link ClassCastException}
	 * will be thrown.
	 * 
	 * @param <M>                 the expected type of the message
	 * @param origin              the message to which the replies are searched.
	 * @param howMany             the number of replies to wait for
	 * @param timeOutMilliSeconds the maximum time to wait, in milliseconds.
	 * @return the list of replies to the <i>origin</i> message
	 */
	@SuppressWarnings("unchecked")
	public synchronized <M extends Message> List<M> waitAnswers(Message origin, int howMany,
			Integer timeOutMilliSeconds) {
		return (List<M>) waitMessages(timeOutMilliSeconds, howMany, new ConversationFilter(origin));
	}

	/**
	 * @param receptions
	 */
	void addAll(List<Message> receptions) {
		synchronized (messageBox) {
			messageBox.addAll(receptions);
		}
	}

	/**
	 * Returns the number of messages in the mailbox.
	 * 
	 * @return the number of messages in the mailbox.
	 */
	public int size() {
		return messageBox.size();
	}

}
