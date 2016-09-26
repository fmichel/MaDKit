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

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import madkit.action.KernelAction;
import madkit.gui.SwingUtil;
import madkit.i18n.Words;
import madkit.kernel.Madkit;

/**
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.17
 * @version 0.91
 * 
 */
final public class HelpMenu extends JMenu {
	
	private static final ImageIcon	HELP_ICON	= new ImageIcon(SwingUtil.class.getResource("images/help.png"));

	private static final long serialVersionUID = 6177193453649323680L;
	final private static ActionListener about = new ActionListener() {
		@SuppressWarnings("unused")
		@Override
		public void actionPerformed(ActionEvent e) {
			new AboutFrame();
		}
	};
	final private static ActionListener tuto = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().browse(new URI(Madkit.WEB+"/"+e.getActionCommand()));
				} catch (IOException | URISyntaxException e1) {
					e1.printStackTrace();
				}
		}
	};

	public HelpMenu(){
		super(Words.HELP.toString());
		setMnemonic(KeyEvent.VK_E);
		JMenuItem mi;
		if (Desktop.isDesktopSupported()) {
			final ImageIcon ii = KernelAction.CONNECT_WEB_REPO.getActionInfo().getSmallIcon();
			mi = new JMenuItem("API",ii);
			mi.addActionListener(tuto);
			mi.setActionCommand("repository/MaDKit-"+Madkit.VERSION+"/docs/api");
			add(mi);
			mi = new JMenuItem(Words.TUTORIALS.toString(),ii);
			mi.addActionListener(tuto);
			mi.setActionCommand("tutorials");
			add(mi);
			mi = new JMenuItem("Forum",ii);
			mi.addActionListener(tuto);
			mi.setActionCommand("forum");
			add(mi);
		}
		mi = new JMenuItem(Words.ABOUT.toString(),HELP_ICON);
		mi.setMnemonic(KeyEvent.VK_A);
		mi.addActionListener(about);
		add(mi);
	}
}
