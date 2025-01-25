/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.gui;

import java.util.HashMap;
import java.util.Map;

import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.PropertyEditor;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import madkit.kernel.Madkit;

/**
 * A property editor for editing double values using a {@link Slider}.
 * 
 */
public class SliderEditor extends AbstractPropertyEditor<Double, Slider> implements PropertyEditor<Double> {

	/**
	 * A map of sliders for each property name
	 */
	final static Map<String, Slider> sliders = new HashMap<>();

	/** The tooltip. */
	Tooltip tooltip = new Tooltip();

	/**
	 * Constructs a new SliderEditor for the given item.
	 * 
	 * @param item the item to edit
	 */
	public SliderEditor(Item item) {
		super(item, sliders.get(item.getName()));
		getEditor().setTooltip(tooltip);
		getEditor().valueProperty().addListener((_, _, newValue) -> {
			setValue(newValue.doubleValue());
			tooltip.setText("" + newValue);
		});
		try {
			getEditor().setValue((double) item.getValue());
		} catch (ClassCastException e) {
			Madkit.MDK_LOGGER.severe("****************** " + SliderProperty.class
					+ " only works on double! \nPlease change " + item.getName() + " type to double in your class");
			e.printStackTrace();
		}
	}

	/**
	 * Gets the observable value.
	 *
	 * @return the observable value
	 */
	@Override
	protected ObservableValue<Double> getObservableValue() {
		return getEditor().valueProperty().asObject();
	}

	/**
	 * Sets the value.
	 *
	 * @param value the new value
	 */
	@Override
	public void setValue(Double value) {
		getProperty().setValue(value);
		tooltip.setText("" + value);
	}

}