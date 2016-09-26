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
package madkit.gui.menu;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import madkit.gui.SwingUtil;
import madkit.i18n.Words;
import madkit.kernel.Madkit;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.17
 * @version 0.91
 * 
 */
final class AboutFrame extends JDialog {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -6873297970139729692L;

	public AboutFrame() {
		setTitle("MaDKit");

		final JLabel icon = new JLabel(SwingUtil.MADKIT_LOGO);
		icon.setBounds(10, 10, 10, 10);
		add(icon, BorderLayout.WEST);
		setIconImage(SwingUtil.MADKIT_LOGO.getImage());

		final JTextPane textPanel = new JTextPane();
		textPanel.setEditable(false);
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
		textPanel.add(Box.createRigidArea(new Dimension(0, 110)));

//		final JLabel name = new JLabel("Visit");
//		name.setAlignmentX(0.5f);
//		textPanel.add(name);

		final StyledDocument doc = textPanel.getStyledDocument();
		final Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		final Style regular = doc.addStyle("regular", def);
		Style s = doc.addStyle("italic", regular);
		StyleConstants.setItalic(s, true);

		s = doc.addStyle("small", regular);
		StyleConstants.setFontSize(s, 10);

		s = doc.addStyle("large", regular);
		StyleConstants.setBold(s, true);
		StyleConstants.setFontSize(s, 20);

		String version = null;
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL(Madkit.WEB + "/LAST").openStream()))){
			version = "\n\n   " + Words.LAST_AVAILABLE.toString() + ": " + in.readLine() + "\n";
		} catch (IOException e) {//just offline
		}
		try {
			doc.insertString(doc.getLength(), "  MaDKit\n", doc.getStyle("large"));
			doc.insertString(doc.getLength(), "   Multiagent Development Kit\n\n", doc.getStyle("italic"));
			doc.insertString(doc.getLength(), "   Version: " + Madkit.VERSION + "\n   Build id: " + Madkit.BUILD_ID + (version == null ? "" : version),
					doc.getStyle("small"));
			textPanel.add(new SwingLink(Madkit.WEB.substring(7,21), new URI(Madkit.WEB)));
		} catch (BadLocationException | URISyntaxException e) {
			e.printStackTrace();
		}

		add(textPanel);

		final JButton close = new JButton("OK");
		close.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				dispose();
			}
		});

		add(close, BorderLayout.SOUTH);
		setModalityType(ModalityType.MODELESS);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(400, 250);
		setLocationRelativeTo(null);
		setVisible(true);
		close.requestFocus();
	}

}

final class SwingLink extends JLabel {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -6185189962939821896L;

	public SwingLink(String text, final URI uri) {
		setText("<html><span style=\"color: #000099;\">" + text + "</span></html>");

		setToolTipText(uri.toString());
		addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				if (Desktop.isDesktopSupported()) {
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.browse(uri);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "Failed to launch the link, " + "your computer is likely misconfigured.",
								"Cannot Launch Link", JOptionPane.WARNING_MESSAGE);
					}
				}
				else {
					JOptionPane.showMessageDialog(null, "Java is not able to launch links on your computer.", "Cannot Launch Link",
							JOptionPane.WARNING_MESSAGE);
				}
			}
		});
	}
}
