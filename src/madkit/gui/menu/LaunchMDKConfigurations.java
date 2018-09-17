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
import java.io.IOException;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import madkit.action.GlobalAction;
import madkit.kernel.MadkitClassLoader;
import madkit.kernel.MadkitProperties;

/**
 * This class builds a {@link JMenu} containing all the MDK configuration files found on the class path. Each item will
 * launch a separate instance of MaDKit using the corresponding configuration file
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.2
 * @version 0.9
 */
public class LaunchMDKConfigurations extends ClassPathSensitiveMenu {// NOSONAR

    private static final long serialVersionUID = -3650744981788324553L;

    /**
     * Builds a new menu.
     * 
     * @param title
     *            the title to use
     */
    public LaunchMDKConfigurations(final String title) {
	super(title);
	setMnemonic(KeyEvent.VK_C);
	update();
    }

    @Override
    public void update() {// TODO clean up xml related
	removeAll();
	for (final String string : MadkitClassLoader.getMDKFiles()) {
	    try {
		final MadkitProperties madkitProperties = new MadkitProperties();
		madkitProperties.loadPropertiesFromFile(string);
		JMenuItem name = new JMenuItem(GlobalAction.LAUNCH_MDK_CONFIG);
		name.setActionCommand(string);
		name.setText(string + " " + madkitProperties);
		add(name);
	    }
	    catch(IOException e) {
		e.printStackTrace();
	    }
	}
	setVisible(getItemCount() != 0);
    }

}
