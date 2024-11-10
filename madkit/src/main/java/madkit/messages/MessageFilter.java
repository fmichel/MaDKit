
package madkit.messages;

import madkit.kernel.Message;


/**
 * Instances of classes that implement this interface are used to
 * filter messages when consulting the mailbox.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.4
 * @version 1
 */
public interface MessageFilter {

   /**
    * Tests if a specified message matches the requirement.
    *
    * @param   m   the message to test.
    * @return  <code>true</code> if and only if the message matches the requirement.
    */
public boolean accept(Message m);

}
