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

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
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
import madkit.kernel.Agent;

/**
 * 
 * This class is used to create a property sheet from an object's fields.
 * <p>
 * It uses the {@link UIProperty} annotation to specify which fields should be displayed.
 * </p>
 * <p>
 * It also uses the {@link SliderProperty} annotation to specify which fields should be
 * displayed as sliders.
 * </p>
 * 
 */
public class PropertySheetFactory {

	private PropertySheetFactory() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Get a parameters sheet made of the objects' properties annotated with
	 * {@link UIProperty} or {@link SliderProperty} annotations.
	 * 
	 * @param objects the objects from which to take fields as properties to display in the
	 *                sheet
	 * @return the property sheet containing the objects properties
	 */
	public static PropertySheet getSheet(Object... objects) {
		ObservableList<Item> parameters = FXCollections.observableArrayList();
		for (Object o : objects) {
			parameters.addAll(getAnnotatedProperties(o));
		}
		return new PropertySheet(parameters);
	}

	/**
	 * Returns a titled pane containing the objects' properties annotated with
	 * {@link UIProperty} or {@link SliderProperty} annotations.
	 * 
	 * @param title   the title of the titled pane
	 * @param objects the objects from which to take fields as properties to display
	 * 
	 * @return a titled pane containing the objects' properties, or <code>null</code> if the
	 *         property sheet is empty
	 */
	public static TitledPane getTitledPaneSheet(String title, Object... objects) {
		PropertySheet sheet = PropertySheetFactory.getSheet(objects);
		if (sheet.getItems().isEmpty()) {
			return null;
		}
		return new TitledPane(title, sheet);
	}

	/**
	 * Returns a titled pane containing the object's properties annotated with
	 * {@link UIProperty} or {@link SliderProperty} annotations. The title is automatically
	 * set to the agent's name if the object is an agent, to the class's simple name if the
	 * object is a class, or to the object's string representation otherwise.
	 * 
	 * @param object the object from which to take fields as properties to display
	 * @return a titled pane containing the object's properties
	 */
	public static TitledPane getTitledPaneSheet(Object object) {
		String title = switch (object) {
		case Agent a -> a.getName();
		case Class<?> c -> c.getSimpleName();
		default -> object.toString();
		};
		return getTitledPaneSheet(title, object);
	}

	/**
	 * Get a VBox containing the object's parameters annotated with {@link UIProperty} or
	 * {@link SliderProperty} annotations.
	 * 
	 * @param objects the objects from which to take fields as properties to display in the
	 *                VBox
	 * @return a VBox containing the object's parameters
	 */
	public static VBox getVBoxProperties(Object... objects) {
		VBox vbox = new VBox();
		for (Object o : objects) {
			TitledPane tp = PropertySheetFactory.getTitledPaneSheet(o);
			if (tp != null) {
				vbox.getChildren().add(tp);
			}
		}
		return vbox;
	}

	private static ObservableList<Item> getAnnotatedProperties(Object o) {
		ObservableList<Item> parameters = FXCollections.observableArrayList();
		if (o instanceof Class<?> cl) {
			final Class<?> originType = cl;
			Class<?> currentType = originType;
			while (currentType != Object.class) {
				populateProperties(o, parameters, currentType.getSimpleName(), currentType);
				currentType = currentType.getSuperclass();
			}
		} else {
			final Class<?> originType = o.getClass();
			Class<?> currentType = originType;
			while (currentType != Object.class) {
				populateProperties(o, parameters, originType.getSimpleName(), currentType);
				currentType = currentType.getSuperclass();
			}
		}
		return parameters;
	}

	/**
	 * @param o
	 * @param parameters
	 * @param defaultCategoryName
	 * @param currentType
	 */
	private static void populateProperties(Object o, ObservableList<Item> parameters, String defaultCategoryName,
			Class<?> currentType) {
		for (Field f : currentType.getDeclaredFields()) {
			PropertyDescriptor propertyDescriptor = buildUIPropertyDescriptor(f, currentType);
			if (propertyDescriptor != null) {
				BeanProperty uiProperty = new BeanProperty(o, propertyDescriptor);
				parameters.add(uiProperty);
			}
			propertyDescriptor = buildSliderPropertyDescriptor(f, currentType);
			if (propertyDescriptor != null) {
				BeanProperty sliderProperty = new BeanProperty(o, propertyDescriptor);
				parameters.add(sliderProperty);
			}
		}
	}

	static PropertyDescriptor buildUIPropertyDescriptor(Field f, Class<?> currentType) {
		UIProperty uiAnnotation = f.getAnnotation(UIProperty.class);
		if (uiAnnotation != null) {
			try {
				PropertyDescriptor propDescriptor = new PropertyDescriptor(f.getName(), currentType);
				String displayName = uiAnnotation.displayName();
				String category = uiAnnotation.category();
				if (displayName.isBlank()) {
					displayName = endUserPropertyName(f);
				}
				propDescriptor.setDisplayName(displayName);
				propDescriptor.setValue(BeanProperty.CATEGORY_LABEL_KEY,
						category.isBlank() ? currentType.getSimpleName() : category);
				return propDescriptor;
			} catch (IntrospectionException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	static PropertyDescriptor buildSliderPropertyDescriptor(Field f, Class<?> currentType) {
		SliderProperty sliderAnnotation = f.getAnnotation(SliderProperty.class);
		if (sliderAnnotation != null) {
			try {
				PropertyDescriptor propDescriptor = new PropertyDescriptor(f.getName(), currentType);
				String displayName = sliderAnnotation.displayName();
				String category = sliderAnnotation.category();
				if (displayName.isBlank()) {
					displayName = endUserPropertyName(f);
				}
				propDescriptor.setDisplayName(displayName);
				propDescriptor.setValue(BeanProperty.CATEGORY_LABEL_KEY,
						category.isBlank() ? currentType.getSimpleName() : category);
				SliderEditor.sliders.put(displayName, createSlider(sliderAnnotation));
				propDescriptor.setPropertyEditorClass(SliderEditor.class);
				return propDescriptor;
			} catch (IntrospectionException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * @param sliderAnnotation
	 */
	private static Slider createSlider(SliderProperty a) {
		Slider s = new Slider(a.min(), a.max(), a.min());
		s.setShowTickMarks(true);
		s.setShowTickLabels(true);
		s.setOnScroll((ScrollEvent event) -> s
				.setValue(s.getValue() + (event.getDeltaY() > 0 ? a.scrollPrecision() : -a.scrollPrecision())));
		return s;
	}

	private static String endUserPropertyName(Field f) {
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
