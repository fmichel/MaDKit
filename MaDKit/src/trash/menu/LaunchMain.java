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

import java.awt.event.KeyEvent;

import javax.swing.JMenu;

/**
 * This class builds a {@link JMenu} containing all the agent classes containing a main method
 * 
 *
 * @since MaDKit 5.0.1
 * @version 0.9
 */
public class LaunchMain extends ClassPathSensitiveMenu {// NOSONAR

    private static final long serialVersionUID = 752058124107737762L;

    /**
     * Builds a new menu.
     * 
     * @param title
     *            the title to use
     */
    public LaunchMain(final String title) {
	super(title);
	setMnemonic(KeyEvent.VK_A);
	update();
    }

    @Override
    public void update() {
	removeAll();
//	for (final String string : MadkitClassLoader.getAgentsWithMain()) {
//	    JMenuItem name = new JMenuItem(GlobalAction.LAUNCH_MAIN);
//	    name.setActionCommand(string);
//	    name.setText(string);
//	    add(name);
//	}
	setVisible(getItemCount() != 0);
    }

}
