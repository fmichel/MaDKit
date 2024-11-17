package madkit.kernel;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;
import static madkit.kernel.JunitMadkit.COMMUNITY;
import static madkit.kernel.JunitMadkit.GROUP;
import static madkit.kernel.JunitMadkit.ROLE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.time.LocalDateTime;

/**
 * @author Fabien Michel
 *
 */
public class GenericTestAgent extends Agent implements TestHelpAgent {

	private boolean goThroughEnd = false;
	private boolean oneMethodHasBeenActivated = false;

	/**
	 * 
	 */
	public GenericTestAgent() {
//		getLogger().setLevel(Level.ALL);
//		getLogger().info("********* INIT ***********");
	}

	@Override
	protected void onActivation() {
		orgInActivate();
		behaviorInActivate();
	}

	@Override
	protected void onEnding() {
		goThroughEnd = true;
		orgInEnd();
		behaviorInEnd();
	}

	public boolean didPassThroughEnd() {
		return goThroughEnd;
	}

	protected void checkTermination() {
		assertFalse(alive.get());
		assertEquals(kernel, KernelAgent.deadKernel);
	}

	@Override
	public void createDefaultCGR() {
		createGroup(COMMUNITY, GROUP, false, null);
		assertEquals(requestRole(COMMUNITY, GROUP, ROLE, null), SUCCESS);
	}

	protected void replyToLastReiceivedMessage() {
		Message m = waitNextMessage();
		reply(new Message(), m);
	}

	@SuppressWarnings("unused")
	private void privateMethod() {
		System.out.println("private method");
		setOneMethodHasBeenActivated(true);
	}

	protected void protectedMethod() {
		System.out.println("protected method");
	}

	public void publicMethod() {
		System.out.println("public method");
	}

	@SuppressWarnings("unused")
	private void privateMethodWithArgs(String s, Object o) {
		System.err.println(s);
		System.err.println(o);
		setOneMethodHasBeenActivated(true);
	}

	@SuppressWarnings("unused")
	private void privateMethodWithPrimitiveArgs(String s, int i) {
		System.err.println(s);
		System.err.println(i);
		setOneMethodHasBeenActivated(true);
	}

	public void publicMethodWithPrimitiveArgs(String s, int i) {
		System.err.println(s);
		System.err.println(this.toString() + i);
		setOneMethodHasBeenActivated(true);
	}

	public void printMailbox() {
		getLogger().info(getMailbox().toString());
	}

	/**
	 * @return the oneMethodHasBeenActivated
	 */
	public boolean isOneMethodHasBeenActivated() {
		return oneMethodHasBeenActivated;
	}

	/**
	 * @param oneMethodHasBeenActivated the oneMethodHasBeenActivated to set
	 */
	public void setOneMethodHasBeenActivated(boolean oneMethodHasBeenActivated) {
		this.oneMethodHasBeenActivated = oneMethodHasBeenActivated;
	}

	@Override
	public LocalDateTime getNextEventDate() {
		// TODO Auto-generated method stub
		return null;
	}

}
