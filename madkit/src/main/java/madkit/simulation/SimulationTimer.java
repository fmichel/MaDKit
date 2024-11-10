/*******************************************************************************
 * Copyright (c) 2021, MaDKit Team
 *
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
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
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.simulation;

import java.util.Formatter;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import madkit.gui.fx.FXManager;

/**
 * @author Fabien Michel
 * 
 *         since MaDKit 6.0
 */
public class SimulationTimer<T extends Comparable<? super T>> {

	private T initialTime;
	private T currentTime;
	private T endTime;
	private SimpleObjectProperty<T> dateProperty;
	private StringBinding asString;

	/**
	 * Creates a new timer.
	 * 
	 * @param initialTime time at which the simulation should start
	 * @param endTime time at which the simulation should end
	 */
	public SimulationTimer(T initialTime, T endTime) {
		this.initialTime = initialTime;
		currentTime = initialTime;
		this.endTime = endTime;
		dateProperty = new SimpleObjectProperty<>(currentTime);
		asString = dateProperty.asString();
	}

	/**
	 * Checks if this timer has reached its end time.
	 * 
	 * @return <code>true</code> if the current time has reached the end time.
	 */
	public boolean hasReachedEndTime() {
		return endTime.compareTo(currentTime) < 0;
	}

	/**
	 * Returns a label synchronized with the current time. 
	 * 
	 * @return a label to be used in UI
	 */
	public Label getTimeLabel() {
		Label timeLabel = new Label();
		timeLabel.textProperty().bind(asString);
		return timeLabel;
	}

	/**
	 * Sets the formatting of the current time as specified in {@link Formatter}.
	 * 
	 * @param format a printf-style format string
	 */
	public void setTimeFormat(String format) {
		asString = dateProperty.asString(format);
	}

	/**
	 * @return the currentTime
	 */
	public T getCurrentTime() {
		return currentTime;
	}

	/**
	 * @param currentTime the currentTime to set
	 */
	public void setCurrentTime(T currentTime) {
		this.currentTime = currentTime;
		FXManager.runLater(() -> dateProperty.set(currentTime));
	}

	/**
	 * @return the endTime
	 */
	public T getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(T endTime) {
		this.endTime = endTime;
	}
	
	@Override
	public String toString() {
		return currentTime.toString();
	}

	/**
	 * 
	 */
	public void reset() {
		currentTime = initialTime;
	}
	
}
