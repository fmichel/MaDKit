/**
 * Renderer-independent simulation controls and immutable status values.
 *
 * <p>GUI callbacks translate user input into bounded controller commands. They
 * do not call MaDKit agents or OpenGL directly. {@link
 * madkit.gl3d.gui.SimulationStatus} is a read-only presentation boundary built
 * from the authoritative controller/model state.</p>
 */
package madkit.gl3d.gui;
