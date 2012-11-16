/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.gui.menu;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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
import madkit.kernel.Madkit;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.17
 * @version 0.9
 * 
 */
final class AboutFrame extends JDialog {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -6291196559273649285L;

	public AboutFrame() {
		setTitle("MaDKit");
		setLocationRelativeTo(null);

		JLabel icon = new JLabel(SwingUtil.MADKIT_LOGO);
		icon.setBounds(10, 10, 10, 10);
		add(icon, BorderLayout.WEST);
		setIconImage(SwingUtil.MADKIT_LOGO.getImage());

		JTextPane textPanel = new JTextPane();
		textPanel.setEditable(false);
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
		textPanel.add(Box.createRigidArea(new Dimension(0, 110)));

		JLabel name = new JLabel("Visit");

		name.setAlignmentX(0.5f);
		textPanel.add(name);
		StyledDocument doc = textPanel.getStyledDocument();
		Style def = StyleContext.getDefaultStyleContext().getStyle(
				StyleContext.DEFAULT_STYLE);

		Style regular = doc.addStyle("regular", def);
		Style s = doc.addStyle("italic", regular);
		StyleConstants.setItalic(s, true);

		s = doc.addStyle("small", regular);
		StyleConstants.setFontSize(s, 10);

		s = doc.addStyle("large", regular);
		StyleConstants.setBold(s, true);
		StyleConstants.setFontSize(s, 20);

		try {
			doc.insertString(doc.getLength(), "MaDKit\n", doc.getStyle("large"));
			doc.insertString(doc.getLength(),
					"The Multiagent Development Kit\n\n", doc.getStyle("italic"));
			doc.insertString(doc.getLength(), "Version: " + Madkit.VERSION
					+ "\nBuild id: " + Madkit.BUILD_ID, doc.getStyle("small"));
		} catch (BadLocationException e) {
		}

		try {
			textPanel.add(new SwingLink("www.madkit.org", new URI("http://www.madkit.org")));
		} catch (URISyntaxException e) {
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
		setSize(400, 200);
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
		setText("<html><span style=\"color: #000099;\">" + text
				+ "</span></html>");

		setToolTipText(uri.toString());
		addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				if (Desktop.isDesktopSupported()) {
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.browse(uri);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null,
								"Failed to launch the link, "
										+ "your computer is likely misconfigured.",
								"Cannot Launch Link", JOptionPane.WARNING_MESSAGE);
					}
				}
				else {
					JOptionPane.showMessageDialog(null,
							"Java is not able to launch links on your computer.",
							"Cannot Launch Link", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
	}
}
