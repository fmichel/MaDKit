package madkit.kernel;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.function.Predicate;

import org.testng.annotations.Test;

import madkit.messages.StringMessage;

public class MailboxTest {

	@Test
	public void givenMailboxWithMessages_WhenNextIsCalled_ThenReturnCorrectMessage() {
		Mailbox mb = new Mailbox();
		mb.add(new StringMessage("first"));
		mb.add(new Message());

		// Retrieve the next message
		StringMessage message = mb.next();

		assertEquals(message.getContent(), "first");
		assertEquals(mb.size(), 1); // Ensure the message was removed from the mailbox
	}

	@Test
	public void givenANoneEmptyMailbox_WhenFilterMatch_thenReturnTheMessage() {
		Mailbox mb = new Mailbox();
		mb.add(new StringMessage("test"));
		StringMessage sm = mb.next(m -> {
			return m.getContent().equals("test");
		});
		assertNotNull(sm);
		assertTrue(mb.isEmpty());
	}

	@Test
	public void givenANoneEmptyMailbox_WhenNextIsUsedFilterDoesNotMatch_thenNullIsReturned() {
		Mailbox mb = new Mailbox();
		mb.add(new Message());
		mb.add(new StringMessage("testa"));
		StringMessage sm = mb.next(m -> {
			return m.getContent().equals("test");
		});
		assertNull(sm);
		assertFalse(mb.isEmpty());
	}

	@Test
	public void givenAMailboxWithThreeMessages_WhenFilterMatchTheSecondOne_thenTheOrderIsPreserved() {
		Mailbox mb = new Mailbox();
		mb.add(new Message());
		mb.add(new StringMessage("test"));
		mb.add(new Message());
		StringMessage sm = mb.next(m -> {
			return m.getContent().equals("test");
		});
		assertNotNull(sm);
		assertEquals(mb.size(), 2);
	}

	@Test
	public void givenAMailboxWithThreeMessages_WhenReturnTypeDoesNotMatch_thenNullIsReturned() {
		Mailbox mb = new Mailbox();
		mb.add(new Message());
		mb.add(new Message());
		StringMessage sm = mb.next(StringMessage.class::isInstance);
		assertNull(sm);
		assertEquals(mb.size(), 2);
	}

	@Test
	public void givenAMailboxWithThreeStringMessages_WhenNextMatchesFilterMatch_ThenTheCorrectListIsReturned() {
		Mailbox mb = new Mailbox();
		mb.add(new Message());
		mb.add(new StringMessage("1"));
		mb.add(new StringMessage("2"));
		mb.add(new StringMessage("3"));
		List<StringMessage> list = mb.nextMatches(StringMessage.class::isInstance);
		assertEquals(list.size(), 3);
		assertEquals(list.get(0).getContent(), "1");
		assertEquals(mb.size(), 1);
	}

	@Test
	public void givenAMailboxWithMessages_WhenNextMatchesFilterMatchOnProperty_ThenTheCorrectListIsReturned() {
		Mailbox mb = new Mailbox();
		mb.add(new Message());
		mb.add(new StringMessage("1"));
		List<StringMessage> list = mb.nextMatches(m -> {
			return m.getContent().equals("1");
		});
		assertEquals(list.size(), 1);
		assertEquals(list.get(0).getContent(), "1");
		assertEquals(mb.size(), 1);
	}

	@Test
	public void givenMailboxWithMessages_WhenGetNewestWithFilter_ThenReturnCorrectMessage() {
		Mailbox mb = new Mailbox();
		mb.add(new StringMessage("first"));
		mb.add(new StringMessage("second"));
		mb.add(new StringMessage("third"));
		mb.add(new Message());

		// Filter to get the newest StringMessage
		StringMessage newestStringMessage = mb.getNewest(StringMessage.class::isInstance);

		assertEquals(newestStringMessage.getContent(), "third");
		assertEquals(mb.size(), 3); // Ensure the message was removed from the mailbox
	}

	@Test
	public void givenMailboxWithMessages_WhenGetNewest_ThenReturnCorrectMessage() {
		Mailbox mb = new Mailbox();
		mb.add(new StringMessage("first"));
		mb.add(new StringMessage("second"));
		mb.add(new StringMessage("third"));

		// Filter to get the newest StringMessage
		StringMessage newestStringMessage = mb.getNewest();

		assertEquals(newestStringMessage.getContent(), "third");
		assertEquals(mb.size(), 2); // Ensure the message was removed from the mailbox
	}

	@Test
	public void givenMailboxWithMessages_WhenGetAllWithFilter_ThenReturnCorrectMessages() {
		Mailbox mb = new Mailbox();
		mb.add(new Message());
		mb.add(new StringMessage("first"));
		mb.add(new StringMessage("second"));

		// Filter to get all StringMessage instances
		List<StringMessage> stringMessages = mb.getAll(StringMessage.class::isInstance);

		assertEquals(stringMessages.size(), 2);
		assertEquals(stringMessages.get(0).getContent(), "first");
		assertEquals(stringMessages.get(1).getContent(), "second");
		assertEquals(mb.size(), 1); // Ensure the messages were removed from the mailbox
	}

	@Test
	public void givenMailboxWithMessages_WhenWaitNextWithFilter_ThenReturnCorrectMessage() {
		Mailbox mb = new Mailbox();
		Thread.ofVirtual().start(() -> {
			mb.add(new Message());
			mb.add(new StringMessage("first"));
		});
		StringMessage message = mb.waitNext(StringMessage.class::isInstance);

		assertEquals(message.getContent(), "first");
		assertEquals(mb.size(), 1); // Ensure the message was removed from the mailbox
	}

	@Test
	public void givenMailboxWithMessages_WhenWaitNextWithFilterAndTimeout_ThenReturnCorrectMessage()
			throws InterruptedException {
		Mailbox mb = new Mailbox();
		Thread.ofVirtual().start(() -> {
			mb.add(new Message());
			mb.add(new StringMessage("first"));
		});

		// Filter to get the next StringMessage
		StringMessage message = mb.waitNext(5000, StringMessage.class::isInstance);

		assertNotNull(message);
		assertEquals(message.getContent(), "first");
		assertEquals(mb.size(), 1); // Ensure the message was removed from the mailbox
	}

	@Test
	public void givenMailboxWithNoMatchingMessages_WhenWaitNextWithFilterAndTimeout_ThenReturnNull()
			throws InterruptedException {
		Mailbox mb = new Mailbox();
		mb.add(new Message());
		mb.add(new StringMessage("first"));

		StringMessage message = mb.waitNext(50, m -> m.getContent().equals("second"));

		assertNull(message);
		assertEquals(mb.size(), 2);
	}

	@Test
	public void givenMailboxWithMessages_WhenWaitNextWithFilterAndTimeoutExpires_ThenReturnNull()
			throws InterruptedException {
		Mailbox mb = new Mailbox();
		mb.add(new Message());

		// Filter to get the next StringMessage
		StringMessage message = mb.waitNext(10, StringMessage.class::isInstance);

		assertNull(message);
		assertEquals(mb.size(), 1); // Ensure no message was removed from the mailbox
	}

	@Test
	public void givenMailboxWithMessages_WhenWaitMessagesWithFilterAndTimeout_ThenReturnCorrectMessages() {
		Mailbox mb = new Mailbox();
		mb.add(new Message());
		mb.add(new StringMessage("first"));
		mb.add(new StringMessage("second"));
		mb.add(new StringMessage("third"));

		// Filter to get StringMessage instances
		List<StringMessage> messages = mb.waitMessages(10, 2, StringMessage.class::isInstance);

		assertNotNull(messages);
		assertEquals(messages.size(), 2);
		assertEquals(messages.get(0).getContent(), "first");
		assertEquals(messages.get(1).getContent(), "second");
		assertEquals(mb.size(), 2); // Ensure the messages were removed from the mailbox
	}

	@Test
	public void givenMailboxWithNoMatchingMessages_WhenWaitMessagesWithFilterAndTimeout_ThenReturnEmptyList()
			throws InterruptedException {
		Mailbox mb = new Mailbox();
		Thread.ofVirtual().start(() -> {
			mb.add(new Message());
			mb.add(new StringMessage("first"));
		});

		// Filter to get StringMessage instances with content "second"
		List<StringMessage> messages = mb.waitMessages(50, 2, m -> m.getContent().equals("second"));

		assertNotNull(messages);
		assertTrue(messages.isEmpty());
		assertEquals(mb.size(), 2); // Ensure no messages were removed from the mailbox
	}

	@Test
	public void givenMailboxWithMessages_WhenWaitMessagesWithFilterAndTimeoutExpires_ThenReturnPartialList()
			throws InterruptedException {
		Mailbox mb = new Mailbox();
		mb.add(new Message());
		mb.add(new StringMessage("first"));
		mb.add(new StringMessage("second"));
		Thread.ofVirtual().start(() -> {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mb.add(new StringMessage("third"));
			mb.add(new StringMessage("fourth"));
		});

		// Filter to get StringMessage instances
		List<StringMessage> messages = mb.waitMessages(10, 2, StringMessage.class::isInstance);

		assertNotNull(messages);
		assertEquals(messages.size(), 2);
		assertEquals(messages.get(0).getContent(), "first");
		assertEquals(messages.get(1).getContent(), "second");
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(mb.size(), 3);
	}

	@Test
	public void givenMailboxWithReplies_WhenWaitAnswersWithOriginAndTimeout_ThenReturnCorrectReplies()
			throws InterruptedException {
		Mailbox mb = new Mailbox();
		Message origin = new Message();
		Thread.ofVirtual().start(() -> {
			Message reply1 = new StringMessage("reply1");
			reply1.setIDFrom(origin);
			mb.add(reply1);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Message reply2 = new StringMessage("reply2");
			reply2.setIDFrom(origin);
			mb.add(reply2);
		});

		// Wait for replies to the origin message
		List<StringMessage> replies = mb.waitAnswers(origin, 2, 5000);

		assertNotNull(replies);
		assertEquals(replies.size(), 2);
		assertEquals(replies.get(0).getContent(), "reply1");
		assertEquals(replies.get(1).getContent(), "reply2");
		assertEquals(mb.size(), 0); // Ensure the messages were removed from the mailbox
	}
	
	@Test
	public void givenMailboxWithNoMatchingReplies_WhenWaitAnswersWithOriginAndTimeout_ThenReturnEmptyList() throws InterruptedException {
	    Mailbox mb = new Mailbox();
	    Message origin = new Message();
	    mb.add(new Message());

	    // Wait for replies to the origin message
	    List<StringMessage> replies = mb.waitAnswers(origin, 2, 5);

	    assertNotNull(replies);
	    assertTrue(replies.isEmpty());
	    assertEquals(mb.size(), 1); // Ensure no messages were removed from the mailbox
	}

	@Test
	public void givenMailboxWithMessages_WhenWaitNextIsCalled_ThenReturnCorrectMessage() throws InterruptedException {
	    Mailbox mb = new Mailbox();
	    mb.add(new StringMessage("first"));
	    mb.add(new Message());

	    // Wait for the next message
	    StringMessage message = mb.waitNext();

	    assertNotNull(message);
	    assertEquals(message.getContent(), "first");
	    assertEquals(mb.size(), 1); // Ensure the message was removed from the mailbox
	}

	@Test
	public void givenEmptyMailbox_WhenWaitNextIsCalled_ThenBlockUntilMessageIsAdded() throws InterruptedException {
	    Mailbox mb = new Mailbox();
	    Thread.ofVirtual().start(() -> {
	        try {
	            Thread.sleep(50);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	        mb.add(new StringMessage("delayed"));
	    });

	    // Wait for the next message
	    StringMessage message = mb.waitNext();

	    assertNotNull(message);
	    assertEquals(message.getContent(), "delayed");
	    assertTrue(mb.isEmpty()); // Ensure the message was removed from the mailbox
	}


@Test
public void givenMailboxWithMessages_WhenWaitNextWithTimeoutIsCalled_ThenReturnCorrectMessage() throws InterruptedException {
    Mailbox mb = new Mailbox();
    mb.add(new StringMessage("first"));
    mb.add(new Message());

    // Wait for the next message with timeout
    StringMessage message = mb.waitNext(5000);

    assertNotNull(message);
    assertEquals(message.getContent(), "first");
    assertEquals(mb.size(), 1); // Ensure the message was removed from the mailbox
}

@Test
public void givenEmptyMailbox_WhenWaitNextWithTimeoutIsCalled_ThenReturnNull() throws InterruptedException {
    Mailbox mb = new Mailbox();

    // Wait for the next message with timeout
    StringMessage message = mb.waitNext(50);

    assertNull(message);
    assertTrue(mb.isEmpty()); // Ensure no message was added to the mailbox
}

@Test
public void givenMailboxWithMessages_WhenPurgeWithFilter_ThenReturnCorrectMessage() {
    Mailbox mb = new Mailbox();
    mb.add(new Message());
    mb.add(new StringMessage("first"));
    mb.add(new StringMessage("second"));

    // Filter to get the newest StringMessage
    StringMessage message = mb.purge(StringMessage.class::isInstance);

    assertNotNull(message);
    assertEquals(message.getContent(), "second");
    assertTrue(mb.isEmpty()); // Ensure the mailbox is empty after purge
}

@Test
public void givenMailboxWithNoMatchingMessages_WhenPurgeWithFilter_ThenReturnNull() {
    Mailbox mb = new Mailbox();
    mb.add(new Message());
    mb.add(new StringMessage("first"));

    // Filter to get a StringMessage with content "second"
    StringMessage message = mb.purge(m -> m.getContent().equals("second"));

    assertNull(message);
    assertTrue(mb.isEmpty()); // Ensure the mailbox is empty after purge
}

@Test
public void givenMailboxWithMessages_WhenPurgeWithFilterAndNoMatch_ThenReturnNull() {
    Mailbox mb = new Mailbox();
    mb.add(new Message());
    mb.add(new StringMessage("first"));

    // Filter to get a Message instance
    StringMessage message = mb.purge(m -> m.getContent().equals("second"));

    assertNull(message);
    assertTrue(mb.isEmpty()); // Ensure the mailbox is empty after purge
}

@Test
public void givenMailboxWithMessages_WhenPurgeIsCalled_ThenReturnMostRecentMessage() {
    Mailbox mb = new Mailbox();
    mb.add(new Message());
    mb.add(new StringMessage("first"));
    mb.add(new StringMessage("second"));

    // Purge the mailbox
    StringMessage message = mb.purge();

    assertNotNull(message);
    assertEquals(message.getContent(), "second");
    assertTrue(mb.isEmpty()); // Ensure the mailbox is empty after purge
}

@Test
public void givenEmptyMailbox_WhenPurgeIsCalled_ThenReturnNull() {
    Mailbox mb = new Mailbox();

    // Purge the mailbox
    Message message = mb.purge();

    assertNull(message);
    assertTrue(mb.isEmpty()); // Ensure the mailbox is empty
}

@Test
public void givenMailboxWithReplies_WhenGetReplyIsCalled_ThenReturnCorrectReply() {
    Mailbox mb = new Mailbox();
    Message origin = new Message();
    Message reply1 = new StringMessage("reply1");
    reply1.setIDFrom(origin);
    mb.add(new StringMessage("other"));
    mb.add(reply1);
    mb.add(new StringMessage("other"));

    // Get the reply to the origin message
    StringMessage reply = mb.getReply(origin);

    assertNotNull(reply);
    assertEquals(reply.getContent(), "reply1");
    assertEquals(mb.size(), 2); // Ensure the reply was removed from the mailbox
}

@Test
public void givenMailboxWithNoMatchingReplies_WhenGetReplyIsCalled_ThenReturnNull() {
    Mailbox mb = new Mailbox();
    Message origin = new Message();
    mb.add(new StringMessage("other"));

    // Get the reply to the origin message
    StringMessage reply = mb.getReply(origin);

    assertNull(reply);
    assertEquals(mb.size(), 1); 
}
}
