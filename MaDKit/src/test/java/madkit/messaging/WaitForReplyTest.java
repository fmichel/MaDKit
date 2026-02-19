/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */
package madkit.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Predicate;

import org.testng.annotations.Test;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import madkit.kernel.DefaultTestAgent;
import madkit.kernel.MadkitConcurrentTestCase;
import madkit.kernel.Message;
import madkit.messages.StringMessage;
import madkit.test.agents.CGRAgent;

/**
 *
 * @since MaDKit 5.0.4
 * @version 0.9
 * 
 */
@SuppressWarnings("all")
public class WaitForReplyTest extends MadkitConcurrentTestCase {

	protected Predicate<Message> filter = m -> m instanceof StringMessage;

	@Test
	public void waitSuccess() {
		runTest(new CGRAgent() {
			@Override
			public void behaviorInActivate() {
				receiveMessage(new Message());
				receiveMessage(new Message());
				assertThat(launchAgent(new ForEverReplierAgent(StringMessage.class))).as("launch replier")
						.isEqualTo(SUCCESS);
				getLogger().info("sending message " + getAgentWithRole(COMMUNITY, GROUP, ROLE));
				send(new Message(), COMMUNITY, GROUP, ROLE);
				pause(20);
				receiveMessage(new Message());
				getLogger().info(getMailbox().toString());
				assertThat(getMailbox().waitNext(filter)).as("waitNext(filter) result").isNotNull();
				getLogger().info(getMailbox().toString());
				assertThat(getMailbox().size()).as("mailbox size").isEqualTo(3);
				getLogger().info(nextMessage().toString());
				resume();
			}
		});
	}

	@Test
	public void waitReturnNull() {
		runTest(new CGRAgent() {
			@Override
			public void behaviorInActivate() {
				receiveMessage(new Message());
				receiveMessage(new Message());
				receiveMessage(new Message());
				getLogger().info(getMailbox().toString());
				launchAgent(new ForEverReplierAgent(StringMessage.class));
				send(new Message(), GROUP, ROLE, COMMUNITY);
				pause(20);
				assertThat(getMailbox().waitNext(1, filter)).as("waitNext with timeout").isNull();
				getLogger().info(getMailbox().toString());
				assertThat(getMailbox().nextMatches(null).size()).as("nextMatches size").isEqualTo(3);
				resume();
			}
		});
	}

	@Test
	public void nullArg() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				getMailbox().waitNext(1);// not fail when messagebox is empty
				try {
					receiveMessage(new Message());
					getMailbox().waitNext(null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					resume();
				}
			}
		});
	}
}
