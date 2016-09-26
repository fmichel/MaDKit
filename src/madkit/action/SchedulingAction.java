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

import static java.awt.event.KeyEvent.VK_DOLLAR;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_P;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_SPACE;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.Action;

import madkit.i18n.I18nUtilities;
import madkit.kernel.Scheduler;
import madkit.message.SchedulingMessage;

/**
 * Enum representing operations which 
 * could be done by a {@link Scheduler} agent.
 * It could be used by an agent to interact with the scheduler
 * by creating {@link Action} using {@link #getActionFor(Scheduler, Object...)}.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 0.9
 * 
 */
public enum SchedulingAction {

	RUN(VK_P),
	STEP(VK_SPACE),
	SPEED_UP(VK_RIGHT),
	SPEED_DOWN(VK_LEFT),
	PAUSE(VK_DOLLAR),
	SHUTDOWN(VK_DOLLAR);
	
	final static private ResourceBundle messages = I18nUtilities.getResourceBundle(SchedulingAction.class.getSimpleName());

	private ActionInfo actionInfo;
	final private int keyEvent;

	private SchedulingAction(int keyEvent){
		this.keyEvent = keyEvent;
	}
	
	/**
	 * @return the actionInfo
	 */
	public ActionInfo getActionInfo() {
		if(actionInfo == null)
			actionInfo = new ActionInfo(this,keyEvent, messages);
		return actionInfo;
	}

	/**
	 * Builds an action that will make the corresponding 
	 * scheduler do the related operation if possible.
	 * 
	 * @param theScheduler the scheduler on which the
	 * action will be triggered if possible
	 * @param parameters the info 
	 * @return the corresponding action 
	 */
	public Action getActionFor(final Scheduler theScheduler, final Object... parameters){
		return new MDKAbstractAction(getActionInfo()){
			private static final long serialVersionUID = 5434867603425806658L;

			@Override
			public void actionPerformed(ActionEvent e) {//TODO I could do the check validity here for logging purpose
				theScheduler.receiveMessage(new SchedulingMessage(SchedulingAction.this,parameters));//TODO work with AA but this is probably worthless	
			}
		};
	}
}
