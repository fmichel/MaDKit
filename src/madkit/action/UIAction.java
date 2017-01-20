/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the [CeCILL|CeCILL-B|CeCILL-C] license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the [CeCILL|CeCILL-B|CeCILL-C]
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
package madkit.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ResourceBundle;

import javax.swing.Action;

import madkit.gui.PrintableFrame;
import madkit.i18n.I18nUtilities;

/**
 * Enum representing actions which could be used on MDK frames
 * @author Fabien Michel
 * @since MaDKit 5.1.0.1
 * @version 0.9
 * 
 */
public enum UIAction {

	PRINT(KeyEvent.VK_P)
	;

	final static private ResourceBundle messages = I18nUtilities.getResourceBundle(UIAction.class.getSimpleName());
	
	
	/**
	 * @return the bundle for internationalized messages
	 */
	public static ResourceBundle getMessages() {
		return messages;
	}

	private ActionInfo actionInfo;
	
	final private int keyEvent;

	/**
	 * @return the actionInfo corresponding to this constant
	 */
	// messages is null if used in the constructor...
	public ActionInfo getActionInfo() {
		if(actionInfo == null)
			actionInfo = new ActionInfo(this,keyEvent,messages);
		return actionInfo;
	}


	private UIAction(int keyEvent){
		this.keyEvent = keyEvent;
	}

	/**
	 * Builds an action that will operate on this frame
	 * 
	 * @param agent the frameon which this action 
	 * will operate
	 * @return the action corresponding to this feature
	 */
	public Action getActionFor(PrintableFrame pf){
		return new MDKAbstractAction(getActionInfo()){
			private static final long serialVersionUID = -3078505474395164899L;

			@Override
			public void actionPerformed(ActionEvent e) {//TODO I could do the check validity here for logging purpose
				PrinterJob job = PrinterJob.getPrinterJob();
				job.setPrintable(pf);
				if (job.printDialog()) {
					try {
						job.print();
					} catch (PrinterException ex) {
						ex.printStackTrace();
					}

			}
		}
	};
}
	
}
