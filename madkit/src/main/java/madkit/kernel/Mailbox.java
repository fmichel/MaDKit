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

/**
 * @author Fabien Michel
 *
 */
public class Mailbox {
	final BlockingDeque<Message> messageBox = new LinkedBlockingDeque<>();

	/**
	 * @return the messageBox
	 */
	BlockingDeque<Message> getMessageBox() {
		return messageBox;
	}
	
	@Override
	public String toString() {
		return messageBox.toString();
	}

	/**
	 * Retrieves and removes the oldest received message contained in the mailbox.
	 *
	 * @return The next message or <code>null</code> if the message box is empty.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Message> T next() {
		return (T) messageBox.poll();
	}

	/**
	 * Retrieves and removes the first oldest message of the mailbox that matches
	 * the filter.
	 *
	 * @return The next acceptable message or <code>null</code> if such message has
	 *         not been found.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Message> T next(Predicate<? super Message> filter) {
		Objects.requireNonNull(filter);
		synchronized (messageBox) {
			for (final Iterator<Message> iterator = messageBox.iterator(); iterator.hasNext();) {
				final Message m = iterator.next();
				if (filter.test(m)) {
					iterator.remove();
					return (T) m;
				}
			}
		}
		return null;
	}

	/**
	 * Retrieves and removes all the messages of the mailbox that match the filter,
	 * in the order they were received.
	 *
	 * @param filter if <code>null</code> all the messages are returned and removed
	 *               from the mailbox.
	 * @return the ordered list of matching messages, or an empty list if none has
	 *         been found.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Message> List<T> nextMatches(Predicate<? super Message> filter) {
		final List<T> match = new ArrayList<>();
		synchronized (messageBox) {
			if (filter == null) {
				messageBox.drainTo((Collection<? super Message>) match);
			} else {
				for (Iterator<Message> iterator = messageBox.iterator(); iterator.hasNext();) {
					final Message m = iterator.next();
					if (filter.test(m)) {
						iterator.remove();
						match.add((T) m);
					}
				}
			}
		}
		return match;
	}

	/**
	 * Gets the last received message.
	 *
	 * @return the last received message or <code>null</code> if the mailbox is
	 *         empty.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Message> T getNewest() {
		return (T) messageBox.pollLast();
	}

	/**
	 * Gets the last received message according to a filter.
	 *
	 * @param filter the message filter to use
	 * @return the last received message that matches the filter or
	 *         <code>null</code> if such message has not been found.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Message> T getNewest(final Predicate<? super Message> filter) {
		for (final Iterator<Message> iterator = messageBox.descendingIterator(); iterator.hasNext();) {
			final Message message = iterator.next();
			if (filter.test(message)) {
				iterator.remove();
				return (T) message;
			}
		}
		return null;
	}

	/**
	 * Purges the mailbox and returns the most recent received message at that time.
	 *
	 * @return the most recent received message or <code>null</code> if the mailbox
	 *         is already empty.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Message> T purge() {
		synchronized (messageBox) {
			final Message m = messageBox.pollLast();
			messageBox.clear();
			return (T) m;
		}
	}

	/**
	 * Gets the next message which is a reply to the <i>originalMessage</i>.
	 *
	 * @param originalMessage the message to which a reply is searched.
	 * @return a reply to the <i>originalMessage</i> or <code>null</code> if no
	 *         reply to this message has been received.
	 */
	public <T extends Message> T getReply(final Message originalMessage) {
		return next(new ConversationFilter(originalMessage));
	}

	/**
	 * Tells if there is a message in the mailbox
	 *
	 * @return <code>true</code> if there is no message in the mailbox.
	 */
	public boolean isEmpty() {
		return messageBox.isEmpty();
	}

	/**
	 * This method is the blocking version of nextMessage(). If there is no message
	 * in the mailbox, it suspends the agent life until a message is received
	 *
	 * @see #waitNext(long)
	 * @return the first received message
	 */
	@SuppressWarnings("unchecked")
	public <T extends Message> T waitNext() {
		return (T) waitingNextForEver();
	}

	@SuppressWarnings("unchecked")
	public <T extends Message> T waitNext(final long timeout, final TimeUnit unit) {
		try {
			return (T) messageBox.poll(timeout, unit);
		} catch (InterruptedException e) {
			throw new AgentInterruptedException();
		}
	}

	public <T extends Message> T waitNext(final long timeoutMilliSeconds) {
			return waitNext(timeoutMilliSeconds, TimeUnit.MILLISECONDS);
	}

	/**
	 * Retrieves and removes the next message that complies with the filter, waiting
	 * for ever if necessary until a matching message becomes available.
	 * 
	 * @param filter
	 * 
	 * @return the first received message that matches the filter
	 */
	@SuppressWarnings("unchecked")
	public <T extends Message> T waitNext(final Predicate<? super Message> filter) {
		Message m = null;
		final List<Message> receptions = new ArrayList<>();
		try {
			while (!filter.test(m = messageBox.take())) {
				receptions.add(0,m);
			}
		} catch (InterruptedException e) {
			throw new AgentInterruptedException();
		} finally {
			receptions.stream().forEach(messageBox::offerFirst);
		}
		return (T) m;
	}

	/**
	 * This method gets the next message of the mailbox or waits for a new incoming
	 * acceptable message up to a certain delay.
	 * 
	 * @param filter
	 * @param timeOutMilliseconds the maximum time to wait, in milliseconds.
	 * 
	 * @return a message that matches or <code>null</code> otherwise.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Message> T waitNext(final Predicate<? super Message> filter, final Integer timeOutMilliseconds) {
		final List<Message> receptions = new ArrayList<>();
		long timeOutNanos = TimeUnit.MILLISECONDS.toNanos(timeOutMilliseconds!=null ? timeOutMilliseconds : Integer.MAX_VALUE);
		final long endTime = System.nanoTime() + timeOutNanos;
		Message answer = waitNext(timeOutNanos, TimeUnit.NANOSECONDS);
		while (answer != null && ! filter.test(answer) && timeOutNanos > 0) {
			receptions.add(0,answer);
			timeOutNanos = endTime - System.nanoTime();
			answer = waitNext(timeOutNanos, TimeUnit.NANOSECONDS);
		}
		receptions.stream().forEach(messageBox::offerFirst);
		return (T) answer;
	}

	@SuppressWarnings("unchecked")
	public <T extends Message> List<T> waitMessages(final Predicate<? super Message> filter, final int howMany, final Integer timeOutMilliseconds) {
		final List<Message> answers = new ArrayList<>(howMany);
		final List<Message> receptions = new ArrayList<>();
		long timeOutNanos = TimeUnit.MILLISECONDS.toNanos(timeOutMilliseconds!=null ? timeOutMilliseconds : Integer.MAX_VALUE);
		final long endTime = System.nanoTime() + timeOutNanos;
		while (answers.size() < howMany && timeOutNanos > 0) {
			Message answer = waitNext(timeOutNanos, TimeUnit.NANOSECONDS);
			if(filter.test(answer)) {
				answers.add(answer);
			} else {
				receptions.add(0,answer);
			}
			timeOutNanos = endTime - System.nanoTime();
		}
		receptions.stream().forEach(messageBox::offerFirst);
		return (List<T>) answers;
	}

	public <T extends Message> List<T> waitAnswers(final Message message, final int howMany, final Integer timeOutMilliSeconds) {
		return waitMessages(new ConversationFilter(message), howMany, timeOutMilliSeconds);
	}

	/**
	 * @param receptions
	 */
	void addAll(final List<Message> receptions) {
		synchronized (messageBox) {
			messageBox.addAll(receptions);
		}
	}
	
	

	/**
	 * @return message
	 * @since MaDKit 5
	 */
	Message waitingNextForEver() {
		try {
			return messageBox.take();
		} catch (InterruptedException e) {
			throw new AgentInterruptedException();
		}
	}

	/**
	 * @return
	 */
	public int size() {
		return messageBox.size();
	}

}
