/**
 * Provides classes that are fundamental to the design of MaDKit applications. This
 * package is the heart of MaDKit. Especially, it contains the {@link madkit.kernel.Agent}
 * class which is the base class for all agents in MaDKit.
 * <p>
 * It also contains the {@link madkit.kernel.Message} class which is the base class for
 * all messages. Since MaDKit 6, the {@link madkit.kernel.Mailbox} class allows agents to
 * have more control over how they can manage the received messages. For instance,
 * filtering messages, or managing which type of message should be retrieved first.
 * <p>
 * This package also contains all the base classes for the MaDKit organization model, such
 * as {@link madkit.kernel.Organization}, {@link madkit.kernel.Role},
 * {@link madkit.kernel.Group} and {@link madkit.kernel.Community}.
 * <p>
 * Additionally, this package contains classes that are fundamental to the design of
 * MaDKit simulations, such as {@link madkit.kernel.Scheduler},
 * {@link madkit.kernel.Activator}, and {@link madkit.kernel.Probe}.
 * 
 * 
 * @since MaDKit 1.0 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * @version 6.0
 * 
 */
package madkit.kernel;
