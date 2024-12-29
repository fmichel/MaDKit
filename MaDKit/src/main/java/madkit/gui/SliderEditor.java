package madkit.gui;

import java.util.HashMap;
import java.util.Map;

import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.PropertyEditor;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;

/**
 * A property editor for editing double values using a {@link Slider}.
 * 
 */
public class SliderEditor extends AbstractPropertyEditor<Double, Slider> implements PropertyEditor<Double> {

	/**
	 * A map of sliders for each property name
	 */
	static Map<String, Slider> sliders = new HashMap<>();
	Tooltip tooltip = new Tooltip();

	/**
	 * Constructs a new SliderEditor for the given item.
	 * 
	 * @param item the item to edit
	 */
	public SliderEditor(Item item) {
		super(item, sliders.get(item.getName()));
		getEditor().setTooltip(tooltip);
		getEditor().valueProperty().addListener((observable, oldValue, newValue) -> {
			setValue(newValue.doubleValue());
			tooltip.setText("" + newValue);
		});
		getEditor().setValue((double) item.getValue());
	}

	@Override
	protected ObservableValue<Double> getObservableValue() {
		return getEditor().valueProperty().asObject();
	}

	@Override
	public void setValue(Double value) {
		getProperty().setValue(value);
		tooltip.setText("" + value);
	}

}
