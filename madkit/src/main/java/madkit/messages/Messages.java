package madkit.messages;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * @author Fabien Michel
 * 
 * since MaDKit 6.0
 *
 */
public class Messages {

	public static <T extends Object, M extends ObjectMessage<T>> Map<T, List<M>> groupingByContent(Collection<M> messages) {
		return messages.stream().collect(Collectors.groupingBy(M::getContent));
	}

	/**
	 * Returns the message containing the maximum element of the given collection, according to the <i>natural ordering</i>
	 * of the elements contained in the message. All elements in the messages must implement the <tt>Comparable</tt>
	 * interface. Furthermore, all elements must be <i>mutually comparable</i> (that is, <tt>e1.compareTo(e2)</tt> must not
	 * throw a <tt>ClassCastException</tt> for any elements <tt>e1</tt> and <tt>e2</tt> in the collection).
	 * <p>
	 * This method iterates over the entire collection, hence it requires time proportional to the size of the collection.
	 *
	 * @param messages
	 *            the collection of messages whose maximum element is to be determined.
	 * @return the message containing the maximum element.
	 * @throws ClassCastException
	 *             if the content of the messages are not <i>mutually comparable</i>.
	 * @throws NoSuchElementException
	 *             if the collection is empty.
	 * @see Comparable
	 */
	public static <T extends Object & Comparable<? super T>, M extends ObjectMessage<T>> List<M> messagesWithMaxContent(Collection<M> messages) {
		return groupingByContent(messages).entrySet().stream().max(Comparator.comparing(Map.Entry::getKey)).get().getValue();
	}

	public static <T extends Object & Comparable<? super T>, M extends ObjectMessage<T>> List<M> messagesWithMinContent(Collection<M> messages) {
		return groupingByContent(messages).entrySet().stream().min(Comparator.comparing(Map.Entry::getKey)).get().getValue();
	}

	public static <T extends Number, M extends ObjectMessage<T>> double averageOnContent(Collection<M> messages) {
		return messages.stream().mapToDouble(m -> m.getContent().doubleValue()).average().getAsDouble();
	}

	public static <T extends Object & Comparable<? super T>, M extends ObjectMessage<T>> M anyMessageWithMaxContent(Collection<M> messages) {
		List<M> l = messagesWithMaxContent(messages);
		return l.isEmpty() ? null : l.get(0);
	}

	public static <T extends Object & Comparable<? super T>, M extends ObjectMessage<T>> M anyMessageWithMinContent(Collection<M> messages) {
		List<M> l = messagesWithMinContent(messages);
		return l.isEmpty() ? null : l.get(0);
	}

}
