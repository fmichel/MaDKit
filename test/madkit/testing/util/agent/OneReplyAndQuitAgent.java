package madkit.testing.util.agent;

import madkit.kernel.Message;


public class OneReplyAndQuitAgent extends ForEverReplierAgent {
	
	@Override
	protected void live() {
		sendReply(waitNextMessage(), new Message());
	}

}
