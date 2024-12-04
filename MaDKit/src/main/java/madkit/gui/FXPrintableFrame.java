
package madkit.gui;

import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.transform.Scale;

/**
 * Interface that adds the printing feature to JavaFX nodes.
 *
 * @author Fabien Michel
 * @since MaDKit 6.0
 */
public interface FXPrintableFrame {

	/**
	 * Prints the node.
	 *
	 * @param job        the printer job
	 * @param pageLayout the page layout
	 * @return true if the print job was successful, false otherwise
	 */
	default boolean print(PrinterJob job, PageLayout pageLayout) {
		Node node = getPrintableNode();
		if (node == null) {
			return false;
		}

		double scaleX = pageLayout.getPrintableWidth() / node.getBoundsInParent().getWidth();
		double scaleY = pageLayout.getPrintableHeight() / node.getBoundsInParent().getHeight();
		double scale = Math.min(scaleX, scaleY);

		node.getTransforms().add(new Scale(scale, scale));

		boolean success = job.printPage(pageLayout, node);
		node.getTransforms().clear();

		return success;
	}

	/**
	 * Returns the node to be printed.
	 *
	 * @return the node to be printed
	 */
	default Node getPrintableNode() {
		return (Node) this;
	}
}
