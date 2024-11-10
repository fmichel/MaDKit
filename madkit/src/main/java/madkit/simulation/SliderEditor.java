package madkit.simulation;

import java.util.HashMap;
import java.util.Map;

import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.PropertyEditor;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;

//TODO change using container with editable value
public class SliderEditor extends AbstractPropertyEditor<Double, Slider> implements PropertyEditor<Double> {

	public static Map<String, Slider> sliders = new HashMap<>();
	Tooltip tooltip = new Tooltip();

	public SliderEditor(Item item) {
		super(item, sliders.get(item.getName()));
		getEditor().setTooltip(tooltip);
		getEditor().valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//				System.err.println("changing value");
				setValue((Double) newValue);
				tooltip.setText(""+newValue);
			}
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
		tooltip.setText(""+value);
	}

//    final IntField intField = new IntField(0, 100, 50);
//    intField.setTooltip(new Tooltip(EDIT_FIELD_TOOLTIP));
//    intField.valueProperty().bindBidirectional(slider.valueProperty());
//    intField.setPrefWidth(50);

}
