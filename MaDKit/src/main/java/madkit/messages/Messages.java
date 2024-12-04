package madkit.messages;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * This class provides utility methods for working with collections of messages.
 * 
 * @author Fabien Michel
 * @since MaDKit 6.0
 */
public class Messages {

	private Messages() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Groups messages by their content.
	 *
	 * @param <T>      the type of the content of the messages
	 * @param <M>      the type of the messages
	 * @param messages the collection of messages to be grouped
	 * @return a map where the keys are the message contents and the values are
	 *         lists of messages with that content
	 */
	public static <T, M extends ObjectMessage<T>> Map<T, List<M>> groupingByContent(Collection<M> messages) {
		return messages.stream().collect(Collectors.groupingBy(M::getContent));
	}

	/**
	 * Returns the messages containing the maximum element of the given collection,
	 * according to the natural ordering of the elements contained in the message.
	 * All elements in the messages must implement the {@link Comparable} interface.
	 * Furthermore, all elements must be mutually comparable (that is,
	 * {@code e1.compareTo(e2)} must not throw a {@link ClassCastException} for any
	 * elements {@code e1} and {@code e2} in the collection).
	 *
	 * @param <T>      the type of the content of the messages, which must be
	 *                 comparable
	 * @param <M>      the type of the messages
	 * @param messages the collection of messages whose maximum element is to be
	 *                 determined
	 * @return the list of messages containing the maximum element
	 * @throws ClassCastException     if the content of the messages are not
	 *                                mutually comparable
	 * @throws NoSuchElementException if the collection is empty
	 * @see Comparable
	 */
	public static <T extends Object & Comparable<? super T>, M extends ObjectMessage<T>> List<M> messagesWithMaxContent(
			Collection<M> messages) {
		Map<T, List<M>> groupedByContent = groupingByContent(messages);
		var max = groupedByContent.keySet().stream().max(Comparator.naturalOrder()).orElseGet(() -> null);
		if (max == null) {
			return Collections.emptyList();
		}
		return groupedByContent.get(max);
	}

	/**
	 * Returns the messages containing the minimum element of the given collection,
	 * according to the natural ordering of the elements contained in the message.
	 * All elements in the messages must implement the {@link Comparable} interface.
	 * Furthermore, all elements must be mutually comparable (that is,
	 * {@code e1.compareTo(e2)} must not throw a {@link ClassCastException} for any
	 * elements {@code e1} and {@code e2} in the collection).
	 *
	 * @param <T>      the type of the content of the messages, which must be
	 *                 comparable
	 * @param <M>      the type of the messages
	 * @param messages the collection of messages whose minimum element is to be
	 *                 determined
	 * @return the list of messages containing the minimum element
	 * @throws ClassCastException     if the content of the messages are not
	 *                                mutually comparable
	 * @throws NoSuchElementException if the collection is empty
	 * @see Comparable
	 */
	public static <T extends Object & Comparable<? super T>, M extends ObjectMessage<T>> List<M> messagesWithMinContent(
			Collection<M> messages) {
		Map<T, List<M>> groupedByContent = groupingByContent(messages);
		var min = groupedByContent.keySet().stream().min(Comparator.naturalOrder()).orElseGet(() -> null);
		if (min == null) {
			return Collections.emptyList();
		}
		return groupedByContent.get(min);
	}

	/**
	 * Calculates the average of the contents of the given collection of messages.
	 * The content of the messages must be of type {@link Number}.
	 *
	 * @param <T>      the type of the content of the messages, which must be a
	 *                 number
	 * @param <M>      the type of the messages
	 * @param messages the collection of messages whose content average is to be
	 *                 calculated
	 * @return the average of the contents of the messages
	 */
	public static <T extends Number, M extends ObjectMessage<T>> double averageOnContent(Collection<M> messages) {
		return messages.stream().mapToDouble(m -> m.getContent().doubleValue()).average().getAsDouble();
	}

	/**
	 * Returns any message containing the maximum element of the given collection,
	 * according to the natural ordering of the elements contained in the message.
	 * All elements in the messages must implement the {@link Comparable} interface.
	 * Furthermore, all elements must be mutually comparable (that is,
	 * {@code e1.compareTo(e2)} must not throw a {@link ClassCastException} for any
	 * elements {@code e1} and {@code e2} in the collection).
	 *
	 * @param <T>      the type of the content of the messages, which must be
	 *                 comparable
	 * @param <M>      the type of the messages
	 * @param messages the collection of messages whose maximum element is to be
	 *                 determined
	 * @return any message containing the maximum element, or {@code null} if the
	 *         collection is empty
	 * @throws ClassCastException     if the content of the messages are not
	 *                                mutually comparable
	 * @throws NoSuchElementException if the collection is empty
	 * @see Comparable
	 */
	public static <T extends Object & Comparable<? super T>, M extends ObjectMessage<T>> M anyMessageWithMaxContent(
			Collection<M> messages) {
		List<M> l = messagesWithMaxContent(messages);
		return l.isEmpty() ? null : l.get(0);
	}

	/**
	 * Returns any message containing the minimum element of the given collection,
	 * according to the natural ordering of the elements contained in the message.
	 * All elements in the messages must implement the {@link Comparable} interface.
	 * Furthermore, all elements must be mutually comparable (that is,
	 * {@code e1.compareTo(e2)} must not throw a {@link ClassCastException} for any
	 * elements {@code e1} and {@code e2} in the collection).
	 *
	 * @param <T>      the type of the content of the messages, which must be
	 *                 comparable
	 * @param <M>      the type of the messages
	 * @param messages the collection of messages whose minimum element is to be
	 *                 determined
	 * @return any message containing the minimum element, or {@code null} if the
	 *         collection is empty
	 * @throws ClassCastException     if the content of the messages are not
	 *                                mutually comparable
	 * @throws NoSuchElementException if the collection is empty
	 * @see Comparable
	 */
	public static <T extends Object & Comparable<? super T>, M extends ObjectMessage<T>> M anyMessageWithMinContent(
			Collection<M> messages) {
		List<M> l = messagesWithMinContent(messages);
		return l.isEmpty() ? null : l.get(0);
	}

}
