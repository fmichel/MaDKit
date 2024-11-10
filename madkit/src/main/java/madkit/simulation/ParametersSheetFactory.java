package madkit.simulation;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.BeanProperty;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;

/**
 * @author Fabien Michel
 *
 */
public class ParametersSheetFactory {

	/**
	 * Get a parameters sheet made of the object's parameters, plus static
	 * parameters coming from other classes
	 * 
	 * @param o
	 * @param classes
	 * @return
	 */
	public static PropertySheet getSheet(Object o) {
		ObservableList<Item> parameters = FXCollections.observableArrayList();
		addParameters(o, o.getClass(), parameters, false);
		for (Class<?> c : getObservedClasses(o.getClass())) {
			addParameters(o, c, parameters, true);
		}
		return new PropertySheet(parameters);
	}

	/**
	 * Get a parameters sheet made of the object's parameters, plus static
	 * parameters coming from other classes
	 * 
	 * @param o
	 * @param classes
	 * @return
	 */
	public static PropertySheet getSheet(Object o, Class<?>... classes) {
		ObservableList<Item> parameters = FXCollections.observableArrayList();
		addParameters(o, o.getClass(), parameters, false);
		if(classes == null || classes.length == 0) {
			classes = getObservedClasses(o.getClass());
		}
		for (Class<?> c : classes) {
			addParameters(o, c, parameters, true);
		}
		return new PropertySheet(parameters);
	}

	public static TitledPane getTitledPaneSheet(Object o, String title, Class<?>... classes) {
		PropertySheet sheet = ParametersSheetFactory.getSheet(o, classes);
		if (sheet.getItems().isEmpty()) {
			return null;
		}
		return new TitledPane(title, sheet);
	}

	public static TitledPane getTitledPaneSheet(Object o) {
		return getTitledPaneSheet(o, o.getClass().getSimpleName());
	}

	private static Class<?>[] getObservedClasses(Class<?> c) {
		PropertySheetAgents annotation = c.getDeclaredAnnotation(PropertySheetAgents.class);
		if (annotation == null) {
			return new Class<?>[0];
		}
		return annotation.classesToBuildUIWith();
	}

	public static VBox getVBoxProperties(Object... objects) {
		VBox vbox = new VBox();
		for (Object o : objects) {
			TitledPane tp = ParametersSheetFactory.getTitledPaneSheet(o);
			if (tp != null) {
				vbox.getChildren().add(tp);
			}
		}
		return vbox;
	}

	/**
	 * @param o
	 * @param c
	 * @param parameters
	 */
	private static void addParameters(Object o, Class<?> c, ObservableList<Item> parameters, boolean onlyStatic) {
//		System.err.println("" + o + " -> " + Arrays.deepToString(c.getDeclaredFields()));
		final Class<?> originType = c;
		while (c != Object.class) {
			for (Field f : c.getDeclaredFields()) {
				if (onlyStatic && !Modifier.isStatic(f.getModifiers()))
					continue;
				Parameter annotation = f.getAnnotation(Parameter.class);
				if (annotation != null) {
					try {
						PropertyDescriptor propDescriptor = new PropertyDescriptor(f.getName(), c);
						SliderAnnotation sliderAnnotation = f.getAnnotation(SliderAnnotation.class);
						String displayName = annotation.displayName();
						if (displayName.isBlank()) {
							displayName = endUserName(f);
						}
						if (sliderAnnotation != null) {
							propDescriptor.setPropertyEditorClass(SliderEditor.class);
							SliderEditor.sliders.put(displayName, createSlider(sliderAnnotation));
						}
						propDescriptor.setDisplayName(displayName);
						String category = annotation.category();
						propDescriptor.setValue(BeanProperty.CATEGORY_LABEL_KEY,
								category.isBlank() ? originType.getSimpleName() : category);
						BeanProperty property = new BeanProperty(o, propDescriptor);
						parameters.add(property);
					} catch (IntrospectionException e) {
						e.printStackTrace();
					}
				}
			}
			c = c.getSuperclass();
		}
	}

//	private static void getBeanproperties(Object o, Class<?> c, ObservableList<Item> parameters, boolean onlyStatic) {

	/**
	 * @param sliderAnnotation
	 */
	private static Slider createSlider(SliderAnnotation a) {
		Slider s = new Slider(a.minValue(), a.maxValue(), a.minValue());
		s.setShowTickMarks(true);
		s.setShowTickLabels(true);
		s.setOnScroll((ScrollEvent event) -> {
			s.setValue(s.getValue() + (event.getDeltaY() > 0 ? a.scrollPrecision() : -a.scrollPrecision()));
		});
		return s;
	}

	static private String endUserName(Field f) {
		Pattern wordFinder = Pattern.compile("(([A-Z]?[a-z]+)|([A-Z]))");
		Matcher matcher = wordFinder.matcher(f.getName());
		matcher.find();
		StringBuilder sb = new StringBuilder(matcher.group());
		while (matcher.find()) {
			sb.append(' ').append(matcher.group().toLowerCase());
		}
		return sb.toString();
	}

}
