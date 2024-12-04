
package madkit.messages;

import static org.testng.Assert.*;
import org.testng.annotations.Test;
import java.util.*;

public class MessagesTest {

	@Test
	public void givenMessages_whenGroupedByContent_thenCorrectGrouping() {
		StringMessage msg1 = new StringMessage("content1");
		StringMessage msg2 = new StringMessage("content2");
		StringMessage msg3 = new StringMessage("content1");
		List<StringMessage> messages = Arrays.asList(msg1, msg2, msg3);

		Map<String, List<StringMessage>> grouped = Messages.groupingByContent(messages);

		assertEquals(grouped.get("content1").size(), 2);
		assertEquals(grouped.get("content2").size(), 1);
	}

	@Test
	public void givenMessages_whenMessagesWithMaxContent_thenCorrectMessages() {
		IntegerMessage msg1 = new IntegerMessage(1);
		IntegerMessage msg2 = new IntegerMessage(3);
		IntegerMessage msg3 = new IntegerMessage(2);
		List<IntegerMessage> messages = Arrays.asList(msg1, msg2, msg3);

		List<IntegerMessage> maxMessages = Messages.messagesWithMaxContent(messages);

		assertEquals(maxMessages.size(), 1);
		assertEquals(maxMessages.get(0).getContent().intValue(), 3);
	}

	@Test
	public void givenMessages_whenMessagesWithMinContent_thenCorrectMessages() {
		IntegerMessage msg1 = new IntegerMessage(1);
		IntegerMessage msg2 = new IntegerMessage(3);
		IntegerMessage msg3 = new IntegerMessage(2);
		List<IntegerMessage> messages = Arrays.asList(msg1, msg2, msg3);

		List<IntegerMessage> minMessages = Messages.messagesWithMinContent(messages);

		assertEquals(minMessages.size(), 1);
		assertEquals(minMessages.get(0).getContent().intValue(), 1);
	}

	@Test
	public void givenMessages_whenAverageOnContent_thenCorrectAverage() {
		IntegerMessage msg1 = new IntegerMessage(1);
		IntegerMessage msg2 = new IntegerMessage(3);
		IntegerMessage msg3 = new IntegerMessage(2);
		List<IntegerMessage> messages = Arrays.asList(msg1, msg2, msg3);

		double average = Messages.averageOnContent(messages);

		assertEquals(average, 2.0);
	}

	@Test
	public void givenMessages_whenAnyMessageWithMaxContent_thenCorrectMessage() {
		IntegerMessage msg1 = new IntegerMessage(1);
		IntegerMessage msg2 = new IntegerMessage(3);
		IntegerMessage msg3 = new IntegerMessage(2);
		List<IntegerMessage> messages = Arrays.asList(msg1, msg2, msg3);

		IntegerMessage maxMessage = Messages.anyMessageWithMaxContent(messages);

		assertNotNull(maxMessage);
		assertEquals(maxMessage.getContent().intValue(), 3);
	}

	@Test
	public void givenMessages_whenAnyMessageWithMinContent_thenCorrectMessage() {
		IntegerMessage msg1 = new IntegerMessage(1);
		IntegerMessage msg2 = new IntegerMessage(3);
		IntegerMessage msg3 = new IntegerMessage(2);
		List<IntegerMessage> messages = Arrays.asList(msg1, msg2, msg3);

		IntegerMessage minMessage = Messages.anyMessageWithMinContent(messages);

		assertNotNull(minMessage);
		assertEquals(minMessage.getContent().intValue(), 1);
	}

	@Test
	public void givenEmptyMessages_whenMessagesWithMaxContent_thenReturnEmptyList() {
		List<IntegerMessage> messages = Collections.emptyList();
		List<IntegerMessage> maxMessages = Messages.messagesWithMaxContent(messages);
		assertTrue(maxMessages.isEmpty());
	}

	@Test
	public void givenEmptyMessages_whenMessagesWithMinContent_thenThrowsException() {
		List<IntegerMessage> messages = Collections.emptyList();
		List<IntegerMessage> minMessages = Messages.messagesWithMinContent(messages);
		assertTrue(minMessages.isEmpty());

	}
}
