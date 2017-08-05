/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.madkit.action;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

/**
 * This class provides an easy way of building new actions ({@link Action})
 * initialized with {@link ActionInfo} objects.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @see Action
 * @version 0.9
 * 
 */
public abstract class MDKAbstractAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1414495456612340010L;

	/**
	 * Builds a new action using the passed action's information
	 * 
	 * @param actionInfo
	 */
	public MDKAbstractAction(final ActionInfo actionInfo) {
		putValue(Action.NAME, actionInfo.getName());
		putValue(Action.SHORT_DESCRIPTION, actionInfo.getShortDescription());
		putValue(Action.LONG_DESCRIPTION, actionInfo.getLongDescription());
		if (actionInfo.getBigIcon() != null) {
			putValue(Action.LARGE_ICON_KEY, actionInfo.getBigIcon());
			putValue(Action.SMALL_ICON, actionInfo.getSmallIcon());
		}
		if (actionInfo.getKeyEvent() != KeyEvent.VK_DOLLAR) {
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(actionInfo.getKeyEvent(), InputEvent.CTRL_MASK));
			putValue(Action.MNEMONIC_KEY, new Integer(actionInfo.getKeyEvent()));
		}
		putValue(Action.ACTION_COMMAND_KEY, actionInfo.getName());
		putValue(Action.SELECTED_KEY, new Boolean(false));
	}

}
